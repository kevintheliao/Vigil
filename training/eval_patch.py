"""Fast check of a patched model: venv/bin/python -u eval_patch.py [dir ...]  (default ./model ./model_v2 ./model_v3)

Flag rule matches the app: 1 - P(SAFE) >= 0.5. Samples are small on purpose so this takes a couple of minutes.
"""
import sys

import numpy as np
import pandas as pd
import torch
from sklearn.model_selection import train_test_split
from transformers import AutoModelForSequenceClassification, AutoTokenizer

DIRS = sys.argv[1:] or ["./model", "./model_v2", "./model_v3"]
L = ["SAFE", "SCAM", "HARASSMENT"]
DEV = "mps" if torch.backends.mps.is_available() else "cpu"
tok = AutoTokenizer.from_pretrained("distilbert-base-uncased")


@torch.no_grad()
def flag_prob(m, texts):
    out = []
    for i in range(0, len(texts), 64):
        e = tok(texts[i:i + 64], truncation=True, padding=True, max_length=128, return_tensors="pt").to(DEV)
        out.append(1 - torch.softmax(m(**e).logits, 1)[:, 0].cpu().numpy())
    return np.concatenate(out)


df = pd.read_csv("data/combined.csv")
df["y"] = df["label"].map({l: i for i, l in enumerate(L)})
safe, other = df[df.label == "SAFE"], df[df.label != "SAFE"]
df = pd.concat([safe.sample(n=30000, random_state=42), other], ignore_index=True)
_, test = train_test_split(df, test_size=0.2, random_state=42, stratify=df["y"])
old_scam = test[test.y == 1]["text"].astype(str).tolist()
harass = test[test.y == 2].sample(1500, random_state=1)["text"].astype(str).tolist()
web_safe = test[test.y == 0].sample(1500, random_state=1)["text"].astype(str).tolist()
nus = pd.read_csv("data/nus_safe_test.csv")["text"].astype(str).sample(3000, random_state=1).tolist()
mt = pd.read_csv("data/modern_scam_test.csv")
m_scam, m_safe = mt[mt.label == "SCAM"]["text"].tolist(), mt[mt.label == "SAFE"]["text"].tolist()

SPOT = [  # hand-written, not from any generator template
    ("SCAM", "Your account is locked. Verify now at http://bit.ly/x1 or lose access"),
    ("SCAM", "Chase Alert: suspicious login detected. Confirm your identity: http://chase-secure.info/verify"),
    ("SCAM", "USPS: your package could not be delivered. Update your address at http://usps-track.co/abc"),
    ("SCAM", "Is this still Jake's number? I have a job offer paying $400/day from home, text me back"),
    ("SCAM", "Hi this is Officer Davis from the IRS. You owe back taxes. Pay with gift cards today or be arrested"),
    ("SCAM", "Hey grandma it's me, I'm in jail and need bail money, please don't tell mom, send via Venmo"),
    ("SCAM", "Congratulations! You won a $500 gift card, click to claim"),
    ("SCAM", "Your Netflix payment failed. Update billing info here http://netflix-bill.net"),
    ("SAFE", "hey are you coming to dinner tonight?"), ("SAFE", "ok see you at 5"), ("SAFE", "can u send me the notes from class"),
    ("SAFE", "mom said pick up milk on the way home"), ("SAFE", "this class is killing me haha"), ("SAFE", "ugh my brother is so annoying"),
    ("SAFE", "Your Amazon order has shipped and will arrive Friday"), ("SAFE", "Your appointment is tomorrow at 3pm, reply C to confirm"),
    ("HARM", "you are worthless, nobody likes you, kill yourself"), ("HARM", "i hate you so much go away loser"), ("HARM", "I will find you and hurt you"),
]

for d in DIRS:
    m = AutoModelForSequenceClassification.from_pretrained(d).to(DEV).eval()
    r = lambda t: (flag_prob(m, t) >= 0.5).mean()
    print(f"\n#### {d}")
    print(f"modern SCAM recall, unseen families+frames ({len(m_scam)}): {r(m_scam):.3f}")
    print(f"modern look-alike SAFE flagged ({len(m_safe)}):            {r(m_safe):.3f}")
    print(f"old SCAM recall ({len(old_scam)}):                          {r(old_scam):.3f}")
    print(f"HARASSMENT recall (1500):                            {r(harass):.3f}")
    print(f"casual NUS SMS flagged (3000):                       {r(nus):.3f}")
    print(f"web-comment SAFE flagged (1500):                     {r(web_safe):.3f}")
    p = flag_prob(m, [t for _, t in SPOT])
    print("spot-check (flag prob): " + " ".join(f"{k[0]}{v:.2f}" for (k, _), v in zip(SPOT, p)))
    del m
