# Production access application: draft answers (do not submit until the v1.4 build is in the closed track)

Facts used here come from the Play Console, the git history, and the tester pack. Edit anything that is not true for you.

## 1. How did you recruit testers and run the closed test?
Vigil was tested for 16 days by a group of 14 testers (12 or more opted in continuously) recruited through a peer app-testing community, where members install and use each other's apps daily. Testers installed the closed-track build from the opt-in link, opened the app repeatedly, and submitted written feedback.

## 2. What feedback did you get, and what did you change?
- Our own testing found that Home showed "Detection is ready" even when Display over other apps was off, which meant detection ran but no warning could appear. Change (v1.3, Oct 6): Home now shows "Detection is on, warnings are off" with a banner and Turn on buttons for the overlay and usage-access permissions, and re-checks them when the user returns to the app.
- A researcher who works on harmful-text detection suggested letting users tune how sensitive detection is. Change (Oct 10): Settings now has a detection sensitivity slider ("Fewer alerts" to "More alerts").
- Review of the Play policy led to clearer disclosures. Change (v1.4): the permission screen now says plainly that Vigil reads incoming text messages (SMS) to scan them on the device, and Settings > Privacy opens the full privacy policy.

## 3. How did you decide the app is ready for production?
Android vitals shows no crashes or ANRs from Sep 20 to Oct 18, 2026. The app works fully offline with no INTERNET permission, so there is no server dependency. Testers installed and used the app on at least five different devices (Android 10 and Android 16) and reported that it opened and worked as expected. Detection was evaluated separately by the developer, not by testers. The on-device model was trained on public SMS-spam, phishing and harassment datasets and scores 92.7% accuracy and 93.3% macro F1 on a held-out split of that data (quantized model). Real messages differ from the training data, so sensitivity is user-adjustable in Settings and results should be read as a development benchmark, not a guarantee.

## 4. Who is the target audience and what value does it give?
People who receive scam, phishing, and harassing text messages, including teens and older adults. Vigil scores each incoming SMS on the device with a bundled machine-learning model and shows a warning chip, without uploading anything.

## 5. Expected growth
Start small: classmates, family, and online communities, plus anyone who finds it in the Play Store.

## Sensitive permission note
The SMS declaration (Aug 19) selects "Caller ID, spam detection, and blocking" and "Anti-SMS Phishing", and includes reviewer instructions (447/500 characters). The video-instructions field is empty. Add an unlisted demo video link there before production review if you can.
