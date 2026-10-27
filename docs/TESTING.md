# Testing matrix

Manual verification targets (record results per release):

| API | Device/emulator | Areas |
|---|---|---|
| 30 (Android 11) | emulator | static theme, exact alarm, fallback call UI, RTL/LTR |
| 31 (Android 12) | emulator | exact-alarm permission flow, splash animation |
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

## v1.3 additions

Automated, new in 1.3:

- `update/UpdateGateTest` — the update install gate fails closed: a
  missing, blank or mismatching SHA-256 never installs.
- `data/ReschedulePolicyTest` — boot/package-replace policy for pending
  scheduled calls: future → reschedule, just-missed (≤ 60 s) → fire,
  older → settle as MISSED.
- `data/CallStatusRulesTest` — history status transitions:
  `SCHEDULED → RINGING → ANSWERED → COMPLETED`, `RINGING → MISSED`,
  terminal rows never move again.
- Shell: `bash scripts/check-release-version.test.sh` covers the release
  tag/version gate used by `.github/workflows/release.yml`.

Manual verification checklist for 1.3 (record the result per release —
these need a real device and are the owner's checks):

| Check | API |
|---|---|
| Each QS tile starts its preset, no crash | 34+ |
| Answer from notification opens the in-call screen | 31+ |
| Unanswered call ends as Missed after 45s (both modes) | any |
| Reboot with a past-due unanswered call: nothing rings | any |
| App update with a pending scheduled call: it still rings | any |
| Cancel a scheduled call; "Clear history" cancels pending alarms | any |
| Update with `.sha256` missing/unreachable is refused | any |
| Speaker toggle works and resets after the call | 31+ |
| Theme: System/Light/Dark all selectable | any |

| Release | Date | Checked by | Notes |
|---|---|---|---|
| 1.3 | — | — | pending owner verification |

## Update sheet & updater (addendum U1–U11, owner device checks)

| Scenario | Expected |
|---|---|
| Open sheet, start download | Sheet height constant across all states; no jump |
| Throttled network | Smooth bar (no stepping); indeterminate bar if size unknown |
| Tap Download twice quickly | One download only |
| Swipe down / Back during download | Blocked; Cancel button works and cleans the partial file |
| Airplane mode mid-download | NETWORK message, retry works |
| Almost-full storage | NOT_ENOUGH_STORAGE before downloading |
| .sha256 asset missing | Refuses to install (CHECKSUM_UNAVAILABLE) |
| Wrong checksum | Refuses, file deleted |
| Install a build signed with a different key | Sheet shows INSTALL_FAILED, no silent close |
| Deny then grant "install unknown apps" | Hint disappears on return |
| Arabic + largest font + 360 dp width | Nothing clipped; buttons reachable; no word-by-word wrapping |
| Full flow 1.x → newer | Installs; app restarts on the new version |
