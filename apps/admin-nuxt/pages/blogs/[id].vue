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
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('blogs.edit.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.edit.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UBreadcrumb
              :links="[
                { label: t('blogs.edit.breadcrumb.blogs'), to: '/blogs' },
                { label: blog ? `${blog.title} (${blog.locale.toUpperCase()})` : t('blogs.edit.breadcrumb.edit') }
              ]"
              class="hidden sm:flex"
            />
            <div v-if="autoSaving" class="flex items-center gap-2 rounded-full bg-blue-100 dark:bg-blue-900/30 px-3 py-1 text-xs text-blue-700 dark:text-blue-300">
              <UIcon name="i-lucide-loader-2" class="h-3 w-3 animate-spin" />
              {{ t('blogs.edit.autoSaving') }}
            </div>
            <UBadge v-if="blog" color="gray" variant="soft" size="md">
              {{ blog.locale.toUpperCase() }}
            </UBadge>
            <UBadge v-if="blog" :color="getStatusColor(blog.status)" variant="soft" size="md">
              {{ blog.status }}
            </UBadge>
            <UButton
              v-if="blog?.status === 'DRAFT'"
              color="green"
              icon="i-lucide-check-circle"
              @click="handlePublish"
              :loading="publishing"
            >
              {{ t('blogs.edit.actions.publish') }}
            </UButton>
            <UButton
              v-else-if="blog?.status === 'PUBLISHED'"
              color="amber"
              icon="i-lucide-archive"
              @click="handleUnpublish"
              :loading="publishing"
            >
              {{ t('blogs.edit.actions.unpublish') }}
            </UButton>
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left"
              @click="router.push('/blogs')"
            >
              {{ t('blogs.edit.actions.backToBlogs') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-5xl mx-auto px-6 py-8">

      <div v-if="loading" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-newspaper" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('blogs.edit.loading.title') }}</h2>
              <p class="text-sm text-blue-100">{{ t('blogs.edit.loading.subtitle') }}</p>
            </div>
          </div>
        </div>
        <div class="p-6">
          <div class="space-y-6">
            <div class="animate-pulse space-y-4">
              <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-1/4"></div>
              <div class="h-12 bg-slate-200 dark:bg-slate-700 rounded-xl"></div>
              <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-1/3"></div>
              <div class="h-32 bg-slate-200 dark:bg-slate-700 rounded-xl"></div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="blog" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
        <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
          <div class="flex items-center gap-3">
            <UIcon name="i-lucide-newspaper" class="h-5 w-5 text-white" />
            <div>
              <h2 class="text-lg font-semibold text-white">{{ t('blogs.edit.sections.main.title') }}</h2>
              <p class="text-sm text-blue-100">{{ blog ? `${blog.title} (${blog.locale.toUpperCase()})` : t('blogs.edit.sections.main.subtitle') }}</p>
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
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.edit.sections.basicInfo.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.edit.sections.basicInfo.subtitle') }}</p>
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
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.edit.sections.seo.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.edit.sections.seo.subtitle') }}</p>
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
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.edit.sections.openGraph.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.edit.sections.openGraph.subtitle') }}</p>
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

          <!-- Statistics -->
          <div v-if="blog" class="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-6 border border-slate-200 dark:border-slate-600">
            <div class="flex items-center gap-3 mb-4">
              <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/20">
                <UIcon name="i-lucide-bar-chart-3" class="h-5 w-5 text-amber-600 dark:text-amber-400" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('blogs.edit.sections.statistics.title') }}</h2>
                <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('blogs.edit.sections.statistics.subtitle') }}</p>
              </div>
            </div>
            <div class="grid gap-4 md:grid-cols-3">
              <div class="bg-white dark:bg-slate-600 rounded-xl border border-slate-200 dark:border-slate-500 p-4">
                <div class="flex items-center gap-2 mb-2">
                  <UIcon name="i-lucide-eye" class="h-4 w-4 text-slate-400" />
                  <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('blogs.edit.statistics.totalViews') }}</p>
                </div>
                <p class="text-2xl font-bold text-slate-900 dark:text-white">{{ blog.viewCount }}</p>
              </div>
              <div class="bg-white dark:bg-slate-600 rounded-xl border border-slate-200 dark:border-slate-500 p-4">
                <div class="flex items-center gap-2 mb-2">
                  <UIcon name="i-lucide-calendar-plus" class="h-4 w-4 text-slate-400" />
                  <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('blogs.edit.statistics.created') }}</p>
                </div>
                <p class="text-sm font-medium text-slate-900 dark:text-white">{{ formatDateTime(blog.createdAt) }}</p>
              </div>
              <div class="bg-white dark:bg-slate-600 rounded-xl border border-slate-200 dark:border-slate-500 p-4">
                <div class="flex items-center gap-2 mb-2">
                  <UIcon name="i-lucide-calendar" class="h-4 w-4 text-slate-400" />
                  <p class="text-xs text-slate-500 dark:text-slate-400">{{ t('blogs.edit.statistics.lastUpdated') }}</p>
                </div>
                <p class="text-sm font-medium text-slate-900 dark:text-white">{{ formatDateTime(blog.updatedAt) }}</p>
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-600">
            <UButton type="button" variant="ghost" color="gray" :disabled="saving" @click="router.push('/blogs')">
              {{ t('blogs.edit.actions.cancel') }}
            </UButton>
            <UButton type="submit" color="blue" :loading="saving" icon="i-lucide-save">
              {{ t('blogs.edit.actions.save') }}
            </UButton>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from "vue";
import type { Blog } from "@/types/blogs";

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const toast = useEnhancedToast();
const blogApi = useBlogs();

const id = computed(() => Number(route.params.id));

const languages = [
  { code: 'en', name: 'English' },
  { code: 'ar', name: 'Arabic' }
];

const { data: blog, pending: loading, refresh } = await useAsyncData(
  `blog-${id.value}`,
  () => blogApi.getBlogById(id.value)
);

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
const publishing = ref(false);

watch(
  blog,
  (newBlog) => {
    if (newBlog) {
      form.title = newBlog.title;
      form.slug = newBlog.slug;
      form.excerpt = newBlog.excerpt || "";
      form.content = newBlog.content;
      form.featuredImage = newBlog.featuredImage || "";
      form.metaTitle = newBlog.metaTitle || "";
      form.metaDescription = newBlog.metaDescription || "";
      form.metaKeywords = newBlog.metaKeywords || "";
      form.ogTitle = newBlog.ogTitle || "";
      form.ogDescription = newBlog.ogDescription || "";
      form.ogImage = newBlog.ogImage || "";
      form.locale = newBlog.locale;
    }
  },
  { immediate: true }
);

// Use clinic timezone for all date/time displays
// CRITICAL: All admins see times in clinic timezone, not their browser timezone
const { timezone, abbreviation } = useClinicTimezone();

const formatDateTime = (date: string | number | null | undefined) => {
  if (!date) return "—";
  // Use clinic timezone formatter with abbreviation (e.g., "Jan 15, 2024, 2:00 PM EET")
  return formatDateTimeInClinicTimezone(date, timezone.value, abbreviation.value);
}

function getStatusColor(status: string) {
  switch (status) {
    case 'PUBLISHED':
      return 'green';
    case 'DRAFT':
      return 'amber';
    case 'ARCHIVED':
      return 'gray';
    default:
      return 'gray';
  }
}

async function autoSaveBlog() {
  if (autoSaving.value) return;
  
  autoSaving.value = true;
  
  try {
    const payload = {
      title: form.title.trim(),
      slug: form.slug.trim(),
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

    await blogApi.updateBlog(id.value, payload);
    await refresh();
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
  await autoSaveBlog();
  toast.success({
    title: t('blogs.create.toasts.imageRemoveSuccess'),
    description: t('blogs.create.toasts.imageRemoveSuccessDesc')
  });
}

async function removeOgImage() {
  form.ogImage = '';
  await autoSaveBlog();
  toast.success({
    title: t('blogs.create.toasts.ogImageRemoveSuccess'),
    description: t('blogs.create.toasts.ogImageRemoveSuccessDesc')
  });
}

async function handleSave() {
  saving.value = true;
  const payload = {
    title: form.title.trim(),
    slug: form.slug.trim(),
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
      throw new Error("Title is required");
    }
    if (!payload.content) {
      throw new Error("Content is required");
    }

    await blogApi.updateBlog(id.value, payload);
    toast.success({ title: t('blogs.edit.toasts.updateSuccess') });
    await refresh();
  } catch (error: any) {
    toast.error({
      title: t('blogs.edit.toasts.updateError'),
      error
    });
  } finally {
    saving.value = false;
  }
}

async function handlePublish() {
  publishing.value = true;
  try {
    await blogApi.publishBlog(id.value);
    toast.success({ title: t('blogs.edit.toasts.publishSuccess') });
    await refresh();
  } catch (error: any) {
    toast.error({
      title: t('blogs.edit.toasts.publishError'),
      error
    });
  } finally {
    publishing.value = false;
  }
}

async function handleUnpublish() {
  publishing.value = true;
  try {
    await blogApi.unpublishBlog(id.value);
    toast.success({ title: t('blogs.edit.toasts.unpublishSuccess') });
    await refresh();
  } catch (error: any) {
    toast.error({
      title: t('blogs.edit.toasts.unpublishError'),
      error
    });
  } finally {
    publishing.value = false;
  }
}
</script>
