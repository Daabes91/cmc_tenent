<template>
  <div>
    <ErrorBoundary>
      <NuxtLayout>
        <NuxtPage />
      </NuxtLayout>
    </ErrorBoundary>
    
    <!-- Toast Notifications (client-only to avoid hydration mismatch) -->
    <ClientOnly>
      <ToastContainer />
    </ClientOnly>
  </div>
</template>

<script setup lang="ts">
// Set page title
useHead({
  titleTemplate: (titleChunk) => {
    return titleChunk ? `${titleChunk} - SAAS Manager Panel` : 'SAAS Manager Panel'
  }
})
</script>
