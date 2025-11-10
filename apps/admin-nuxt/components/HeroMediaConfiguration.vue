<template>
  <div class="space-y-4 sm:space-y-6">
    <!-- Header - Responsive -->
    <div class="flex flex-col sm:flex-row items-start sm:items-center gap-2 sm:gap-3">
      <UIcon name="i-lucide-layout-template" class="h-5 w-5 sm:h-6 sm:w-6 text-violet-600 dark:text-violet-400 flex-shrink-0" />
      <div>
        <h2 class="text-lg sm:text-xl font-bold text-slate-900 dark:text-white transition-colors">{{ t('heroMedia.config.title') }}</h2>
        <p class="text-xs sm:text-sm text-slate-600 dark:text-slate-400 transition-colors mt-0.5">{{ t('heroMedia.config.description') }}</p>
      </div>
    </div>

    <!-- Media Type Selector - Responsive -->
    <UFormGroup :label="t('heroMedia.config.mediaType.label')" :help="t('heroMedia.config.mediaType.hint')">
      <URadioGroup
        v-model="localMediaType"
        :options="mediaTypeOptions"
        :ui="{
          wrapper: 'flex flex-col sm:flex-row gap-3 sm:gap-4',
          fieldset: 'flex flex-col sm:flex-row gap-3 sm:gap-4'
        }"
      >
        <template #label="{ option }">
          <div class="flex items-center gap-2">
            <UIcon :name="option.icon" class="h-4 w-4 sm:h-5 sm:w-5" />
            <span class="text-sm sm:text-base">{{ option.label }}</span>
          </div>
        </template>
      </URadioGroup>
    </UFormGroup>

    <!-- Image Upload Section - Responsive -->
    <div v-if="localMediaType === 'image'" class="space-y-3 sm:space-y-4">
      <div class="rounded-lg sm:rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-4 sm:p-6 transition-colors duration-300">
        <div class="mb-3 sm:mb-4">
          <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white transition-colors">{{ t('heroMedia.config.image.label') }}</h3>
          <p class="text-xs sm:text-sm text-slate-600 dark:text-slate-400 transition-colors mt-1">{{ t('heroMedia.config.image.hint') }}</p>
        </div>
        
        <!-- Loading overlay during upload - Responsive -->
        <div v-if="imageUploading" class="relative">
          <div class="absolute inset-0 bg-white/80 dark:bg-slate-800/80 backdrop-blur-sm rounded-lg z-10 flex items-center justify-center transition-colors duration-300">
            <div class="text-center space-y-2 sm:space-y-3 px-4">
              <UIcon name="i-lucide-loader-2" class="h-6 w-6 sm:h-8 sm:w-8 text-violet-600 dark:text-violet-400 animate-spin mx-auto" />
              <p class="text-xs sm:text-sm font-medium text-slate-700 dark:text-slate-300 transition-colors">{{ t('heroMedia.config.image.uploading') }}</p>
            </div>
          </div>
        </div>
        
        <ImageUpload
          v-model="localImageUrl"
          :alt-text="t('heroMedia.config.image.altText')"
          :disabled="imageUploading"
          @upload-success="handleImageUploadSuccess"
          @upload-error="handleImageUploadError"
        />
      </div>
    </div>

    <!-- YouTube URL Input Section - Responsive -->
    <div v-else-if="localMediaType === 'video'" class="space-y-3 sm:space-y-4">
      <div class="rounded-lg sm:rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-4 sm:p-6 transition-colors duration-300">
        <div class="mb-3 sm:mb-4">
          <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white transition-colors">{{ t('heroMedia.config.video.label') }}</h3>
          <p class="text-xs sm:text-sm text-slate-600 dark:text-slate-400 transition-colors mt-1">{{ t('heroMedia.config.video.hint') }}</p>
        </div>

        <UFormGroup
          :label="t('heroMedia.config.video.label')"
          :help="t('heroMedia.config.video.hint')"
          :error="youtubeUrlError"
        >
          <UInput
            v-model="youtubeUrl"
            :placeholder="t('heroMedia.config.video.placeholder')"
            icon="i-lucide-video"
            :size="{ xs: 'md', sm: 'lg' }"
            :disabled="validatingYoutubeUrl"
            :ui="{ icon: { trailing: { pointer: '' } } }"
            @blur="validateYoutubeUrl"
            @input="clearYoutubeError"
          >
            <template #trailing>
              <UIcon
                v-if="validatingYoutubeUrl"
                name="i-lucide-loader-2"
                class="h-4 w-4 sm:h-5 sm:w-5 text-violet-500 animate-spin"
              />
              <UIcon
                v-else-if="youtubeUrl && !youtubeUrlError && localVideoId"
                name="i-lucide-check-circle"
                class="h-4 w-4 sm:h-5 sm:w-5 text-green-500"
              />
              <UIcon
                v-else-if="youtubeUrlError"
                name="i-lucide-alert-circle"
                class="h-4 w-4 sm:h-5 sm:w-5 text-red-500"
              />
            </template>
          </UInput>
        </UFormGroup>

        <!-- YouTube URL Format Examples - Responsive -->
        <div class="mt-3 rounded-lg bg-blue-50 dark:bg-blue-900/20 p-3 sm:p-4 transition-colors duration-300">
          <p class="text-[10px] sm:text-xs font-medium text-blue-900 dark:text-blue-100 mb-1.5 sm:mb-2 transition-colors">
            {{ t('heroMedia.config.video.examples.title') }}
          </p>
          <ul class="space-y-1 text-[10px] sm:text-xs text-blue-700 dark:text-blue-300 transition-colors">
            <li class="flex items-start gap-1.5 sm:gap-2">
              <UIcon name="i-lucide-check" class="h-2.5 w-2.5 sm:h-3 sm:w-3 mt-0.5 flex-shrink-0" />
              <code class="break-all text-[9px] sm:text-[10px]">https://www.youtube.com/watch?v=dQw4w9WgXcQ</code>
            </li>
            <li class="flex items-start gap-1.5 sm:gap-2">
              <UIcon name="i-lucide-check" class="h-2.5 w-2.5 sm:h-3 sm:w-3 mt-0.5 flex-shrink-0" />
              <code class="break-all text-[9px] sm:text-[10px]">https://youtu.be/dQw4w9WgXcQ</code>
            </li>
            <li class="flex items-start gap-1.5 sm:gap-2">
              <UIcon name="i-lucide-check" class="h-2.5 w-2.5 sm:h-3 sm:w-3 mt-0.5 flex-shrink-0" />
              <code class="break-all text-[9px] sm:text-[10px]">https://www.youtube.com/embed/dQw4w9WgXcQ</code>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Clear/Reset Button - Responsive -->
    <div v-if="hasConfiguredMedia" class="flex justify-end">
      <UButton
        color="red"
        variant="soft"
        icon="i-lucide-trash-2"
        :size="{ xs: 'sm', sm: 'md' }"
        @click="clearMedia"
      >
        <span class="hidden sm:inline">{{ t('heroMedia.config.actions.clear') }}</span>
        <span class="sm:hidden">{{ t('heroMedia.config.actions.clearShort') || t('heroMedia.config.actions.clear') }}</span>
      </UButton>
    </div>

    <!-- Preview Section -->
    <HeroMediaPreview
      :media-type="localMediaType"
      :image-url="localImageUrl"
      :video-id="localVideoId"
      :loading="previewLoading"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

interface HeroMediaConfig {
  mediaType: 'image' | 'video'
  imageUrl?: string | null
  videoId?: string | null
}

const props = defineProps<{
  modelValue: HeroMediaConfig
}>()

const emit = defineEmits<{
  'update:modelValue': [value: HeroMediaConfig]
}>()

const { t } = useI18n()
const toast = useToast()

// Local state
const localMediaType = ref<'image' | 'video'>(props.modelValue.mediaType || 'image')
const localImageUrl = ref<string | null>(props.modelValue.imageUrl || null)
const localVideoId = ref<string | null>(props.modelValue.videoId || null)
const youtubeUrl = ref<string>('')
const youtubeUrlError = ref<string | null>(null)
const previewLoading = ref(false)
const imageUploading = ref(false)
const validatingYoutubeUrl = ref(false)

// Media type options for radio group
const mediaTypeOptions = computed(() => [
  {
    value: 'image',
    label: t('heroMedia.config.mediaType.options.image'),
    icon: 'i-lucide-image'
  },
  {
    value: 'video',
    label: t('heroMedia.config.mediaType.options.video'),
    icon: 'i-lucide-video'
  }
])

// Check if media is configured
const hasConfiguredMedia = computed(() => {
  return (localMediaType.value === 'image' && localImageUrl.value) ||
         (localMediaType.value === 'video' && localVideoId.value)
})

// YouTube URL validation regex
const YOUTUBE_REGEX = /^(?:https?:\/\/)?(?:www\.)?(?:youtube\.com\/(?:watch\?v=|embed\/)|youtu\.be\/)([a-zA-Z0-9_-]{11})(?:\?.*)?$/

/**
 * Validates YouTube URL and extracts video ID
 */
const validateYoutubeUrl = async () => {
  if (!youtubeUrl.value.trim()) {
    youtubeUrlError.value = null
    localVideoId.value = null
    emitUpdate()
    return
  }

  validatingYoutubeUrl.value = true
  
  try {
    const match = youtubeUrl.value.trim().match(YOUTUBE_REGEX)
    
    if (!match || !match[1]) {
      youtubeUrlError.value = t('heroMedia.config.video.errors.invalidUrl')
      localVideoId.value = null
      
      // Show error toast for invalid URL
      toast.add({
        title: t('heroMedia.toasts.invalidYoutubeUrl.title'),
        description: t('heroMedia.toasts.invalidYoutubeUrl.description'),
        color: 'red',
        icon: 'i-lucide-alert-circle',
        timeout: 5000
      })
      
      emitUpdate()
      return
    }

    // Valid URL - extract video ID
    youtubeUrlError.value = null
    localVideoId.value = match[1]
    
    // Show success toast
    toast.add({
      title: t('heroMedia.toasts.youtubeUrlValid.title'),
      description: t('heroMedia.toasts.youtubeUrlValid.description'),
      color: 'green',
      icon: 'i-lucide-check-circle',
      timeout: 3000
    })
    
    emitUpdate()
  } catch (error) {
    console.error('YouTube URL validation error:', error)
    youtubeUrlError.value = t('heroMedia.config.video.errors.validationFailed')
    localVideoId.value = null
    emitUpdate()
  } finally {
    validatingYoutubeUrl.value = false
  }
}

/**
 * Clears YouTube URL error when user types
 */
const clearYoutubeError = () => {
  if (youtubeUrlError.value) {
    youtubeUrlError.value = null
  }
}

/**
 * Handles successful image upload
 */
const handleImageUploadSuccess = (data: { imageId: string; filename: string; publicUrl: string; variants: Record<string, string> }) => {
  console.log('Hero image upload success:', data)
  imageUploading.value = false
  localImageUrl.value = data.publicUrl
  
  // Show success toast
  toast.add({
    title: t('heroMedia.toasts.imageUploadSuccess.title'),
    description: t('heroMedia.toasts.imageUploadSuccess.description'),
    color: 'green',
    icon: 'i-lucide-check-circle',
    timeout: 3000
  })
  
  emitUpdate()
}

/**
 * Handles image upload error
 */
const handleImageUploadError = (error: string) => {
  console.error('Hero image upload error:', error)
  imageUploading.value = false
  
  // Show error toast with retry option
  toast.add({
    title: t('heroMedia.toasts.imageUploadError.title'),
    description: error || t('heroMedia.toasts.imageUploadError.description'),
    color: 'red',
    icon: 'i-lucide-alert-circle',
    timeout: 8000
  })
}

/**
 * Handles image upload start
 */
const handleImageUploadStart = () => {
  imageUploading.value = true
}

/**
 * Clears all media configuration
 */
const clearMedia = () => {
  localImageUrl.value = null
  localVideoId.value = null
  youtubeUrl.value = ''
  youtubeUrlError.value = null
  
  // Show confirmation toast
  toast.add({
    title: t('heroMedia.toasts.mediaCleared.title'),
    description: t('heroMedia.toasts.mediaCleared.description'),
    color: 'blue',
    icon: 'i-lucide-info',
    timeout: 3000
  })
  
  emitUpdate()
}

/**
 * Emits updated configuration to parent
 */
const emitUpdate = () => {
  emit('update:modelValue', {
    mediaType: localMediaType.value,
    imageUrl: localMediaType.value === 'image' ? localImageUrl.value : null,
    videoId: localMediaType.value === 'video' ? localVideoId.value : null
  })
}

// Watch for media type changes
watch(localMediaType, (newType) => {
  // Clear the other media type when switching
  if (newType === 'image') {
    localVideoId.value = null
    youtubeUrl.value = ''
    youtubeUrlError.value = null
  } else if (newType === 'video') {
    localImageUrl.value = null
  }
  emitUpdate()
})

// Watch for image URL changes
watch(localImageUrl, () => {
  if (localMediaType.value === 'image') {
    emitUpdate()
  }
})

// Initialize YouTube URL if video ID is provided
watch(() => props.modelValue, (newValue) => {
  if (newValue.mediaType === 'video' && newValue.videoId && !youtubeUrl.value) {
    // Reconstruct YouTube URL from video ID
    youtubeUrl.value = `https://www.youtube.com/watch?v=${newValue.videoId}`
    localVideoId.value = newValue.videoId
  }
}, { immediate: true })
</script>
