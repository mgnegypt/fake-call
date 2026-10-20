# Call engine decision

## Question

How do we present a simulated incoming call that works reliably on Android 11
(API 30) through Android 17 (API 37), with the screen off, on the lock screen,
and with the app closed?

## Options considered (from the official Android documentation)

### 1. Telecom framework (ConnectionService)

- Register a `PhoneAccount` with `TelecomManager`, implement a
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

**Telecom first (call-provider account), automatic fallback.**

Field finding (2026-10-20): our first build registered the PhoneAccount as
`CAPABILITY_SELF_MANAGED`, and on the owner's device no call UI ever
appeared — self-managed accounts are responsible for their own UI, and many
OEM dialers render nothing for them. The reference behavior (verified from
the mechanism, implemented independently here) is to register the account
as **`CAPABILITY_CALL_PROVIDER`**: non-self-managed calls are rendered by
the default phone app's InCallService — the system shows its real incoming
call screen, plays the ringtone, and writes the call-log entry.

`CallController.startIncoming()`:

1. If the PhoneAccount is enabled → raise the call through Telecom
   (`addNewIncomingCall`); the **system phone app presents the call**. Our
   `FakeConnection` callbacks (`onAnswer` / `onReject` / `onDisconnect`)
   fire from the user's actions in that system UI, drive the history row,
   and play the answer audio on the voice-communication route. Our own call
   activity stays out of the way on this path; `Connection.onShowIncomingCallUi`
   is kept only as an OEM safety net (Telecom normally never calls it for
   call-provider accounts), bridged to the live connection if it ever fires.
2. Otherwise (account not enabled, exception, or the user picked "App screen"
   in Settings) → post the full-screen-intent notification and show our own
   incoming-call screen, mirroring the same states (ringing → in-call →
   ended) and logging history identically.

Note: switching an existing install's account capability can reset its
enabled state on some builds — the Home readiness card detects this and
asks the user to enable the account once more.

Both paths write the same Room history record and honour the same caller name,
number, avatar, and answer audio. The Settings screen shows the account state
and links to the system screen where the user can enable it, and the Home
screen warns (with a shortcut) when neither path is fully permitted
(exact-alarm / full-screen-intent / notification permission missing).

## Reboot

Alarms do not survive reboot. `BootReceiver` (on `BOOT_COMPLETED`) re-registers
every pending scheduled call from Room and re-registers the PhoneAccount.
