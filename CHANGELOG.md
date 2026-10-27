# Changelog

## [Unreleased]

## English
### Fixed
- Quick Settings preset tiles no longer crash on Android 14+ (the tile now launches through a PendingIntent, as required for apps targeting API 34+) and no longer read the database on the main thread
- The Answer button on the incoming-call notification now opens the call screen already in-call on Android 12+ (it launches the call activity directly instead of going through a broadcast receiver, whose activity start is blocked)
- Unanswered calls now end as Missed after 45 seconds of ringing in both call modes, instead of ringing forever; a call that already rang is never re-fired after a reboot, and long-past scheduled calls settle as Missed instead of ringing hours late

## العربية
### تم الإصلاح
- بلاطات الإعدادات السريعة للسيناريوهات لم تعد تنهار على Android 14+ (أصبح التشغيل يتم عبر PendingIntent كما هو مطلوب للتطبيقات المستهدفة API 34+) ولم تعد تقرأ قاعدة البيانات على الخيط الرئيسي
- زر «الرد» في إشعار المكالمة الواردة أصبح يفتح شاشة المكالمة وهي في حالة اتصال فعلي على Android 12+ (يشغّل شاشة المكالمة مباشرة بدل المرور عبر مستقبل بثّ ممنوع من بدء الشاشات)
- المكالمات التي لا يُردّ عليها تنتهي الآن كـ«فائتة» بعد 45 ثانية من الرنين في كلا وضعي الاتصال بدل الرنين بلا نهاية؛ والمكالمة التي رنّت لا تُعاد أبدًا بعد إعادة التشغيل، والمكالمات المجدولة التي فات وقتها منذ زمن تُسوّى كـ«فائتة» بدل أن ترن متأخرة بساعات

## [1.2] - 2026-10-22

## English
### Added
- Contacts picker on Home: tap the button beside the number field, grant READ_CONTACTS once, and pick from a searchable in-app list of your saved contacts (name + number) to fill the caller fields. The permission is only used for this feature and is not part of the call-readiness set
- The About screen's stat cards (Open source, Arabic & English, Material 3) are now tappable and open a short bullet description of each
- Gentle animations on the About screen: the page fades in, buttons respond when pressed, and the disclaimer expands smoothly

### Changed
- About screen rebuilt and rewritten: official app and developer texts (developer: Mahmoud Nassar), the MGN STUDIO logo replaces the "mg" monogram, and the full new disclaimer
- About texts, the licence popups and release notes are now formatted as readable bullet points and sections instead of solid text; the MIT licence and open-source licences open as structured popups
- The check-for-updates button on About now shows its progress and confirms when you are on the latest version
- New launcher icon with the burgundy gradient artwork
- Bottom navigation bar is icon-only (labels remain for screen readers), so labels no longer wrap or clip on narrow screens
- Call-mode segment now uses short labels: System / App

### Removed
- Monet dynamic colour: removed end to end; the burgundy palette is the single identity in both themes
- Vibration as an app feature (including the VIBRATE permission) — vibration is left to the system, as on any normal call

### Fixed
- Permissions summary hero no longer renders as a tall mostly-empty card; heroes now hug their content
- Home caller card no longer breaks on narrow screens: the contacts button beside the number field keeps the number field's height and stacks its icon over centred text, so its Arabic label no longer wraps word-by-word
- Long release notes in the update sheet could push the download buttons out of view; notes now scroll inside a height-capped card

## العربية
### تمت الإضافة
- منتقي جهات اتصال في الرئيسية: اضغط الزر بجانب حقل الرقم، وامنح إذن READ_CONTACTS مرة واحدة، واختر من قائمة قابلة للبحث داخل التطبيق بجهات اتصالك المحفوظة (الاسم + الرقم) لملء حقول المتصل. الإذن يُستخدم لهذه الميزة فقط وليس جزءًا من مجموعة أذونات جاهزية المكالمات
- بطاقات شاشة «حول» (مفتوح المصدر، عربي وإنجليزي، Material 3) صارت قابلة للضغط وتفتح شرحًا مختصرًا عن كل واحدة في نقاط
- حركات لطيفة في شاشة «حول»: الصفحة تظهر تدريجيًا، والأزرار تستجيب عند الضغط، وإخلاء المسؤولية يتمدد بسلاسة

### تم التغيير
- إعادة بناء وكتابة شاشة «حول»: النصوص الرسمية الجديدة للتطبيق والمطوّر (المطوّر: محمود نصار)، وشعار MGN STUDIO يحل محل دائرة «mg»، ونص إخلاء المسؤولية الكامل الجديد
- نصوص «حول» ونوافذ الرخص وملاحظات الإصدار صارت منسّقة كنقاط وأقسام مقروءة بدل النصوص المتصلة؛ ورخصة MIT وتراخيص المصادر المفتوحة تفتح كنوافذ منظمة
- زر «البحث عن تحديثات» في «حول» صار يعرض تقدّم البحث ويؤكد لك أنك على أحدث إصدار
- أيقونة تشغيل جديدة بتصميم التدرّج البرجندي
- شريط التنقّل السفلي صار بالأيقونات فقط (الأسماء باقية لقارئات الشاشة)، فلا تلتفّ الأسماء أو تُقصّ على الشاشات الضيقة
- شريط وضع المكالمة صار بتسميات قصيرة: النظام / التطبيق

### تمت الإزالة
- ألوان Monet الديناميكية: أُزيلت بالكامل؛ اللوحة البرجندية هي الهوية الوحيدة في الوضعين
- الاهتزاز كميزة داخل التطبيق (بما فيه إذن VIBRATE) — الاهتزاز متروك للنظام كما في أي مكالمة عادية

### تم الإصلاح
- بطاقة ملخص الأذونات لم تعد تظهر كبطاقة طويلة شبه فارغة؛ البطاقات الآن على قد محتواها
- بطاقة المتصل في الرئيسية لم تعد تنكسر على الشاشات الضيقة: زر جهات الاتصال بجانب حقل الرقم صار بنفس ارتفاع الحقل ومحتواه مكدّس (الأيقونة فوق نص متوسّط)، فلا يلتفّ اسمه العربي كلمة كلمة بعد الآن
- ملاحظات الإصدار الطويلة في ورقة التحديث كانت تدفع أزرار التنزيل خارج الشاشة؛ الملاحظات الآن تمرّر داخل بطاقة محددة الارتفاع


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
