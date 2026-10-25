"""Compare Vigil with Detoxify's toxic-bert on harassment vs safe, same held-out split as evaluate.py.

Usage: ./venv/bin/python baseline_detoxify.py [N]   (N = score only N rows, for a quick test)
Scores are cached in baseline_results/scores.csv; delete it to rescore.
"""
import os
import sys

import numpy as np
import pandas as pd
import torch
from sklearn.model_selection import train_test_split
from transformers import AutoModelForSequenceClassification, AutoTokenizer

OUT = "baseline_results/scores.csv"
LIMIT = int(sys.argv[1]) if len(sys.argv) > 1 else None
DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"


def load_test():
    # same split as evaluate.py
    df = pd.read_csv("data/combined.csv")
    vigil = AutoModelForSequenceClassification.from_pretrained("./model")
    df["label_id"] = df["label"].map(vigil.config.label2id)
    safe, other = df[df["label"] == "SAFE"], df[df["label"] != "SAFE"]
    if len(safe) > 30000:
        safe = safe.sample(n=30000, random_state=42)
    df = pd.concat([safe, other], ignore_index=True)
    _, test = train_test_split(df, test_size=0.2, random_state=42, stratify=df["label_id"])
    return test[test["label"] != "SCAM"].reset_index(drop=True)  # toxicity models don't cover scams


@torch.no_grad()
def score(model_path, texts, fn):
    tok = AutoTokenizer.from_pretrained(model_path)
    model = AutoModelForSequenceClassification.from_pretrained(model_path).to(DEVICE).eval()
    out = []
    for i in range(0, len(texts), 64):
        enc = tok(texts[i:i + 64], truncation=True, max_length=128, padding=True, return_tensors="pt").to(DEVICE)
        out.append(fn(model(**enc).logits).cpu().numpy())
    return np.concatenate(out)


if os.path.exists(OUT) and LIMIT is None:
    test = pd.read_csv(OUT)
else:
    test = load_test()
    if LIMIT:
        test = test.sample(n=LIMIT, random_state=0).reset_index(drop=True)
    texts = test["text"].astype(str).tolist()
    # toxic-bert: multi-label sigmoid, index 0 = "toxic" (Perspective TOXICITY analogue)
    test["detoxify"] = score("unitary/toxic-bert", texts, lambda l: torch.sigmoid(l[:, 0]))
    # Vigil: softmax, index 2 = HARASSMENT
    test["vigil"] = score("./model", texts, lambda l: torch.softmax(l, dim=1)[:, 2])
    if LIMIT is None:
        os.makedirs("baseline_results", exist_ok=True)
        test.to_csv(OUT, index=False)

y = (test["label"] == "HARASSMENT").to_numpy()
print(f"rows={len(test)} harassment={y.sum()} safe={(~y).sum()} device={DEVICE}")
print(f"{'model':9} {'thr':>4} {'FPR':>7} {'FNR':>7} {'prec':>7} {'recall':>7}")
for name in ("vigil", "detoxify"):
    for t in (0.1, 0.3, 0.5, 0.7, 0.9):
        p = test[name].to_numpy() >= t
        tp, fp, fn, tn = (p & y).sum(), (p & ~y).sum(), (~p & y).sum(), (~p & ~y).sum()
        print(f"{name:9} {t:4.1f} {fp / max(fp + tn, 1):7.3f} {fn / max(fn + tp, 1):7.3f} "
              f"{tp / max(tp + fp, 1):7.3f} {tp / max(tp + fn, 1):7.3f}")
