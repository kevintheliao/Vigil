import pandas as pd
import numpy as np
import torch
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix
from datasets import Dataset
from transformers import AutoTokenizer, AutoModelForSequenceClassification, DataCollatorWithPadding, Trainer

MODEL_DIR = "./model"
LABELS = ["SAFE", "SCAM", "HARASSMENT"]
label2id = {label: i for i, label in enumerate(LABELS)}

#reproduce the exact same split train.py used (same seed, same SAFE cap)
df = pd.read_csv("data/combined.csv")
df["label_id"] = df["label"].map(label2id)

SAFE_CAP = 30000
safe_rows = df[df["label"] == "SAFE"]
other_rows = df[df["label"] != "SAFE"]
if len(safe_rows) > SAFE_CAP:
    safe_rows = safe_rows.sample(n=SAFE_CAP, random_state=42)
df = pd.concat([safe_rows, other_rows], ignore_index=True)

_, test_df = train_test_split(df, test_size=0.2, random_state=42, stratify=df["label_id"])

tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_DIR)

test_ds = Dataset.from_pandas(test_df[["text", "label_id"]].rename(columns={"label_id": "label"}))
test_ds = test_ds.map(lambda b: tokenizer(b["text"], truncation=True, max_length=128), batched=True)

trainer = Trainer(model=model, data_collator=DataCollatorWithPadding(tokenizer=tokenizer))
preds = trainer.predict(test_ds)
y_true = preds.label_ids
y_pred = np.argmax(preds.predictions, axis=1)

print(classification_report(y_true, y_pred, target_names=LABELS, digits=3))
print("confusion matrix (rows=true, cols=pred):")
print(pd.DataFrame(confusion_matrix(y_true, y_pred), index=LABELS, columns=LABELS))

#dump misclassified SAFE texts (true=SAFE, pred!=SAFE) for label-quality inspection
mis = test_df.reset_index(drop=True).copy()
mis["pred"] = [LABELS[p] for p in y_pred]
mis = mis[(mis["label"] == "SAFE") & (mis["pred"] != "SAFE")]
mis[["text", "label", "pred"]].to_csv("safe_false_positives.csv", index=False)
print(f"\n{len(mis)} SAFE->non-SAFE misclassifications written to safe_false_positives.csv")
