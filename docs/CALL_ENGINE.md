# Call engine decision

## Question

How do we present a simulated incoming call that works reliably on Android 11
(API 30) through Android 17 (API 37), with the screen off, on the lock screen,
and with the app closed?

## Options considered (from the official Android documentation)

### 1. Telecom framework (self-managed ConnectionService)

- Register a self-managed `PhoneAccount` with `TelecomManager`, implement a
  `ConnectionService`, and raise the call with `addNewIncomingCall`.
- The system in-call UI (the device's default dialer / Telecom UI) renders the
  ringing and in-call experience and the call is written to the system call
  log — the most "native" result.
- Limits, documented by Google: the account must be **enabled by the user** in
  the system call-account settings before calls can be placed; self-managed
  calls do not ring with the default ringtone UI on every OEM build, behavior
  varies by device, and the app must hold `READ_PHONE_STATE`. Audio routing is
  ours to manage (`MODE_IN_COMMUNICATION`, audio focus on the voice stream).
- If the account is disabled or the platform rejects the call, no UI appears at
  all — a silent failure for the user.

### 2. Own full-screen UI (fallback)

- A high-priority notification with a full-screen intent launches our own
  incoming-call activity (`showWhenLocked` + `turnScreenOn`), with our own
  ringtone playback and vibration.
- Works without any account setup and looks identical on every device.
- Limits: Android 14+ requires the `USE_FULL_SCREEN_INTENT` capability to be
  granted (system setting), and background starts are mediated by the
  notification. Audio is played by us on the media/voice stream.

## Decision

**Telecom first, automatic fallback — with our own UI on both paths.**

Field finding (2026-10-20): several OEM dialers never render an in-call UI
for self-managed accounts, so "let the system UI ring" silently produced no
visible call on real devices. The documented contract for self-managed
accounts is that the app presents the UI; Telecom signals this through
`ConnectionService.onShowIncomingCallUi`.

`CallController.startIncoming()` now:

1. If the PhoneAccount is enabled → raise the call through Telecom
   (`addNewIncomingCall`). When Telecom asks for UI
   (`onShowIncomingCallUi`), our incoming-call activity opens; its buttons
   are bridged to the live `Connection` (`answer()` / `reject()` /
   `disconnect()`), so the call is a real Telecom call — in the system call
   log and audio-routed — with our screen as its face. If the device's
   dialer also shows its own UI, both control the same connection.
2. Otherwise (account not enabled, exception, or the user picked "App screen"
   in Settings) → post the full-screen-intent notification and show the same
   incoming-call screen, mirroring the same states (ringing → in-call →
   ended) and logging history identically.

Both paths write the same Room history record and honour the same caller name,
number, avatar, and answer audio. The Settings screen shows the account state
and links to the system screen where the user can enable it, and the Home
screen warns (with a shortcut) when neither path is fully permitted
(exact-alarm / full-screen-intent / notification permission missing).

## Reboot

Alarms do not survive reboot. `BootReceiver` (on `BOOT_COMPLETED`) re-registers
every pending scheduled call from Room and re-registers the PhoneAccount.
