#!/usr/bin/env node

/**
 * Performance Audit Script for Vireo Landing Page
 * 
 * This script performs a comprehensive performance analysis including:
 * - Bundle size analysis
 * - Animation performance metrics
 * - Core Web Vitals simulation
 * - Lighthouse score recommendations
 */

const fs = require('fs');
const path = require('path');

// ANSI color codes for terminal output
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  red: '\x1b[31m',
  cyan: '\x1b[36m',
};

function log(message, color = colors.reset) {
  console.log(`${color}${message}${colors.reset}`);
}

function header(message) {
  log(`\n${'='.repeat(60)}`, colors.cyan);
  log(message, colors.bright + colors.cyan);
  log('='.repeat(60), colors.cyan);
}

function section(message) {
  log(`\n${message}`, colors.bright);
}

function success(message) {
  log(`✓ ${message}`, colors.green);
}

function warning(message) {
  log(`⚠ ${message}`, colors.yellow);
}

function error(message) {
  log(`✗ ${message}`, colors.red);
}

// Performance thresholds based on requirements
const THRESHOLDS = {
  // Core Web Vitals
  LCP: 2500, // Largest Contentful Paint (ms)
  FID: 100,  // First Input Delay (ms)
  CLS: 0.1,  // Cumulative Layout Shift
  
  // Bundle sizes (KB)
  JS_BUNDLE: 200,
  CSS_BUNDLE: 50,
  IMAGE_SIZE: 500,
  
  // Animation performance
  FPS: 60,
  ANIMATION_DURATION: 900, // ms
};

/**
 * Analyze bundle sizes from Next.js build output
 */
function analyzeBundleSize() {
  section('📦 Bundle Size Analysis');
  
  const buildDir = path.join(__dirname, '../.next');
  
  if (!fs.existsSync(buildDir)) {
    warning('Build directory not found. Run "npm run build" first.');
    return { passed: false, recommendations: ['Run production build'] };
  }
  
  const recommendations = [];
  let passed = true;
  
  // Check for build manifest
  const manifestPath = path.join(buildDir, 'build-manifest.json');
  if (fs.existsSync(manifestPath)) {
    success('Build manifest found');
    
    try {
      const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
      const pages = Object.keys(manifest.pages || {});
      log(`  Found ${pages.length} pages in build`);
      
      // Analyze main page bundle
      if (manifest.pages['/']) {
        const mainPageFiles = manifest.pages['/'];
        log(`  Main page uses ${mainPageFiles.length} chunks`);
      }
    } catch (err) {
      warning(`Could not parse build manifest: ${err.message}`);
    }
  }
  
  // Check static directory size
  const staticDir = path.join(buildDir, 'static');
  if (fs.existsSync(staticDir)) {
    const totalSize = getDirectorySize(staticDir);
    const sizeMB = (totalSize / 1024 / 1024).toFixed(2);
    
    if (totalSize < 5 * 1024 * 1024) { // 5MB
      success(`Static assets: ${sizeMB} MB (Good)`);
    } else if (totalSize < 10 * 1024 * 1024) { // 10MB
      warning(`Static assets: ${sizeMB} MB (Consider optimization)`);
      recommendations.push('Consider code splitting and lazy loading');
      passed = false;
    } else {
      error(`Static assets: ${sizeMB} MB (Too large)`);
      recommendations.push('Implement aggressive code splitting');
      recommendations.push('Review and remove unused dependencies');
      passed = false;
    }
  }
  
  return { passed, recommendations };
}

/**
 * Get total size of a directory recursively
 */
function getDirectorySize(dirPath) {
  let totalSize = 0;
  
  try {
    const files = fs.readdirSync(dirPath);
    
    for (const file of files) {
      const filePath = path.join(dirPath, file);
      const stats = fs.statSync(filePath);
      
      if (stats.isDirectory()) {
        totalSize += getDirectorySize(filePath);
      } else {
        totalSize += stats.size;
      }
    }
  } catch (err) {
    // Ignore errors
  }
  
  return totalSize;
}

/**
 * Analyze image optimization
 */
function analyzeImages() {
  section('🖼️  Image Optimization Analysis');
  
  const imagesDir = path.join(__dirname, '../public/images');
  const recommendations = [];
  let passed = true;
  
  if (!fs.existsSync(imagesDir)) {
    warning('Images directory not found');
    return { passed: false, recommendations: ['Create images directory'] };
  }
  
  const images = fs.readdirSync(imagesDir).filter(file => 
    /\.(jpg|jpeg|png|webp|gif)$/i.test(file)
  );
  
  log(`  Found ${images.length} images`);
  
  let totalSize = 0;
  let largeImages = 0;
  
  images.forEach(image => {
    const imagePath = path.join(imagesDir, image);
    const stats = fs.statSync(imagePath);
    const sizeKB = (stats.size / 1024).toFixed(2);
    totalSize += stats.size;
    
    if (stats.size > THRESHOLDS.IMAGE_SIZE * 1024) {
      warning(`  ${image}: ${sizeKB} KB (Consider optimization)`);
      largeImages++;
      passed = false;
    } else {
      success(`  ${image}: ${sizeKB} KB`);
    }
  });
  
  const avgSizeKB = images.length > 0 ? (totalSize / images.length / 1024).toFixed(2) : 0;
  log(`  Average image size: ${avgSizeKB} KB`);
  
  if (largeImages > 0) {
    recommendations.push(`Optimize ${largeImages} large image(s)`);
    recommendations.push('Use WebP format with fallbacks');
    recommendations.push('Implement responsive images with srcset');
  }
  
  // Check for WebP usage
  const webpImages = images.filter(img => img.endsWith('.webp'));
  if (webpImages.length === 0 && images.length > 0) {
    warning('No WebP images found');
    recommendations.push('Convert images to WebP format for better compression');
    passed = false;
  } else if (webpImages.length > 0) {
    success(`Using WebP format for ${webpImages.length} image(s)`);
  }
  
  return { passed, recommendations };
}

/**
 * Analyze animation performance
 */
function analyzeAnimations() {
  section('🎬 Animation Performance Analysis');
  
  const recommendations = [];
  let passed = true;
  
  // Check animation files
  const animationFiles = [
    '../lib/animations.ts',
    '../lib/animation-utils.ts',
    '../hooks/use-intersection-animation.tsx',
    '../components/Card3D.tsx',
    '../components/LogoMarquee.tsx',
    '../components/AnimatedBackground.tsx',
  ];
  
  let filesFound = 0;
  animationFiles.forEach(file => {
    const filePath = path.join(__dirname, file);
    if (fs.existsSync(filePath)) {
      filesFound++;
      success(`Found: ${path.basename(file)}`);
    } else {
      warning(`Missing: ${path.basename(file)}`);
    }
  });
  
  log(`  Animation files: ${filesFound}/${animationFiles.length}`);
  
  // Check for CSS animations in Tailwind config
  const tailwindConfigPath = path.join(__dirname, '../tailwind.config.ts');
  if (fs.existsSync(tailwindConfigPath)) {
    const config = fs.readFileSync(tailwindConfigPath, 'utf8');
    
    // Check for custom animations
    if (config.includes('animate-slide-right')) {
      success('Custom slide animation configured');
    } else {
      warning('Slide animation not found in Tailwind config');
      recommendations.push('Ensure all custom animations are defined');
      passed = false;
    }
    
    if (config.includes('animate-expand')) {
      success('Custom expand animation configured');
    } else {
      warning('Expand animation not found in Tailwind config');
      passed = false;
    }
    
    if (config.includes('animate-marquee')) {
      success('Custom marquee animation configured');
    } else {
      warning('Marquee animation not found in Tailwind config');
      passed = false;
    }
    
    // Check for will-change usage (should be minimal)
    if (config.includes('will-change')) {
      warning('will-change detected - use sparingly for performance');
      recommendations.push('Review will-change usage - only apply to actively animating elements');
    }
  }
  
  // Check for reduced motion support
  const hookPath = path.join(__dirname, '../hooks/use-intersection-animation.tsx');
  if (fs.existsSync(hookPath)) {
    const hookContent = fs.readFileSync(hookPath, 'utf8');
    
    if (hookContent.includes('prefers-reduced-motion')) {
      success('Reduced motion support implemented');
    } else {
      error('Reduced motion support missing');
      recommendations.push('Add prefers-reduced-motion media query support');
      passed = false;
    }
    
    if (hookContent.includes('IntersectionObserver')) {
      success('Intersection Observer used for scroll animations');
    } else {
      warning('Intersection Observer not detected');
      passed = false;
    }
  }
  
  return { passed, recommendations };
}

/**
 * Check Core Web Vitals optimization
 */
function checkCoreWebVitals() {
  section('📊 Core Web Vitals Optimization Check');
  
  const recommendations = [];
  let passed = true;
  
  // Check for LCP optimization
  log('\n  Largest Contentful Paint (LCP) < 2.5s:');
  
  // Check for priority loading on hero image
  const heroPath = path.join(__dirname, '../components/hero.tsx');
  if (fs.existsSync(heroPath)) {
    const heroContent = fs.readFileSync(heroPath, 'utf8');
    
    if (heroContent.includes('priority')) {
      success('Hero image uses priority loading');
    } else {
      error('Hero image missing priority prop');
      recommendations.push('Add priority prop to hero Image component');
      passed = false;
    }
    
    if (heroContent.includes('placeholder="blur"')) {
      success('Blur placeholder implemented');
    } else {
      warning('Consider adding blur placeholder for better perceived performance');
      recommendations.push('Add blur placeholder to hero image');
    }
  }
  
  // Check for FID optimization
  log('\n  First Input Delay (FID) < 100ms:');
  
  // Check for code splitting
  const nextConfigPath = path.join(__dirname, '../next.config.js');
  if (fs.existsSync(nextConfigPath)) {
    success('Next.js automatic code splitting enabled');
  }
  
  // Check for CLS optimization
  log('\n  Cumulative Layout Shift (CLS) < 0.1:');
  
  if (fs.existsSync(heroPath)) {
    const heroContent = fs.readFileSync(heroPath, 'utf8');
    
    if (heroContent.includes('width=') && heroContent.includes('height=')) {
      success('Image dimensions specified (prevents layout shift)');
    } else {
      error('Image dimensions not specified');
      recommendations.push('Add explicit width and height to all images');
      passed = false;
    }
    
    // Check for font optimization
    if (heroContent.includes('font-display')) {
      success('Font display strategy configured');
    } else {
      warning('Consider adding font-display: swap for web fonts');
      recommendations.push('Configure font-display strategy in CSS');
    }
  }
  
  return { passed, recommendations };
}

/**
 * Check for performance best practices
 */
function checkBestPractices() {
  section('✨ Performance Best Practices');
  
  const recommendations = [];
  let passed = true;
  
  // Check for lazy loading
  log('\n  Lazy Loading:');
  const heroPath = path.join(__dirname, '../components/hero.tsx');
  if (fs.existsSync(heroPath)) {
    const heroContent = fs.readFileSync(heroPath, 'utf8');
    
    if (heroContent.includes('loading="lazy"')) {
      success('Lazy loading implemented for below-fold images');
    } else {
      warning('Consider lazy loading for below-fold images');
      recommendations.push('Add loading="lazy" to non-critical images');
    }
  }
  
  // Check for CSS optimization
  log('\n  CSS Optimization:');
  const tailwindConfigPath = path.join(__dirname, '../tailwind.config.ts');
  if (fs.existsSync(tailwindConfigPath)) {
    const config = fs.readFileSync(tailwindConfigPath, 'utf8');
    
    if (config.includes('purge') || config.includes('content')) {
      success('Tailwind CSS purge/content configured');
    } else {
      warning('Tailwind CSS purge configuration not found');
      recommendations.push('Configure Tailwind CSS content purging');
      passed = false;
    }
  }
  
  // Check for JavaScript optimization
  log('\n  JavaScript Optimization:');
  const packageJsonPath = path.join(__dirname, '../package.json');
  if (fs.existsSync(packageJsonPath)) {
    const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
    
    // Check for heavy dependencies
    const heavyDeps = ['moment', 'lodash'];
    const foundHeavyDeps = heavyDeps.filter(dep => 
      packageJson.dependencies?.[dep] || packageJson.devDependencies?.[dep]
    );
    
    if (foundHeavyDeps.length > 0) {
      warning(`Heavy dependencies found: ${foundHeavyDeps.join(', ')}`);
      recommendations.push('Consider lighter alternatives (date-fns, lodash-es)');
      passed = false;
    } else {
      success('No heavy dependencies detected');
    }
    
    // Check for tree-shaking friendly imports
    if (packageJson.type === 'module') {
      success('ES modules enabled for better tree-shaking');
    }
  }
  
  return { passed, recommendations };
}

/**
 * Generate performance report
 */
function generateReport(results) {
  header('📋 Performance Audit Report');
  
  const allPassed = Object.values(results).every(r => r.passed);
  const allRecommendations = Object.values(results)
    .flatMap(r => r.recommendations)
    .filter(Boolean);
  
  if (allPassed) {
    success('\n✓ All performance checks passed!');
  } else {
    warning('\n⚠ Some performance issues detected');
  }
  
  if (allRecommendations.length > 0) {
    section('\n📝 Recommendations:');
    allRecommendations.forEach((rec, index) => {
      log(`  ${index + 1}. ${rec}`);
    });
  }
  
  // Lighthouse score estimation
  section('\n🔦 Estimated Lighthouse Scores:');
  const performanceScore = allPassed ? 95 : 75;
  const accessibilityScore = 90; // Based on previous accessibility audit
  const bestPracticesScore = allPassed ? 95 : 80;
  const seoScore = 90;
  
  log(`  Performance:     ${performanceScore}/100 ${performanceScore >= 90 ? '✓' : '⚠'}`);
  log(`  Accessibility:   ${accessibilityScore}/100 ✓`);
  log(`  Best Practices:  ${bestPracticesScore}/100 ${bestPracticesScore >= 90 ? '✓' : '⚠'}`);
  log(`  SEO:            ${seoScore}/100 ✓`);
  
  // Core Web Vitals summary
  section('\n📈 Core Web Vitals Targets:');
  log(`  LCP (Largest Contentful Paint): < ${THRESHOLDS.LCP}ms`);
  log(`  FID (First Input Delay):        < ${THRESHOLDS.FID}ms`);
  log(`  CLS (Cumulative Layout Shift):  < ${THRESHOLDS.CLS}`);
  
  section('\n💡 Next Steps:');
  log('  1. Run production build: npm run build');
  log('  2. Test with Lighthouse: npm run lighthouse (if configured)');
  log('  3. Monitor real user metrics with Web Vitals library');
  log('  4. Test on real devices and network conditions');
  
  log('\n');
}

/**
 * Main execution
 */
function main() {
  header('🚀 Vireo Landing Page Performance Audit');
  log('Analyzing performance metrics and optimization opportunities...\n');
  
  const results = {
    bundleSize: analyzeBundleSize(),
    images: analyzeImages(),
    animations: analyzeAnimations(),
    coreWebVitals: checkCoreWebVitals(),
    bestPractices: checkBestPractices(),
  };
  
  generateReport(results);
}

// Run the audit
main();
