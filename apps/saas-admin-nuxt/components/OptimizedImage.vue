<template>
  <picture v-if="!error">
    <!-- WebP format for modern browsers -->
    <source
      v-if="webpSrc"
      :srcset="webpSrc"
      type="image/webp"
    />
    
    <!-- Fallback to original format -->
    <img
      ref="imageRef"
      :src="currentSrc"
      :alt="alt"
      :class="imageClass"
      :loading="loading"
      :width="width"
      :height="height"
      @load="onLoad"
      @error="onError"
    />
  </picture>
  
  <!-- Error fallback -->
  <div
    v-else
    :class="['flex items-center justify-center bg-gray-100', imageClass]"
    :style="{ width: width ? `${width}px` : '100%', height: height ? `${height}px` : 'auto' }"
  >
    <UIcon name="i-heroicons-photo" class="text-gray-400 text-4xl" />
  </div>
</template>

<script setup lang="ts">
interface Props {
  src: string
  alt: string
  width?: number
  height?: number
  loading?: 'lazy' | 'eager'
  class?: string
  webpSrc?: string // Optional WebP version of the image
}

const props = withDefaults(defineProps<Props>(), {
  loading: 'lazy',
  class: ''
})

const imageRef = ref<HTMLImageElement | null>(null)
const isLoaded = ref(false)
const error = ref(false)
const currentSrc = ref(props.src)

const imageClass = computed(() => {
  const classes = [props.class]
  
  // Add transition class when loading
  if (!isLoaded.value) {
    classes.push('opacity-0 transition-opacity duration-300')
  } else {
    classes.push('opacity-100 transition-opacity duration-300')
  }
  
  return classes.join(' ')
})

const onLoad = () => {
  isLoaded.value = true
}

const onError = () => {
  error.value = true
  console.error(`Failed to load image: ${props.src}`)
}

// Update src when prop changes
watch(() => props.src, (newSrc) => {
  currentSrc.value = newSrc
  isLoaded.value = false
  error.value = false
})
</script>
