<template>
  <NuxtLoadingIndicator color="#6d28d9" />
  <NuxtLayout>
    <NuxtPage :key="pageKey" />
  </NuxtLayout>
  <ClientOnly>
    <UNotifications />
  </ClientOnly>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { getPageNameFromRoute } from "~/utils/pageNameUtils";

import { createSafeAdminTitle } from "~/utils/titleUtils";
import { 
  createSafeBrandingConfig, 
  logBrandingError, 
  monitorTitleUpdatePerformance,
  monitorClinicSettingsLoading,
  BrandingErrorType 
} from "~/utils/brandingErrorHandler";

const route = useRoute();
const pageKey = computed(() => route.fullPath);
const { locale, locales } = useI18n();

const activeLocale = computed(() => locales.value.find((item: any) => item.code === locale.value));
const direction = computed(() => activeLocale.value?.dir ?? "ltr");

const auth = useAuth();
// Get clinic settings for dynamic branding
const { settings: clinicSettings, refresh: loadClinicSettings, reset: resetClinicSettings } = useClinicSettings({
  immediate: false
});

if (import.meta.server && auth.isAuthenticated.value) {
  try {
    await loadClinicSettings();
  } catch (error) {
    logBrandingError({
      type: BrandingErrorType.API_FAILURE,
      message: "Failed to prefetch clinic settings during SSR",
      originalError: error instanceof Error ? {
        name: error.name,
        message: error.message,
        stack: error.stack
      } : null,
      timestamp: new Date().toISOString()
    });
  }
} else {
  resetClinicSettings();
}

// Page name for dynamic title
const pageName = ref('Dashboard');

// Use comprehensive branding configuration with enhanced error logging
const brandingConfig = computed(() => {
  return createSafeBrandingConfig(
    pageName.value,
    clinicSettings.value,
    logBrandingError
  );
});

// Admin-specific title format with comprehensive error handling
const adminTitle = computed(() => brandingConfig.value.title);



if (import.meta.client) {
  watch(
    () => auth.isAuthenticated.value,
    (isAuthed) => {
      if (isAuthed) {
        loadClinicSettings().catch((error) => {
          logBrandingError({
            type: BrandingErrorType.API_FAILURE,
            message: 'Failed to load clinic settings after authentication',
            originalError: error instanceof Error ? {
              name: error.name,
              message: error.message,
              stack: error.stack
            } : null,
            timestamp: new Date().toISOString()
          });
        });
      } else {
        resetClinicSettings();
      }
    },
    { immediate: true }
  );
}

// Update page name based on route changes with performance monitoring
watch(() => route.path, (newPath: string) => {
  try {
    pageName.value = monitorTitleUpdatePerformance(
      newPath,
      () => getPageNameFromRoute(newPath)
    );
  } catch (error) {
    logBrandingError({
      type: BrandingErrorType.TITLE_UPDATE_FAILED,
      message: `Failed to update page name for route: ${newPath}`,
      originalError: error instanceof Error ? {
        name: error.name,
        message: error.message,
        stack: error.stack
      } : null,
      timestamp: new Date().toISOString()
    });
    pageName.value = 'Dashboard'; // Fallback
  }
}, { immediate: true });

// Initialize clinic timezone on app load with comprehensive error logging
// This ensures all admins see appointment times in the clinic's timezone
const { fetchClinicTimezone } = useClinicTimezone();
onMounted(async () => {
  try {
    await monitorClinicSettingsLoading(
      () => fetchClinicTimezone(),
      0
    );
  } catch (error) {
    logBrandingError({
      type: BrandingErrorType.API_FAILURE,
      message: 'Failed to initialize clinic timezone during app startup',
      originalError: error instanceof Error ? {
        name: error.name,
        message: error.message,
        stack: error.stack
      } : null,
      timestamp: new Date().toISOString()
    });
  }
});

useHead(() => ({
  title: adminTitle.value,
  htmlAttrs: {
    lang: locale.value,
    dir: direction.value
  },
  bodyAttrs: {
    dir: direction.value
  }
}));
</script>
