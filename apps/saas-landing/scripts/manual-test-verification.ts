#!/usr/bin/env tsx

/**
 * Manual Testing Verification Script
 * 
 * This script performs automated checks to support manual testing.
 * It verifies:
 * - Healthcare content terminology
 * - Blog post structure
 * - SEO metadata
 * - Content completeness
 */

import { healthcareCopy } from '../lib/content/healthcare-copy';
import { getAllBlogPosts } from '../lib/blog/get-posts';
import fs from 'fs';
import path from 'path';

interface TestResult {
  category: string;
  test: string;
  status: 'PASS' | 'FAIL' | 'WARN';
  message: string;
}

const results: TestResult[] = [];

function addResult(category: string, test: string, status: 'PASS' | 'FAIL' | 'WARN', message: string) {
  results.push({ category, test, status, message });
}

// Healthcare Terminology Check
function checkHealthcareTerminology() {
  console.log('\n🏥 Checking Healthcare Terminology...\n');
  
  const genericTerms = ['dashboard', 'workspace', 'projects', 'users', 'saas'];
  const healthcareTerms = ['patient', 'appointment', 'clinic', 'provider', 'treatment', 'healthcare'];
  
  const enContent = JSON.stringify(healthcareCopy.en).toLowerCase();
  
  // Check for generic terms
  let foundGeneric = false;
  genericTerms.forEach(term => {
    if (enContent.includes(term)) {
      addResult('Content', `Generic term "${term}"`, 'WARN', `Found generic SaaS term in content`);
      foundGeneric = true;
    }
  });
  
  if (!foundGeneric) {
    addResult('Content', 'Generic terms', 'PASS', 'No generic SaaS terms found');
  }
  
  // Check for healthcare terms
  let foundHealthcare = false;
  healthcareTerms.forEach(term => {
    if (enContent.includes(term)) {
      foundHealthcare = true;
    }
  });
  
  if (foundHealthcare) {
    addResult('Content', 'Healthcare terms', 'PASS', 'Healthcare-specific terminology present');
  } else {
    addResult('Content', 'Healthcare terms', 'FAIL', 'Missing healthcare-specific terminology');
  }
}

// Testimonials Structure Check
function checkTestimonials() {
  console.log('\n💬 Checking Testimonials Structure...\n');
  
  const testimonials = healthcareCopy.en.testimonials.items;
  
  if (testimonials.length >= 3) {
    addResult('Testimonials', 'Count', 'PASS', `${testimonials.length} testimonials found`);
  } else {
    addResult('Testimonials', 'Count', 'FAIL', `Only ${testimonials.length} testimonials (need at least 3)`);
  }
  
  testimonials.forEach((testimonial, index) => {
    const hasName = !!testimonial.name;
    const hasRole = !!testimonial.role;
    const hasClinicType = !!testimonial.clinicType;
    const hasQuote = !!testimonial.quote;
    const hasMetric = !!testimonial.metric;
    const metricValue = testimonial.metric ?? "";
    
    if (hasName && hasRole && hasClinicType && hasQuote) {
      addResult('Testimonials', `Testimonial ${index + 1}`, 'PASS', `Complete structure`);
    } else {
      addResult('Testimonials', `Testimonial ${index + 1}`, 'FAIL', `Missing required fields`);
    }
    
    if (hasMetric && /\d+/.test(metricValue)) {
      addResult('Testimonials', `Testimonial ${index + 1} metric`, 'PASS', `Quantifiable metric present`);
    }
  });
}

// Pricing Structure Check
function checkPricing() {
  console.log('\n💰 Checking Pricing Structure...\n');
  
  const tiers = healthcareCopy.en.pricing.tiers;
  
  if (tiers.length === 3) {
    addResult('Pricing', 'Tier count', 'PASS', '3 pricing tiers found');
  } else {
    addResult('Pricing', 'Tier count', 'WARN', `${tiers.length} tiers (expected 3)`);
  }
  
  tiers.forEach((tier, index) => {
    const monthly = tier.price?.monthly ?? 0;
    const annual = tier.price?.annual ?? 0;
    const hasMonthly = monthly > 0;
    const hasAnnual = annual > 0;
    const hasHealthcareFeatures = tier.features.some(f => 
      /patient|provider|appointment|clinic|treatment/i.test(f)
    );
    const hasHealthcareLimits = tier.limits && (
      'providers' in tier.limits || 
      'patients' in tier.limits || 
      'appointments' in tier.limits
    );
    
    if (hasMonthly && hasAnnual) {
      addResult('Pricing', `${tier.name} - Pricing`, 'PASS', 'Both monthly and annual pricing present');
    } else {
      addResult('Pricing', `${tier.name} - Pricing`, 'FAIL', 'Missing monthly or annual pricing');
    }
    
    if (hasHealthcareFeatures) {
      addResult('Pricing', `${tier.name} - Features`, 'PASS', 'Healthcare-specific features present');
    } else {
      addResult('Pricing', `${tier.name} - Features`, 'WARN', 'No healthcare-specific features found');
    }
    
    if (hasHealthcareLimits) {
      addResult('Pricing', `${tier.name} - Limits`, 'PASS', 'Healthcare-specific limits present');
    } else {
      addResult('Pricing', `${tier.name} - Limits`, 'FAIL', 'Missing healthcare-specific limits');
    }
  });
}

// Blog Posts Check
async function checkBlogPosts() {
  console.log('\n📝 Checking Blog Posts...\n');
  
  try {
    const posts = await getAllBlogPosts();
    
    if (posts.length >= 5) {
      addResult('Blog', 'Post count', 'PASS', `${posts.length} blog posts found`);
    } else {
      addResult('Blog', 'Post count', 'WARN', `Only ${posts.length} posts (recommended at least 5)`);
    }
    
    // Check for draft posts in published list
    const draftPosts = posts.filter(p => p.draft === true);
    if (draftPosts.length === 0) {
      addResult('Blog', 'Draft visibility', 'PASS', 'No draft posts in published list');
    } else {
      addResult('Blog', 'Draft visibility', 'FAIL', `${draftPosts.length} draft posts visible`);
    }
    
    // Check for scheduled posts
    const now = new Date();
    const scheduledPosts = posts.filter(p => new Date(p.publishedAt) > now);
    if (scheduledPosts.length === 0) {
      addResult('Blog', 'Scheduled visibility', 'PASS', 'No future-dated posts in published list');
    } else {
      addResult('Blog', 'Scheduled visibility', 'FAIL', `${scheduledPosts.length} scheduled posts visible`);
    }
    
    // Check post structure
    posts.slice(0, 5).forEach((post, index) => {
      const hasTitle = !!post.title;
      const hasExcerpt = !!post.excerpt;
      const hasAuthor = !!post.author?.name;
      const hasDate = !!post.publishedAt;
      const hasCategory = !!post.category;
      const hasFeaturedImage = !!post.featuredImage;
      
      if (hasTitle && hasExcerpt && hasAuthor && hasDate && hasCategory) {
        addResult('Blog', `Post ${index + 1} structure`, 'PASS', 'Complete metadata');
      } else {
        addResult('Blog', `Post ${index + 1} structure`, 'FAIL', 'Missing required metadata');
      }
      
      // Check SEO metadata
      if (post.seo?.title && post.seo?.description) {
        addResult('Blog', `Post ${index + 1} SEO`, 'PASS', 'SEO metadata present');
      } else {
        addResult('Blog', `Post ${index + 1} SEO`, 'WARN', 'Missing SEO metadata');
      }
    });
    
  } catch (error) {
    addResult('Blog', 'System', 'FAIL', `Error loading blog posts: ${error}`);
  }
}

// Integrations Check
function checkIntegrations() {
  console.log('\n🔌 Checking Integrations...\n');
  
  const integrations = healthcareCopy.en.integrations.items;
  
  if (integrations.length >= 6) {
    addResult('Integrations', 'Count', 'PASS', `${integrations.length} integrations found`);
  } else {
    addResult('Integrations', 'Count', 'WARN', `Only ${integrations.length} integrations`);
  }
  
  integrations.forEach((integration, index) => {
    const hasDescription = !!integration.description;
    const mentionsClinic = /clinic|patient|appointment|healthcare|provider/i.test(integration.description);
    
    if (hasDescription && mentionsClinic) {
      addResult('Integrations', `${integration.name}`, 'PASS', 'Healthcare-relevant description');
    } else if (hasDescription) {
      addResult('Integrations', `${integration.name}`, 'WARN', 'Description not healthcare-specific');
    } else {
      addResult('Integrations', `${integration.name}`, 'FAIL', 'Missing description');
    }
  });
}

// Security Section Check
function checkSecurity() {
  console.log('\n🔒 Checking Security Section...\n');
  
  const security = healthcareCopy.en.security;
  
  const mentionsHIPAA = security.badges.some(b => /hipaa/i.test(b));
  const mentionsGDPR = security.badges.some(b => /gdpr/i.test(b));
  
  if (mentionsHIPAA) {
    addResult('Security', 'HIPAA', 'PASS', 'HIPAA compliance mentioned');
  } else {
    addResult('Security', 'HIPAA', 'FAIL', 'HIPAA compliance not mentioned');
  }
  
  if (mentionsGDPR) {
    addResult('Security', 'GDPR', 'PASS', 'GDPR compliance mentioned');
  } else {
    addResult('Security', 'GDPR', 'WARN', 'GDPR compliance not mentioned');
  }
  
  if (security.trustIndicators.length >= 4) {
    addResult('Security', 'Trust indicators', 'PASS', `${security.trustIndicators.length} trust indicators`);
  } else {
    addResult('Security', 'Trust indicators', 'WARN', 'Few trust indicators');
  }
}

// Print Results
function printResults() {
  console.log('\n' + '='.repeat(80));
  console.log('MANUAL TESTING VERIFICATION RESULTS');
  console.log('='.repeat(80) + '\n');
  
  const categories = [...new Set(results.map(r => r.category))];
  
  categories.forEach(category => {
    console.log(`\n📋 ${category}`);
    console.log('-'.repeat(80));
    
    const categoryResults = results.filter(r => r.category === category);
    categoryResults.forEach(result => {
      const icon = result.status === 'PASS' ? '✅' : result.status === 'FAIL' ? '❌' : '⚠️';
      console.log(`${icon} ${result.test}: ${result.message}`);
    });
  });
  
  // Summary
  const passed = results.filter(r => r.status === 'PASS').length;
  const failed = results.filter(r => r.status === 'FAIL').length;
  const warnings = results.filter(r => r.status === 'WARN').length;
  const total = results.length;
  
  console.log('\n' + '='.repeat(80));
  console.log('SUMMARY');
  console.log('='.repeat(80));
  console.log(`✅ Passed: ${passed}/${total}`);
  console.log(`❌ Failed: ${failed}/${total}`);
  console.log(`⚠️  Warnings: ${warnings}/${total}`);
  
  const passRate = ((passed / total) * 100).toFixed(1);
  console.log(`\n📊 Pass Rate: ${passRate}%`);
  
  if (failed === 0) {
    console.log('\n🎉 All critical tests passed!');
  } else {
    console.log('\n⚠️  Some tests failed. Please review the results above.');
  }
  
  console.log('\n' + '='.repeat(80) + '\n');
}

// Main execution
async function main() {
  console.log('🚀 Starting Manual Testing Verification...\n');
  
  checkHealthcareTerminology();
  checkTestimonials();
  checkPricing();
  await checkBlogPosts();
  checkIntegrations();
  checkSecurity();
  
  printResults();
  
  // Exit with error code if any tests failed
  const failed = results.filter(r => r.status === 'FAIL').length;
  process.exit(failed > 0 ? 1 : 0);
}

main().catch(error => {
  console.error('Error running verification:', error);
  process.exit(1);
});
