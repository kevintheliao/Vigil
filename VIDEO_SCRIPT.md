# Vigil — CAC Demo Video Script

Target: 2:45–2:55 (limit is 3:00 sharp — being 5-10s under is safer than risking a cutoff).
Format that's won this district 3 of the last 5 years: personal/local hook → stat → live screen-recorded demo → tech stack named out loud → honest close.

---

## 0:00–0:20 — Hook (talking head, camera)

Pick ONE, whichever is true for you — a real personal hook beat every generic one in the winners we reviewed:

> **Option A (personal):** "[Fill in: a scam or harassing text you or someone close to you actually got — what it said, how it made you feel, what you did about it.]"

> **Option B (no personal story):** "Every year, texts like 'Your account will be suspended, click here to verify' land on millions of phones — including probably yours. Most people can't tell which ones are real."

Don't skip this — read it out loud once before filming, if it doesn't sound like you actually experienced it, use Option B instead. Judges can tell.

## 0:20–0:40 — The problem, with numbers

> "Text scams cost people $470 million a year, according to the FTC. And it's not just scams — 1 in 3 teens have been cyberbullied at some point, 46% have experienced it directly. Most phones do nothing to warn you. You just get the text."

## 0:40–0:50 — Introduce Vigil

> "That's why I built Vigil — an Android app that watches your incoming texts for scams and harassment, and warns you the moment a harmful one arrives. Everything happens on your phone. Nothing you receive ever leaves the device."

*(cut to screen recording)*

## 0:50–2:00 — Live demo (screen recording, voiceover)

Film this on a real device — the emulator can't receive SMS. Send yourself a test scam text and a test harassment text from a second phone/Google Voice number while recording.

1. **Onboarding** (5s, quick cut): show the permissions flow briefly — "Vigil asks for SMS access to scan messages, and Display-over-other-apps to show the alert — that's it, no internet permission at all."
2. **Incoming scam text fires the chip** (15-20s): show a text arriving, the alert chip popping up over the messaging app in real time. "The second a suspicious text lands, Vigil flags it — here's a gift-card scam text arriving live."
3. **Tap the chip → Analysis screen** (15-20s): "Tapping the alert shows you why it was flagged — here you can see the exact signals that matched: gift card request, urgency language."
4. **Home tab / history** (10s): "Every classification gets logged, so you can see your message history and what's been flagged over time."
5. **Harassment example** (15-20s): send a harassment-pattern text live, show the chip firing with the different color/severity, tap through to show a matched signal like a threat or guilt-tripping language.
6. **(Optional, if time allows) Context-awareness**: "If you're already reading the exact conversation the text came in, Vigil doesn't bother you with a redundant alert — it only interrupts you when you're not already looking at it."

## 2:00–2:35 — How it works (tech stack, named explicitly)

> "Vigil runs two detection layers. The first is a DistilBERT transformer model — fine-tuned on real SMS spam, phishing, and cyberbullying datasets, then quantized to run fully on-device through ONNX Runtime, with a WordPiece tokenizer I built from scratch. It's about 92.7% accurate, 93.3% macro F1, on held-out data.
>
> The second layer is a rule-based scorer that catches specific patterns the model wasn't trained on — romance scams, grooming attempts, emotional manipulation — plus a text normalizer that undoes obfuscation, like spaced-out letters or invisible characters people use to dodge filters.
>
> The whole app is Kotlin and Jetpack Compose. Since there's no INTERNET permission in the manifest at all, it's not just a privacy promise — the OS itself guarantees nothing you text can leave the phone."

*(cut back to camera, optional: quick VS Code/Android Studio glance at the model export or the detection code, like the past winners did)*

## 2:35–2:55 — Close

> "[If applicable: I've been using it on my own phone for X weeks / shared it with friends and family.] I built Vigil because [your real reason]. I hope it helps people catch a harmful text before it catches them. Thank you."

---

## Filming notes
- Talking-head segments: plain background, phone/laptop camera at eye level, like every past winner did (no fancy lighting needed).
- Screen recording: use Android's built-in screen recorder on a real device, not the emulator.
- Say every stat and every technology name out loud, clearly — this is what the Code/Ideology rubric rows are actually scored on.
- Do one full read-through with a timer before recording for real — trim the tech section first if you're over, it's the most compressible part.
