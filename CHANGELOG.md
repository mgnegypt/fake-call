# Changelog

## [Unreleased]

## [1.0] - 2026-10-20

## English
### Added
- Simulated incoming calls through the Android Telecom framework: calls ring in the system phone app, with an optional in-app call screen mode you choose in Settings
- Scheduling with exact alarms, delays, and reboot persistence
- Optional audio file played when the call is answered
- Call history and up to 5 quick presets with launcher shortcuts and Quick Settings tiles
- Automation API for Tasker, MacroDroid, and ADB
- In-app updates from GitHub Releases with checksum verification
- Arabic and English interfaces with full RTL/LTR support
- Animated splash and intro, dynamic color on Android 12 and above
- A Logs screen in Settings that records the last call attempt's diagnostics and exports them as a text file for support

### Improved
- Dual visual identity: Volt UI in dark mode (bold, high-contrast, lime-accented) and Aura UI in light mode (calm, organic, sage-green), with matching splash screens, launcher artwork, shapes, and system bars
- Dynamic color (Monet) is now an opt-in in Settings; the brand palettes are the default
- Permission setup verified against the reference checklist style: a persistent Permissions section in Settings shows the live state of every permission, and battery-optimization exemption is now checked and offered — a common cause of missed scheduled calls on OEM devices

### Fixed
- Calls now arrive through the system dialer with the correct caller name and number: the Telecom account uses the call-provider mechanism, caller data is delivered in the nested extras the framework expects, and the system mute button silences the answer audio
- If the system call account is not enabled, the app now says so honestly with an enable shortcut instead of silently ringing on its own screen

## العربية
### تمت الإضافة
- مكالمات واردة وهمية عبر إطار عمل Telecom في أندرويد: المكالمات ترنّ في تطبيق هاتف النظام، مع وضع شاشة مكالمة داخل التطبيق تختاره من الإعدادات
- جدولة بمنبهات دقيقة وتأخير واستمرارية بعد إعادة التشغيل
- ملف صوتي اختياري يُشغَّل عند الرد على المكالمة
- سجل مكالمات وحتى 5 إعدادات سريعة مع اختصارات الشاشة وبلاطات الإعدادات السريعة
- واجهة أتمتة لتطبيقات Tasker وMacroDroid وأداة ADB
- تحديثات داخل التطبيق من إصدارات GitHub مع التحقق من البصمة
- واجهة عربية وإنجليزية مع دعم كامل لاتجاهي RTL وLTR
- شاشة بداية ومقدمة متحركة وألوان ديناميكية على أندرويد 12 فأحدث
- شاشة سجلات في الإعدادات تسجّل تشخيص آخر محاولة اتصال وتصدّره كملف نصي للدعم

### تم التحسين
- هوية بصرية مزدوجة: واجهة Volt في الوضع الداكن (جريئة وعالية التباين بلمسات ليمونية) وواجهة Aura في الوضع الفاتح (هادئة وطبيعية بأخضر المريمية)، مع شاشات بداية وأيقونة وأشكال وأشرطة نظام متناسقة
- الألوان الديناميكية (Monet) أصبحت اختيارية من الإعدادات؛ لوحات العلامة هي الافتراضية
- إعدادات الأذونات بمزيد من التحقق: قسم أذونات دائم في الإعدادات يعرض الحالة الحية لكل إذن، وإعفاء تحسين البطارية أصبح يُفحص ويُعرض — وهو سبب شائع لضياع المكالمات المجدولة على أجهزة الشركات

### تم الإصلاح
- المكالمات تصل الآن عبر تطبيق هاتف النظام بالاسم والرقم الصحيحين للمتصل: حساب Telecom يعمل بآلية موفّر المكالمات، وبيانات المتصل تصل بالشكل المتعشّش الذي يتوقعه النظام، وزر كتم الصوت في واجهة النظام يسكّت صوت الرد فعليًا
- إذا لم يكن حساب مكالمات النظام مُفعَّلًا، يخبرك التطبيق بذلك صراحة مع اختصار للتفعيل بدل الرنين بصمت على شاشته هو
