<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4 py-12">
    <div class="max-w-md w-full">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 rounded-full bg-red-100 dark:bg-red-900/20 flex items-center justify-center">
                <UIcon 
                  name="i-heroicons-x-circle-20-solid" 
                  class="w-6 h-6 text-red-600 dark:text-red-400"
                />
              </div>
            </div>
            <div>
              <h1 class="text-xl font-bold text-gray-900 dark:text-white">
                {{ $t('billing.canceled.title') }}
              </h1>
            </div>
          </div>
        </template>

        <div class="space-y-4">
          <p class="text-gray-600 dark:text-gray-400">
            {{ $t('billing.canceled.description') }}
          </p>

          <div class="bg-red-50 dark:bg-red-900/10 border border-red-200 dark:border-red-800 rounded-lg p-4">
            <p class="text-sm text-red-900 dark:text-red-200 mb-2">
              {{ $t('billing.canceled.instructions') }}
            </p>
          </div>

          <div class="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
            <div class="flex items-start gap-2">
              <UIcon 
                name="i-heroicons-information-circle-20-solid" 
                class="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5"
              />
              <p class="text-sm text-blue-900 dark:text-blue-200">
                {{ $t('billing.canceled.dataRetention') }}
              </p>
            </div>
          </div>

          <!-- Action buttons -->
          <div class="flex flex-col gap-3 pt-4">
            <UButton
              :label="$t('billing.canceled.action')"
              color="red"
              size="lg"
              block
              icon="i-heroicons-arrow-path-20-solid"
              @click="handleReactivate"
              :loading="isProcessing"
            />

            <UButton
              :label="$t('common.logout')"
              color="gray"
              variant="ghost"
              size="lg"
              block
              @click="handleLogout"
            />
          </div>

          <!-- Help text -->
          <div class="pt-4 border-t border-gray-200 dark:border-gray-800">
            <p class="text-sm text-gray-500 dark:text-gray-400 text-center">
              {{ $t('billing.needHelp') }}
              <a 
                href="mailto:support@example.com" 
                class="text-primary-600 hover:text-primary-700 dark:text-primary-400 dark:hover:text-primary-300"
              >
                {{ $t('billing.contactSupport') }}
              </a>
            </p>
          </div>
        </div>
      </UCard>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: false,
  auth: true,
  billing: false
});

const { t } = useI18n();
const auth = useAuth();
const billing = useBillingStatus();
const {
  resumePlan,
  fetchPlanDetails
} = useBillingPlan();
const toast = useToast();

const isProcessing = ref(false);

async function handleReactivate() {
  isProcessing.value = true;
  
  try {
    await resumePlan();
    await fetchPlanDetails(true);
    await billing.refreshBillingStatus();

    if (billing.isActive.value) {
      toast.add({
        title: t('billing.reactivatedTitle'),
        description: t('billing.reactivatedMessage'),
        color: 'green'
      });
      await navigateTo('/billing');
      return;
    }

    toast.add({
      title: t('billing.reactivationRequired'),
      description: t('billing.contactSupportForReactivation'),
      color: 'red',
      timeout: 5000
    });
  } catch (error) {
    console.error('Error checking billing status:', error);
    toast.add({
      title: t('common.error'),
      description: t('billing.errorCheckingStatus'),
      color: 'red',
      timeout: 5000
    });
  } finally {
    isProcessing.value = false;
  }
}

async function handleLogout() {
  await auth.logout();
  await navigateTo('/login');
}
</script>
