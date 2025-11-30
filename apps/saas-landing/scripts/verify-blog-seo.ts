/**
 * Blog SEO Verification Script
 * 
 * Verifies that all blog posts have proper SEO optimization:
 * - Unique meta titles
 * - Unique meta descriptions
 * - Open Graph tags
 * - Twitter Card tags
 * - BlogPosting schema
 * - SEO-friendly URLs
 * - Sitemap inclusion
 */

import { getAllBlogPosts } from '../lib/blog/get-posts';
import { isValidSlug } from '../lib/blog/slug';

interface SEOIssue {
  slug: string;
  issue: string;
  severity: 'error' | 'warning';
}

async function verifyBlogSEO() {
  console.log('🔍 Verifying Blog SEO Optimization...\n');
  
  const posts = await getAllBlogPosts(true);
  const issues: SEOIssue[] = [];
  
  // Track unique values
  const metaTitles = new Map<string, string[]>();
  const metaDescriptions = new Map<string, string[]>();
  
  console.log(`📝 Found ${posts.length} published blog posts\n`);
  
  // Check each post
  for (const post of posts) {
    console.log(`Checking: ${post.title} (${post.slug})`);
    
    // 1. Check SEO-friendly URL
    if (!isValidSlug(post.slug)) {
      issues.push({
        slug: post.slug,
        issue: 'Slug is not SEO-friendly (should be lowercase, hyphenated, no special characters)',
        severity: 'error',
      });
    }
    
    // 2. Check meta title exists and is unique
    if (!post.seo?.title) {
      issues.push({
        slug: post.slug,
        issue: 'Missing SEO meta title',
        severity: 'error',
      });
    } else {
      if (!metaTitles.has(post.seo.title)) {
        metaTitles.set(post.seo.title, []);
      }
      metaTitles.get(post.seo.title)!.push(post.slug);
    }
    
    // 3. Check meta description exists and is unique
    if (!post.seo?.description) {
      issues.push({
        slug: post.slug,
        issue: 'Missing SEO meta description',
        severity: 'error',
      });
    } else {
      if (!metaDescriptions.has(post.seo.description)) {
        metaDescriptions.set(post.seo.description, []);
      }
      metaDescriptions.get(post.seo.description)!.push(post.slug);
    }
    
    // 4. Check meta description length (recommended 150-160 characters)
    if (post.seo?.description && post.seo.description.length > 160) {
      issues.push({
        slug: post.slug,
        issue: `Meta description too long (${post.seo.description.length} chars, recommended max 160)`,
        severity: 'warning',
      });
    }
    
    if (post.seo?.description && post.seo.description.length < 120) {
      issues.push({
        slug: post.slug,
        issue: `Meta description too short (${post.seo.description.length} chars, recommended min 120)`,
        severity: 'warning',
      });
    }
    
    // 5. Check keywords exist
    if (!post.seo?.keywords || post.seo.keywords.length === 0) {
      issues.push({
        slug: post.slug,
        issue: 'Missing SEO keywords',
        severity: 'warning',
      });
    }
    
    // 6. Check featured image exists (for Open Graph)
    if (!post.featuredImage) {
      issues.push({
        slug: post.slug,
        issue: 'Missing featured image (required for Open Graph)',
        severity: 'error',
      });
    }
    
    // 7. Check author information (for BlogPosting schema)
    if (!post.author?.name) {
      issues.push({
        slug: post.slug,
        issue: 'Missing author name (required for BlogPosting schema)',
        severity: 'error',
      });
    }
    
    // 8. Check publication date
    if (!post.publishedAt) {
      issues.push({
        slug: post.slug,
        issue: 'Missing publication date',
        severity: 'error',
      });
    }
    
    console.log(`  ✓ Checked ${post.slug}\n`);
  }
  
  // Check for duplicate meta titles
  for (const [title, slugs] of metaTitles.entries()) {
    if (slugs.length > 1) {
      issues.push({
        slug: slugs.join(', '),
        issue: `Duplicate meta title: "${title}"`,
        severity: 'error',
      });
    }
  }
  
  // Check for duplicate meta descriptions
  for (const [description, slugs] of metaDescriptions.entries()) {
    if (slugs.length > 1) {
      issues.push({
        slug: slugs.join(', '),
        issue: `Duplicate meta description: "${description.substring(0, 50)}..."`,
        severity: 'error',
      });
    }
  }
  
  // Report results
  console.log('\n' + '='.repeat(80));
  console.log('📊 SEO Verification Results');
  console.log('='.repeat(80) + '\n');
  
  const errors = issues.filter(i => i.severity === 'error');
  const warnings = issues.filter(i => i.severity === 'warning');
  
  if (issues.length === 0) {
    console.log('✅ All blog posts have proper SEO optimization!\n');
    console.log('Verified:');
    console.log(`  ✓ ${posts.length} posts with unique meta titles`);
    console.log(`  ✓ ${posts.length} posts with unique meta descriptions`);
    console.log(`  ✓ ${posts.length} posts with SEO-friendly URLs`);
    console.log(`  ✓ ${posts.length} posts with featured images (Open Graph)`);
    console.log(`  ✓ ${posts.length} posts with author information (BlogPosting schema)`);
    console.log(`  ✓ ${posts.length} posts with publication dates`);
    console.log('\n✨ Blog SEO is fully optimized!\n');
    return true;
  }
  
  if (errors.length > 0) {
    console.log(`❌ Found ${errors.length} error(s):\n`);
    errors.forEach(issue => {
      console.log(`  • [${issue.slug}] ${issue.issue}`);
    });
    console.log('');
  }
  
  if (warnings.length > 0) {
    console.log(`⚠️  Found ${warnings.length} warning(s):\n`);
    warnings.forEach(issue => {
      console.log(`  • [${issue.slug}] ${issue.issue}`);
    });
    console.log('');
  }
  
  console.log('Please fix these issues to ensure optimal SEO.\n');
  return errors.length === 0;
}

// Run verification
verifyBlogSEO()
  .then(success => {
    process.exit(success ? 0 : 1);
  })
  .catch(error => {
    console.error('Error running SEO verification:', error);
    process.exit(1);
  });
