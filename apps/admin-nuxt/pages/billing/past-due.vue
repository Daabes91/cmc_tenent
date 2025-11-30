<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4 py-12">
    <div class="max-w-md w-full">
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 rounded-full bg-orange-100 dark:bg-orange-900/20 flex items-center justify-center">
                <UIcon 
                  name="i-heroicons-exclamation-triangle-20-solid" 
                  class="w-6 h-6 text-orange-600 dark:text-orange-400"
                />
              </div>
            </div>
            <div>
              <h1 class="text-xl font-bold text-gray-900 dark:text-white">
                {{ $t('billing.pastDue.title') }}
              </h1>
            </div>
          </div>
        </template>

        <div class="space-y-4">
          <p class="text-gray-600 dark:text-gray-400">
            {{ $t('billing.pastDue.description') }}
          </p>

          <div class="bg-orange-50 dark:bg-orange-900/10 border border-orange-200 dark:border-orange-800 rounded-lg p-4">
            <p class="text-sm text-orange-900 dark:text-orange-200 mb-2">
              {{ $t('billing.pastDue.instructions') }}
            </p>
            <p class="text-sm font-semibold text-orange-900 dark:text-orange-200">
              {{ $t('billing.pastDue.warning') }}
            </p>
          </div>

          <!-- Action buttons -->
          <div class="flex flex-col gap-3 pt-4">
            <UButton
              :label="$t('billing.pastDue.action')"
              color="orange"
              size="lg"
              block
              icon="i-heroicons-credit-card-20-solid"
              @click="handleUpdatePayment"
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
  auth: false,
  billing: false
});

const { t } = useI18n();
const auth = useAuth();
const billing = useBillingStatus();

const isProcessing = ref(false);

async function handleUpdatePayment() {
  isProcessing.value = true;
  
  try {
    // Refresh billing status to check if payment was updated
    await billing.refreshBillingStatus();
    
    if (billing.isActive.value) {
      // Payment updated, redirect to dashboard
      await navigateTo('/');
    } else {
      // Payment still past due, redirect to PayPal or show instructions
      // For now, we'll show a toast message
      // In a real implementation, this would redirect to PayPal subscription management page
      const toast = useToast();
      toast.add({
        title: t('billing.updateRequired'),
        description: t('billing.contactSupportForPaymentUpdate'),
        color: 'orange',
        timeout: 5000
      });
    }
  } catch (error) {
    console.error('Error checking billing status:', error);
    const toast = useToast();
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
