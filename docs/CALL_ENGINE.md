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

**Two strict modes, chosen by the user in Settings. No silent substitution.**

Field finding (2026-10-20): our first build registered the PhoneAccount as
`CAPABILITY_SELF_MANAGED`, and on the owner's device no call UI ever
appeared — self-managed accounts are responsible for their own UI, and many
OEM dialers render nothing for them. The reference behavior (verified from
the mechanism, implemented independently here) is to register the account
as **`CAPABILITY_CALL_PROVIDER`**: non-self-managed calls are rendered by
the default phone app's InCallService — the system shows its real incoming
call screen, plays the ringtone, and writes the call-log entry.

Second field finding (2026-10-20, owner's REDMAGIC screenshots): even with
the call-provider account in place, calls still rang in OUR aurora screen
while the reference app rang in the plain system dialer. Two defects
combined to cause this: the write side of the Telecom extras did not match
the read side (see "Extras contract" below), and `startIncoming` silently
fell back to the app screen whenever any Telecom gate link failed — so a
disabled account (its enabled state can be reset when an existing
install's account capability changes) permanently masked the system UI,
and the user had no way to tell why. The fallback has been removed from
Telecom mode.

`CallController.startIncoming()` — the single funnel for every trigger
(immediate, scheduled alarm, boot reschedule, automation broadcast,
preset, QS tile; all arrive via `CallRepository.fireNow`). The routing
decision itself is the pure, unit-tested `CallRouting.decide(mode,
placed)`:

1. **Mode "telecom" (system dialer) — Telecom ONLY.**
   - `addNewIncomingCall` accepted → the **system
     phone app presents the call**. Our `FakeConnection` callbacks
     (`onAnswer` / `onReject` / `onDisconnect`) fire from the user's
     actions in that system UI, drive the history row, and play the
     answer audio on the voice-communication route. Our own call activity
     plays NO part on this path — there is no `onShowIncomingCallUi`
     override and no app-side answering of Telecom calls.
   - Placement rejected/thrown → **nothing rings**. State
     returns to Idle, the history row is finished as `FAILED`, and the
     user gets an honest notification ("System call account not
     available") whose tap/action deep-links to the system call-account
     settings (`TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS`). The app
     screen is NEVER substituted in this mode: a substituted UI is what
     hid the disabled account in the first place.
2. **Mode "app" (app call screen)** → post the full-screen-intent
   notification and show our own incoming-call screen, mirroring the same
   states (ringing → in-call → ended) and logging history identically.
   The Telecom account state is irrelevant in this mode.

Placement is attempted UNCONDITIONALLY in Telecom mode — the registrar
decides whether the call goes through, not a probe on our side. An
earlier revision gated placement on an `accountEnabled` check; on the
owner's REDMAGIC 10 Pro every trigger then died at that gate (FAILED rows
+ the unavailable notification) while the system settings showed the
Fake Call account toggle ON and the reference app rang normally. Whether
that probe read a stale/different account state or something else, gating
on it was the defect: the check is still taken on every trigger, but
only as diagnostics.

Every trigger logs one line under tag `FakeCallTelecom` with the mode,
the account-enabled probe result, whether placement was attempted, its
result, the placement exception (class + message) when it throws, and
the chosen route. The same record — plus a registrar snapshot
(`getPhoneAccount` for our handle: found/enabled/capabilities, and
every call-capable account with label + enabled state) — is persisted
and shown in Settings under "Last call attempt", so the device's own
report drives the next diagnosis instead of guessing.

Note: switching an existing install's account capability can reset its
enabled state on some builds — the Home readiness card detects this and
asks the user to enable the account once more.

Both successful paths write the same Room history record and honour the
same caller name, number, and answer audio. The Settings screen shows the
account state and links to the system screen where the user can enable
it, and the Home screen warns (with a shortcut) when a prerequisite is
missing (disabled account / exact-alarm / full-screen-intent /
notification permission).

## Extras contract (addNewIncomingCall ↔ ConnectionService)

The bundle handed to `addNewIncomingCall` has a fixed two-level shape,
and writer and reader must agree on it exactly:

- **Top level:** `TelecomManager.EXTRA_INCOMING_CALL_ADDRESS` = the
  `tel:` URI for the caller number.
- **Nested** under `TelecomManager.EXTRA_INCOMING_CALL_EXTRAS`: a child
  bundle carrying the app-specific values — history id, caller name, and
  the optional answer-audio URI.

Telecom delivers the outer bundle as `ConnectionRequest.extras`, and
`FakeConnectionService` reads the child bundle from the nested key. An
earlier build wrote the app values at the TOP level while the service
read the nested bundle, so caller name, audio URI, and history id all
arrived empty on the Telecom path — keep both sides aligned with this
contract when touching either one.

## Reboot

Alarms do not survive reboot. `BootReceiver` (on `BOOT_COMPLETED`) re-registers
every pending scheduled call from Room and re-registers the PhoneAccount.

## Overlapping calls

One live call at a time. If a new trigger arrives while a call is `Ringing`
or `InCall`, the new history row is settled as `MISSED` and the trigger is
dropped: it never replaces the active call's screen, ringtone or
notification. (`CallController.startIncoming`, since 1.3.)

Forensic addendum (2026-10-20, from the owner's device screenshots — our UI
still opened over the system's and the caller showed as "Unknown"):

- `ConnectionRequest` extras: the bundle passed to `addNewIncomingCall`
  arrives nested under `TelecomManager.EXTRA_INCOMING_CALL_EXTRAS`. Reading
  the top level loses caller name, audio URI, and history id. Fixed.
- The connection itself must carry `setAddress(tel:…)` and a display name
  (falling back to the number), `setAudioModeIsVoip(true)`, and the
  initializing → ringing sequence, or the system UI renders "Unknown".
  Fixed.
- An `onShowIncomingCallUi` override — even as a "safety net" — makes some
  OEM Telecom builds hand the UI to the app on top of the system's own
  presentation. Removed entirely; the reference mechanism has none.
