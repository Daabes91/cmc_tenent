<template>
  <UPopover mode="hover" :popper="{ strategy: 'fixed' }">
    <UButton
      color="white"
      variant="solid"
      size="sm"
      class="hidden items-center gap-2 rounded-xl border border-slate-200/70 bg-white/80 px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-600 shadow-sm backdrop-blur dark:border-white/10 dark:bg-white/5 dark:text-slate-200 md:inline-flex"
    >
      <UIcon name="i-lucide-network" class="h-4 w-4 text-primary-500 dark:text-primary-300" />
      <span>{{ t("tenantSwitcher.activeTenant") }}</span>
      <code class="rounded bg-slate-100 px-2 py-1 text-[11px] text-slate-700 dark:bg-white/10 dark:text-white">
        {{ tenantSlug }}
      </code>
    </UButton>

    <template #panel>
      <div class="w-72 space-y-4 rounded-2xl border border-slate-200/70 bg-white/95 p-4 text-sm shadow-xl backdrop-blur dark:border-white/10 dark:bg-slate-900/95">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.24em] text-slate-400 dark:text-slate-500">
            {{ t("tenantSwitcher.title") }}
          </p>
          <p class="mt-1 text-sm text-slate-600 dark:text-slate-300">
            {{ t("tenantSwitcher.description") }}
          </p>
        </div>

        <UFormGroup :label="t('tenantSwitcher.label')" size="xs">
          <UInput
            v-model="draftSlug"
            :placeholder="t('tenantSwitcher.placeholder')"
            icon="i-lucide-hash"
            autofocus
          />
        </UFormGroup>

        <p class="text-xs text-slate-400 dark:text-slate-500">
          {{ t("tenantSwitcher.hint") }}
        </p>

        <div class="flex gap-2">
          <UButton
            block
            color="violet"
            :disabled="!draftSlug.trim().length"
            @click="applyTenant"
          >
            {{ t("tenantSwitcher.apply") }}
          </UButton>
          <UButton
            block
            variant="ghost"
            @click="resetTenant"
          >
            {{ t("tenantSwitcher.reset") }}
          </UButton>
        </div>
      </div>
    </template>
  </UPopover>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";

const { tenantSlug, setTenantSlug } = useTenantSlug();
const { t } = useI18n();
const runtimeConfig = useRuntimeConfig();

const defaultTenant =
  runtimeConfig.public?.defaultTenantSlug?.toLowerCase() || "default";

const draftSlug = ref(tenantSlug.value);

watch(
  () => tenantSlug.value,
  (value) => {
    draftSlug.value = value;
  }
);

const reloadWindow = () => {
  if (process.client) {
    window.location.reload();
  }
};

const applyTenant = () => {
  setTenantSlug(draftSlug.value);
  reloadWindow();
};

const resetTenant = () => {
  draftSlug.value = defaultTenant;
  setTenantSlug(defaultTenant);
  reloadWindow();
};
</script>
