<div align="center">

<img src="docs/icon.svg" alt="أيقونة مكالمة وهمية" width="140" height="140">

# مكالمة وهمية (Fake Call)

**حاكِ مكالمة واردة واقعية — في الوقت الذي تختاره، بالاسم والرقم والصوت الذي تحدده.**

[![Release](https://img.shields.io/github/v/release/mgnegypt/fake-call?style=for-the-badge&color=8B1A3F)](https://github.com/mgnegypt/fake-call/releases)
[![Stars](https://img.shields.io/github/stars/mgnegypt/fake-call?style=for-the-badge&logo=github&color=8B1A3F)](https://github.com/mgnegypt/fake-call/stargazers)
[![License: MIT](https://img.shields.io/badge/License-MIT-8B1A3F?style=for-the-badge)](LICENSE)
![Android](https://img.shields.io/badge/Android-11%20→%2017-8B1A3F?style=for-the-badge&logo=android&logoColor=white)

[⬇️ **تحميل**](https://github.com/mgnegypt/fake-call/releases) &nbsp;·&nbsp; [🐞 أبلغ عن مشكلة](https://github.com/mgnegypt/fake-call/issues) &nbsp;·&nbsp; [🇬🇧 **English**](README.md)

</div>

<br>

<div align="center">

| 🔒 **صفر بيانات** | 🚫 **بدون إعلانات** | 🌍 **عربي + إنجليزي** | 🎨 **Material 3** |
|:---:|:---:|:---:|:---:|
| بدون تتبع أو تحليلات | بدون متتبعات نهائيًا | اتجاهان كاملان RTL / LTR | وضع فاتح وداكن |

</div>

<div dir="rtl">

## 📖 نظرة عامة

**مكالمة وهمية** تطبيق أندرويد مفتوح المصدر يحاكي مكالمة هاتفية واردة في الوقت الذي تختاره، باسم ورقم متصل تحددهما، مع صوت اختياري يُشغَّل عند الرد. مفيد للخصوصية والترفيه والعروض التوضيحية والاختبار.

## 📸 لقطات الشاشة

</div>

<div align="center">

| الرئيسية | المكالمة الواردة | الإعدادات | حول التطبيق |
|:---:|:---:|:---:|:---:|
| <img src="docs/screenshots/home.jpg" width="200" alt="الشاشة الرئيسية"> | <img src="docs/screenshots/incoming-call.jpg" width="200" alt="شاشة المكالمة الواردة"> | <img src="docs/screenshots/settings.jpg" width="200" alt="شاشة الإعدادات"> | <img src="docs/screenshots/about.jpg" width="200" alt="شاشة حول التطبيق"> |

</div>

<div dir="rtl">

## ✨ المميزات

- 📞 **تجربة مكالمة حقيقية** — ترن في تطبيق الهاتف الخاص بالنظام عبر إطار عمل Telecom في أندرويد
- 🛟 **بديل ذكي** — يستخدم شاشة المكالمة الخاصة به تلقائيًا لو حساب النظام غير مفعّل ([التفاصيل](docs/CALL_ENGINE.md))
- ⏱️ **جدولة مرنة** — رنّ الآن، أو بعد مهلة، أو في وقت محدد بدقة
- 🔁 **تستمر بعد إعادة التشغيل** — المكالمات المعلّقة تُعاد جدولتها تلقائيًا
- 🎙️ **صوت عند الرد** — شغّل أي ملف صوتي لحظة الرد
- 👤 **اختيار من جهات الاتصال** — اختر المتصل من جهات اتصالك
- 🕘 **سجل المكالمات** — يُسجَّل تم الرد / رفض / اكتملت
- ⚡ **إعدادات سريعة** — حتى 5 إعدادات، متاحة أيضًا كاختصارات وبلاطات في الإعدادات السريعة
- 🤖 **واجهة أتمتة** — شغّل المكالمات من Tasker أو MacroDroid أو ADB
- 🔄 **تحديثات من داخل التطبيق** — ملاحظات إصدار بلغتين، مع التحقق من SHA-256، ولا يثبّت إلا عند الضغط
- 🌗 **تصميم عصري** — ثيم عنابي Material 3، وضع فاتح وداكن، ومقدمة متحركة سلسة

## 📲 التثبيت

</div>

<div align="center">

| الخطوة | ما تفعله |
|:---:|:---:|
| 1️⃣ | نزّل ملف `fake-call-vX.Y.apk` من [**صفحة الإصدارات**](https://github.com/mgnegypt/fake-call/releases) |
| 2️⃣ | افتح الملف واسمح بالتثبيت من المتصفح أو مدير الملفات إذا طلب أندرويد |
| 3️⃣ | افتح **مكالمة وهمية** وحدد المتصل والتوقيت والصوت الاختياري |
| 4️⃣ | اختياري: فعّل حساب **مكالمة وهمية** لترن المكالمات في تطبيق الهاتف |

</div>

<div dir="rtl">

> [!TIP]
> لترن المكالمة في تطبيق الهاتف الحقيقي، فعّل خيار **مكالمة وهمية** من *الإعدادات ← التطبيقات ← التطبيقات الافتراضية ← تطبيق الهاتف / حسابات الاتصال*. وإن لم تفعّله يستخدم التطبيق شاشة المكالمة الخاصة به بملء الشاشة.

## 🛠️ التقنيات المستخدمة

</div>

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Material 3](https://img.shields.io/badge/Material%203-8B1A3F?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org)
[![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/features/actions)

</div>

<div dir="rtl">

## 🔐 شرح الأذونات

لكل إذن وظيفة واحدة واضحة. لا شيء يُجمع، ولا شيء يغادر جهازك.

| | الإذن | لماذا يحتاجه التطبيق |
|:---:|---|---|
| 📞 | **حالة الهاتف** <br><sub>`READ_PHONE_STATE`</sub> | تسجيل حساب Telecom والاستعلام عنه |
| 👤 | **جهات الاتصال** <br><sub>`READ_CONTACTS`</sub> | اختيار المتصل من جهات الاتصال. يُستخدم فقط مع المنتقي ولا يُرفع أي شيء |
| ⏰ | **المنبّهات الدقيقة** | الرنين في الوقت المجدول بالضبط |
| 🔔 | **الإشعارات** <br><sub>+ ملء الشاشة</sub> | عرض المكالمة الواردة فوق شاشة القفل (الواجهة البديلة) |
| 🔊 | **إعدادات الصوت** <br><sub>`MODIFY_AUDIO_SETTINGS`</sub> | تشغيل صوتك عبر مسار المكالمات عند الرد |
| 📦 | **تثبيت الحزم** | تثبيت التحديثات التي تؤكدها بنفسك |
| 🌐 | **الإنترنت** | فحص التحديثات من إصدارات GitHub فقط |
| 🔁 | **اكتمال الإقلاع** | إعادة جدولة المكالمات المعلّقة بعد إعادة التشغيل |
| 🔋 | **استثناء توفير البطارية** | منع توفير البطارية من تأخير المكالمات المجدولة. يُطلب من داخل التطبيق ولا يُفرض أبدًا |

## 🤖 واجهة الأتمتة

شغّل مكالمة من Tasker أو MacroDroid أو ADB عبر بثّ (broadcast).

| | |
|---|---|
| **الإجراء (Action)** | `com.mgneg.fakecall.action.TRIGGER` |
| **المعاملات (Extras)** | ‏`caller_name` (نص)، ‏`caller_number` (نص)، ‏`delay` (رقم صحيح بالثواني) |

أي معامل ناقص يُستبدل بقيمك الافتراضية المحفوظة.

<details>
<summary><b>مثال ADB</b></summary>

<div dir="ltr">

```bash
adb shell am broadcast \
  -a com.mgneg.fakecall.action.TRIGGER \
  --es caller_name "Mom" \
  --es caller_number "+201001234567" \
  --ei delay 10
```

</div>

</details>

<details>
<summary><b>🧱 البناء من المصدر</b></summary>

<br>

**المتطلبات:** JDK 17، وحزمة Android SDK ‏`android-37.0` مع build-tools ‏`37.0.0`، وGradle 9.8.2 (عبر الـ wrapper).

<div dir="ltr">

```bash
./gradlew :app:assembleDebug        # debug build
./gradlew :app:testDebugUnitTest    # unit tests
./gradlew :app:assembleRelease      # release (needs signing env)
```

</div>

يُقرأ توقيع الإصدار من متغيرات البيئة (لا ترفع الأسرار أبدًا): ‏`RELEASE_STORE_FILE` و`RELEASE_STORE_PASSWORD` و`RELEASE_KEY_ALIAS` و`RELEASE_KEY_PASSWORD`. في CI تأتي من GitHub Secrets، راجع [`.github/workflows/release.yml`](.github/workflows/release.yml). إصدارات المكتبات مثبّتة على أحدث النسخ المستقرة وموثّقة في [docs/VERSIONS.md](docs/VERSIONS.md).

</details>

## 🛡️ الخصوصية

التطبيق **لا يجمع أي بيانات**. جهات الاتصال والإعدادات المسبقة والسجل والإعدادات لا تغادر جهازك أبدًا. الطلبات الوحيدة عبر الشبكة هي فحص التحديثات من `api.github.com`، ويمكنك تجاهلها — فالتطبيق يعمل بالكامل بدون إنترنت.

## ⚠️ إخلاء المسؤولية

> [!WARNING]
> التطبيق مخصص للخصوصية والترفيه والعروض التوضيحية والاختبار. لا تستخدمه لخداع أو مضايقة أو إيذاء أي شخص، أو لمخالفة أي قانون. أنت وحدك المسؤول عن طريقة استخدامك له.

## ⭐ ادعم المشروع

لو أفادك التطبيق، لفتة بسيطة منك تفرق كثيرًا:

</div>

<div align="center">

[![Star](https://img.shields.io/badge/⭐%20ضع%20نجمة%20للمشروع-8B1A3F?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mgnegypt/fake-call/stargazers)
[![Fork](https://img.shields.io/badge/🍴%20اعمل%20Fork-8B1A3F?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mgnegypt/fake-call/fork)
[![Issues](https://img.shields.io/badge/💡%20اقترح%20فكرة-8B1A3F?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mgnegypt/fake-call/issues)

</div>

<div dir="rtl">

## 👨‍💻 المطوّر والذكاء الاصطناعي

</div>

<div align="center">

| 👤 المطوّر | 🤖 الذكاء الاصطناعي |
|:---:|:---:|
| <img src="https://github.com/mgnegypt.png?size=120" width="90" alt="mgneg"><br>**محمود نصار (mgneg)**<br>مبرمج ومهتم بالتكنولوجيا<br>[MGN Studio](https://github.com/mgnegypt) | <br>**كتابة وبرمجة مشتركة**<br>ساعدت أدوات الذكاء الاصطناعي في كتابة وتحسين الكود والتوثيق |

</div>

<div dir="rtl">

صُمّم هذا التطبيق وكُتب وبُرمج بواسطة **mgneg** بالتعاون مع **الذكاء الاصطناعي (AI)**. الفكرة والتوجيه وقرارات التصميم والاختبار مني، أما الذكاء الاصطناعي فساعد في كتابة وصقل الكود والتوثيق. النتيجة تمت مراجعتها وبناؤها وصيانتها بواسطتي.

</div>

---

<div align="center">

مستوحى من [Phony](https://github.com/DDOneApps/Phony) من DDOneApps — تنفيذ مستقل مكتوب من الصفر، بدون أي كود أو أصول أو ترجمات من ذلك المشروع.
<br>
[رخصة MIT](LICENSE) © 2026 mgneg · [سجل التغييرات](CHANGELOG.md) · [إشعارات الأطراف الثالثة](THIRD_PARTY_NOTICES.md)

</div>
