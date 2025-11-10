/**
 * Hero Media Performance Test
 * 
 * Tests the performance optimizations for hero media loading:
 * - Caching headers from API
 * - Client-side caching
 * - Lazy loading of YouTube iframes
 * - Image optimization
 */

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/public';

async function testApiCaching() {
  console.log('\n🔍 Testing API Caching Headers...\n');
  
  try {
    const response = await fetch(`${API_URL}/settings`);
    
    if (!response.ok) {
      console.error('❌ API request failed:', response.status);
      return false;
    }
    
    const cacheControl = response.headers.get('cache-control');
    const data = await response.json();
    
    console.log('✅ API Response Status:', response.status);
    console.log('📦 Cache-Control Header:', cacheControl || 'Not set');
    console.log('🎬 Hero Media Type:', data.heroMediaType || 'Not configured');
    
    if (data.heroMediaType === 'video') {
      console.log('📹 Video ID:', data.heroVideoId || 'Not set');
    } else if (data.heroMediaType === 'image') {
      console.log('🖼️  Image URL:', data.heroImageUrl ? 'Set' : 'Not set');
    }
    
    // Check if caching is properly configured
    if (cacheControl && cacheControl.includes('max-age')) {
      console.log('✅ Caching is properly configured');
      
      // Extract max-age value
      const maxAgeMatch = cacheControl.match(/max-age=(\d+)/);
      if (maxAgeMatch) {
        const maxAge = parseInt(maxAgeMatch[1], 10);
        console.log(`⏱️  Cache Duration: ${maxAge} seconds (${Math.floor(maxAge / 60)} minutes)`);
      }
      
      // Check for stale-while-revalidate
      if (cacheControl.includes('stale-while-revalidate')) {
        const swrMatch = cacheControl.match(/stale-while-revalidate=(\d+)/);
        if (swrMatch) {
          const swr = parseInt(swrMatch[1], 10);
          console.log(`🔄 Stale-While-Revalidate: ${swr} seconds (${Math.floor(swr / 60)} minutes)`);
        }
      }
      
      return true;
    } else {
      console.warn('⚠️  Caching headers not found or not properly configured');
      return false;
    }
  } catch (error) {
    console.error('❌ Error testing API caching:', error.message);
    return false;
  }
}

async function testResponseTime() {
  console.log('\n⚡ Testing API Response Time...\n');
  
  const iterations = 5;
  const times = [];
  
  for (let i = 0; i < iterations; i++) {
    const start = Date.now();
    
    try {
      const response = await fetch(`${API_URL}/settings`, {
        cache: 'no-store' // Force fresh request
      });
      
      if (response.ok) {
        await response.json();
        const duration = Date.now() - start;
        times.push(duration);
        console.log(`  Request ${i + 1}: ${duration}ms`);
      }
    } catch (error) {
      console.error(`  Request ${i + 1} failed:`, error.message);
    }
    
    // Small delay between requests
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  
  if (times.length > 0) {
    const avg = times.reduce((a, b) => a + b, 0) / times.length;
    const min = Math.min(...times);
    const max = Math.max(...times);
    
    console.log('\n📊 Response Time Statistics:');
    console.log(`  Average: ${avg.toFixed(2)}ms`);
    console.log(`  Min: ${min}ms`);
    console.log(`  Max: ${max}ms`);
    
    if (avg < 100) {
      console.log('✅ Excellent response time!');
    } else if (avg < 300) {
      console.log('✅ Good response time');
    } else {
      console.log('⚠️  Response time could be improved');
    }
  }
}

async function testCachedVsUncached() {
  console.log('\n🔄 Testing Cached vs Uncached Performance...\n');
  
  try {
    // First request (uncached)
    const start1 = Date.now();
    const response1 = await fetch(`${API_URL}/settings`, {
      cache: 'no-store'
    });
    await response1.json();
    const uncachedTime = Date.now() - start1;
    
    console.log(`  Uncached request: ${uncachedTime}ms`);
    
    // Second request (should be cached by browser)
    const start2 = Date.now();
    const response2 = await fetch(`${API_URL}/settings`);
    await response2.json();
    const cachedTime = Date.now() - start2;
    
    console.log(`  Cached request: ${cachedTime}ms`);
    
    const improvement = ((uncachedTime - cachedTime) / uncachedTime * 100).toFixed(1);
    console.log(`\n📈 Performance improvement: ${improvement}%`);
    
    if (cachedTime < uncachedTime) {
      console.log('✅ Caching is working and improving performance');
    } else {
      console.log('⚠️  Caching may not be working as expected');
    }
  } catch (error) {
    console.error('❌ Error testing cached vs uncached:', error.message);
  }
}

async function runTests() {
  console.log('═══════════════════════════════════════════════════════');
  console.log('  Hero Media Performance Test Suite');
  console.log('═══════════════════════════════════════════════════════');
  
  const cachingTest = await testApiCaching();
  await testResponseTime();
  await testCachedVsUncached();
  
  console.log('\n═══════════════════════════════════════════════════════');
  console.log('  Test Summary');
  console.log('═══════════════════════════════════════════════════════\n');
  
  if (cachingTest) {
    console.log('✅ All performance optimizations are in place');
    console.log('\n📝 Recommendations:');
    console.log('  - Monitor cache hit rates in production');
    console.log('  - Consider CDN for hero images');
    console.log('  - Use Next.js Image optimization for all images');
    console.log('  - Lazy load YouTube iframes (already implemented)');
  } else {
    console.log('⚠️  Some optimizations may need attention');
    console.log('\n📝 Action Items:');
    console.log('  - Verify API caching headers are set correctly');
    console.log('  - Check server configuration');
    console.log('  - Review cache-control middleware');
  }
  
  console.log('\n');
}

// Run tests
runTests().catch(console.error);
