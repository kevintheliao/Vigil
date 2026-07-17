import json
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

#for sms_phishing_mendeley.csv - real SMS, label casing is inconsistent
#in the source file (ham/Spam/spam/Smishing/smishing), so lowercase
#before mapping
mendeley_df = pd.read_csv("data/sms_phishing_mendeley.csv")
mendeley_df["label"] = mendeley_df["LABEL"].str.lower().map(
    {"ham": "SAFE", "spam": "SCAM", "smishing": "SCAM"}
)
mendeley_df = mendeley_df.rename(columns={"TEXT": "text"})[["text", "label"]]

#for hatexplain_dataset.json - 3 annotators per post, majority vote;
#hatespeech/offensive -> HARASSMENT, normal -> SAFE, no majority -> dropped
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

#for cyberbullying_aggressive.csv / cyberbullying_nonaggressive.csv (Mendeley,
#Wikipedia/Twitter aggression corpus)
agg_df = pd.read_csv("data/cyberbullying_aggressive.csv")
agg_df = agg_df.rename(columns={"Message": "text"})[["text"]]
agg_df["label"] = "HARASSMENT"

nonagg_df = pd.read_csv("data/cyberbullying_nonaggressive.csv")
nonagg_df = nonagg_df.rename(columns={"Message": "text"})[["text"]]
nonagg_df["label"] = "SAFE"

#combiner
combined = pd.concat(
    [
        spam_df[["text", "label"]],
        toxic_df,
        mendeley_df,
        hatexplain_df,
        agg_df,
        nonagg_df,
    ],
    ignore_index=True,
)
combined = combined.dropna(subset=["text", "label"])
combined = combined.drop_duplicates(subset=["text"])
combined.to_csv("data/combined.csv", index=False)

print(combined["label"].value_counts())
print(f"Total rows: {len(combined)}")
