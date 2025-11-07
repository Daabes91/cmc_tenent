<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-newspaper" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('blogs.create.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.create.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UBreadcrumb
              :links="[
                { label: t('blogs.create.breadcrumb.blogs'), to: '/blogs' },
                { label: t('blogs.create.breadcrumb.newPost') }
              ]"
              class="hidden sm:flex"
            />
            <div v-if="autoSaving" class="flex items-center gap-2 rounded-full bg-blue-100 dark:bg-blue-900/30 px-3 py-1 text-xs text-blue-700 dark:text-blue-300">
              <UIcon name="i-lucide-loader-2" class="h-3 w-3 animate-spin" />
              {{ t('blogs.create.autoSaving') }}
            </div>
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left"
              @click="router.push('/blogs')"
            >
              {{ t('blogs.create.actions.cancel') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-5xl mx-auto px-6 py-8">

      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-newspaper" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('blogs.create.sections.main.title') }}</h2>
              <p class="text-sm text-blue-100">{{ t('blogs.create.sections.main.subtitle') }}</p>
            </div>
          </div>
        </div>
        <form class="p-6 space-y-8" @submit.prevent="handleSave">
          <!-- Basic Information -->
          <div class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600">
            <div class="flex items-center gap-3 mb-4">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 dark:bg-blue-900/20">
                <UIcon name="i-lucide-edit" class="h-5 w-5 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.create.sections.basicInfo.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.create.sections.basicInfo.subtitle') }}</p>
              </div>
            </div>
            <div class="space-y-5">
            <UFormGroup :label="t('blogs.create.fields.title.label')" required>
              <UInput v-model="form.title" :placeholder="t('blogs.create.fields.title.placeholder')" size="lg" />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.slug.label')" :hint="t('blogs.create.fields.slug.hint')" required>
              <UInput v-model="form.slug" :placeholder="t('blogs.create.fields.slug.placeholder')" size="lg" />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.language.label')" required>
              <USelect
                v-model="form.locale"
                :options="languages"
                option-attribute="name"
                value-attribute="code"
                size="lg"
              />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.excerpt.label')" :hint="t('blogs.create.fields.excerpt.hint')">
              <UTextarea
                v-model="form.excerpt"
                :rows="3"
                size="lg"
                :placeholder="t('blogs.create.fields.excerpt.placeholder')"
              />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.content.label')" required>
              <RichTextEditor v-model="form.content" />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.featuredImage.label')" :hint="t('blogs.create.fields.featuredImage.hint')">
              <ImageUpload
                v-model="form.featuredImage"
                alt-text="Blog featured image"
                :disabled="saving || autoSaving"
                @upload-success="handleFeaturedImageUpload"
                @upload-error="handleFeaturedImageError"
              />
              <!-- Featured Image Preview -->
              <div v-if="form.featuredImage" class="mt-3 rounded-lg border border-slate-200 bg-slate-50 p-3">
                <div class="flex items-center gap-3">
                  <img 
                    :src="form.featuredImage" 
                    alt="Featured image preview" 
                    class="h-16 w-16 rounded-lg object-cover shadow-sm"
                  />
                  <div class="flex-1">
                    <p class="text-sm font-medium text-slate-900">{{ t('blogs.create.fields.featuredImage.preview.title') }}</p>
                    <p class="text-xs text-slate-500">{{ t('blogs.create.fields.featuredImage.preview.description') }}</p>
                  </div>
                  <UButton 
                    size="xs" 
                    variant="ghost" 
                    color="red" 
                    icon="i-lucide-x"
                    @click="removeFeaturedImage"
                  >
                    {{ t('blogs.create.fields.featuredImage.preview.remove') }}
                  </UButton>
                </div>
              </div>
            </UFormGroup>
            </div>
          </div>

          <!-- SEO Settings -->
          <div class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600">
            <div class="flex items-center gap-3 mb-4">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-green-50 dark:bg-green-900/20">
                <UIcon name="i-lucide-search" class="h-5 w-5 text-green-600 dark:text-green-400" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.create.sections.seo.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.create.sections.seo.subtitle') }}</p>
              </div>
            </div>
            <div class="space-y-5">
            <UFormGroup :label="t('blogs.create.fields.metaTitle.label')" :hint="t('blogs.create.fields.metaTitle.hint')">
              <UInput v-model="form.metaTitle" :placeholder="t('blogs.create.fields.metaTitle.placeholder')" size="lg" maxlength="70" />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.metaDescription.label')" :hint="t('blogs.create.fields.metaDescription.hint')">
              <UTextarea
                v-model="form.metaDescription"
                :rows="3"
                size="lg"
                :placeholder="t('blogs.create.fields.metaDescription.placeholder')"
                maxlength="160"
              />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.metaKeywords.label')" :hint="t('blogs.create.fields.metaKeywords.hint')">
              <UInput v-model="form.metaKeywords" :placeholder="t('blogs.create.fields.metaKeywords.placeholder')" size="lg" />
            </UFormGroup>
            </div>
          </div>

          <!-- Open Graph -->
          <div class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600">
            <div class="flex items-center gap-3 mb-4">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 dark:bg-purple-900/20">
                <UIcon name="i-lucide-share-2" class="h-5 w-5 text-purple-600 dark:text-purple-400" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.create.sections.openGraph.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.create.sections.openGraph.subtitle') }}</p>
              </div>
            </div>
            <div class="space-y-5">
            <UFormGroup :label="t('blogs.create.fields.ogTitle.label')" :hint="t('blogs.create.fields.ogTitle.hint')">
              <UInput v-model="form.ogTitle" :placeholder="t('blogs.create.fields.ogTitle.placeholder')" size="lg" maxlength="95" />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.ogDescription.label')" :hint="t('blogs.create.fields.ogDescription.hint')">
              <UTextarea
                v-model="form.ogDescription"
                :rows="3"
                size="lg"
                :placeholder="t('blogs.create.fields.ogDescription.placeholder')"
                maxlength="200"
              />
            </UFormGroup>

            <UFormGroup :label="t('blogs.create.fields.ogImage.label')" :hint="t('blogs.create.fields.ogImage.hint')">
              <ImageUpload
                v-model="form.ogImage"
                alt-text="Blog OG image"
                :disabled="saving || autoSaving"
                @upload-success="handleOgImageUpload"
                @upload-error="handleOgImageError"
              />
              <!-- OG Image Preview -->
              <div v-if="form.ogImage" class="mt-3 rounded-lg border border-slate-200 bg-slate-50 p-3">
                <div class="flex items-center gap-3">
                  <img 
                    :src="form.ogImage" 
                    alt="OG image preview" 
                    class="h-16 w-16 rounded-lg object-cover shadow-sm"
                  />
                  <div class="flex-1">
                    <p class="text-sm font-medium text-slate-900">{{ t('blogs.create.fields.ogImage.preview.title') }}</p>
                    <p class="text-xs text-slate-500">{{ t('blogs.create.fields.ogImage.preview.description') }}</p>
                  </div>
                  <UButton 
                    size="xs" 
                    variant="ghost" 
                    color="red" 
                    icon="i-lucide-x"
                    @click="removeOgImage"
                  >
                    {{ t('blogs.create.fields.ogImage.preview.remove') }}
                  </UButton>
                </div>
              </div>
            </UFormGroup>
            </div>
          </div>

          <div class="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-600">
            <UButton type="button" variant="ghost" color="gray" :disabled="saving" @click="router.push('/blogs')">
              {{ t('blogs.create.actions.cancel') }}
            </UButton>
            <UButton type="submit" color="blue" :loading="saving" icon="i-lucide-plus">
              {{ t('blogs.create.actions.create') }}
            </UButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from "vue";

const { t } = useI18n();
const toast = useEnhancedToast();
const router = useRouter();
const blogApi = useBlogs();

const languages = [
  { code: 'en', name: 'English' },
  { code: 'ar', name: 'Arabic' }
];

const form = reactive({
  title: "",
  slug: "",
  excerpt: "",
  content: "",
  featuredImage: "",
  metaTitle: "",
  metaDescription: "",
  metaKeywords: "",
  ogTitle: "",
  ogDescription: "",
  ogImage: "",
  locale: "en"
});

const saving = ref(false);
const autoSaving = ref(false);
const slugManuallyEdited = ref(false);
const blogId = ref<number | null>(null);

watch(
  () => form.title,
  value => {
    if (!slugManuallyEdited.value) {
      form.slug = slugify(value ?? "");
    }
  },
  { immediate: true }
);

watch(
  () => form.slug,
  value => {
    slugManuallyEdited.value = value.trim() !== slugify(form.title);
  }
);

function slugify(input: string): string {
  return (input ?? "")
    .toString()
    .normalize("NFKD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, "")
    .trim()
    .replace(/\s+/g, "-")
    .replace(/-+/g, "-");
}

async function autoSaveBlog() {
  if (autoSaving.value) return;
  
  autoSaving.value = true;
  
  try {
    const payload = {
      title: form.title.trim() || "Untitled Blog Post",
      slug: form.slug.trim() || slugify(form.title || "untitled-blog-post"),
      excerpt: form.excerpt.trim() || undefined,
      content: form.content.trim() || "Draft content",
      featuredImage: form.featuredImage.trim() || undefined,
      metaTitle: form.metaTitle.trim() || undefined,
      metaDescription: form.metaDescription.trim() || undefined,
      metaKeywords: form.metaKeywords.trim() || undefined,
      ogTitle: form.ogTitle.trim() || undefined,
      ogDescription: form.ogDescription.trim() || undefined,
      ogImage: form.ogImage.trim() || undefined,
      locale: form.locale
    };

    if (blogId.value) {
      // Update existing blog
      await blogApi.updateBlog(blogId.value, payload);
    } else {
      // Create new blog as draft
      const newBlog = await blogApi.createBlog(payload);
      blogId.value = newBlog.id;
      
      // Update the URL to the edit page
      await router.replace(`/blogs/${newBlog.id}`);
    }
  } catch (error: any) {
    console.error('Auto-save failed:', error);
    toast.error({
      title: t('blogs.create.toasts.autoSaveError'),
      description: t('blogs.create.toasts.autoSaveErrorDesc')
    });
  } finally {
    autoSaving.value = false;
  }
}

async function handleFeaturedImageUpload(data: { imageId: string; filename: string; publicUrl: string; variants: Record<string, string> }) {
  form.featuredImage = data.publicUrl;
  
  // Auto-save the blog post
  await autoSaveBlog();
  
  toast.success({
    title: t('blogs.create.toasts.imageUploadSuccess'),
    description: t('blogs.create.toasts.imageUploadSuccessDesc')
  });
}

function handleFeaturedImageError(error: string) {
  toast.error({
    title: t('blogs.create.toasts.imageUploadError'),
    description: error
  });
}

async function handleOgImageUpload(data: { imageId: string; filename: string; publicUrl: string; variants: Record<string, string> }) {
  form.ogImage = data.publicUrl;
  
  // Auto-save the blog post
  await autoSaveBlog();
  
  toast.success({
    title: t('blogs.create.toasts.ogImageUploadSuccess'),
    description: t('blogs.create.toasts.ogImageUploadSuccessDesc')
  });
}

function handleOgImageError(error: string) {
  toast.error({
    title: t('blogs.create.toasts.ogImageUploadError'),
    description: error
  });
}

async function removeFeaturedImage() {
  form.featuredImage = '';
  if (blogId.value) {
    await autoSaveBlog();
    toast.success({
      title: t('blogs.create.toasts.imageRemoveSuccess'),
      description: t('blogs.create.toasts.imageRemoveSuccessDesc')
    });
  }
}

async function removeOgImage() {
  form.ogImage = '';
  if (blogId.value) {
    await autoSaveBlog();
    toast.success({
      title: t('blogs.create.toasts.ogImageRemoveSuccess'), 
      description: t('blogs.create.toasts.ogImageRemoveSuccessDesc')
    });
  }
}

async function handleSave() {
  saving.value = true;
  const payload = {
    title: form.title.trim(),
    slug: form.slug.trim() || slugify(form.title),
    excerpt: form.excerpt.trim() || undefined,
    content: form.content.trim(),
    featuredImage: form.featuredImage.trim() || undefined,
    metaTitle: form.metaTitle.trim() || undefined,
    metaDescription: form.metaDescription.trim() || undefined,
    metaKeywords: form.metaKeywords.trim() || undefined,
    ogTitle: form.ogTitle.trim() || undefined,
    ogDescription: form.ogDescription.trim() || undefined,
    ogImage: form.ogImage.trim() || undefined,
    locale: form.locale
  };

  try {
    if (!payload.title) {
      throw new Error(t('blogs.create.toasts.titleRequired'));
    }
    if (!payload.content) {
      throw new Error(t('blogs.create.toasts.contentRequired'));
    }

    await blogApi.createBlog(payload);
    toast.success({ title: t('blogs.create.toasts.createSuccess') });
    router.push("/blogs");
  } catch (error: any) {
    toast.error({
      title: t('blogs.create.toasts.createError'),
      error
    });
  } finally {
    saving.value = false;
  }
}
</script>
