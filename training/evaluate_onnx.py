import sys
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix
from optimum.onnxruntime import ORTModelForSequenceClassification
from transformers import AutoTokenizer

#optional arg: model dir to evaluate, e.g. python evaluate_onnx.py ./model_shipped
MODEL_DIR = sys.argv[1] if len(sys.argv) > 1 else "./model_onnx_quantized"

#base tokenizer: fine-tuning never touches the vocab, and shipped model dirs lack tokenizer files
tokenizer = AutoTokenizer.from_pretrained("distilbert-base-uncased")
model = ORTModelForSequenceClassification.from_pretrained(MODEL_DIR, file_name="model_quantized.onnx")
id2label = model.config.id2label
label2id = model.config.label2id

#rebuild the exact same test split used for training/evaluation
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

#manual batch loop: quantized ONNX model isn't what HF Trainer.predict expects
BATCH_SIZE = 32
texts = test_df["text"].tolist()
true_ids = test_df["label_id"].to_numpy()
pred_ids = []

for i in range(0, len(texts), BATCH_SIZE):
    batch = texts[i : i + BATCH_SIZE]
    inputs = tokenizer(batch, truncation=True, padding=True, max_length=128, return_tensors="pt")
    outputs = model(**inputs)
    logits = np.asarray(outputs.logits)
    pred_ids.extend(np.argmax(logits, axis=1).tolist())

pred_ids = np.array(pred_ids)
target_names = [id2label[i] for i in sorted(id2label)]

print(f"=== Classification report ({MODEL_DIR}) ===")
print(classification_report(true_ids, pred_ids, target_names=target_names, digits=3))

print("=== Confusion matrix ===")
print("rows = true label, columns = predicted label")
print(target_names)
print(confusion_matrix(true_ids, pred_ids))
