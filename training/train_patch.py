"""Short continued fine-tune of model_v2 on modern scams, so it keeps casual-SMS gains and recovers phishing detection.

Mix = modern scam/safe-lookalike train set + all old SCAM + replay sample of SAFE (web + NUS casual) and HARASSMENT.
Run: caffeinate -i venv/bin/python train_patch.py   (about 15 min on the Mac) -> ./model_v3
"""
import numpy as np
import pandas as pd
import torch
from datasets import Dataset
from sklearn.utils.class_weight import compute_class_weight
from transformers import (AutoModelForSequenceClassification, AutoTokenizer, DataCollatorWithPadding, Trainer, TrainingArguments)

LABELS = ["SAFE", "SCAM", "HARASSMENT"]
label2id = {l: i for i, l in enumerate(LABELS)}

df = pd.read_csv("data/combined.csv")
old_safe = df[df.label == "SAFE"].sample(7000, random_state=7)
old_harass = df[df.label == "HARASSMENT"].sample(7000, random_state=7)
old_scam = df[df.label == "SCAM"]
nus = pd.read_csv("data/nus_safe_train.csv").sample(7000, random_state=7)
modern = pd.read_csv("data/modern_scam_train.csv")
mix = pd.concat([old_safe, old_harass, old_scam, nus, modern])[["text", "label"]].dropna().sample(frac=1, random_state=42)
mix["label"] = mix["label"].map(label2id)
print(mix["label"].map(dict(enumerate(LABELS))).value_counts().to_dict())

weights = torch.tensor(compute_class_weight("balanced", classes=np.array([0, 1, 2]), y=mix["label"].values), dtype=torch.float32)

tok = AutoTokenizer.from_pretrained("./model_v2")
ds = Dataset.from_pandas(mix.reset_index(drop=True)).map(lambda b: tok(b["text"], truncation=True, max_length=128), batched=True)
model = AutoModelForSequenceClassification.from_pretrained("./model_v2")


class WeightedTrainer(Trainer):
    def compute_loss(self, model, inputs, return_outputs=False, **kwargs):
        labels = inputs.pop("labels")
        out = model(**inputs)
        loss = torch.nn.CrossEntropyLoss(weight=weights.to(out.logits.device))(out.logits, labels)
        return (loss, out) if return_outputs else loss


args = TrainingArguments(
    output_dir="./results_v3", num_train_epochs=1, per_device_train_batch_size=16, learning_rate=1e-5,
    logging_steps=50, save_strategy="no", report_to="none",
)
WeightedTrainer(model=model, args=args, train_dataset=ds, data_collator=DataCollatorWithPadding(tok)).train()
model.save_pretrained("./model_v3")
tok.save_pretrained("./model_v3")
