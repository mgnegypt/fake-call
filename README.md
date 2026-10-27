# Fake Call

Fake Call simulates an incoming phone call at a time you choose, with a caller
name and number you set, and optional audio when you answer. Use it for
privacy, entertainment, presentations, and testing.

- Android 11 (API 30) up to Android 17 (API 37)
- Arabic and English, fully RTL/LTR
- No ads, no analytics, no trackers, no data collection. The only network use
  is checking GitHub for updates.

## Features

- **Real call experience** — two strict call modes, chosen in Settings.
  *System dialer*: calls are raised through the Android Telecom framework
  and ring in the system dialer (after you enable the Fake Call account
  once in system settings); if the account cannot take the call, nothing
  rings and you get an honest notice — the app never substitutes its own
  screen. *App screen*: the app's own full-screen call UI. See
  [docs/CALL_ENGINE.md](docs/CALL_ENGINE.md) for the decision and its limits.
- **Scheduling** — ring now or after a delay (a countdown), with exact
  alarms; pending calls survive reboot and app updates.
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

Automation triggers are **off by default**. Enable "Allow automation triggers"
in Settings → Automation first — while it is off, trigger broadcasts are
ignored (and logged). Preset launcher shortcuts and Quick Settings tiles are
not affected; they are part of the app itself.

Tasker, MacroDroid, or ADB can then trigger a call with a broadcast:

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
| Contacts (`READ_CONTACTS`) | Only for the contacts picker on Home, requested when you tap it |
| Exact alarms | Ring at the exact scheduled time |
| Notifications (+ full-screen intent) | Show the incoming call over the lock screen (app-screen mode) |
| Install packages | Install in-app updates you explicitly confirm |
| Internet | Update checks against GitHub Releases only |
| Boot completed | Re-schedule pending calls after a reboot |
| Battery optimization exemption | Keep scheduled calls from being delayed or killed by OEM battery saving (requested in the app, never forced) |
| Audio settings | Voice-route audio playback (vibration is left to the system) |

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
Those checks run automatically about once a day (WorkManager) so the app
can offer you new releases; the "update notifications" setting only
controls whether you are notified, not whether the check happens.

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
- وضعا اتصال صارمان: الاتصال عبر نظام الهاتف (إطار عمل Telecom، بعد تفعيل
  حساب «مكالمة وهمية» مرة واحدة من إعدادات النظام) أو شاشة مكالمة داخل
  التطبيق — دون استبدال أحدهما بالآخر تلقائيًا أبدًا.
- جدولة بعدّاد تأخير، صوت عند الرد، سجل مكالمات، وحتى 5 إعدادات سريعة مع اختصارات
  وبلاطات إعدادات سريعة، وواجهة أتمتة لـ Tasker وMacroDroid وADB.
- تحديثات من داخل التطبيق عبر إصدارات GitHub مع التحقق من بصمة SHA-256.
- لا إعلانات ولا تتبع ولا جمع بيانات؛ الشبكة تُستخدم فقط للتحقق من التحديثات.

التثبيت: نزّل ملف `fake-call-vX.Y.apk` من صفحة الإصدارات وافتحه. البناء من
المصدر والتفاصيل الكاملة في الأقسام الإنجليزية أعلاه.
