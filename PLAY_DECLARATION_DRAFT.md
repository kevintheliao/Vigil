# Play Console submission drafts (Vigil, com.vigilapp.vigil)

Paste into App content > Sensitive permissions and APIs. Keep every answer factual; reviewers compare it to the app and the privacy policy.

## SMS permissions (READ_SMS, RECEIVE_SMS)

Core functionality: Vigil's single purpose is to detect scam, phishing, and harassing text messages. It reads each incoming SMS and scores it with an on-device machine-learning model, then shows a warning chip. Without SMS access the app has no function.

Be upfront: Vigil is not the default SMS handler. Select the closest available use case, and in the free-text box say: "Vigil is a safety layer that works alongside the user's own messaging app. It cannot be the default SMS app without replacing the user's messaging experience, which would defeat its purpose."

Data handling statement: "Message content is processed only on the device. The app has no INTERNET permission, so it cannot transmit data. Detection history (the last 100 results, each with a short snippet) is stored in private app storage, excluded from backup, and deleted on uninstall or with Clear detection log. No SMS data is shared with anyone."

Demo video: a short screen recording that shows (1) the in-app disclosure, (2) the permission prompt, (3) a scam text triggering the warning chip, (4) Settings > Privacy opening the policy. The disclosure text in the app now names SMS explicitly.

## Usage access (PACKAGE_USAGE_STATS)

Optional. Used only to show alert chips while the messaging app is in the foreground. Nothing is recorded or transmitted. Say it is optional and the app works without it.

## Display over other apps (SYSTEM_ALERT_WINDOW)

Used to show the warning chip above the messaging app when a message is flagged. No data collected.

## Data safety form

- Data collected: none. Data shared: none.
- Encrypted in transit: not applicable (no network access).
- Users can request deletion: Clear detection log button, or uninstall.
- Privacy policy URL: https://kevintheliao.github.io/Vigil/

## Checks before submitting

1. Policy contact email (kevintheliao@gmail.com) is an inbox you read.
2. Store listing says Vigil reads SMS and runs on-device; do not claim it blocks or deletes texts.
3. No crashes or ANRs in the pre-launch report.
4. Expect a possible rejection on the SMS declaration. If so, the fallback is a NotificationListenerService build (weaker detection) or distributing the APK directly.
