# Fake Call

Fake Call simulates an incoming phone call at a time you choose, with a caller
name and number you set, and optional audio when you answer. Use it for
privacy, entertainment, presentations, and testing.

- Android 11 (API 30) up to Android 17 (API 37)
- Arabic and English, fully RTL/LTR
- No ads, no analytics, no trackers, no data collection. The only network use
  is checking GitHub for updates.

## Features

- **Real call experience** — calls are raised through the Android Telecom
  framework and ring in the system dialer (after you enable the Fake Call
  account once in system settings). If the account is not enabled, the app
  automatically falls back to its own full-screen call UI; see
  [docs/CALL_ENGINE.md](docs/CALL_ENGINE.md) for the decision and its limits.
- **Scheduling** — ring now, after a delay, or at an exact time, with exact
  alarms; pending calls survive reboot.
- **Audio on answer** — pick any audio file; it plays on the voice route when
  the call is answered.
- **History** — every simulated call is logged (answered / declined / completed).
- **Presets** — up to 5 quick presets, also exposed as launcher shortcuts and
  Quick Settings tiles.
- **In-app updates** — the app checks GitHub Releases, shows bilingual release
  notes, downloads the new APK, verifies its SHA-256, and installs it (only
  when you tap). You can always fall back to the GitHub page.
- **Polished startup** — system splash screen plus a short animated intro that
  respects the system animation settings.

## Install

1. Download `fake-call-vX.Y.apk` from the
   [Releases page](https://github.com/mgnegypt/fake-call/releases).
2. Open it and allow installs from your browser/file manager if Android asks.
3. Open Fake Call, and (optionally) enable the **Fake Call** entry under
   system Settings → Apps → Default apps → Phone app / call accounts so calls
   ring in the system dialer.

## Automation API

Tasker, MacroDroid, or ADB can trigger a call with a broadcast:

- Action: `com.mgneg.fakecall.action.TRIGGER`
- Extras: `caller_name` (String), `caller_number` (String), `delay` (Int, seconds)
- Missing extras fall back to your saved defaults.

ADB example:

```bash
adb shell am broadcast \
  -a com.mgneg.fakecall.action.TRIGGER \
  --es caller_name "Mom" \
  --es caller_number "+201001234567" \
  --ei delay 10
```

## Permissions explained

| Permission | Why |
|---|---|
| Phone state (`READ_PHONE_STATE`) | Register and query the Telecom call account |
| Exact alarms | Ring at the exact scheduled time |
| Notifications (+ full-screen intent) | Show the incoming call over the lock screen (fallback UI) |
| Install packages | Install in-app updates you explicitly confirm |
| Internet | Update checks against GitHub Releases only |
| Boot completed | Re-schedule pending calls after a reboot |
| Battery optimization exemption | Keep scheduled calls from being delayed or killed by OEM battery saving (requested in the app, never forced) |
| Vibrate / audio settings | Ring vibration and voice-route audio playback |

## Building from source

Requirements: JDK 17, Android SDK platform `android-37.0` + build-tools
`37.0.0`, Gradle 9.8.2 (via the wrapper).

```bash
./gradlew :app:assembleDebug        # debug build
./gradlew :app:testDebugUnitTest    # unit tests
./gradlew :app:assembleRelease      # release (needs signing env, below)
```

Release signing is read from environment variables (never commit secrets):
`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
`RELEASE_KEY_PASSWORD`. In CI they come from GitHub Secrets; see
`.github/workflows/release.yml`. Dependency versions are pinned to the latest
stable releases and recorded in [docs/VERSIONS.md](docs/VERSIONS.md).

## Privacy

Fake Call collects no data. Contacts, presets, history, and settings never
leave the device. The only network requests are update checks to
`api.github.com` (which you can ignore — the app works fully offline).

## Inspiration

Fake Call is inspired by [Phony](https://github.com/DDOneApps/Phony) by
DDOneApps. This is an independent implementation written from scratch; no
source code, assets, or translations from that project are used.

## License

MIT — see [LICENSE](LICENSE). Copyright (c) 2026 mgneg.
Third-party notices: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

---

## بالعربية

**مكالمة وهمية** يحاكي مكالمة هاتفية واردة في وقت تختاره، باسم ورقم متصل
تحددهما، مع صوت اختياري عند الرد. مفيد للخصوصية والترفيه والعروض التوضيحية
والاختبار.

- يعمل على أندرويد 11 حتى أندرويد 17، بواجهة عربية وإنجليزية كاملة الاتجاهين.
- المكالمات ترن عبر إطار عمل Telecom في النظام (بعد تفعيل حساب «مكالمة وهمية»
  مرة واحدة من إعدادات النظام)، مع شاشة مكالمة داخل التطبيق كبديل تلقائي.
- جدولة دقيقة، صوت عند الرد، سجل مكالمات، وحتى 5 إعدادات سريعة مع اختصارات
  وبلاطات إعدادات سريعة، وواجهة أتمتة لـ Tasker وMacroDroid وADB.
- تحديثات من داخل التطبيق عبر إصدارات GitHub مع التحقق من بصمة SHA-256.
- لا إعلانات ولا تتبع ولا جمع بيانات؛ الشبكة تُستخدم فقط للتحقق من التحديثات.

التثبيت: نزّل ملف `fake-call-vX.Y.apk` من صفحة الإصدارات وافتحه. البناء من
المصدر والتفاصيل الكاملة في الأقسام الإنجليزية أعلاه.
