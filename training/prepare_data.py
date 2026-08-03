import json
import numpy as np
import pandas as pd

# For sms_spam.tsv
spam_df = pd.read_csv(
    "data/sms_spam.tsv", sep="\t", header=None, names=["label", "text"]
)
spam_df["label"] = spam_df["label"].map({"spam": "SCAM", "ham": "SAFE"})

#for toxic_comments.csv
toxic_df = pd.read_csv("data/toxic_comments.csv")
toxic_df["label"] = toxic_df["toxic"].map({1: "HARASSMENT", 0: "SAFE"})
toxic_df = toxic_df.rename(columns={"comment_text": "text"})[["text", "label"]]

#for sms_phishing_mendeley.csv - label casing is inconsistent in the source, so lowercase before mapping
mendeley_df = pd.read_csv("data/sms_phishing_mendeley.csv")
mendeley_df["label"] = mendeley_df["LABEL"].str.lower().map(
    {"ham": "SAFE", "spam": "SCAM", "smishing": "SCAM"}
)
mendeley_df = mendeley_df.rename(columns={"TEXT": "text"})[["text", "label"]]

#for hatexplain_dataset.json - majority vote of 3 annotators; hatespeech/offensive -> HARASSMENT, normal -> SAFE, ties dropped
with open("data/hatexplain_dataset.json") as f:
    hatexplain_raw = json.load(f)
hatexplain_rows = []
for post in hatexplain_raw.values():
    labels = [a["label"] for a in post["annotators"]]
    majority = max(set(labels), key=labels.count)
    if labels.count(majority) < 2:
        continue
    label = "SAFE" if majority == "normal" else "HARASSMENT"
    hatexplain_rows.append({"text": " ".join(post["post_tokens"]), "label": label})
hatexplain_df = pd.DataFrame(hatexplain_rows)

#for cyberbullying_*.csv (Mendeley Wikipedia/Twitter aggression corpus)
agg_df = pd.read_csv("data/cyberbullying_aggressive.csv")
agg_df = agg_df.rename(columns={"Message": "text"})[["text"]]
agg_df["label"] = "HARASSMENT"

nonagg_df = pd.read_csv("data/cyberbullying_nonaggressive.csv")
nonagg_df = nonagg_df.rename(columns={"Message": "text"})[["text"]]
nonagg_df["label"] = "SAFE"

#for civil_comments (Jigsaw/Google) - continuous toxicity score, threshold at 0.5
civil_df = pd.concat(
    [
        pd.read_parquet("data/civil_comments_train_0.parquet"),
        pd.read_parquet("data/civil_comments_train_1.parquet"),
    ],
    ignore_index=True,
)
civil_df["label"] = civil_df["toxicity"].apply(lambda t: "HARASSMENT" if t >= 0.5 else "SAFE")
civil_df = civil_df[["text", "label"]]

#for convabuse (amandacurry/convabuse) - per-annotator one-hot abuse scale {1,0,-1,-2,-3};
#row score is the mean over annotators who actually rated it, clear-abuse (<0) -> HARASSMENT,
#clear-not-abuse (==1) -> SAFE, ambiguous (0 <= mean < 1) dropped like hatexplain ties
def load_convabuse(path):
    df = pd.read_csv(path)
    buckets = [1, 0, -1, -2, -3]
    annotator_ids = sorted({
        int(c.split("Annotator")[1].split("_")[0])
        for c in df.columns if c.startswith("Annotator") and "_is_abuse." in c
    })
    scores = []
    for i in annotator_ids:
        cols = [f"Annotator{i}_is_abuse.{b}" for b in buckets]
        cols = [c for c in cols if c in df.columns]
        if not cols:
            continue
        #cells are sometimes "1, 1"/"0, 0" (duplicate submissions collapsed); take the first value
        parsed = {
            c: pd.to_numeric(df[c].astype("string").str.split(",").str[0], errors="coerce")
            for c in cols
        }
        annotated = pd.concat(parsed.values(), axis=1).notna().any(axis=1)
        value = sum(parsed[c].fillna(0) * b for c, b in zip(cols, buckets))
        scores.append(value.where(annotated))
    mean_score = pd.concat(scores, axis=1).mean(axis=1, skipna=True)
    label = pd.Series(np.nan, index=df.index, dtype=object)
    label[mean_score < 0] = "HARASSMENT"
    label[mean_score == 1] = "SAFE"
    out = pd.DataFrame({"text": df["user"], "label": label})
    return out.dropna(subset=["label"])

convabuse_df = pd.concat(
    [load_convabuse(f"data/convabuse_{split}.csv") for split in ("train", "valid", "test")],
    ignore_index=True,
)

#combiner
combined = pd.concat(
    [
        spam_df[["text", "label"]],
        toxic_df,
        mendeley_df,
        hatexplain_df,
        agg_df,
        nonagg_df,
        civil_df,
        convabuse_df,
    ],
    ignore_index=True,
)
combined = combined.dropna(subset=["text", "label"])
combined = combined.drop_duplicates(subset=["text"])
combined.to_csv("data/combined.csv", index=False)

print(combined["label"].value_counts())
print(f"Total rows: {len(combined)}")
