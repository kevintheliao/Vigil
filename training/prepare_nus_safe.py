"""Casual real-SMS SAFE set from the NUS SMS corpus, split by sender so no one's texts land in both train and test."""
import hashlib
import re
import xml.etree.ElementTree as ET

import numpy as np
import pandas as pd


def key(t):
    return hashlib.md5(re.sub(r"[^a-z0-9]", "", str(t).lower()).encode()).hexdigest()


# NUS is unlabeled and contains some promo/spam; drop anything that looks like it so SCAM recall is not poisoned
PROMO = re.compile(r"https?://|www\.|\.com\b|\b\d{5,}\b|£|\bprize\b|\bwinner\b|\bcongratulations\b|\bfree\b.*\b(txt|text|call|msg)\b|\b(txt|text) (stop|win)\b|\bclaim\b", re.I)

rows = []
for m in ET.parse("data/nus_sms_corpus.xml").getroot().iter("message"):
    t = m.findtext("text")
    uid = m.findtext("source/userProfile/userID") or "unknown"
    if t and t.strip():
        rows.append((t.strip(), uid))
nus = pd.DataFrame(rows, columns=["text", "uid"]).drop_duplicates("text")
n0 = len(nus)

seen = set(key(t) for t in pd.read_csv("data/combined.csv", usecols=["text"])["text"].dropna())
nus = nus[~nus["text"].map(key).isin(seen)]
n1 = len(nus)
nus = nus[~nus["text"].str.contains(PROMO)]
nus = nus[nus["text"].str.len() >= 3]
n2 = len(nus)

uids = np.array(sorted(nus["uid"].unique()))
rng = np.random.default_rng(42)
test_uids = set(rng.choice(uids, size=max(1, int(0.2 * len(uids))), replace=False))
is_test = nus["uid"].isin(test_uids)
nus["label"] = "SAFE"
nus[~is_test][["text", "label"]].to_csv("data/nus_safe_train.csv", index=False)
nus[is_test][["text", "label"]].to_csv("data/nus_safe_test.csv", index=False)
print(f"{n0} unique -> {n1} after overlap removal -> {n2} after promo filter")
print(f"{len(uids)} senders; train {(~is_test).sum()} rows, test {is_test.sum()} rows ({len(test_uids)} held-out senders)")
