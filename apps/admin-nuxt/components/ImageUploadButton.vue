<template>
  <div class="space-y-4">
    <div class="flex gap-3">
      <UInput
        :model-value="imageUrl"
        type="url"
        size="lg"
        :placeholder="placeholder || 'https://example.com/image.jpg'"
        icon="i-lucide-image"
        class="flex-1"
        @update:model-value="$emit('update:imageUrl', $event)"
        @blur="$emit('validate')"
        @input="$emit('clearError')"
      />
      <UButton
        variant="outline"
        color="gray"
        icon="i-lucide-upload"
        :loading="uploading"
        @click="triggerFileUpload"
      >
        Upload
      </UButton>
    </div>
    
    <!-- Hidden file input -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      class="hidden"
      @change="handleFileSelect"
    />
    
    <!-- Upload Progress -->
    <div v-if="uploading" class="space-y-2">
      <div class="flex items-center justify-between text-sm">
        <span class="text-slate-700 dark:text-slate-300">Uploading image...</span>
        <span class="text-slate-500 dark:text-slate-400">{{ uploadProgress }}%</span>
      </div>
      <div class="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-2 overflow-hidden">
        <div
          class="bg-blue-600 dark:bg-blue-500 h-2 transition-all duration-300 ease-out"
          :style="{ width: `${uploadProgress}%` }"
        ></div>
      </div>
    </div>
    
    <!-- Image Preview -->
    <div v-if="imageUrl" class="flex items-center gap-3 p-3 bg-slate-50 dark:bg-slate-700 rounded-xl">
      <div 
        class="border border-slate-200 dark:border-slate-600 rounded-lg overflow-hidden bg-white dark:bg-slate-800 flex items-center justify-center"
        :class="previewSize"
      >
        <img
          :src="imageUrl"
          :alt="altText || 'Preview'"
          class="w-full h-full object-cover"
          @error="$emit('imageError')"
          @load="$emit('imageLoad')"
        >
      </div>
      <div class="flex-1">
        <p class="text-sm font-medium text-slate-900 dark:text-white">{{ previewTitle || 'Image Preview' }}</p>
        <p class="text-xs text-slate-500 dark:text-slate-400">{{ previewDescription || 'Image will appear like this' }}</p>
      </div>
      <UButton
        variant="ghost"
        color="red"
        icon="i-lucide-x"
        size="sm"
        @click="removeImage"
      >
        Remove
      </UButton>
    </div>
  </div>
</template>

<script setup lang="ts">




interface Props {
  imageUrl?: string
  placeholder?: string
  altText?: string
  previewTitle?: string
  previewDescription?: string
  previewSize?: string
}

interface Emits {
  'update:imageUrl': [value: string]
  'validate': []
  'clearError': []
  'imageError': []
  'imageLoad': []
  'uploadSuccess': [response: any]
  'uploadError': [error: string]
}

const props = withDefaults(defineProps<Props>(), {
  imageUrl: '',
  placeholder: 'https://example.com/image.jpg',
  altText: 'Preview',
  previewTitle: 'Image Preview',
  previewDescription: 'Image will appear like this',
  previewSize: 'w-16 h-16'
})

const emit = defineEmits<Emits>()

const toast = useToast()
const fileInput = ref<HTMLInputElement>()

// Image upload functionality
const { uploading, uploadProgress, uploadImage } = useImageUpload()

const triggerFileUpload = () => {
  fileInput.value?.click()
}

const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  try {
    const response = await uploadImage(file)
    console.log('Upload response:', response)
    console.log('Public URL:', response.publicUrl)
    console.log('Emitting update:imageUrl with:', response.publicUrl)
    
    emit('update:imageUrl', response.publicUrl)
    emit('clearError')
    emit('uploadSuccess', response)
    
    toast.add({
      title: "Image uploaded",
      description: "Image has been uploaded successfully",
      color: "green",
      icon: "i-lucide-check-circle"
    })
  } catch (error: any) {
    console.error('Upload error:', error)
    emit('uploadError', error.message)
    toast.add({
      title: "Upload failed",
      description: error.message,
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    // Reset file input
    if (target) target.value = ''
  }
}

const removeImage = () => {
  emit('update:imageUrl', '')
  emit('clearError')
}
</script>