<template>
  <div class="space-y-4">
    <!-- Current Image Preview -->
    <div v-if="currentImageUrl" class="relative inline-block">
      <img
        :src="currentImageUrl"
        :alt="altText"
        class="max-w-full h-auto max-h-64 rounded-lg shadow-md object-cover"
      />
      <UButton
        v-if="!disabled"
        icon="i-lucide-x"
        color="red"
        size="xs"
        class="absolute top-2 right-2"
        @click="removeImage"
      >
        {{ t('imageUpload.remove') }}
      </UButton>
    </div>

    <!-- Upload Section -->
    <div v-if="!currentImageUrl || showUploadArea" class="space-y-3">
      <div
        class="relative border-2 border-dashed rounded-lg p-8 text-center transition-colors"
        :class="{
          'border-violet-400 bg-violet-50 dark:bg-violet-950/20': isDragging,
          'border-gray-300 dark:border-gray-600 hover:border-violet-400 dark:hover:border-violet-400': !isDragging && !disabled,
          'border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 opacity-50 cursor-not-allowed': disabled
        }"
        @dragover.prevent="handleDragOver"
        @dragleave.prevent="handleDragLeave"
        @drop.prevent="handleDrop"
      >
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="hidden"
          :disabled="disabled"
          @change="handleFileSelect"
        />

        <div class="space-y-3">
          <UIcon
            name="i-lucide-image"
            class="mx-auto h-12 w-12 text-gray-400 dark:text-gray-500"
          />

          <div>
            <p class="text-sm font-medium text-gray-700 dark:text-gray-300">
              {{ isDragging ? t('imageUpload.dropHere') : t('imageUpload.dragAndDrop') }}
            </p>
            <UButton
              v-if="!isDragging"
              color="violet"
              variant="soft"
              size="sm"
              class="mt-2"
              :disabled="disabled"
              @click="triggerFileSelect"
            >
              {{ t('imageUpload.chooseFile') }}
            </UButton>
          </div>

          <p class="text-xs text-gray-500 dark:text-gray-400">
            {{ t('imageUpload.supportedFormats') }}
          </p>
        </div>
      </div>

      <!-- Upload Progress -->
      <div v-if="uploading" class="space-y-2">
        <div class="flex items-center justify-between text-sm">
          <span class="text-gray-700 dark:text-gray-300">{{ t('imageUpload.uploading') }}</span>
          <span class="text-gray-500 dark:text-gray-400">{{ uploadProgress }}%</span>
        </div>
        <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2 overflow-hidden">
          <div
            class="bg-violet-600 dark:bg-violet-500 h-2 transition-all duration-300 ease-out"
            :style="{ width: `${uploadProgress}%` }"
          ></div>
        </div>
      </div>

      <!-- Error Message -->
      <UAlert
        v-if="errorMessage"
        color="red"
        variant="soft"
        icon="i-lucide-alert-circle"
        :title="errorMessage"
        :close-button="{ icon: 'i-lucide-x', color: 'red', variant: 'link' }"
        @close="errorMessage = null"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const props = defineProps<{
  modelValue?: string | null
  altText?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
  'upload-success': [data: { imageId: string; filename: string; publicUrl: string; variants: Record<string, string> }]
  'upload-error': [error: string]
}>()

const { request } = useAdminApi()
const { t } = useI18n()

const fileInput = ref<HTMLInputElement>()
const isDragging = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const errorMessage = ref<string | null>(null)
const showUploadArea = ref(false)

const currentImageUrl = computed(() => props.modelValue)

const triggerFileSelect = () => {
  fileInput.value?.click()
}

const handleDragOver = (e: DragEvent) => {
  if (props.disabled) return
  isDragging.value = true
}

const handleDragLeave = (e: DragEvent) => {
  isDragging.value = false
}

const handleDrop = (e: DragEvent) => {
  isDragging.value = false
  if (props.disabled) return

  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    uploadFile(files[0])
  }
}

const handleFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  const files = target.files
  if (files && files.length > 0) {
    uploadFile(files[0])
  }
}

const uploadFile = async (file: File) => {
  // Validate file type
  if (!file.type.startsWith('image/')) {
    errorMessage.value = t('imageUpload.errors.invalidType')
    return
  }

  // Validate file size (10MB)
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    errorMessage.value = t('imageUpload.errors.fileTooLarge')
    return
  }

  errorMessage.value = null
  uploading.value = true
  uploadProgress.value = 0

  try {
    console.log('Starting upload for file:', file.name, 'size:', file.size)
    
    const formData = new FormData()
    formData.append('file', file)

    // Simulate progress (since we don't have real upload progress)
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 200)

    console.log('Making request to /images/upload')
    const response = await request<{ 
      imageId: string; 
      filename: string; 
      publicUrl: string;
      variants: Record<string, string>
    }>('/images/upload', {
      method: 'POST',
      body: formData
    })
    
    console.log('Upload response:', response)

    clearInterval(progressInterval)
    uploadProgress.value = 100

    emit('update:modelValue', response.publicUrl)
    emit('upload-success', {
      imageId: response.imageId,
      filename: response.filename,
      publicUrl: response.publicUrl,
      variants: response.variants
    })
    showUploadArea.value = false

    setTimeout(() => {
      uploading.value = false
      uploadProgress.value = 0
    }, 500)
  } catch (error: any) {
    console.error('Upload error:', error)
    errorMessage.value = error.data?.error?.message || error.message || t('imageUpload.errors.uploadFailed')
    emit('upload-error', errorMessage.value)
    uploading.value = false
    uploadProgress.value = 0
  }
}

const removeImage = () => {
  emit('update:modelValue', null)
  showUploadArea.value = true
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}
</script>
