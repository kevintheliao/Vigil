# Vigil

On-device SMS threat detection for Android. Vigil watches incoming text messages for scams, phishing, and harassment, and surfaces a real-time alert chip over your messaging app — all processing stays on the device. The app has no internet permission: nothing it reads can leave your phone.

<p align="center">
  <img src="vigillogo1.png" alt="Vigil logo" width="120" />
</p>

## Getting started

**Requirements**

- Android Studio (latest stable)
- JDK 11+
- Android SDK 36 (min SDK 26 / Android 8.0)

**Run it**

```bash
git clone git@github.com:kevintheliao/Vigil.git
```

Open the project in Android Studio, let Gradle sync, and run the `app` configuration on an emulator or device. Or from the command line:

```bash
./gradlew installDebug
```

## How detection works

1. `SmsReceiver` catches incoming SMS broadcasts (`RECEIVE_SMS`).
2. `OnnxMessageClassifier` runs the message through a quantized DistilBERT model (`app/src/main/assets/model_quantized.onnx`, int8, ~67MB) with three labels: **SAFE / SCAM / HARASSMENT**. Tokenization is a from-scratch WordPiece implementation (`WordPieceTokenizer.kt`).
3. Threats trigger `DetectionOverlayService`, which floats an alert chip (severity color + confidence %) over the messaging app. With Usage Access granted, the chip only appears while the default SMS app is foreground.
4. Every classification is recorded in `DetectionLog` (SharedPreferences, last 100 entries) and shown on the Home and Logs tabs.

`MessageScorer.kt` is an earlier rule-based signal scorer kept alongside the ML path.

### Model training

The `training/` directory contains the full pipeline: `prepare_data.py` (dataset merging), `train.py` (DistilBERT fine-tune with class weighting), `evaluate.py` / `evaluate_onnx.py`, and `export_onnx.py` (ONNX export + int8 quantization). Raw datasets are not committed. The shipped model was trained on:

- [UCI SMS Spam Collection](https://archive.ics.uci.edu/dataset/228/sms+spam+collection)
- [Jigsaw Toxic Comment Classification](https://www.kaggle.com/c/jigsaw-toxic-comment-classification-challenge)
- [SMS Phishing Dataset for Machine Learning and Pattern Recognition](https://data.mendeley.com/datasets/f45bkkt8pr) (Mendeley Data)
- [HateXplain](https://github.com/hate-alert/HateXplain) (MIT)
- [A Comprehensive Dataset for Automated Cyberbullying Detection](https://data.mendeley.com/datasets/wmx9jj2htd/2) (Mendeley Data, CC BY 4.0 — Ejaz, Razi & Choudhury, *Computers in Human Behavior*, 2023)

Held-out eval: 92.7% accuracy, 93.3% macro F1 (quantized).

## How the app flows

Everything starts in `VigilApp.kt`, which drives the onboarding sequence via a `Flow` enum:

```
Welcome → Facts → Overview → Privacy → Permissions → OverlayPermission → UsageAccess → Main
```

| Step | Screen | Purpose |
|---|---|---|
| Welcome | `WelcomeScreen.kt` | Logo + intro |
| Facts | `SafetyFactsScreen.kt` | Cyberbullying/scam statistics with cited sources |
| Overview | `ProtectionOverviewScreen.kt` | Animated demo of Vigil flagging harmful texts |
| Privacy | `PrivacyCommitmentScreen.kt` | On-device processing commitment |
| Permissions | `PermissionsScreen.kt` | Requests `READ_SMS` + `RECEIVE_SMS` |
| OverlayPermission | `OverlayPermissionScreen.kt` | Explains and requests "Display over other apps" |
| UsageAccess | `UsageAccessScreen.kt` | Optional: explains and requests "Usage access" |
| Main | `HomeScreen.kt` / `AllLogsScreen.kt` / `EducationScreen.kt` | Home dashboard, full log history, education tab |

Each onboarding step renders inside `OnboardScaffold` (in `VigilApp.kt`), which pins the primary button at a fixed height on every screen and supports an optional `secondary` slot (captions, source citations, "Maybe later" links) directly above the button.

## Project structure

```
app/src/main/java/com/example/vigil/
├── MainActivity.kt              # Entry point, hosts VigilApp
├── OnboardingPrefs.kt           # Onboarding completion persistence
├── detection/                   # Detection pipeline + overlay
│   ├── SmsReceiver.kt           # SMS broadcast entry point
│   ├── OnnxMessageClassifier.kt # DistilBERT ONNX inference
│   ├── WordPieceTokenizer.kt    # BERT WordPiece tokenizer
│   ├── MessageScorer.kt         # Rule-based signal scorer
│   ├── DetectionLog.kt          # Rolling 100-entry classification history
│   ├── DetectionUiState.kt      # Severity (SAFE/MEDIUM/HIGH/UNKNOWN), message, risk %
│   ├── DetectionIndicator.kt    # Alert chip composable + severity colors/icons
│   └── DetectionOverlayService.kt  # Overlay window + SMS-app-foreground gating
├── ui/
│   ├── screens/                 # One file per screen + shared Components.kt
│   └── theme/                   # Colors, typography, VigilTheme
training/                        # Model training pipeline (Python)
```

Shared UI pieces live in `ui/screens/Components.kt`: `VigilCard`, `VigilPrimaryButton`, `FeatureRow`, `ShieldEmblem`, and `MessagesScanDemo` (the fake messaging-app animation used on the Overview slide).

## Permissions

| Permission | Why |
|---|---|
| `READ_SMS` / `RECEIVE_SMS` | Scan incoming messages for threats, on-device only |
| `SYSTEM_ALERT_WINDOW` | Show the detection chip over the messaging app (Settings toggle, walked through in onboarding) |
| `PACKAGE_USAGE_STATS` | Optional: keep the chip inside the SMS app instead of over every app. Without it, alerts show everywhere |

No `INTERNET` permission — the OS itself guarantees no data leaves the device. See [PRIVACY.md](PRIVACY.md).

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- ONNX Runtime for on-device inference
- Single-activity architecture, no navigation library — screen state held in `VigilApp.kt`
- Gradle version catalog (`gradle/libs.versions.toml`)

## Development notes

- Verify changes compile with `./gradlew compileDebugKotlin`
- Run unit tests with `./gradlew testDebugUnitTest`
- Most screens have `@Preview` composables — use Android Studio's preview pane for quick UI iteration
- Fact sources on the Facts slide: UNICEF (2019), FTC text-scam loss data (2025 report, 2024 losses), Pew Research Center (2022)

## License

All rights reserved — see [LICENSE](LICENSE). The code is public for viewing and reference; reuse requires written permission.
