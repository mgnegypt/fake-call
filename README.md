<div align="center">

<img src="docs/icon.svg" alt="Fake Call icon" width="140" height="140">

# Fake Call

**Simulate a realistic incoming phone call — on your schedule, with your caller, and your audio.**

[![Release](https://img.shields.io/github/v/release/mgnegypt/fake-call?style=for-the-badge&color=8B1A3F)](https://github.com/mgnegypt/fake-call/releases)
[![Stars](https://img.shields.io/github/stars/mgnegypt/fake-call?style=for-the-badge&logo=github&color=8B1A3F)](https://github.com/mgnegypt/fake-call/stargazers)
[![License: MIT](https://img.shields.io/badge/License-MIT-8B1A3F?style=for-the-badge)](LICENSE)
![Android](https://img.shields.io/badge/Android-11%20→%2017-8B1A3F?style=for-the-badge&logo=android&logoColor=white)

[⬇️ **Download**](https://github.com/mgnegypt/fake-call/releases) &nbsp;·&nbsp; [🐞 Report a bug](https://github.com/mgnegypt/fake-call/issues) &nbsp;·&nbsp; [🇪🇬 **العربية**](README.ar.md)

</div>

<br>

<div align="center">

| 🔒 **Zero data** | 🚫 **No ads** | 🌍 **AR + EN** | 🎨 **Material 3** |
|:---:|:---:|:---:|:---:|
| No tracking, no analytics | No trackers, ever | Full RTL / LTR | Light & dark themes |

</div>

## 📖 Overview

Fake Call is an open-source Android app that simulates an incoming phone call at a time you choose, with a caller name and number you set, and optional audio that plays when you answer. Use it for privacy, entertainment, presentations, demos, and testing.

## 📸 Screenshots

<div align="center">

| Home | Incoming call | Settings | About |
|:---:|:---:|:---:|:---:|
| <img src="docs/screenshots/home.jpg" width="200" alt="Home screen"> | <img src="docs/screenshots/incoming-call.jpg" width="200" alt="Incoming call screen"> | <img src="docs/screenshots/settings.jpg" width="200" alt="Settings screen"> | <img src="docs/screenshots/about.jpg" width="200" alt="About screen"> |

</div>

## ✨ Features

- 📞 **Real call experience** — rings in the system dialer through the Android Telecom framework
- 🛟 **Smart fallback** — automatically uses its own full-screen call UI if the system account isn't enabled ([details](docs/CALL_ENGINE.md))
- ⏱️ **Flexible scheduling** — ring now, after a delay, or at an exact time
- 🔁 **Survives reboot** — pending calls are re-scheduled automatically
- 🎙️ **Audio on answer** — play any audio file the moment you pick up
- 👤 **Contact picker** — choose a caller from your contacts
- 🕘 **Call history** — answered, declined, and completed calls are logged
- ⚡ **Quick presets** — up to 5, also as launcher shortcuts and Quick Settings tiles
- 🤖 **Automation API** — trigger calls from Tasker, MacroDroid, or ADB
- 🔄 **In-app updates** — bilingual release notes, SHA-256 verified, installs only when you tap
- 🌗 **Modern design** — burgundy Material 3 theme, light/dark modes, smooth intro animation

## 📲 Install

<table>
<tr>
<td width="60" align="center">1️⃣</td>
<td>Download <code>fake-call-vX.Y.apk</code> from the <a href="https://github.com/mgnegypt/fake-call/releases"><b>Releases page</b></a>.</td>
</tr>
<tr>
<td align="center">2️⃣</td>
<td>Open the file and allow installs from your browser or file manager if Android asks.</td>
</tr>
<tr>
<td align="center">3️⃣</td>
<td>Open <b>Fake Call</b> and set your caller, timing, and optional audio.</td>
</tr>
<tr>
<td align="center">4️⃣</td>
<td>Optional: enable the <b>Fake Call</b> account so calls ring in the system dialer.</td>
</tr>
</table>

> [!TIP]
> To ring in the real phone app, enable the **Fake Call** entry under *Settings → Apps → Default apps → Phone app / call accounts*. Skip it and the app uses its own full-screen call screen instead.

## 🛠️ Built with

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Material 3](https://img.shields.io/badge/Material%203-8B1A3F?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org)
[![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)

## 🔐 Permissions explained

Every permission has one clear job. Nothing is collected, nothing leaves your device.

| | Permission | Why the app needs it |
|:---:|---|---|
| 📞 | **Phone state** <br><sub>`READ_PHONE_STATE`</sub> | Register and query the Telecom call account |
| 👤 | **Contacts** <br><sub>`READ_CONTACTS`</sub> | Pick a caller from your contacts. Used only with the contact picker, never uploaded |
| ⏰ | **Exact alarms** | Ring at the exact scheduled time |
| 🔔 | **Notifications** <br><sub>+ full-screen intent</sub> | Show the incoming call over the lock screen (fallback UI) |
| 🔊 | **Audio settings** <br><sub>`MODIFY_AUDIO_SETTINGS`</sub> | Play your audio on the voice route when you answer |
| 📦 | **Install packages** | Install in-app updates you explicitly confirm |
| 🌐 | **Internet** | Update checks against GitHub Releases only |
| 🔁 | **Boot completed** | Re-schedule pending calls after a reboot |
| 🔋 | **Battery optimization exemption** | Stop OEM battery savers from delaying scheduled calls. Requested in the app, never forced |

## 🤖 Automation API

Trigger a call from Tasker, MacroDroid, or ADB with a broadcast.

| | |
|---|---|
| **Action** | `com.mgneg.fakecall.action.TRIGGER` |
| **Extras** | `caller_name` (String), `caller_number` (String), `delay` (Int, seconds) |

Missing extras fall back to your saved defaults.

<details>
<summary><b>ADB example</b></summary>

```bash
adb shell am broadcast \
  -a com.mgneg.fakecall.action.TRIGGER \
  --es caller_name "Mom" \
  --es caller_number "+201001234567" \
  --ei delay 10
```

</details>

<details>
<summary><b>🧱 Build from source</b></summary>

<br>

**Requirements:** JDK 17, Android SDK platform `android-37.0` + build-tools `37.0.0`, and Gradle 9.8.2 (via the wrapper).

```bash
./gradlew :app:assembleDebug        # debug build
./gradlew :app:testDebugUnitTest    # unit tests
./gradlew :app:assembleRelease      # release (needs signing env, below)
```

Release signing is read from environment variables (never commit secrets): `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`. In CI they come from GitHub Secrets; see [`.github/workflows/release.yml`](.github/workflows/release.yml). Dependency versions are pinned to the latest stable releases and recorded in [docs/VERSIONS.md](docs/VERSIONS.md).

</details>

## 🛡️ Privacy

Fake Call collects **no data**. Contacts, presets, history, and settings never leave your device. The only network requests are update checks to `api.github.com`, which you can ignore — the app works fully offline.

## ⚠️ Disclaimer

> [!WARNING]
> Fake Call is meant for privacy, entertainment, presentations, and testing. Do not use it to deceive, harass, or harm anyone, or to break any law. You are solely responsible for how you use it.

## ⭐ Support the project

If Fake Call is useful to you, a small gesture goes a long way:

<div align="center">

[![Star](https://img.shields.io/badge/⭐%20Star%20this%20repo-8B1A3F?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mgnegypt/fake-call/stargazers)
[![Fork](https://img.shields.io/badge/🍴%20Fork%20it-8B1A3F?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mgnegypt/fake-call/fork)
[![Issues](https://img.shields.io/badge/💡%20Suggest%20an%20idea-8B1A3F?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mgnegypt/fake-call/issues)

</div>

## 👨‍💻 Developer & AI

<div align="center">

| 👤 Developer | 🤖 AI |
|:---:|:---:|
| <img src="https://github.com/mgnegypt.png?size=120" width="90" alt="mgneg"><br>**Mahmoud Nassar (mgneg)**<br>Developer and tech enthusiast<br>[MGN Studio](https://github.com/mgnegypt) | <br>**Written and programmed together**<br>AI tools helped write and refine the code and documentation |

</div>

This app was designed, written, and programmed by **mgneg** together with **AI**. The idea, direction, design decisions, and testing are mine; AI helped write and polish the code and the docs. The result is reviewed, built, and maintained by me.

---

<div align="center">

Inspired by [Phony](https://github.com/DDOneApps/Phony) by DDOneApps — an independent implementation written from scratch, with no code, assets, or translations from that project.
<br>
[MIT License](LICENSE) © 2026 mgneg · [Changelog](CHANGELOG.md) · [Third-party notices](THIRD_PARTY_NOTICES.md)

</div>
