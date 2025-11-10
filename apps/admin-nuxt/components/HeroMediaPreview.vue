<template>
  <div class="space-y-3">
    <div class="flex items-center gap-2">
      <UIcon name="i-lucide-eye" class="h-5 w-5 text-violet-600 dark:text-violet-400" />
      <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('heroMedia.preview.title') }}</h3>
    </div>
    <p class="text-sm text-slate-600 dark:text-slate-400">
      {{ t('heroMedia.preview.description') }}
    </p>

    <!-- Preview Container - Responsive padding and spacing -->
    <div class="relative rounded-xl sm:rounded-2xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 p-4 sm:p-6 transition-colors duration-300">
      <div class="relative">
        <!-- Loading State - Responsive height -->
        <div v-if="loading" class="animate-pulse h-[250px] sm:h-[350px] md:h-[400px] w-full rounded-2xl sm:rounded-3xl bg-slate-200 dark:bg-slate-700 transition-colors duration-300" />

        <!-- YouTube Video Preview - Responsive with lazy loading -->
        <div
          v-else-if="mediaType === 'video' && videoId"
          ref="previewContainer"
          class="relative overflow-hidden rounded-2xl sm:rounded-3xl border border-slate-100/60 shadow-xl sm:shadow-2xl dark:border-slate-800/60 dark:shadow-blue-900/50 transition-all duration-300"
        >
          <!-- Responsive 16:9 aspect ratio container -->
          <div class="relative pb-[56.25%]">
            <iframe
              v-if="shouldLoadIframe"
              :src="youtubeEmbedUrl"
              class="absolute inset-0 h-full w-full"
              allow="autoplay; encrypted-media"
              :allowfullscreen="false"
              :title="t('heroMedia.preview.videoTitle')"
              loading="lazy"
              style="border: none; pointer-events: none;"
              @error="handleVideoError"
            />
            <!-- Placeholder before lazy load -->
            <div
              v-else
              class="absolute inset-0 flex items-center justify-center bg-slate-200 dark:bg-slate-700 transition-colors duration-300"
            >
              <UIcon name="i-lucide-video" class="h-12 w-12 text-slate-400 dark:text-slate-500" />
            </div>
          </div>
          
          <!-- Gradient overlay for text readability (matching landing page) - Enhanced for dark mode -->
          <div 
            class="absolute inset-0 rounded-2xl sm:rounded-3xl bg-gradient-to-tr from-blue-600/10 via-blue-500/5 dark:from-blue-500/20 dark:via-blue-600/10 to-transparent pointer-events-none transition-colors duration-300"
            aria-hidden="true"
          />
        </div>

        <!-- Image Preview - Responsive -->
        <div
          v-else-if="mediaType === 'image' && imageUrl"
          class="relative"
        >
          <img
            :src="imageUrl"
            :alt="t('heroMedia.preview.imageAlt')"
            class="h-full w-full rounded-2xl sm:rounded-3xl border border-slate-100/60 object-cover shadow-xl sm:shadow-2xl dark:border-slate-800/60 dark:shadow-blue-900/50 transition-all duration-300"
            style="max-height: 300px; min-height: 250px;"
            :style="{ 
              maxHeight: 'min(500px, 60vh)',
              minHeight: '250px'
            }"
            @error="handleImageError"
          />
          <!-- Gradient overlay (matching landing page) - Enhanced for dark mode -->
          <div class="absolute inset-0 rounded-2xl sm:rounded-3xl bg-gradient-to-tr from-blue-600/10 via-blue-500/5 dark:from-blue-500/20 dark:via-blue-600/10 to-transparent transition-colors duration-300" />
        </div>

        <!-- Fallback Display (no media configured) - Responsive -->
        <div
          v-else
          class="relative"
        >
          <img
            :src="defaultImageUrl"
            :alt="t('heroMedia.preview.defaultImageAlt')"
            class="h-full w-full rounded-2xl sm:rounded-3xl border border-slate-100/60 object-cover shadow-xl sm:shadow-2xl dark:border-slate-800/60 dark:shadow-blue-900/50 transition-all duration-300"
            :style="{ 
              maxHeight: 'min(500px, 60vh)',
              minHeight: '250px'
            }"
          />
          <!-- Gradient overlay (matching landing page) - Enhanced for dark mode -->
          <div class="absolute inset-0 rounded-2xl sm:rounded-3xl bg-gradient-to-tr from-blue-600/10 via-blue-500/5 dark:from-blue-500/20 dark:via-blue-600/10 to-transparent transition-colors duration-300" />
          
          <!-- Fallback indicator - Responsive -->
          <div class="absolute inset-0 flex items-center justify-center p-4">
            <div class="rounded-lg sm:rounded-xl bg-white/90 dark:bg-slate-900/90 px-4 sm:px-6 py-3 sm:py-4 shadow-lg backdrop-blur-sm transition-colors duration-300 max-w-sm">
              <div class="flex flex-col sm:flex-row items-center gap-2 sm:gap-3">
                <UIcon name="i-lucide-image" class="h-5 w-5 sm:h-6 sm:w-6 text-slate-600 dark:text-slate-400 flex-shrink-0" />
                <div class="text-center sm:text-left">
                  <p class="text-xs sm:text-sm font-semibold text-slate-900 dark:text-white transition-colors">
                    {{ t('heroMedia.preview.fallback.title') }}
                  </p>
                  <p class="text-[10px] sm:text-xs text-slate-600 dark:text-slate-400 transition-colors mt-0.5">
                    {{ t('heroMedia.preview.fallback.description') }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Error State - Responsive -->
        <div
          v-if="hasError"
          class="absolute inset-0 flex items-center justify-center rounded-2xl sm:rounded-3xl bg-red-50/90 dark:bg-red-900/20 backdrop-blur-sm transition-colors duration-300 p-4"
        >
          <div class="text-center max-w-xs">
            <UIcon name="i-lucide-alert-circle" class="mx-auto h-10 w-10 sm:h-12 sm:w-12 text-red-600 dark:text-red-400" />
            <p class="mt-2 sm:mt-3 text-xs sm:text-sm font-semibold text-red-900 dark:text-red-100 transition-colors">
              {{ t('heroMedia.preview.error.title') }}
            </p>
            <p class="mt-1 text-[10px] sm:text-xs text-red-700 dark:text-red-300 transition-colors">
              {{ t('heroMedia.preview.error.description') }}
            </p>
          </div>
        </div>
      </div>

      <!-- Preview Info Badge - Responsive -->
      <div class="mt-3 sm:mt-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 sm:gap-0 rounded-lg sm:rounded-xl border border-blue-200 bg-blue-50 px-3 sm:px-4 py-2 sm:py-3 dark:border-blue-800 dark:bg-blue-900/20 transition-colors duration-300">
        <div class="flex items-center gap-2">
          <UIcon 
            :name="mediaType === 'video' ? 'i-lucide-video' : 'i-lucide-image'" 
            class="h-3.5 w-3.5 sm:h-4 sm:w-4 text-blue-600 dark:text-blue-400 flex-shrink-0" 
          />
          <span class="text-xs sm:text-sm font-medium text-blue-900 dark:text-blue-100 transition-colors">
            {{ mediaType === 'video' ? t('heroMedia.preview.type.video') : t('heroMedia.preview.type.image') }}
          </span>
        </div>
        <UBadge 
          :color="(mediaType === 'video' && videoId) || (mediaType === 'image' && imageUrl) ? 'green' : 'gray'" 
          variant="subtle"
          size="xs"
        >
          {{ (mediaType === 'video' && videoId) || (mediaType === 'image' && imageUrl) 
            ? t('heroMedia.preview.status.configured') 
            : t('heroMedia.preview.status.default') 
          }}
        </UBadge>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { watch, computed, ref, shallowRef } from 'vue';

const props = defineProps<{
  mediaType: 'image' | 'video'
  imageUrl?: string | null
  videoId?: string | null
  loading?: boolean
}>()

const { t } = useI18n()

// Default hero image URL (matching web-next)
const defaultImageUrl = 'https://images.unsplash.com/photo-1606811971618-4486d14f3f99?q=80&w=1200&auto=format&fit=crop'

const hasError = ref(false)
const shouldLoadIframe = ref(false)
const previewContainer = shallowRef<HTMLElement | null>(null)

// Construct YouTube embed URL with optimal parameters for background video
// (matching web-next YouTubeEmbed component)
const youtubeEmbedUrl = computed(() => {
  if (!props.videoId) return ''
  
  return `https://www.youtube.com/embed/${props.videoId}?autoplay=1&mute=1&loop=1&playlist=${props.videoId}&controls=0&showinfo=0&rel=0&modestbranding=1&playsinline=1`
})

const handleVideoError = () => {
  console.error('Failed to load YouTube video preview:', props.videoId)
  hasError.value = true
}

const handleImageError = () => {
  console.error('Failed to load image preview:', props.imageUrl)
  hasError.value = true
}

// Lazy load iframe when it comes into view
const setupIntersectionObserver = () => {
  if (!previewContainer.value || typeof window === 'undefined') return

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && !shouldLoadIframe.value) {
          shouldLoadIframe.value = true
          observer.disconnect()
        }
      })
    },
    {
      rootMargin: '50px',
      threshold: 0.1,
    }
  )

  observer.observe(previewContainer.value)
}

// Reset error state and lazy load when media changes
watch(() => [props.mediaType, props.imageUrl, props.videoId], () => {
  hasError.value = false
  shouldLoadIframe.value = false
  
  // Re-setup observer for video type
  if (props.mediaType === 'video' && props.videoId) {
    nextTick(() => {
      setupIntersectionObserver()
    })
  }
}, { immediate: true })

onMounted(() => {
  if (props.mediaType === 'video' && props.videoId) {
    setupIntersectionObserver()
  }
})
</script>
