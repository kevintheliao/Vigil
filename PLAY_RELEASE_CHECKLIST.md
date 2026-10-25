# Vigil production release checklist

Status as of 2026-10-24: production access GRANTED (Oct 23). Production track still inactive. Current build: 1.4 (versionCode 5). Blocker: casual-SMS false-alarm retrain.

## A. Model (do first)
- [ ] `training/train.py` finished, `model_v2/` exists
- [ ] `venv/bin/python compare_models.py` run, results saved
- [ ] Pass bar (suggested): held-out NUS flag rate well under 24% (aim under 10% at default), harassment and scam recall not worse than about 3 points, tweets AUC not lower
- [ ] Export: `export_onnx.py` on `model_v2`, quantize, run `evaluate_onnx.py ./model_onnx_quantized`
- [ ] Replace `app/src/main/assets/model_quantized.onnx` (vocab.txt and config.json unchanged unless the tokenizer changed)
- [ ] Re-check `DetectionSensitivity.kt` cutoffs: the slider (0.2 to 0.8) was tuned for the old model; the default may need to move
- [ ] `./gradlew test` passes (MessageScorerTest, DetectionSensitivityTest expect specific scores)
- [ ] Emulator check: casual texts do not flag, a known scam and a harassment text do

## B. Build
- [ ] Bump `versionCode` (5 -> 6) and `versionName` (1.4 -> 1.5) in `app/build.gradle.kts`
- [ ] Includes Terms of Use link in Settings (commit 8844a8a)
- [ ] Signed release AAB built with the upload keystore (keep the keystore and passwords backed up outside the repo)
- [ ] Install the release build on a real phone, run through onboarding: disclosure screen, SMS permission, overlay, usage access
- [ ] Commit and push

## C. Play Console (already done, re-verify nothing changed)
- [x] Closed testing requirement and production access granted
- [x] App content: 11 declarations actioned, nothing under "Need attention"
- [x] SMS declaration filled: "Caller ID, spam detection, and blocking", review notes, demo video, all 4 boxes ticked (still "Ready to send for review": it goes out with the production release)
- [x] Data safety: collects/shares no data (matches no INTERNET permission)
- [x] Store listing: name, short description (78/80), full description (1081/4000), icon, feature graphic, 4 of 8 phone screenshots
- [ ] Listing text reads honestly: say Vigil may miss threats or flag safe texts; mention it reads SMS on-device to scan for scams. Policy and review risk: keep it consistent with the SMS declaration
- [ ] Screenshots show the real current UI (update if Settings or Home changed in 1.5)
- [ ] Privacy policy URL set to https://kevintheliao.github.io/Vigil/ and page loads
- [ ] Contact email set, category and tags set
- [ ] Optional: add terms URL line in the description (https://kevintheliao.github.io/Vigil/terms.html)
- [ ] Countries/regions chosen

## D. Release
- [ ] Create production release: upload AAB, release name "1.5", release notes (what changed: fewer false alarms on everyday texts, Terms of Use, sensitivity slider)
- [ ] Use a staged rollout (about 20%) so a bad build can be halted
- [ ] Send changes for review (this also sends the SMS declaration for review; expect several days)
- [ ] Keep the closed track tester group for future updates

## E. After release
- [ ] Watch Android vitals (crashes, ANRs) and ratings daily for the first week
- [ ] Reply to reviews; if reviews say false alarms, point to the sensitivity slider
- [ ] Raise rollout to 100% after about 3 days with no issues
- [ ] Update college profile, Christina, and outreach contacts with the live link
