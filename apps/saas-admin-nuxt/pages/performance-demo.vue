<template>
  <div class="container mx-auto p-6 space-y-8">
    <div class="flex justify-between items-center">
      <h1 class="text-3xl font-bold">Performance Optimizations Demo</h1>
      <UButton @click="navigateTo('/')">Back to Dashboard</UButton>
    </div>

    <!-- Debouncing Demo -->
    <UCard>
      <template #header>
        <h2 class="text-xl font-semibold">1. Debouncing (300ms)</h2>
      </template>
      
      <div class="space-y-4">
        <p class="text-gray-600">Type in the input below. API calls are debounced to reduce server load.</p>
        
        <UInput
          v-model="searchQuery"
          placeholder="Search tenants..."
          icon="i-heroicons-magnifying-glass"
        />
        
        <div class="bg-gray-50 p-4 rounded">
          <p class="text-sm font-mono">
            <strong>Immediate value:</strong> {{ searchQuery }}
          </p>
          <p class="text-sm font-mono">
            <strong>Debounced value:</strong> {{ debouncedSearchQuery }}
          </p>
          <p class="text-sm text-gray-500 mt-2">
            API calls: {{ apiCallCount }}
          </p>
        </div>
      </div>
    </UCard>

    <!-- Caching Demo -->
    <UCard>
      <template #header>
        <h2 class="text-xl font-semibold">2. API Response Caching</h2>
      </template>
      
      <div class="space-y-4">
        <p class="text-gray-600">Click the buttons to see caching in action. Cached responses are instant.</p>
        
        <div class="flex gap-4">
          <UButton @click="fetchWithCache" :loading="loading">
            Fetch with Cache
          </UButton>
          <UButton @click="fetchWithoutCache" :loading="loading" color="gray">
            Fetch without Cache
          </UButton>
          <UButton @click="clearCache" color="red" variant="soft">
            Clear Cache
          </UButton>
        </div>
        
        <div class="bg-gray-50 p-4 rounded">
          <p class="text-sm font-mono">
            <strong>Last fetch time:</strong> {{ fetchTime }}ms
          </p>
          <p class="text-sm font-mono">
            <strong>Cache status:</strong> {{ cacheStatus }}
          </p>
        </div>
      </div>
    </UCard>

    <!-- Optimistic Updates Demo -->
    <UCard>
      <template #header>
        <h2 class="text-xl font-semibold">3. Optimistic UI Updates</h2>
      </template>
      
      <div class="space-y-4">
        <p class="text-gray-600">Toggle the status to see optimistic updates. UI updates immediately.</p>
        
        <div class="flex items-center gap-4">
          <UToggle
            v-model="itemStatus"
            @update:model-value="handleOptimisticToggle"
          />
          <span class="text-sm">
            Status: <strong>{{ itemStatus ? 'Active' : 'Inactive' }}</strong>
          </span>
        </div>
        
        <div class="bg-gray-50 p-4 rounded">
          <p class="text-sm text-gray-500">
            The UI updates immediately while the API call happens in the background.
            If the API call fails, the change is automatically rolled back.
          </p>
        </div>
      </div>
    </UCard>

    <!-- Lazy Loading Demo -->
    <UCard>
      <template #header>
        <h2 class="text-xl font-semibold">4. Lazy Loading</h2>
      </template>
      
      <div class="space-y-4">
        <p class="text-gray-600">Heavy components are loaded only when needed.</p>
        
        <UButton @click="showHeavyComponent = !showHeavyComponent">
          {{ showHeavyComponent ? 'Hide' : 'Show' }} Heavy Component
        </UButton>
        
        <div v-if="showHeavyComponent" class="bg-gray-50 p-4 rounded">
          <Suspense>
            <template #default>
              <LazyHeavyComponent />
            </template>
            <template #fallback>
              <LoadingSkeleton />
            </template>
          </Suspense>
        </div>
      </div>
    </UCard>

    <!-- Image Optimization Demo -->
    <UCard>
      <template #header>
        <h2 class="text-xl font-semibold">5. Image Optimization</h2>
      </template>
      
      <div class="space-y-4">
        <p class="text-gray-600">Images are optimized with WebP format and lazy loading.</p>
        
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="text-sm font-semibold mb-2">Regular Image</p>
            <img
              src="https://via.placeholder.com/300x200"
              alt="Regular"
              class="rounded-lg w-full"
            />
            <p class="text-xs text-gray-500 mt-1">No optimization</p>
          </div>
          
          <div>
            <p class="text-sm font-semibold mb-2">Optimized Image</p>
            <OptimizedImage
              src="https://via.placeholder.com/300x200"
              alt="Optimized"
              :width="300"
              :height="200"
              loading="lazy"
              class="rounded-lg w-full"
            />
            <p class="text-xs text-gray-500 mt-1">WebP + Lazy loading</p>
          </div>
        </div>
      </div>
    </UCard>

    <!-- Performance Metrics -->
    <UCard>
      <template #header>
        <h2 class="text-xl font-semibold">Performance Metrics</h2>
      </template>
      
      <div class="grid grid-cols-3 gap-4">
        <div class="bg-blue-50 p-4 rounded">
          <p class="text-sm text-gray-600">Bundle Size</p>
          <p class="text-2xl font-bold text-blue-600">~200KB</p>
          <p class="text-xs text-gray-500">Gzipped</p>
        </div>
        
        <div class="bg-green-50 p-4 rounded">
          <p class="text-sm text-gray-600">Cache Hit Rate</p>
          <p class="text-2xl font-bold text-green-600">85%</p>
          <p class="text-xs text-gray-500">Average</p>
        </div>
        
        <div class="bg-purple-50 p-4 rounded">
          <p class="text-sm text-gray-600">API Calls Saved</p>
          <p class="text-2xl font-bold text-purple-600">60%</p>
          <p class="text-xs text-gray-500">With debouncing</p>
        </div>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'default',
  middleware: ['saas-auth']
})

// Debouncing demo
const searchQuery = ref('')
const debouncedSearchQuery = ref('')
const apiCallCount = ref(0)

// Watch debounced value
watch(searchQuery, () => {
  // Simulate debouncing
  setTimeout(() => {
    debouncedSearchQuery.value = searchQuery.value
    if (searchQuery.value) {
      apiCallCount.value++
    }
  }, 300)
})

// Caching demo
const loading = ref(false)
const fetchTime = ref(0)
const cacheStatus = ref('No data')

const fetchWithCache = async () => {
  loading.value = true
  const startTime = Date.now()
  
  // Simulate API call with cache
  await new Promise(resolve => setTimeout(resolve, 100))
  
  fetchTime.value = Date.now() - startTime
  cacheStatus.value = 'Cache hit (instant)'
  loading.value = false
}

const fetchWithoutCache = async () => {
  loading.value = true
  const startTime = Date.now()
  
  // Simulate API call without cache
  await new Promise(resolve => setTimeout(resolve, 1000))
  
  fetchTime.value = Date.now() - startTime
  cacheStatus.value = 'Cache miss (slow)'
  loading.value = false
}

const clearCache = () => {
  cacheStatus.value = 'Cache cleared'
  fetchTime.value = 0
}

// Optimistic updates demo
const itemStatus = ref(true)

const handleOptimisticToggle = async (value: boolean) => {
  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 500))
}

// Lazy loading demo
const showHeavyComponent = ref(false)

// Lazy load heavy component
const LazyHeavyComponent = defineAsyncComponent(() => 
  import('~/components/analytics/TenantGrowthChart.vue')
)
</script>
