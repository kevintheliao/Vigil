import pandas as pd
import numpy as np
import torch
from sklearn.model_selection import train_test_split
from sklearn.utils.class_weight import compute_class_weight
from datasets import Dataset
from transformers import (
    AutoTokenizer,
    AutoModelForSequenceClassification,
    DataCollatorWithPadding,
    Trainer,
    TrainingArguments,
)

MODEL_NAME = "distilbert-base-uncased"
LABELS = ["SAFE", "SCAM", "HARASSMENT"]
label2id = {label: i for i, label in enumerate(LABELS)}
id2label = {i: label for i, label in enumerate(LABELS)}

#load and split data
df = pd.read_csv("data/combined.csv")
df["label_id"] = df["label"].map(label2id)

#cap dominant SAFE class; SCAM/HARASSMENT kept as-is since they're scarce
SAFE_CAP = 30000
safe_rows = df[df["label"] == "SAFE"]
other_rows = df[df["label"] != "SAFE"]
if len(safe_rows) > SAFE_CAP:
    safe_rows = safe_rows.sample(n=SAFE_CAP, random_state=42)
df = pd.concat([safe_rows, other_rows], ignore_index=True)

train_df, test_df = train_test_split(
    df, test_size=0.2, random_state=42, stratify=df["label_id"]
)

#class weights
class_weights = compute_class_weight(
    class_weight="balanced",
    classes=np.array(sorted(label2id.values())),
    y=train_df["label_id"].values,
)
class_weights = torch.tensor(class_weights, dtype=torch.float32)

#tokenize without fixed padding; the collator pads each batch to its own longest example
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
def tokenize(batch):
    return tokenizer(batch["text"], truncation=True, max_length=128)

train_ds = Dataset.from_pandas(train_df[["text", "label_id"]].rename(columns={"label_id": "label"}))
test_ds = Dataset.from_pandas(test_df[["text", "label_id"]].rename(columns={"label_id": "label"}))

train_ds = train_ds.map(tokenize, batched=True)
test_ds = test_ds.map(tokenize, batched=True)

data_collator = DataCollatorWithPadding(tokenizer=tokenizer)

#model
model = AutoModelForSequenceClassification.from_pretrained(
    MODEL_NAME, num_labels=len(LABELS), id2label=id2label, label2id=label2id
)

#Trainer
class WeightedTrainer(Trainer):
    def compute_loss(self, model, inputs, return_outputs=False, **kwargs):
        labels = inputs.pop("labels")
        outputs = model(**inputs)
        logits = outputs.logits
        loss_fct = torch.nn.CrossEntropyLoss(weight=class_weights.to(logits.device))
        loss = loss_fct(logits, labels)
        return (loss, outputs) if return_outputs else loss
    
training_args = TrainingArguments(
    output_dir="./results",
    num_train_epochs=3,
    per_device_train_batch_size=16,
    per_device_eval_batch_size=32,
    eval_strategy="epoch",
    save_strategy="epoch",
    logging_steps=50,
    load_best_model_at_end=True,
)

trainer = WeightedTrainer(
    model=model,
    args=training_args,
    train_dataset=train_ds,
    eval_dataset=test_ds,
    data_collator=data_collator,
)

trainer.train()
trainer.save_model("./model")
tokenizer.save_pretrained("./model")
