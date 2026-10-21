# Changelog

## [Unreleased]

## [1.1] - 2026-10-21

## English
### Changed
- A completely new look: the Burgundy soft-glass design replaces the Volt/Aura themes across the whole app — a pearl-and-burgundy palette for light and dark modes, Cairo typography, soft rounded cards, and a floating bottom navigation bar with the Call now button in the centre
- Theme selection: follow the system, force light, or force dark, from Settings; the choice applies everywhere, including the in-app call screen (Monet dynamic colour remains available and unchanged)
- Scheduling is friendlier: quick delay chips plus a custom-time picker with hour, minute, and second steppers, still driving the same delays as before

### Added
- A dedicated Permissions screen: the warning badge on Home shows how many permissions are missing and opens a screen listing each one, why the app needs it, and its enable action

### Fixed
- Custom delays longer than 99 hours can be entered again, up to the scheduler's limit
- Switches now render and behave correctly in right-to-left layouts, text on cards keeps its contrast with dynamic colour enabled, and the update sheet's GitHub button uses its localized label

## العربية
### تم التغيير
- شكل جديد بالكامل: تصميم «البرجندي» الزجاجي الناعم يحلّ محلّ ثيمَي Volt وAura في التطبيق كله — لوحة ألوان لؤلؤية وبرجندية للوضعين الفاتح والداكن، خط القاهرة، بطاقات مستديرة ناعمة، وشريط تنقّل سفلي عائم في وسطه زر الاتصال الآن
- اختيار المظهر: اتباع النظام أو فرض الفاتح أو فرض الداكن من الإعدادات؛ ويُطبَّق الاختيار في كل مكان بما فيه شاشة المكالمة داخل التطبيق (وألوان Monet الديناميكية ما زالت متاحة كما هي)
- الجدولة صارت أسهل: شرائح تأخير سريعة ومنتقي وقت مخصص بعدّادات للساعات والدقائق والثواني، يقود نفس التأخيرات كما من قبل

### تمت الإضافة
- شاشة أذونات مخصصة: شارة التحذير في الرئيسية تعرض عدد الأذونات الناقصة وتفتح شاشة تشرح كل إذن وسبب حاجة التطبيق إليه وزر تفعيله

### تم الإصلاح
- التأخيرات المخصصة الأطول من 99 ساعة عادت قابلة للإدخال حتى حدّ المجدوِل
- المفاتيح تظهر وتتصرّف صحيحًا في التخطيطات من اليمين لليسار، وتباين النص على البطاقات ثابت مع تفعيل الألوان الديناميكية، وزر GitHub في ورقة التحديث يستخدم نصّه المترجم

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
