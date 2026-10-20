# Testing matrix

Manual verification targets (record results per release):

| API | Device/emulator | Areas |
|---|---|---|
| 30 (Android 11) | emulator | static theme (no Monet), exact alarm, fallback call UI, RTL/LTR |
| 31 (Android 12) | emulator | dynamic color, exact-alarm permission flow, splash animation |
| 33 (Android 13) | emulator | POST_NOTIFICATIONS flow, per-app language switch |
| 34 (Android 14) | emulator | full-screen intent permission, Telecom account enable flow |
| 35 (Android 15) | device | edge-to-edge, lock-screen call, reboot persistence |
| 36 (Android 16) | emulator | behavior-change pass from release notes |
| 37 (Android 17) | emulator | behavior-change pass from release notes |

Scenarios that must pass on every level:

- Scheduled call rings on time: screen off, lock screen, app closed, after reboot.
- Decline and answer both write the correct history entry.
- Answer audio plays once, on the voice route, and stops on end.
- Arabic = full RTL, English = full LTR; phone numbers render LTR and readable.
- Update flow with a fake lower versionName: badge, notes (AR/EN), checksum
  verification, install, GitHub fallback.
- Splash + intro: smooth, skippable, shortened when system animations are off.

Automated: JVM unit tests under `app/src/test` cover the version comparator,
the release-notes parser (both languages, missing/malformed sections), asset
selection, and SHA-256 verification. They run in CI on every push.
