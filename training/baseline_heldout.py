"""Score Vigil and Detoxify on data neither was trained on.

A) data/twitter_harassment.csv : labeled tweets (sexism/racism vs none)  -> misses AND false alarms
B) data/nus_sms_corpus.xml     : real SMS, unlabeled, mostly everyday chat -> false-alarm rate only

Rows that also appear in data/combined.csv (Vigil's training pool) are dropped first.
Usage: ./venv/bin/python baseline_heldout.py [N]   (N = rows per set, quick test; no cache written)
"""
import hashlib
import os
import re
import sys
import xml.etree.ElementTree as ET

import numpy as np
import pandas as pd
import torch
from sklearn.metrics import roc_auc_score, roc_curve
from transformers import AutoModelForSequenceClassification, AutoTokenizer

LIMIT = int(sys.argv[1]) if len(sys.argv) > 1 else None
NUS_CAP = 10000
DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"
OUT = "baseline_results"


def key(t):
    # letters and digits only, so tokenizer spacing and punctuation do not hide a duplicate
    return hashlib.md5(re.sub(r"[^a-z0-9]", "", str(t).lower()).encode()).hexdigest()


@torch.no_grad()
def score(model_path, texts, fn):
    tok = AutoTokenizer.from_pretrained(model_path)
    model = AutoModelForSequenceClassification.from_pretrained(model_path).to(DEVICE).eval()
    out = []
    for i in range(0, len(texts), 64):
        enc = tok(texts[i:i + 64], truncation=True, max_length=128, padding=True, return_tensors="pt").to(DEVICE)
        out.append(fn(model(**enc).logits).cpu().numpy())
    return np.concatenate(out)


def score_both(df):
    texts = df["text"].astype(str).tolist()
    df["detoxify"] = score("unitary/toxic-bert", texts, lambda l: torch.sigmoid(l[:, 0]))  # "toxic"
    df["vigil"] = score("./model", texts, lambda l: torch.softmax(l, dim=1)[:, 2])         # HARASSMENT
    return df


cache = f"{OUT}/heldout_scores.pkl"
if os.path.exists(cache) and LIMIT is None:
    tw, nus = pd.read_pickle(cache)
else:
    seen = set(key(t) for t in pd.read_csv("data/combined.csv", usecols=["text"])["text"].dropna())

    tw = pd.read_csv("data/twitter_harassment.csv").rename(columns={"Text": "text"})
    tw = tw.dropna(subset=["text", "Annotation"])
    tw["y"] = (tw["Annotation"].str.strip().str.lower() != "none").astype(int)  # sexism/racism = 1
    tw = tw[["text", "y", "Annotation"]].drop_duplicates("text")
    n0 = len(tw)
    tw = tw[~tw["text"].map(key).isin(seen)].reset_index(drop=True)
    print(f"twitter: {n0} rows, {n0 - len(tw)} overlapped Vigil's training pool and were dropped, {len(tw)} kept")

    msgs = [m.findtext("text") for m in ET.parse("data/nus_sms_corpus.xml").getroot().iter("message")]
    nus = pd.DataFrame({"text": [m for m in msgs if m]}).drop_duplicates("text")
    n1 = len(nus)
    nus = nus[~nus["text"].map(key).isin(seen)].reset_index(drop=True)
    print(f"nus sms: {n1} rows, {n1 - len(nus)} overlapped and were dropped, {len(nus)} kept")
    nus = nus.sample(n=min(NUS_CAP, len(nus)), random_state=0).reset_index(drop=True)

    if LIMIT:
        tw, nus = tw.sample(n=min(LIMIT, len(tw)), random_state=0), nus.sample(n=min(LIMIT, len(nus)), random_state=0)
    tw, nus = score_both(tw.copy()), score_both(nus.copy())
    if LIMIT is None:
        os.makedirs(OUT, exist_ok=True)
        pd.to_pickle((tw, nus), cache)

print(f"\n=== A) Tweets: {len(tw)} rows, {int(tw['y'].sum())} labeled sexism/racism, {int((1 - tw['y']).sum())} none ===")
print(f"{'model':9} {'thr':>4} {'FPR':>7} {'FNR':>7}")
for m in ("vigil", "detoxify"):
    for t in (0.1, 0.3, 0.5, 0.7, 0.9):
        p, y = tw[m].to_numpy() >= t, tw["y"].to_numpy() == 1
        print(f"{m:9} {t:4.1f} {(p & ~y).sum() / max((~y).sum(), 1):7.3f} {(~p & y).sum() / max(y.sum(), 1):7.3f}")
y = tw["y"].to_numpy() == 1
for m in ("vigil", "detoxify"):
    fpr, tpr, _ = roc_curve(y, tw[m].to_numpy())
    cells = [f"FNR@FPR{int(f * 100)}%={1 - tpr[np.searchsorted(fpr, f, side='right') - 1]:.3f}" for f in (0.05, 0.10, 0.25)]
    print(f"{m:9} AUC {roc_auc_score(y, tw[m].to_numpy()):.3f}  " + "  ".join(cells))

print(f"\n=== B) Real SMS: {len(nus)} messages, share flagged as harassment (all assumed safe) ===")
for m in ("vigil", "detoxify"):
    print(f"{m:9} " + "  ".join(f">={t}: {(nus[m] >= t).mean():.3f}" for t in (0.1, 0.3, 0.5, 0.7, 0.9)))
if LIMIT is None:
    top = nus.sort_values("vigil", ascending=False).head(40)
    top.to_csv(f"{OUT}/nus_top_flagged_by_vigil.csv", index=False)
    print(f"\nsaved {OUT}/nus_top_flagged_by_vigil.csv for manual review")
