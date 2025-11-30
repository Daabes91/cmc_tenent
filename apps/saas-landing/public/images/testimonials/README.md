# Testimonial Avatar Images

This directory contains avatar images for healthcare testimonials displayed on the landing page.

## Required Images

The following avatar images are referenced in the healthcare content configuration:

1. **dr-sarah-johnson.jpg** - Dr. Sarah Johnson, Clinic Director, Family Medicine Practice
2. **michael-chen.jpg** - Michael Chen, Practice Manager, Dental Clinic  
3. **dr-aisha-alrashid.jpg** - Dr. Aisha Al-Rashid, Owner, Physical Therapy Center
4. **dr-layla-qadri.jpg** - Dr. Layla Qadri, Medical Director, Qadri Dental Group
5. **sameer-haddad.jpg** - Sameer Haddad, COO, Shifa Health Network

## Image Specifications

- **Format**: JPG or WebP
- **Dimensions**: 400x400px minimum (square aspect ratio)
- **File Size**: < 100KB per image (optimized)
- **Quality**: Professional headshots with neutral backgrounds

## Fallback Behavior

If avatar images are not available, the component will use placeholder images from Unsplash as fallbacks. However, for production use, custom avatar images should be provided.

## Adding New Testimonials

When adding new testimonials:

1. Add the testimonial data to `lib/content/healthcare-copy.ts`
2. Place the avatar image in this directory
3. Reference the image path in the testimonial configuration: `/images/testimonials/filename.jpg`
4. Ensure the image meets the specifications above

## Image Optimization

All images should be optimized before deployment:

```bash
# Using ImageOptim, TinyPNG, or similar tools
# Target: < 100KB per image while maintaining quality
```

## Privacy & Consent

Ensure you have proper consent and rights to use all testimonial images and quotes. For stock photos, verify licensing allows commercial use.
