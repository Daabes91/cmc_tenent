# Integrations Section - Manual Test Guide

## Test Environment Setup
1. Navigate to `apps/saas-landing`
2. Run `npm run dev`
3. Open `http://localhost:3003` in browser
4. Scroll to integrations section (after pricing)

## Visual Tests

### Test 1: Section Display
**Expected:**
- Section appears after pricing section
- Title: "Integrates with your healthcare workflow"
- Subtitle: "Connect with the tools you already use to streamline your clinic operations"
- 6 integration cards displayed in grid

**Steps:**
1. Scroll to integrations section
2. Verify title and subtitle text
3. Count integration cards (should be 6)

**Result:** ☐ Pass ☐ Fail

---

### Test 2: Integration Cards Content
**Expected:** Each card should display:
- Integration logo (visible icon)
- Integration name
- Description mentioning clinic operations

**Steps:**
1. Check each integration card:
   - ☐ Stripe: "Accept patient payments securely with automated billing for **clinic services**"
   - ☐ PayPal: "Process **clinic payments** and manage subscription billing for **treatment plans**"
   - ☐ Google Calendar: "Sync appointment schedules with provider calendars for seamless **clinic coordination**"
   - ☐ Mailchimp: "Send appointment reminders and health tips to **patients** automatically"
   - ☐ QuickBooks: "Sync **clinic revenue** and expenses for accurate **healthcare practice accounting**"
   - ☐ Twilio: "Send SMS appointment reminders and notifications to **patients**"

**Result:** ☐ Pass ☐ Fail

---

### Test 3: Responsive Design

#### Mobile (375px)
**Expected:**
- 2 columns grid
- Cards stack properly
- Text remains readable
- No horizontal scroll

**Steps:**
1. Resize browser to 375px width
2. Verify 2-column layout
3. Check text readability
4. Scroll horizontally (should not be possible)

**Result:** ☐ Pass ☐ Fail

#### Tablet (768px)
**Expected:**
- 3 columns grid
- Proper spacing between cards
- Balanced layout

**Steps:**
1. Resize browser to 768px width
2. Verify 3-column layout
3. Check spacing consistency

**Result:** ☐ Pass ☐ Fail

#### Desktop (1024px+)
**Expected:**
- 5 columns grid
- All cards visible without scrolling
- Proper alignment

**Steps:**
1. Resize browser to 1024px+ width
2. Verify 5-column layout
3. Check all cards fit in viewport

**Result:** ☐ Pass ☐ Fail

---

### Test 4: Hover Effects
**Expected:**
- Card border changes to primary color on hover
- Background changes to slate-50
- Gradient glow appears behind logo
- Smooth transition animation

**Steps:**
1. Hover over each integration card
2. Verify border color change
3. Verify background color change
4. Verify logo glow effect
5. Check animation smoothness

**Result:** ☐ Pass ☐ Fail

---

### Test 5: Dark Mode
**Expected:**
- Section background: gray-950
- Card background: gray-900
- Card border: gray-800
- Text color: white/gray-400
- Hover effects work in dark mode

**Steps:**
1. Toggle dark mode
2. Verify section background color
3. Verify card styling
4. Test hover effects
5. Check text contrast/readability

**Result:** ☐ Pass ☐ Fail

---

### Test 6: CTA Button
**Expected:**
- Button text: "View All Integrations"
- Button has outline style with primary border
- Arrow icon appears on right
- Hover effect: background changes to primary/10
- Focus ring appears on keyboard focus

**Steps:**
1. Locate CTA button below integration cards
2. Verify button text
3. Hover over button
4. Tab to button (keyboard navigation)
5. Verify focus ring appears

**Result:** ☐ Pass ☐ Fail

---

### Test 7: CTA Navigation
**Expected:**
- Clicking CTA opens API documentation in new tab
- URL matches `API_DOCS_URL` environment variable
- Default: `http://localhost:8080/swagger-ui/index.html`

**Steps:**
1. Click "View All Integrations" button
2. Verify new tab opens
3. Verify correct URL
4. Close new tab

**Result:** ☐ Pass ☐ Fail

---

### Test 8: Bilingual Support (Arabic)
**Expected:**
- Title: "يتكامل مع سير عمل الرعاية الصحية الخاص بك"
- Subtitle: "اتصل بالأدوات التي تستخدمها بالفعل لتبسيط عمليات عيادتك"
- CTA: "عرض جميع التكاملات"
- Integration descriptions in Arabic
- RTL layout applied

**Steps:**
1. Switch language to Arabic (use language toggle)
2. Scroll to integrations section
3. Verify Arabic title and subtitle
4. Verify Arabic descriptions
5. Verify RTL text direction
6. Verify CTA button text in Arabic

**Result:** ☐ Pass ☐ Fail

---

### Test 9: Animation Effects
**Expected:**
- Cards fade in with stagger effect when scrolling into view
- Scale animation from 0.8 to 1.0
- Smooth 0.4s transition
- Animation triggers once (not on every scroll)

**Steps:**
1. Reload page
2. Scroll slowly to integrations section
3. Observe cards appearing with stagger effect
4. Scroll up and down (animation should not repeat)

**Result:** ☐ Pass ☐ Fail

---

### Test 10: Logo Display
**Expected:**
- All 6 logos render correctly
- Logos are visible (not broken images)
- Logos have proper contrast
- Logos invert colors in dark mode

**Steps:**
1. Check each logo renders:
   - ☐ Stripe logo
   - ☐ PayPal logo
   - ☐ Google Calendar logo
   - ☐ Mailchimp logo
   - ☐ QuickBooks logo
   - ☐ Twilio logo
2. Verify no broken image icons
3. Toggle dark mode and verify logo visibility

**Result:** ☐ Pass ☐ Fail

---

### Test 11: Accessibility

#### Keyboard Navigation
**Expected:**
- Can tab to CTA button
- Focus indicator visible
- Enter key activates CTA

**Steps:**
1. Use Tab key to navigate to integrations section
2. Tab to CTA button
3. Verify focus ring appears
4. Press Enter to activate

**Result:** ☐ Pass ☐ Fail

#### Screen Reader
**Expected:**
- Section has proper heading hierarchy
- Integration names are announced
- CTA button has descriptive text
- ARIA labels present where needed

**Steps:**
1. Enable screen reader (VoiceOver/NVDA)
2. Navigate to integrations section
3. Listen to announcements
4. Verify all content is accessible

**Result:** ☐ Pass ☐ Fail

---

### Test 12: Analytics Tracking
**Expected:**
- CTA click sends analytics event
- Event includes: button text, section name, destination URL

**Steps:**
1. Open browser DevTools → Network tab
2. Filter for analytics requests
3. Click "View All Integrations" button
4. Verify analytics event sent with correct data

**Result:** ☐ Pass ☐ Fail

---

## Browser Compatibility Tests

### Chrome
- ☐ Layout correct
- ☐ Animations smooth
- ☐ Hover effects work
- ☐ CTA navigation works

### Firefox
- ☐ Layout correct
- ☐ Animations smooth
- ☐ Hover effects work
- ☐ CTA navigation works

### Safari
- ☐ Layout correct
- ☐ Animations smooth
- ☐ Hover effects work
- ☐ CTA navigation works

### Edge
- ☐ Layout correct
- ☐ Animations smooth
- ☐ Hover effects work
- ☐ CTA navigation works

---

## Performance Tests

### Load Time
**Expected:** Section loads within 2 seconds

**Steps:**
1. Open DevTools → Performance tab
2. Reload page
3. Measure time to integrations section render

**Result:** ☐ Pass ☐ Fail

### Image Optimization
**Expected:** SVG logos load instantly (< 100ms)

**Steps:**
1. Open DevTools → Network tab
2. Filter for images
3. Check SVG load times

**Result:** ☐ Pass ☐ Fail

---

## Issue Reporting Template

```
**Test:** [Test Name]
**Browser:** [Browser Name & Version]
**Device:** [Desktop/Mobile/Tablet]
**Issue:** [Description of issue]
**Expected:** [What should happen]
**Actual:** [What actually happened]
**Screenshot:** [If applicable]
**Steps to Reproduce:**
1. 
2. 
3. 
```

---

## Test Summary

**Date:** _______________
**Tester:** _______________
**Environment:** _______________

**Total Tests:** 12
**Passed:** _____ / 12
**Failed:** _____ / 12

**Overall Status:** ☐ Pass ☐ Fail

**Notes:**
_______________________________________
_______________________________________
_______________________________________
