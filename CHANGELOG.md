# Changelog

## [Unreleased]

## English
### Changed
- Theme is now a three-way choice (Follow system / Light / Dark) in Settings — before, once you picked light or dark there was no way back to following the system

### Fixed
- The update flow is covered by unit tests for its state machine: happy path, missing/mismatched checksum, double-start, cancel, install failure and unknown-size progress.
- Downloaded update files clean up after themselves: the cached APK is deleted once the install session takes it, and stale downloads or partial files from older versions are cleared on app start.
- The update sheet fits narrow screens and large fonts: the new version stands on its own line (smaller when long), the current version sits beneath it, and the download button takes the full width with GitHub/Dismiss on their own row.
- The "allow installs" hint in the update sheet now disappears as soon as you return from granting the permission, and a download you started before granting continues automatically.
- Install results are no longer silent: the sheet stays open while installing, a failed install (for example a signature mismatch) shows the install-failed message — and a notification if the app is in the background — and cancelling the system prompt returns the sheet to its idle state.
- Update failures now say what actually went wrong (network, not enough storage, checksum unavailable, checksum mismatch, install failed) instead of one generic message; the storage case is detected before downloading, install errors are reported instead of crashing, and cancelling a download is no longer treated as a failure.
- Two quick taps on "Download and install" can no longer start two downloads writing the same file: starting is atomic, and the APK downloads to a temporary ".part" file that is renamed only when complete.
- The update no longer freezes the UI: fetching the checksum, verifying it and writing the install session all run off the main thread, and each phase is labelled in the sheet (fetching / verifying / installing).
- Update progress is smooth and honest: the bar animates between percents, and when the server does not report the APK size the sheet shows an indeterminate bar instead of sitting at 0%.
- The update sheet no longer jumps while updating: it opens fully expanded with a fixed-height status area, cannot be swiped away mid-download (a Cancel button replaces Dismiss while busy), and scrolls on small screens or large fonts.
- The update download/verify/install flow moved out of the update sheet into an app-level controller: closing the sheet no longer cancels a download, and the install-permission/download state survives the sheet being dismissed and reopened.
- Quick Settings preset tiles no longer crash on Android 14+ (the tile now launches through a PendingIntent, as required for apps targeting API 34+) and no longer read the database on the main thread
- The Answer button on the incoming-call notification now opens the call screen already in-call on Android 12+ (it launches the call activity directly instead of going through a broadcast receiver, whose activity start is blocked)
- Unanswered calls now end as Missed after 45 seconds of ringing in both call modes, instead of ringing forever; a call that already rang is never re-fired after a reboot, and long-past scheduled calls settle as Missed instead of ringing hours late
- Scheduled calls now survive an app update: pending alarms are re-registered when the package is replaced, not only at boot
- Scheduled calls can now be cancelled from History, and clearing history cancels their alarms too, so a deleted schedule never rings
- Security: an update whose checksum is missing or cannot be fetched is now refused instead of being installed unverified; a failed verification deletes the download and says why
- A second call triggered while one is already ringing or in progress no longer leaves an orphaned looping ringtone: it settles as Missed and leaves the active call untouched
- The mute button in the app's call screen now actually silences the answer audio (before, it only changed the icon)
- Speakerphone now uses the modern audio-routing API on Android 12+ and is always reset when a call ends, so the route never leaks into the next call
- Answer audio now loads without blocking the call screen, and a broken audio file logs its failure instead of answering with silence
- Picked answer audio now shows the file's real name, and replaced or removed audio copies are cleaned up instead of leaking in private storage (copies a preset or scheduled call still uses are kept)
- Automation triggers are now off by default: the Tasker/MacroDroid/ADB broadcast is ignored until you turn on "Allow automation triggers" in Settings, and the preset trigger screen is no longer visible to other apps
- Android backup is disabled for the app (call history contains phone numbers and is never backed up), and the database no longer wipes itself on schema changes — future versions must migrate it properly
- Lint is clean: the telecom diagnostics probes are explicitly marked best-effort (they run without READ_PHONE_STATE by design), screen snackbar/log strings are resolved in composition, and the Arabic permissions plural gained its missing "zero" form
- Release safety: the release workflow now refuses a tag that does not match the app version, requires every release to raise the version code, and runs the unit tests and lint before building
- Removed the READ_PHONE_STATE permission: registering and using the app's own call account never required it, so it is gone from the manifest and from the permissions checklist
- Internals: history status changes now follow one tested rule set (a settled call can never be rewritten), and docs/TESTING.md records the release verification checklist

## العربية
### تم التغيير
- المظهر أصبح اختيارًا ثلاثيًا (حسب النظام / فاتح / داكن) في الإعدادات — قبل ذلك، متى اخترت الفاتح أو الداكن لم تكن هناك طريقة للعودة إلى إعداد النظام

### تم الإصلاح
- عملية التحديث أصبحت مغطاة باختبارات وحدة لآلة حالاتها: المسار الناجح، البصمة المفقودة/غير المطابقة، البدء المزدوج، الإلغاء، فشل التثبيت، والتقدم عند حجم مجهول.
- ملفات التحديث المنزّلة تنظف وراءها: يُحذف الـAPK من الكاش بمجرد أن تتسلمه جلسة التثبيت، والتنزيلات القديمة أو الملفات الناقصة من إصدارات أقدم تُمسح عند بدء التطبيق.
- شاشة التحديث تناسب الشاشات الضيقة والخطوط الكبيرة: الإصدار الجديد على سطر مستقل (أصغر عندما يطول)، والإصدار الحالي تحته، وزر التنزيل بعرض الشاشة كاملًا مع GitHub و«إغلاق» على سطر خاص بهما.
- تلميح «السماح بالتثبيت» في شاشة التحديث يختفي الآن فور رجوعك من منح الإذن، والتنزيل الذي بدأته قبل المنح يكمل تلقائيًا.
- نتائج التثبيت لم تعد صامتة: تبقى الشاشة مفتوحة أثناء التثبيت، والفشل (كاختلاف التوقيع) يعرض رسالة فشل التثبيت — وإشعارًا لو كان التطبيق في الخلفية — وإلغاء نافذة النظام يعيد الشاشة لحالتها الأولى.
- إخفاقات التحديث أصبحت تقول ما حدث فعلًا (شبكة، مساحة غير كافية، بصمة تحقق غير متاحة، بصمة غير مطابقة، فشل التثبيت) بدل رسالة عامة واحدة؛ وحالة المساحة تُكتشف قبل التنزيل، وأخطاء التثبيت تُبلَّغ بدل أن تسبب انهيارًا، وإلغاء التنزيل لم يعد يُعامل كفشل.
- نقرتان سريعتان على «تنزيل وتثبيت» لم تعودا تستطيعان بدء تنزيلين يكتبان في نفس الملف: البدء ذرّي، والـAPK يُنزَّل إلى ملف مؤقت «.part» يُعاد تسميته فقط عند اكتمال التنزيل.
- التحديث لم يعد يجمّد الواجهة: جلب بصمة التحقق والتحقق منها وكتابة جلسة التثبيت كلها تجري خارج الخيط الرئيسي، وكل مرحلة موسومة في الشاشة (جلب / تحقق / تثبيت).
- تقدم التحديث أصبح سلسًا وصادقًا: الشريط يتحرك بسلاسة بين النسب، وعندما لا يعلن الخادم حجم الـAPK تعرض الشاشة شريطًا غير محدد بدل الوقوف عند 0%.
- شاشة التحديث لم تعد تقفز أثناء التحديث: تُفتح ممتدة بالكامل مع منطقة حالة ثابتة الارتفاع، ولا يمكن إغلاقها بالسحب أثناء التنزيل (يحل زر «إلغاء» محل «إغلاق» أثناء الانشغال)، وتتمرر على الشاشات الصغيرة أو الخطوط الكبيرة.
- انتقلت عملية التحديث (تنزيل وتحقق وتثبيت) من شاشة التحديث إلى متحكم على مستوى التطبيق: إغلاق الشاشة لم يعد يلغي التنزيل، وحالة التحديث تبقى بعد إغلاق الشاشة وإعادة فتحها
- بلاطات الإعدادات السريعة للسيناريوهات لم تعد تنهار على Android 14+ (أصبح التشغيل يتم عبر PendingIntent كما هو مطلوب للتطبيقات المستهدفة API 34+) ولم تعد تقرأ قاعدة البيانات على الخيط الرئيسي
- زر «الرد» في إشعار المكالمة الواردة أصبح يفتح شاشة المكالمة وهي في حالة اتصال فعلي على Android 12+ (يشغّل شاشة المكالمة مباشرة بدل المرور عبر مستقبل بثّ ممنوع من بدء الشاشات)
- المكالمات التي لا يُردّ عليها تنتهي الآن كـ«فائتة» بعد 45 ثانية من الرنين في كلا وضعي الاتصال بدل الرنين بلا نهاية؛ والمكالمة التي رنّت لا تُعاد أبدًا بعد إعادة التشغيل، والمكالمات المجدولة التي فات وقتها منذ زمن تُسوّى كـ«فائتة» بدل أن ترن متأخرة بساعات
- المكالمات المجدولة أصبحت تنجو من تحديث التطبيق: تُعاد تسجيل المنبهات المعلقة عند استبدال الحزمة، وليس عند الإقلاع فقط
- أصبح بالإمكان إلغاء المكالمة المجدولة من شاشة السجل، ومسح السجل يلغي منبهاتها أيضًا فلا ترن مكالمة محذوفة أبدًا
- أمان: التحديث الذي تتعذر بصمته أو يستحيل جلبها أصبح يُرفض الآن بدل تثبيته دون تحقق؛ وفشل التحقق يحذف التنزيل ويوضح السبب
- مكالمة ثانية تُستدعى أثناء رنين أو سير مكالمة أخرى لم تعد تترك نغمة رنين يتيمة تدور بلا نهاية: تُسوّى كـ«فائتة» وتترك المكالمة الجارية دون مساس
- زر كتم الصوت في شاشة مكالمة التطبيق أصبح يُسكت صوت الرد فعليًا (قبل ذلك كان يغيّر الأيقونة فقط)
- مكبر الصوت أصبح يستخدم واجهة توجيه الصوت الحديثة على Android 12+ ويُعاد ضبطه دائمًا عند انتهاء المكالمة فلا يتسرب التوجيه للمكالمة التالية
- صوت الرد أصبح يُحمّل دون تعطيل شاشة المكالمة، وملف الصوت التالف يُسجّل فشله بدل الرد بصمت
- صوت الرد المختار أصبح يعرض اسم الملف الحقيقي، والنسخ المستبدلة أو المحذوفة تُنظّف بدل التسرب في التخزين الخاص (النسخ التي يستخدمها سيناريو أو مكالمة مجدولة تبقى)
- مشغلات الأتمتة أصبحت مغلقة افتراضيًا: بث Tasker وMacroDroid وADB يُتجاهل حتى تفعّل «السماح بمشغلات الأتمتة» في الإعدادات، وشاشة تشغيل السيناريوهات لم تعد ظاهرة للتطبيقات الأخرى
- النسخ الاحتياطي من Android أصبح معطّلًا للتطبيق (سجل المكالمات يحتوي أرقام هواتف ولا يُنسخ أبدًا)، وقاعدة البيانات لم تعد تمسح نفسها عند تغيّر مخططها — الإصدارات القادمة يجب أن ترحّلها ترحيلًا صحيحًا
- أمان الإصدارات: سير عمل الإصدار أصبح يرفض وسمًا لا يطابق إصدار التطبيق، ويشترط رفع رمز الإصدار في كل إصدار، ويشغّل اختبارات الوحدة والفحص قبل البناء
- أُزيل إذن حالة الهاتف (READ_PHONE_STATE): تسجيل حساب اتصال التطبيق واستخدامه لم يتطلباه أبدًا، فحُذف من البيان ومن قائمة الأذونات
- داخليًا: تغييرات حالة السجل أصبحت تتبع مجموعة قواعد واحدة مختبَرة (المكالمة المستقرة لا تُعاد كتابتها أبدًا)، وdocs/TESTING.md يوثّق قائمة تحقق الإصدار

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
