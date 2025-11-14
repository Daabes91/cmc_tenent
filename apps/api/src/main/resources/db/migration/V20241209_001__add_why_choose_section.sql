ALTER TABLE clinic_settings
    ADD COLUMN IF NOT EXISTS why_choose_title_en TEXT,
    ADD COLUMN IF NOT EXISTS why_choose_title_ar TEXT,
    ADD COLUMN IF NOT EXISTS why_choose_subtitle_en TEXT,
    ADD COLUMN IF NOT EXISTS why_choose_subtitle_ar TEXT,
    ADD COLUMN IF NOT EXISTS why_choose_features JSONB;

UPDATE clinic_settings
SET
    why_choose_title_en = COALESCE(why_choose_title_en, 'Why Choose Qadri''s Clinic?'),
    why_choose_title_ar = COALESCE(why_choose_title_ar, 'لماذا تختار عيادة قدري؟'),
    why_choose_subtitle_en = COALESCE(why_choose_subtitle_en, 'We combine cutting-edge technology with compassionate care to deliver exceptional dental experiences.'),
    why_choose_subtitle_ar = COALESCE(why_choose_subtitle_ar, 'نجمع بين أحدث التقنيات والرعاية الإنسانية لنقدم تجربة أسنان استثنائية.'),
    why_choose_features = COALESCE(
        why_choose_features,
        '[
            {
                "key": "experts",
                "icon": "shield-check",
                "titleEn": "Expert Professionals",
                "titleAr": "أطباء محترفون",
                "descriptionEn": "Highly qualified dentists with years of specialized experience",
                "descriptionAr": "أطباء أسنان مؤهلون يتمتعون بسنوات من الخبرة المتخصصة"
            },
            {
                "key": "technology",
                "icon": "beaker",
                "titleEn": "Advanced Technology",
                "titleAr": "تقنيات متقدمة",
                "descriptionEn": "State-of-the-art equipment for precise diagnosis and treatment",
                "descriptionAr": "أحدث الأجهزة للحصول على تشخيص وعلاج دقيق"
            },
            {
                "key": "comfort",
                "icon": "smile",
                "titleEn": "Patient Comfort",
                "titleAr": "راحة المرضى",
                "descriptionEn": "Relaxing environment designed to ease dental anxiety",
                "descriptionAr": "بيئة مريحة تساعد على تقليل القلق من علاج الأسنان"
            },
            {
                "key": "affordable",
                "icon": "wallet",
                "titleEn": "Affordable Care",
                "titleAr": "رعاية ميسورة",
                "descriptionEn": "Flexible payment plans and insurance options available",
                "descriptionAr": "خيارات دفع مرنة وتغطية تأمينية متاحة"
            }
        ]'::jsonb
    )
WHERE why_choose_title_en IS NULL OR why_choose_features IS NULL;
