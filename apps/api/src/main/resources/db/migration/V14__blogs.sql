-- Create blogs table with SEO support
CREATE TABLE blogs (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(250) NOT NULL UNIQUE,
    excerpt TEXT,
    content TEXT NOT NULL,
    featured_image VARCHAR(500),
    author_id BIGINT,
    author_name VARCHAR(100),

    -- SEO fields
    meta_title VARCHAR(70),
    meta_description VARCHAR(160),
    meta_keywords VARCHAR(255),
    og_title VARCHAR(95),
    og_description VARCHAR(200),
    og_image VARCHAR(500),

    -- Status and publishing
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP,
    view_count BIGINT NOT NULL DEFAULT 0,

    -- Locale support
    locale VARCHAR(5) NOT NULL DEFAULT 'en',

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Indexes for performance
    CONSTRAINT blogs_status_check CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

-- Create indexes for better query performance
CREATE INDEX idx_blogs_slug ON blogs(slug);
CREATE INDEX idx_blogs_status ON blogs(status);
CREATE INDEX idx_blogs_locale ON blogs(locale);
CREATE INDEX idx_blogs_status_locale ON blogs(status, locale);
CREATE INDEX idx_blogs_published_at ON blogs(published_at DESC);
CREATE INDEX idx_blogs_status_published_at ON blogs(status, published_at DESC);

-- Insert sample blog posts
INSERT INTO blogs (title, slug, excerpt, content, featured_image, author_id, author_name, meta_title, meta_description, meta_keywords, og_title, og_description, status, published_at, locale)
VALUES
(
    'The Importance of Regular Dental Checkups',
    'importance-of-regular-dental-checkups',
    'Regular dental checkups are essential for maintaining optimal oral health. Learn why you should visit your dentist every six months.',
    '<h2>Why Regular Dental Checkups Matter</h2><p>Regular dental checkups are crucial for maintaining good oral health and preventing serious dental problems. During these visits, your dentist can detect issues early, such as cavities, gum disease, and even oral cancer.</p><h3>Benefits of Regular Checkups</h3><ul><li>Early detection of dental problems</li><li>Professional teeth cleaning</li><li>Oral cancer screening</li><li>Prevention of tooth decay and gum disease</li><li>Advice on proper oral hygiene</li></ul><p>We recommend visiting your dentist at least twice a year for optimal oral health.</p>',
    '/images/blog/dental-checkup.jpg',
    1,
    'Clinic Admin',
    'Importance of Regular Dental Checkups | Cliniqax Clinic',
    'Discover why regular dental checkups are essential for your oral health. Learn about the benefits and what to expect during your visit.',
    'dental checkup, oral health, dentist, teeth cleaning, preventive care',
    'The Importance of Regular Dental Checkups',
    'Regular dental checkups are essential for maintaining optimal oral health. Learn why you should visit your dentist every six months.',
    'PUBLISHED',
    CURRENT_TIMESTAMP,
    'en'
),
(
    'أهمية الفحوصات الدورية للأسنان',
    'importance-of-regular-dental-checkups-ar',
    'الفحوصات الدورية للأسنان ضرورية للحفاظ على صحة الفم المثلى. تعرف على سبب ضرورة زيارة طبيب الأسنان كل ستة أشهر.',
    '<h2>لماذا تهم الفحوصات الدورية للأسنان</h2><p>الفحوصات الدورية للأسنان ضرورية للحفاظ على صحة الفم الجيدة ومنع مشاكل الأسنان الخطيرة. خلال هذه الزيارات، يمكن لطبيب الأسنان اكتشاف المشاكل مبكراً، مثل التسوس وأمراض اللثة وحتى سرطان الفم.</p><h3>فوائد الفحوصات الدورية</h3><ul><li>الكشف المبكر عن مشاكل الأسنان</li><li>تنظيف الأسنان المهني</li><li>فحص سرطان الفم</li><li>الوقاية من تسوس الأسنان وأمراض اللثة</li><li>نصائح حول نظافة الفم الصحيحة</li></ul><p>نوصي بزيارة طبيب الأسنان مرتين على الأقل في السنة للحصول على صحة الفم المثلى.</p>',
    '/images/blog/dental-checkup.jpg',
    1,
    'Clinic Admin',
    'أهمية الفحوصات الدورية للأسنان | عيادة القادري',
    'اكتشف لماذا الفحوصات الدورية للأسنان ضرورية لصحة فمك. تعرف على الفوائد وما يمكن توقعه خلال زيارتك.',
    'فحص الأسنان, صحة الفم, طبيب الأسنان, تنظيف الأسنان, الرعاية الوقائية',
    'أهمية الفحوصات الدورية للأسنان',
    'الفحوصات الدورية للأسنان ضرورية للحفاظ على صحة الفم المثلى.',
    'PUBLISHED',
    CURRENT_TIMESTAMP,
    'ar'
),
(
    'Common Dental Problems and How to Prevent Them',
    'common-dental-problems-prevention',
    'Learn about the most common dental problems people face and practical tips to prevent them.',
    '<h2>Common Dental Problems</h2><p>Understanding common dental problems can help you take preventive measures to maintain your oral health.</p><h3>1. Tooth Decay</h3><p>Tooth decay is one of the most common dental problems. It occurs when bacteria in your mouth produce acids that erode tooth enamel.</p><h3>2. Gum Disease</h3><p>Gum disease ranges from simple gum inflammation to serious disease that can damage the soft tissue and bone supporting your teeth.</p><h3>3. Tooth Sensitivity</h3><p>Tooth sensitivity affects many people and can be caused by worn enamel, exposed roots, or cavities.</p><h3>Prevention Tips</h3><ul><li>Brush twice daily with fluoride toothpaste</li><li>Floss daily</li><li>Limit sugary foods and drinks</li><li>Visit your dentist regularly</li><li>Use mouthwash</li></ul>',
    '/images/blog/dental-problems.jpg',
    1,
    'Clinic Admin',
    'Common Dental Problems & Prevention Tips | Cliniqax Clinic',
    'Learn about common dental problems like cavities, gum disease, and sensitivity. Get expert tips on prevention and treatment.',
    'dental problems, tooth decay, gum disease, tooth sensitivity, oral health',
    'Common Dental Problems and How to Prevent Them',
    'Learn about the most common dental problems and get expert prevention tips.',
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '7 days',
    'en'
),
(
    'Teeth Whitening: What You Need to Know',
    'teeth-whitening-guide',
    'Considering teeth whitening? Learn about the different methods, benefits, and what to expect from professional teeth whitening treatments.',
    '<h2>Teeth Whitening Guide</h2><p>A bright, white smile can boost your confidence and make a great first impression. Here''s everything you need to know about teeth whitening.</p><h3>Professional vs. At-Home Whitening</h3><p>Professional teeth whitening performed by a dentist offers faster and more dramatic results compared to at-home treatments.</p><h3>Benefits of Professional Whitening</h3><ul><li>Faster results</li><li>More effective</li><li>Safer for your teeth</li><li>Customized treatment</li><li>Long-lasting results</li></ul><h3>What to Expect</h3><p>During a professional whitening session, your dentist will apply a whitening gel to your teeth and use a special light to activate it. The procedure typically takes 60-90 minutes.</p>',
    '/images/blog/teeth-whitening.jpg',
    1,
    'Clinic Admin',
    'Teeth Whitening Guide - Professional Treatment | Cliniqax Clinic',
    'Everything you need to know about professional teeth whitening. Learn about methods, benefits, and what to expect during treatment.',
    'teeth whitening, dental bleaching, cosmetic dentistry, white teeth, smile makeover',
    'Teeth Whitening: What You Need to Know',
    'Comprehensive guide to professional teeth whitening treatments and their benefits.',
    'DRAFT',
    NULL,
    'en'
);
