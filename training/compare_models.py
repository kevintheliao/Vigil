"""Old model vs retrained model. Run after train.py finishes: venv/bin/python compare_models.py [old_dir] [new_dir]

1) in-distribution test split (same split as train.py) -> per-class recall, SAFE false-alarm rate
2) held-out NUS senders (casual real SMS, all treated as SAFE) -> false-alarm rate
3) held-out tweets (cached from baseline_heldout.py) -> harassment FPR/FNR/AUC
"""
import sys

import numpy as np
import pandas as pd
import torch
from sklearn.metrics import classification_report, roc_auc_score
from sklearn.model_selection import train_test_split
from transformers import AutoModelForSequenceClassification, AutoTokenizer

OLD, NEW = (sys.argv[1], sys.argv[2]) if len(sys.argv) == 3 else ("./model", "./model_v2")
LABELS = ["SAFE", "SCAM", "HARASSMENT"]
DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"


@torch.no_grad()
def probs(path, texts):
    tok = AutoTokenizer.from_pretrained(path)
    model = AutoModelForSequenceClassification.from_pretrained(path).to(DEVICE).eval()
    out = []
    for i in range(0, len(texts), 64):
        enc = tok(texts[i:i + 64], truncation=True, max_length=128, padding=True, return_tensors="pt").to(DEVICE)
        out.append(torch.softmax(model(**enc).logits, dim=1).cpu().numpy())
    return np.concatenate(out)


# 1) same split as train.py (v2 adds NUS to the train side only, so this test set is unchanged)
df = pd.read_csv("data/combined.csv")
df["y"] = df["label"].map({l: i for i, l in enumerate(LABELS)})
safe, other = df[df["label"] == "SAFE"], df[df["label"] != "SAFE"]
df = pd.concat([safe.sample(n=30000, random_state=42) if len(safe) > 30000 else safe, other], ignore_index=True)
_, test = train_test_split(df, test_size=0.2, random_state=42, stratify=df["y"])
texts = test["text"].astype(str).tolist()

nus = pd.read_csv("data/nus_safe_test.csv")["text"].astype(str).tolist()
tw, _ = pd.read_pickle("baseline_results/heldout_scores.pkl")  # (tweets, nus); nus half is unusable, v2 trained on it
tw_texts, tw_y = tw["text"].astype(str).tolist(), tw["y"].to_numpy() == 1

for name, path in (("OLD", OLD), ("NEW", NEW)):
    print(f"\n################ {name}: {path}")
    p = probs(path, texts)
    print("=== 1) in-distribution test split ===")
    print(classification_report(test["y"], p.argmax(1), target_names=LABELS, digits=3))
    safe_mask = test["y"].to_numpy() == 0
    print(f"SAFE false-alarm rate (argmax != SAFE): {(p[safe_mask].argmax(1) != 0).mean():.3f}")

    q = probs(path, nus)
    print(f"\n=== 2) held-out NUS senders: {len(nus)} casual real SMS, all assumed safe ===")
    print(f"flagged as anything non-SAFE (argmax): {(q.argmax(1) != 0).mean():.3f}")
    print("P(HARASSMENT) >= t: " + "  ".join(f"{t}: {(q[:, 2] >= t).mean():.3f}" for t in (0.1, 0.3, 0.5, 0.7, 0.9)))
    print(f"flagged as SCAM (argmax): {(q.argmax(1) == 1).mean():.3f}")

    h = probs(path, tw_texts)[:, 2]
    print(f"\n=== 3) held-out tweets: {len(tw_texts)} rows, {int(tw_y.sum())} sexism/racism ===")
    for t in (0.3, 0.5, 0.7, 0.9):
        pred = h >= t
        print(f"thr {t}: FPR {(pred & ~tw_y).sum() / (~tw_y).sum():.3f}  FNR {(~pred & tw_y).sum() / tw_y.sum():.3f}")
    print(f"AUC {roc_auc_score(tw_y, h):.3f}")
