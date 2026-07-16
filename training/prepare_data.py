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

#combiner
combined = pd.concat(
    [spam_df[["text", "label"]], toxic_df, mendeley_df], ignore_index=True
)
combined = combined.dropna(subset=["text", "label"])
combined = combined.drop_duplicates(subset=["text"])
combined.to_csv("data/combined.csv", index=False)

print(combined["label"].value_counts())
print(f"Total rows: {len(combined)}")
