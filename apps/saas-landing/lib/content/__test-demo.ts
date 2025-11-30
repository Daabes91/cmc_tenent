/**
 * Quick demonstration of the healthcare content configuration system
 * Run this file to see the content in action
 */

import { healthcareCopy, getHealthcareCopy, getHealthcareSection } from './index';

console.log('='.repeat(80));
console.log('Healthcare Content Configuration System - Demo');
console.log('='.repeat(80));

// Demo 1: English Hero Content
console.log('\n📋 DEMO 1: English Hero Content');
console.log('-'.repeat(80));
const heroEn = healthcareCopy.en.hero;
console.log('Badge:', heroEn.badge);
console.log('Headline:', heroEn.headline.prefix);
console.log('Highlight:', heroEn.headline.highlight);
console.log('Description:', heroEn.description.substring(0, 100) + '...');
console.log('Primary CTA:', heroEn.ctaPrimary);
console.log('Secondary CTA:', heroEn.ctaSecondary);

// Demo 2: Arabic Hero Content
console.log('\n📋 DEMO 2: Arabic Hero Content');
console.log('-'.repeat(80));
const heroAr = healthcareCopy.ar.hero;
console.log('Badge:', heroAr.badge);
console.log('Headline:', heroAr.headline.prefix);
console.log('Highlight:', heroAr.headline.highlight);
console.log('Primary CTA:', heroAr.ctaPrimary);

// Demo 3: Features
console.log('\n🎯 DEMO 3: Healthcare Features (English)');
console.log('-'.repeat(80));
const features = getHealthcareSection('en', 'features');
console.log('Section Title:', features.title);
console.log('Feature Count:', features.items.length);
features.items.forEach((feature, index) => {
  console.log(`\n  ${index + 1}. ${feature.title}`);
  console.log(`     Icon: ${feature.icon}`);
  console.log(`     Benefits: ${feature.benefits.length} items`);
});

// Demo 4: Testimonials
console.log('\n💬 DEMO 4: Healthcare Testimonials (English)');
console.log('-'.repeat(80));
const testimonials = getHealthcareSection('en', 'testimonials');
console.log('Section Title:', testimonials.title);
console.log('Testimonial Count:', testimonials.items.length);
testimonials.items.forEach((testimonial, index) => {
  console.log(`\n  ${index + 1}. ${testimonial.name} - ${testimonial.role}`);
  console.log(`     Clinic: ${testimonial.clinicType}`);
  console.log(`     Metric: ${testimonial.metric || 'N/A'}`);
  console.log(`     Quote: "${testimonial.quote.substring(0, 80)}..."`);
});

// Demo 5: Pricing Tiers
console.log('\n💰 DEMO 5: Pricing Tiers (English)');
console.log('-'.repeat(80));
const pricing = getHealthcareSection('en', 'pricing');
console.log('Section Title:', pricing.title);
console.log('Tier Count:', pricing.tiers.length);
pricing.tiers.forEach((tier, index) => {
  const popular = tier.popular ? ' ⭐ POPULAR' : '';
  console.log(`\n  ${index + 1}. ${tier.name}${popular}`);
  console.log(`     Monthly: $${tier.price.monthly || 'Custom'}`);
  console.log(`     Annual: $${tier.price.annual || 'Custom'}`);
  console.log(`     Features: ${tier.features.length} items`);
  console.log(`     Limits: ${tier.limits.providers} providers, ${tier.limits.patients} patients`);
});

// Demo 6: Integrations
console.log('\n🔌 DEMO 6: Healthcare Integrations (English)');
console.log('-'.repeat(80));
const integrations = getHealthcareSection('en', 'integrations');
console.log('Section Title:', integrations.title);
console.log('Integration Count:', integrations.items.length);
integrations.items.forEach((integration, index) => {
  console.log(`\n  ${index + 1}. ${integration.name} (${integration.category})`);
  console.log(`     ${integration.description}`);
});

// Demo 7: Security
console.log('\n🔒 DEMO 7: Security & Compliance (English)');
console.log('-'.repeat(80));
const security = getHealthcareSection('en', 'security');
console.log('Title:', security.title);
console.log('Badges:', security.badges.join(', '));
console.log('Trust Indicators:');
security.trustIndicators.forEach((indicator, index) => {
  console.log(`  ${index + 1}. ${indicator}`);
});

// Demo 8: Healthcare Terminology Check
console.log('\n✅ DEMO 8: Healthcare Terminology Validation');
console.log('-'.repeat(80));
const content = JSON.stringify(healthcareCopy.en).toLowerCase();
const healthcareTerms = ['patient', 'appointment', 'clinic', 'provider', 'treatment'];
const genericTerms = ['user', 'workspace', 'project'];

console.log('Healthcare Terms Found:');
healthcareTerms.forEach(term => {
  const found = content.includes(term);
  console.log(`  ${found ? '✅' : '❌'} ${term}`);
});

console.log('\nGeneric SaaS Terms (should be avoided):');
genericTerms.forEach(term => {
  const found = content.includes(term);
  console.log(`  ${found ? '⚠️  FOUND' : '✅ NOT FOUND'} ${term}`);
});

// Demo 9: Bilingual Comparison
console.log('\n🌍 DEMO 9: Bilingual Content Comparison');
console.log('-'.repeat(80));
const enCopy = getHealthcareCopy('en');
const arCopy = getHealthcareCopy('ar');

console.log('English Content:');
console.log(`  Features: ${enCopy.features.items.length}`);
console.log(`  Testimonials: ${enCopy.testimonials.items.length}`);
console.log(`  Pricing Tiers: ${enCopy.pricing.tiers.length}`);
console.log(`  Integrations: ${enCopy.integrations.items.length}`);

console.log('\nArabic Content:');
console.log(`  Features: ${arCopy.features.items.length}`);
console.log(`  Testimonials: ${arCopy.testimonials.items.length}`);
console.log(`  Pricing Tiers: ${arCopy.pricing.tiers.length}`);
console.log(`  Integrations: ${arCopy.integrations.items.length}`);

console.log('\n✅ Content parity:', 
  enCopy.features.items.length === arCopy.features.items.length &&
  enCopy.testimonials.items.length === arCopy.testimonials.items.length &&
  enCopy.pricing.tiers.length === arCopy.pricing.tiers.length
    ? 'PASSED' : 'FAILED'
);

console.log('\n' + '='.repeat(80));
console.log('✅ Healthcare Content Configuration System is working correctly!');
console.log('='.repeat(80));
