import pandas as pd
import numpy as np
import torch
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix
from datasets import Dataset
from transformers import (
    AutoTokenizer,
    AutoModelForSequenceClassification,
    DataCollatorWithPadding,
    Trainer,
)

MODEL_DIR = "./model"

#tokenizer, id2label, label2id
tokenizer = AutoTokenizer.from_pretrained(MODEL_DIR)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_DIR)
id2label = model.config.id2label
label2id = model.config.label2id

#load and split the data
df = pd.read_csv("data/combined.csv")
df["label_id"] = df["label"].map(label2id)

SAFE_CAP = 30000
safe_rows = df[df["label"] == "SAFE"]
other_rows = df[df["label"] != "SAFE"]
if len(safe_rows) > SAFE_CAP:
    safe_rows = safe_rows.sample(n=SAFE_CAP, random_state=42)
df = pd.concat([safe_rows, other_rows], ignore_index=True)

_, test_df = train_test_split(
    df, test_size=0.2, random_state=42, stratify=df["label_id"]
)

#tokenize, truncation
def tokenize(batch):
    return tokenizer(batch["text"], truncation=True, max_length=128)

test_ds = Dataset.from_pandas(test_df[["text", "label_id"]].rename(columns={"label_id": "label"}))
test_ds = test_ds.map(tokenize, batched=True)

data_collator = DataCollatorWithPadding(tokenizer=tokenizer)

#Trainer
trainer = Trainer(model=model, data_collator=data_collator)
predictions = trainer.predict(test_ds)

pred_ids = np.argmax(predictions.predictions, axis=1)
true_ids = predictions.label_ids

target_names = [id2label[i] for i in sorted(id2label)]

print("=== Classification report ===")
print(classification_report(true_ids, pred_ids, target_names=target_names, digits=3))

print("=== Confusion matrix ===")
print("rows = true label, columns = predicted label")
print(target_names)
print(confusion_matrix(true_ids, pred_ids))
