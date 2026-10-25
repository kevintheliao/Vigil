"""Templated modern smishing (SCAM) plus look-alike legitimate automated texts (SAFE).

The shipped training data is UK-style 2010s spam, so delivery/bank/toll/job phishing is missed. Synthetic and
template-based: expect it to teach the pattern, not replace real data. Test set holds out whole scam families
and unseen frames, so a good test score means generalization beyond the training templates.

Run: venv/bin/python generate_modern_scams.py -> data/modern_scam_{train,test}.csv
"""
import random

import pandas as pd

rng = random.Random(42)

BRANDS = ["USPS", "FedEx", "UPS", "DHL", "Amazon", "Chase", "Wells Fargo", "Bank of America", "Citi", "PayPal", "Venmo",
          "Zelle", "Apple", "Netflix", "Coinbase", "Capital One", "Cash App", "Navy Federal", "Walmart", "Microsoft"]
TLDS = [".com", ".net", ".info", ".co", ".top", ".xyz", ".online", ".help", ".live", ".site"]
WORDS = ["secure", "verify", "update", "login", "account", "support", "service", "confirm", "track", "portal", "alert", "id"]


def url(brand=None):
    b = (brand or rng.choice(BRANDS)).lower().replace(" ", "")
    kind = rng.random()
    if kind < 0.2:
        return f"http://bit.ly/{''.join(rng.choices('abcdefghjkmnpqrstuvwxyz23456789', k=7))}"
    if kind < 0.3:
        return f"https://tinyurl.com/{''.join(rng.choices('abcdefghjkmnpqrstuvwxyz23456789', k=7))}"
    w = rng.choice(WORDS)
    sep = rng.choice(["-", "", "-"])
    d = rng.choice([f"{b}{sep}{w}", f"{w}{sep}{b}", f"{b}{sep}{w}{rng.randint(1, 99)}"]) + rng.choice(TLDS)
    path = rng.choice(["", "/", f"/{w}", f"/{rng.randint(1000, 99999)}", f"/?id={rng.randint(10000, 999999)}"])
    return rng.choice(["http://", "https://", "https://"]) + d + path


def amount():
    return rng.choice([f"${rng.randint(2, 9)}.{rng.randint(10, 99)}", f"${rng.randint(20, 900)}", f"${rng.randint(1, 5)},{rng.randint(100, 999)}"])


def tracking():
    return rng.choice(["", f" Ref {rng.randint(100000, 999999)}.", f" Tracking #{rng.choice(['US', '1Z', 'TB'])}{rng.randint(10**8, 10**10)}."])


def sign():
    return rng.choice(["", "", "", " Reply STOP to opt out.", " Msg&data rates may apply.", " Thank you."])


# (family, [frames]); {b}=brand {u}=url {a}=amount {t}=tracking
TRAIN_FAMILIES = {
    "delivery": [
        "{b}: your package could not be delivered due to an incorrect address. Update your details here {u}{t}",
        "{b} Notice: we attempted delivery but nobody was home. Reschedule within 24h or it will be returned: {u}",
        "Your parcel is held at our depot. A redelivery fee of {a} is required. Pay now {u}",
        "{b}: package #{t} is pending. Confirm your address to avoid return to sender {u}",
    ],
    "bank": [
        "{b} Alert: suspicious login detected on your account. Confirm your identity now: {u}",
        "{b}: a charge of {a} was attempted on your card. If this wasn't you, secure your account {u}",
        "Fraud Alert from {b}. Your debit card has been locked. Call back or verify here {u}",
        "{b} security: unusual activity on your account. Reply YES to confirm or follow {u} to avoid suspension",
    ],
    "account_locked": [
        "Your {b} account has been locked. Verify now at {u} or lose access",
        "{b}: your account will be suspended in 24 hours unless you update your information {u}",
        "Final notice: your {b} account is restricted. Log in to restore access {u}",
        "We detected a sign in from a new device. If this was not you, reset your password immediately {u}",
    ],
    "tax_gov": [
        "IRS: you are eligible for a tax refund of {a}. Claim it here {u}",
        "This is a final notice from the IRS. You owe back taxes. Call now or a warrant will be issued for your arrest",
        "Social Security Administration: your number has been suspended due to suspicious activity. Call to reactivate",
        "Your stimulus payment of {a} is waiting. Confirm your bank details at {u}",
    ],
    "job_wrong_number": [
        "Hi, is this {n}? I saw your resume. We have a remote job paying {a}/day, no experience needed. Text me back",
        "Hello, I'm hiring part time assistants. Earn {a} per day working from home. Message me on WhatsApp",
        "Sorry wrong number! But you seem nice. I'm {n}, how are you?",
        "Congrats, you've been selected for a paid position. Contact our HR manager to start today: {u}",
    ],
    "prize_gift": [
        "Congratulations! You won a {a} {b} gift card. Claim your prize here {u}",
        "You have been selected for a free iPhone. Complete the short survey to claim {u}",
        "{b} Rewards: you have an unclaimed reward worth {a}. Expires today {u}",
        "Winner! Your number was drawn in our monthly giveaway. Reply with your name and address to receive your prize",
    ],
    "subscription": [
        "{b}: your payment failed. Update your billing info to avoid cancellation {u}",
        "Your {b} membership will renew for {a} today. To cancel, call {p}",
        "{b} receipt: charge of {a} for your order. If you did not authorize this, call {p} now",
        "Your {b} subscription could not be renewed. Re-enter your card details: {u}",
    ],
    "family_emergency": [
        "Hi mom, I lost my phone and this is my new number. I need help paying a bill, can you send {a}?",
        "Grandma it's me, I'm in trouble and need bail money. Please don't tell mom or dad. Send via Venmo",
        "Hey it's your son, I got in an accident and need money for the hospital right now, please wire {a}",
        "Dad I'm stuck abroad and my card was stolen. Can you send gift cards? I'll explain later",
    ],
    "tech_support": [
        "{b} Security: your device is infected with 3 viruses. Call {p} immediately to remove",
        "Your {b} ID has been compromised. A technician will contact you. Do not turn off your device {u}",
        "{b}: your computer has been blocked due to illegal activity. Call support now",
        "Your cloud storage is full and your photos will be deleted. Upgrade now to keep them {u}",
    ],
    "code_theft": [
        "I accidentally sent my verification code to your number, can you please send it back to me?",
        "{b}: to complete your refund, reply with the 6 digit code you just received",
        "Hey I'm locked out and your number got my code by mistake. Pls forward it to me, thanks!",
    ],
}

# held out entirely: model never sees these families in training
TEST_FAMILIES = {
    "toll": [
        "Final notice: you have an unpaid toll balance of {a}. Pay now to avoid additional fees {u}",
        "{b} E-ZPass: your toll account is overdue {a}. Settle at {u} or a late fee applies",
    ],
    "crypto_invest": [
        "Hi, I made {a} last week trading crypto with my mentor's signals. Want in? Message me",
        "Your {b} wallet has been flagged. Verify your recovery phrase to unlock your funds {u}",
        "Guaranteed 300% returns in 7 days. Limited spots, deposit {a} to start: {u}",
    ],
    "utility": [
        "Your electric bill is past due. Service will be disconnected today unless you pay {a} at {u}",
        "Your water service will be shut off at 5 PM. Call {p} to make a payment now",
    ],
    "student_loan": [
        "Your student loan qualifies for forgiveness of up to {a}. Apply before the deadline {u}",
        "Final notice: your federal student aid is at risk. Confirm your FSA ID here {u}",
    ],
}

NAMES = ["Emily", "Jason", "Sarah", "Mike", "Linda", "Chris", "Priya", "David", "Maria", "Alex"]

SAFE_FRAMES = [  # legitimate automated or ordinary texts that share vocabulary with the scams
    "{b}: your order has shipped and will arrive Thursday. Track it in the {b} app anytime.",
    "{b}: your package was delivered to your front door at 2:14 PM.",
    "Your appointment with Dr. {n} is tomorrow at 3:30 PM. Reply C to confirm or R to reschedule.",
    "{b}: a payment of {a} to your account was received. Thank you!",
    "Your verification code is {c}. Do not share it with anyone.",
    "{c} is your {b} security code. It expires in 10 minutes.",
    "{b}: you spent {a} at Starbucks with your card ending in {d}. Reply HELP for help.",
    "Your prescription is ready for pickup at the pharmacy. Store closes at 9 PM.",
    "Reminder: your {b} bill of {a} is due on the 15th. Autopay is on.",
    "Hey it's {n}, just got to the airport. Landing at 6, can you pick me up?",
    "Mom here, can you grab milk on the way home? I'll pay you back tonight",
    "Hi this is {n} from the front desk. Your car is ready for pickup",
    "School alert: classes are delayed 2 hours tomorrow due to weather.",
    "lol I lost my phone charger again, can I borrow yours? will give it back tmrw",
    "Your ride is arriving in 3 minutes. Driver: {n}, white Toyota Camry.",
    "{b}: welcome! Your account was created successfully. No action needed.",
    "Thanks for shopping at {b}! Your receipt for {a} has been emailed to you.",
    "Dinner at 7? I'll text you the address, {n} is coming too",
    "Your gym membership renews on the 1st. Manage it anytime in the app.",
    "Hey did you see the group project doc? I added my part, can you check the last slide",
]


def fill(frame, **kw):
    s = frame.format(
        b=kw.get("b") or rng.choice(BRANDS), u=url(kw.get("b")), a=amount(), t=tracking(), n=rng.choice(NAMES),
        p=f"1-{rng.choice(['800', '888', '877', '866', '855'])}-{rng.randint(200, 999)}-{rng.randint(1000, 9999)}",
        c=rng.randint(100000, 999999), d=rng.randint(1000, 9999),
    )
    s = s.strip() + sign()
    if rng.random() < 0.15:
        s = s.lower()
    if rng.random() < 0.1:
        s = s.replace(".", "").replace(",", "")
    return s


def build(families, n_per_frame):
    return [(fill(f), "SCAM") for frames in families.values() for f in frames for _ in range(n_per_frame)]


# train frames: also hold out one frame per train family for the test set (unseen wording, seen topic)
train_fam, held_frames = {}, {}
for fam, frames in TRAIN_FAMILIES.items():
    held_frames[fam] = [frames[-1]]
    train_fam[fam] = frames[:-1]

train = build(train_fam, 90)
test = build(TEST_FAMILIES, 40) + build(held_frames, 40)
safe_train = [(fill(f), "SAFE") for f in SAFE_FRAMES[:15] for _ in range(70)]
safe_test = [(fill(f), "SAFE") for f in SAFE_FRAMES[15:] for _ in range(50)]

for name, rows in (("train", train + safe_train), ("test", test + safe_test)):
    df = pd.DataFrame(rows, columns=["text", "label"]).drop_duplicates("text").sample(frac=1, random_state=42)
    df.to_csv(f"data/modern_scam_{name}.csv", index=False)
    print(name, df.label.value_counts().to_dict())
print(pd.read_csv("data/modern_scam_train.csv").sample(8, random_state=1).to_string())
