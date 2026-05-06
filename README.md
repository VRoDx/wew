# Aliucord Plugins

مجموعة بلاقنز لـ Aliucord مكتوبة بـ Kotlin.

## البلاقنز المتاحة

| البلاقن | الوصف |
|---------|--------|
| **TypingUsername** | يصلح مؤشر الكتابة في الخاص ليظهر @username بدل الاسم المعروض |
| **GradientRoles** | يعمل جراديانت للألوان على أسماء اليوزرات بالشات حسب رول الألوان |
| **DMCustomStatus** | يعرض الكاستوم ستاتس للشخص التاني في هيدر الخاص |
| **ProfileEffectLoader** | يضيف أنيميشن تحميل لما بروفايل ايفكت أو ديكوريشن بيتحمل |
| **SwipeActions** | سحب يمين لشمال على رسالة: رد على غيرك (لو الخاص مفتوح) أو عدّل/رد على رسالتك |
| **StickerSuggest** | اقتراحات ستيكرز لما تكتب :: متبوعة بنص |
| **LongPressFavorite** | اضغط طويل على ستيكر في البانل عشان تضيفه للمفضلة |

---

## طريقة البناء والتثبيت

### الطريقة 1 — GitHub Actions (الأسهل، مش محتاج تنصّب حاجة)

1. **Fork المشروع ده** على GitHub
2. روح على **Settings → Actions → General** وتأكد إن Actions مفعّلة
3. اعمل **branch جديد** (مثلاً `dev`) وادفع فيه كود
4. GitHub Actions هيبني البلاقنز تلقائياً
5. روح على **Actions → Build → Artifacts** وحمّل ملفات `.zip`
6. افك الضغط، خد ملف `.zip` لكل بلاجن وانقله لموبايلك في:
   ```
   /storage/emulated/0/Aliucord/plugins/
   ```
7. افتح Aliucord وفعّل البلاجن من قائمة الإعدادات

### الطريقة 2 — البناء المحلي (محتاج Android Studio)

```bash
git clone https://github.com/YOUR_USERNAME/aliucord-plugins
cd aliucord-plugins
./gradlew make
```

الملفات المبنية بتظهر في:
```
plugins/PluginName/build/outputs/PluginName.zip
```

---

## ملاحظات مهمة

- البلاقنز متوافقة مع Aliucord 2.7.0+
- بعض الوظائف (زي SwipeActions) ممكن تحتاج تعديل بسيط لو Discord غيّر اسم الكلاس الداخلي
- لو لقيت مشكلة في بلاجن معين، افتح Issue وقولي رقم إصدار Aliucord وDiscord

---

## هيكل المشروع

```
aliucord-plugins/
├── .github/workflows/        ← GitHub Actions للبناء التلقائي
├── gradle/                   ← Gradle Wrapper
├── plugins/
│   ├── TypingUsername/       ← بلاجن إصلاح مؤشر الكتابة
│   ├── GradientRoles/        ← بلاجن جراديانت الرولز
│   ├── DMCustomStatus/       ← بلاجن الكاستوم ستاتس
│   ├── ProfileEffectLoader/  ← بلاجن لودر البروفايل ايفكت
│   ├── SwipeActions/         ← بلاجن السحب للرد/التعديل
│   ├── StickerSuggest/       ← بلاجن اقتراح الستيكرز
│   └── LongPressFavorite/    ← بلاجن المفضلة بالضغط الطويل
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```
