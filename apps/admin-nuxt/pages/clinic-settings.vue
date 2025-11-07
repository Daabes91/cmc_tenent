<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-violet-500 to-purple-600 shadow-lg">
              <UIcon name="i-lucide-settings" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('clinicSettings.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">{{ t('clinicSettings.header.subtitle') }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-refresh-cw" 
              :loading="loading"
              @click="refresh()"
            >
              {{ t('clinicSettings.actions.refresh') }}
            </UButton>
            <UButton 
              color="violet" 
              icon="i-lucide-save" 
              :loading="saving"
              @click="saveSettings"
            >
              {{ t('clinicSettings.actions.save') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div
          v-for="metric in overviewMetrics"
          :key="metric.label"
          class="bg-white dark:bg-slate-800 rounded-2xl p-6 shadow-sm border border-slate-200/60 dark:border-slate-700/60 hover:shadow-md transition-all duration-200"
        >
          <div class="flex items-center gap-3 mb-3">
            <div :class="['flex h-10 w-10 items-center justify-center rounded-xl', metric.iconBg]">
              <UIcon :name="metric.icon" :class="['h-5 w-5', metric.iconColor]" />
            </div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-500 dark:text-slate-400 uppercase tracking-wide">{{ metric.label }}</p>
              <p class="text-lg font-semibold text-slate-900 dark:text-white">{{ metric.value }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 dark:text-slate-300">{{ metric.description }}</p>
        </div>
      </div>

      <!-- Error Alert -->
      <UAlert 
        v-if="loadErrorMessage" 
        color="red" 
        icon="i-lucide-alert-triangle" 
        class="mb-6 rounded-2xl border border-red-200 bg-red-50"
      >
        <template #title>{{ t('clinicSettings.errors.loadTitle') }}</template>
        <template #description>{{ loadErrorMessage }}</template>
      </UAlert>

      <!-- Settings Form -->
      <div v-else class="grid gap-8 lg:grid-cols-3">
        <!-- Main Settings (2/3 width) -->
        <div class="lg:col-span-2 space-y-6">
          <form @submit.prevent="saveSettings" class="space-y-6">
            <!-- Clinic Identity Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-violet-500 to-purple-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-building-2" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.identity.title') }}</h2>
                    <p class="text-sm text-violet-100">{{ t('clinicSettings.sections.identity.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6">

                <div v-if="loading" class="space-y-4">
                  <USkeleton class="h-12 rounded-xl" />
                  <USkeleton class="h-32 rounded-xl" />
                  <USkeleton class="h-12 rounded-xl" />
                  <USkeleton class="h-24 rounded-xl" />
                </div>

                <div v-else class="space-y-6">
                  <div class="grid gap-6 md:grid-cols-2">
                    <div class="md:col-span-1">
                      <UFormGroup :label="t('clinicSettings.form.clinicName.label')" required>
                        <UInput 
                          v-model="formData.clinicName" 
                          size="lg" 
                          :placeholder="t('clinicSettings.form.clinicName.placeholder')" 
                          icon="i-lucide-building-2"
                        />
                      </UFormGroup>
                    </div>
                    <div class="md:col-span-1">
                      <UFormGroup :label="t('clinicSettings.form.virtualFee.label')" required :hint="t('clinicSettings.form.virtualFee.hint')">
                        <UInput 
                          v-model="formData.virtualConsultationFee" 
                          type="number" 
                          step="0.01" 
                          min="0" 
                          size="lg" 
                          :placeholder="t('clinicSettings.form.virtualFee.placeholder')" 
                          icon="i-lucide-badge-dollar-sign"
                        />
                      </UFormGroup>
                    </div>
                    <div class="md:col-span-1">
                      <UFormGroup :label="t('clinicSettings.form.virtualMeetingLink.label')" :hint="t('clinicSettings.form.virtualMeetingLink.hint')">
                        <UInput
                          v-model="formData.virtualConsultationMeetingLink"
                          type="url"
                          size="lg"
                          :placeholder="t('clinicSettings.form.virtualMeetingLink.placeholder')"
                          icon="i-lucide-link"
                        />
                      </UFormGroup>
                    </div>
                  </div>
                  <UFormGroup :label="t('clinicSettings.form.slotDuration.label')" :hint="t('clinicSettings.form.slotDuration.hint')" required>
                    <UInput 
                      v-model="formData.slotDurationMinutes" 
                      type="number" 
                      min="5" 
                      max="240" 
                      step="5" 
                      size="lg" 
                      icon="i-lucide-timer"
                    />
                  </UFormGroup>

                  <UFormGroup :label="t('clinicSettings.form.logo.label')" :hint="t('clinicSettings.form.logo.hint')">
                    <ImageUpload
                      v-model="formData.logoUrl"
                      :alt-text="t('clinicSettings.form.logo.altText')"
                      :disabled="saving"
                      @upload-success="handleLogoUpload"
                      @upload-error="handleLogoError"
                    />
                  </UFormGroup>

                  <div class="grid gap-4 md:grid-cols-2">
                    <UFormGroup :label="t('clinicSettings.form.address.label')">
                      <UInput 
                        v-model="formData.address" 
                        size="lg" 
                        :placeholder="t('clinicSettings.form.address.placeholder')" 
                        icon="i-lucide-map-pin"
                      />
                    </UFormGroup>
                    <UFormGroup :label="t('clinicSettings.form.city.label')">
                      <UInput 
                        v-model="formData.city" 
                        size="lg" 
                        :placeholder="t('clinicSettings.form.city.placeholder')"
                      />
                    </UFormGroup>
                    <UFormGroup :label="t('clinicSettings.form.state.label')">
                      <UInput 
                        v-model="formData.state" 
                        size="lg" 
                        :placeholder="t('clinicSettings.form.state.placeholder')"
                      />
                    </UFormGroup>
                    <UFormGroup :label="t('clinicSettings.form.zipCode.label')">
                      <UInput 
                        v-model="formData.zipCode" 
                        size="lg" 
                        :placeholder="t('clinicSettings.form.zipCode.placeholder')"
                      />
                    </UFormGroup>
                  </div>

                  <UFormGroup :label="t('clinicSettings.form.country.label')">
                    <UInput 
                      v-model="formData.country" 
                      size="lg" 
                      :placeholder="t('clinicSettings.form.country.placeholder')"
                    />
                  </UFormGroup>

                  <UFormGroup :label="t('clinicSettings.form.currency.label')" :hint="t('clinicSettings.form.currency.hint')">
                    <USelect
                      v-model="formData.currency"
                      :options="currencyOptions"
                      size="lg"
                      value-attribute="value"
                      label-attribute="label"
                    >
                      <option
                        v-for="option in currencyOptions"
                        :key="option.value"
                        :value="option.value"
                      >
                        {{ option.symbol }} {{ option.label }}
                      </option>
                    </USelect>
                  </UFormGroup>

                  <!-- Multi-Currency Information -->
                  <UAlert
                    color="blue"
                    icon="i-lucide-info"
                    class="rounded-xl border border-blue-200 bg-blue-50 dark:bg-blue-900/20 dark:border-blue-800"
                  >
                    <template #title>Multi-Currency Payment Information</template>
                    <template #description>
                      <div class="space-y-2 text-sm">
                        <p>
                          <span class="font-semibold">PayPal payments</span> are always processed in <span class="font-semibold">USD</span>, regardless of your selected clinic currency.
                        </p>
                        <p>
                          <span class="font-semibold">Other payments</span> (cash, card, insurance) use your selected clinic currency: <span class="font-semibold">{{ formData.currency || 'USD' }}</span>.
                        </p>
                        <p class="text-blue-700 dark:text-blue-300">
                          Revenue reports will show separate totals for each currency to ensure accurate financial tracking.
                        </p>
                      </div>
                    </template>
                  </UAlert>

                  <!-- Exchange Rates Section -->
                  <div class="space-y-3">
                    <div class="flex items-center gap-2">
                      <UIcon name="i-lucide-arrow-left-right" class="h-5 w-5 text-violet-600 dark:text-violet-400" />
                      <h3 class="text-lg font-semibold text-slate-900 dark:text-white">Exchange Rates</h3>
                    </div>
                    <p class="text-sm text-slate-600 dark:text-slate-400">
                      Manage exchange rates for currency conversion. All rates are relative to USD. For example, if 1 USD = 0.709 JOD, enter 0.709 for JOD.
                    </p>

                    <div class="grid gap-3 md:grid-cols-2 lg:grid-cols-3">
                      <div
                        v-for="option in currencyOptions"
                        :key="option.value"
                        class="flex items-center gap-3 rounded-xl border p-4 transition-all"
                        :class="option.value === 'USD'
                          ? 'border-amber-200 bg-amber-50 dark:bg-amber-900/20 dark:border-amber-800'
                          : 'border-slate-200 bg-slate-50 dark:bg-slate-700/50 dark:border-slate-600 hover:border-violet-300 dark:hover:border-violet-600'"
                      >
                        <div class="flex-1">
                          <div class="flex items-center gap-2 mb-2">
                            <span class="text-lg font-bold text-slate-900 dark:text-white">{{ option.symbol }}</span>
                            <span class="text-sm font-medium text-slate-700 dark:text-slate-300">{{ option.value }}</span>
                            <UBadge v-if="option.value === 'USD'" color="amber" variant="solid" size="xs">BASE</UBadge>
                          </div>
                          <UInput
                            v-if="option.value === 'USD'"
                            :model-value="1.0"
                            type="number"
                            step="0.001"
                            min="0"
                            size="sm"
                            disabled
                            class="font-mono"
                          />
                          <UInput
                            v-else
                            v-model.number="formData.exchangeRates[option.value]"
                            type="number"
                            step="0.001"
                            min="0"
                            size="sm"
                            :placeholder="`Rate to USD`"
                            class="font-mono"
                          />
                        </div>
                      </div>
                    </div>

                    <UAlert
                      color="amber"
                      icon="i-lucide-alert-triangle"
                      class="rounded-xl border border-amber-200 bg-amber-50 dark:bg-amber-900/20 dark:border-amber-800"
                    >
                      <template #title>Important: Exchange Rate Formula</template>
                      <template #description>
                        <div class="space-y-1 text-sm">
                          <p>Enter the rate <span class="font-semibold">TO USD</span>. For example:</p>
                          <ul class="list-disc list-inside space-y-0.5 text-amber-700 dark:text-amber-300">
                            <li>If 1 USD = 0.709 JOD, enter <span class="font-mono font-semibold">0.709</span> for JOD</li>
                            <li>If 1 USD = 3.67 AED, enter <span class="font-mono font-semibold">0.272</span> for AED (1/3.67)</li>
                            <li>If 1 USD = 0.85 EUR, enter <span class="font-mono font-semibold">1.176</span> for EUR (1/0.85)</li>
                          </ul>
                        </div>
                      </template>
                    </UAlert>
                  </div>
                </div>
              </div>
            </div>

            <!-- Contact Information Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-phone" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.contact.title') }}</h2>
                    <p class="text-sm text-emerald-100">{{ t('clinicSettings.sections.contact.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6">
                <div v-if="loading" class="space-y-3">
                  <USkeleton v-for="i in 2" :key="i" class="h-12 rounded-xl" />
                </div>
                <div v-else class="grid gap-4 md:grid-cols-2">
                  <UFormGroup :label="t('clinicSettings.form.phone.label')">
                    <UInput 
                      v-model="formData.phone" 
                      size="lg" 
                      :placeholder="t('clinicSettings.form.phone.placeholder')" 
                      icon="i-lucide-phone"
                    />
                  </UFormGroup>
                  <UFormGroup :label="t('clinicSettings.form.email.label')">
                    <UInput 
                      v-model="formData.email" 
                      type="email" 
                      size="lg" 
                      :placeholder="t('clinicSettings.form.email.placeholder')" 
                      icon="i-lucide-mail"
                    />
                  </UFormGroup>
                </div>
              </div>
            </div>

            <!-- Social Media Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-share-2" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.social.title') }}</h2>
                    <p class="text-sm text-blue-100">{{ t('clinicSettings.sections.social.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6">
                <div v-if="loading" class="space-y-3">
                  <USkeleton v-for="i in 4" :key="i" class="h-12 rounded-xl" />
                </div>
                <div v-else class="grid gap-4 md:grid-cols-2">
                  <UFormGroup :label="t('clinicSettings.form.social.facebook')">
                    <UInput 
                      v-model="formData.socialMedia.facebook" 
                      size="lg" 
                      :placeholder="t('clinicSettings.form.social.facebookPlaceholder')" 
                      icon="i-lucide-facebook"
                    />
                  </UFormGroup>
                  <UFormGroup :label="t('clinicSettings.form.social.instagram')">
                    <UInput 
                      v-model="formData.socialMedia.instagram" 
                      size="lg" 
                      :placeholder="t('clinicSettings.form.social.instagramPlaceholder')" 
                      icon="i-lucide-instagram"
                    />
                  </UFormGroup>
                  <UFormGroup :label="t('clinicSettings.form.social.twitter')">
                    <UInput 
                      v-model="formData.socialMedia.twitter" 
                      size="lg" 
                      :placeholder="t('clinicSettings.form.social.twitterPlaceholder')" 
                      icon="i-lucide-twitter"
                    />
                  </UFormGroup>
                  <UFormGroup :label="t('clinicSettings.form.social.linkedin')">
                    <UInput 
                      v-model="formData.socialMedia.linkedin" 
                      size="lg" 
                      :placeholder="t('clinicSettings.form.social.linkedinPlaceholder')" 
                      icon="i-lucide-linkedin"
                    />
                  </UFormGroup>
                </div>
              </div>
            </div>

            <!-- Payment Settings Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-amber-500 to-orange-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-credit-card" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.payment.title') }}</h2>
                    <p class="text-sm text-amber-100">{{ t('clinicSettings.sections.payment.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6">
                <div v-if="loading" class="space-y-4">
                  <USkeleton class="h-12 rounded-xl" />
                </div>
                <div v-else class="space-y-6">
                  <div class="bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 border border-blue-200 dark:border-blue-800 rounded-xl p-4">
                    <div class="flex items-start gap-3">
                      <UIcon name="i-lucide-info" class="h-5 w-5 text-blue-600 dark:text-blue-400 mt-0.5 flex-shrink-0" />
                      <div>
                        <h4 class="font-semibold text-blue-900 dark:text-blue-100 mb-2">{{ t('clinicSettings.paypal.title') }}</h4>
                        <p class="text-sm text-blue-700 dark:text-blue-300 mb-3">
                          {{ t('clinicSettings.paypal.description') }}
                        </p>
                        <div class="space-y-2 text-xs text-blue-600 dark:text-blue-400 mb-3">
                          <p>{{ t('clinicSettings.paypal.hintPrimary') }}</p>
                          <p>{{ t('clinicSettings.paypal.hintSecondary') }}</p>
                        </div>
                        <div class="flex items-center gap-2">
                          <span class="text-xs text-blue-600 dark:text-blue-400">{{ t('clinicSettings.paypal.currentEnvironment') }}:</span>
                          <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium"
                                :class="paypalEnvironment === 'live' ? 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-300' : 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-300'">
                            {{ paypalEnvironment || t('clinicSettings.paypal.notConfigured') }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="grid gap-4 md:grid-cols-2">
                    <UFormGroup
                      class="md:col-span-2"
                      :label="t('clinicSettings.form.paypal.environment.label')"
                      :hint="t('clinicSettings.form.paypal.environment.hint')"
                    >
                      <USelect
                        v-model="formData.paypalEnvironment"
                        :options="paypalEnvironmentOptions"
                        size="lg"
                        value-attribute="value"
                        label-attribute="label"
                      />
                    </UFormGroup>

                    <UFormGroup
                      :label="t('clinicSettings.form.paypal.clientId.label')"
                      :hint="t('clinicSettings.form.paypal.clientId.hint')"
                    >
                      <UInput
                        v-model="formData.paypalClientId"
                        size="lg"
                        :placeholder="t('clinicSettings.form.paypal.clientId.placeholder')"
                        icon="i-lucide-badge-check"
                      />
                    </UFormGroup>

                    <UFormGroup
                      :label="t('clinicSettings.form.paypal.clientSecret.label')"
                      :hint="t('clinicSettings.form.paypal.clientSecret.hint')"
                    >
                      <UInput
                        v-model="formData.paypalClientSecret"
                        type="password"
                        size="lg"
                        :placeholder="t('clinicSettings.form.paypal.clientSecret.placeholder')"
                        icon="i-lucide-lock"
                      />
                    </UFormGroup>
                  </div>

                  <div v-if="formData.paypalClientId && formData.paypalClientSecret" class="flex items-center gap-2 text-sm text-green-600 dark:text-green-400 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-xl p-3">
                    <UIcon name="i-lucide-check-circle-2" class="h-4 w-4" />
                    <span>{{ t('clinicSettings.paypal.configured') }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Cloudflare Images Settings Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-cyan-500 to-blue-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-cloud-upload" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.cloudflare.title') }}</h2>
                    <p class="text-sm text-cyan-100">{{ t('clinicSettings.sections.cloudflare.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6">
                <div v-if="loading" class="space-y-4">
                  <USkeleton class="h-12 rounded-xl" />
                  <USkeleton class="h-12 rounded-xl" />
                </div>
                <div v-else class="space-y-6">
                  <div class="bg-gradient-to-r from-cyan-50 to-blue-50 dark:from-cyan-900/20 dark:to-blue-900/20 border border-cyan-200 dark:border-cyan-800 rounded-xl p-4">
                    <div class="flex items-start gap-3">
                      <UIcon name="i-lucide-info" class="h-5 w-5 text-cyan-600 dark:text-cyan-400 mt-0.5 flex-shrink-0" />
                      <div>
                        <h4 class="font-semibold text-cyan-900 dark:text-cyan-100 mb-2">{{ t('clinicSettings.cloudflare.title') }}</h4>
                        <p class="text-sm text-cyan-700 dark:text-cyan-300 mb-3">
                          {{ t('clinicSettings.cloudflare.description') }}
                        </p>
                        <p class="text-xs text-cyan-600 dark:text-cyan-400">
                          {{ t('clinicSettings.cloudflare.hint') }}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div class="grid gap-4 md:grid-cols-1">
                    <UFormGroup
                      :label="t('clinicSettings.form.cloudflare.accountId.label')"
                      :hint="t('clinicSettings.form.cloudflare.accountId.hint')"
                    >
                      <UInput
                        v-model="formData.cloudflareAccountId"
                        size="lg"
                        :placeholder="t('clinicSettings.form.cloudflare.accountId.placeholder')"
                        icon="i-lucide-key"
                      />
                    </UFormGroup>

                    <UFormGroup
                      :label="t('clinicSettings.form.cloudflare.apiToken.label')"
                      :hint="t('clinicSettings.form.cloudflare.apiToken.hint')"
                    >
                      <UInput
                        v-model="formData.cloudflareApiToken"
                        type="password"
                        size="lg"
                        :placeholder="t('clinicSettings.form.cloudflare.apiToken.placeholder')"
                        icon="i-lucide-lock"
                      />
                    </UFormGroup>
                  </div>

                  <div v-if="formData.cloudflareAccountId && formData.cloudflareApiToken" class="flex items-center gap-2 text-sm text-green-600 dark:text-green-400 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-xl p-3">
                    <UIcon name="i-lucide-check-circle-2" class="h-4 w-4" />
                    <span>{{ t('clinicSettings.cloudflare.configured') }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Operating Hours Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-indigo-500 to-purple-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-clock" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.hours.title') }}</h2>
                    <p class="text-sm text-indigo-100">{{ t('clinicSettings.sections.hours.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6">
                <div v-if="loading" class="space-y-3">
                  <USkeleton v-for="i in 7" :key="i" class="h-12 rounded-xl" />
                </div>
                <div v-else class="space-y-3">
                  <div
                    v-for="day in weekDays"
                    :key="day.key"
                    class="flex flex-col gap-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-700/50 p-4 transition-all hover:border-indigo-300 dark:hover:border-indigo-600 hover:bg-indigo-50/30 dark:hover:bg-indigo-900/20 md:flex-row md:items-center"
                  >
                    <div class="flex items-center gap-3 text-sm font-medium text-slate-700 dark:text-slate-300 md:w-32">
                      <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-white dark:bg-slate-600 shadow-sm">
                        <UIcon name="i-lucide-calendar" class="h-4 w-4 text-indigo-600 dark:text-indigo-400" />
                      </div>
                      <span>{{ day.label }}</span>
                    </div>
                    <UInput
                      v-model="formData.workingHours[day.key]"
                      size="lg"
                      class="md:flex-1"
                      :placeholder="t('clinicSettings.form.hours.placeholder')"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Form Actions -->
            <div class="flex flex-wrap justify-end gap-3 pt-4">
              <UButton 
                type="button" 
                variant="ghost" 
                color="gray" 
                :disabled="saving" 
                @click="resetForm"
              >
                {{ t('clinicSettings.actions.reset') }}
              </UButton>
              <UButton 
                type="submit" 
                color="violet" 
                :loading="saving" 
                icon="i-lucide-check"
              >
                {{ t('clinicSettings.actions.saveSettings') }}
              </UButton>
            </div>
          </form>
        </div>

        <!-- Sidebar (1/3 width) -->
        <div class="lg:col-span-1 space-y-6">
          <!-- Quick Preview -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-slate-600 to-slate-700 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-eye" class="h-5 w-5 text-white" />
                <div>
                  <h3 class="text-lg font-semibold text-white">{{ t('clinicSettings.preview.title') }}</h3>
                  <p class="text-sm text-slate-300">{{ t('clinicSettings.preview.subtitle') }}</p>
                </div>
              </div>
            </div>
            <div class="p-6 space-y-6">
              <!-- Logo Preview -->
              <div v-if="formData.logoUrl" class="text-center">
                <img 
                  :src="formData.logoUrl" 
                  alt="Clinic logo" 
                  class="mx-auto h-16 w-auto rounded-xl object-contain shadow-md border border-slate-200 dark:border-slate-600"
                />
                <p class="text-xs text-slate-500 dark:text-slate-400 mt-2">{{ t('clinicSettings.preview.logoLabel') }}</p>
              </div>
              
              <!-- Clinic Name -->
              <div v-if="formData.clinicName" class="text-center">
                <h4 class="text-xl font-bold text-slate-900 dark:text-white">{{ formData.clinicName }}</h4>
              </div>

              <!-- Contact Info -->
              <div class="space-y-3">
                <div v-if="formData.phone" class="flex items-center gap-3 p-3 bg-emerald-50 dark:bg-emerald-900/20 rounded-xl">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-100 dark:bg-emerald-800">
                    <UIcon name="i-lucide-phone" class="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
                  </div>
                  <span class="text-sm text-slate-700 dark:text-slate-300">{{ formData.phone }}</span>
                </div>
                
                <div v-if="formData.email" class="flex items-center gap-3 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-xl">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-100 dark:bg-blue-800">
                    <UIcon name="i-lucide-mail" class="h-4 w-4 text-blue-600 dark:text-blue-400" />
                  </div>
                  <span class="text-sm text-slate-700 dark:text-slate-300">{{ formData.email }}</span>
                </div>

                <div v-if="primaryAddress !== 'Address not provided'" class="flex items-start gap-3 p-3 bg-violet-50 dark:bg-violet-900/20 rounded-xl">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-violet-100 dark:bg-violet-800 mt-0.5">
                    <UIcon name="i-lucide-map-pin" class="h-4 w-4 text-violet-600 dark:text-violet-400" />
                  </div>
                  <span class="text-sm text-slate-700 dark:text-slate-300">{{ primaryAddress }}</span>
                </div>

                <div v-if="formData.virtualConsultationFee" class="flex items-center gap-3 p-3 bg-amber-50 dark:bg-amber-900/20 rounded-xl">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-100 dark:bg-amber-800">
                    <UIcon name="i-lucide-badge-dollar-sign" class="h-4 w-4 text-amber-600 dark:text-amber-400" />
                  </div>
                  <span class="text-sm text-slate-700 dark:text-slate-300">{{ t('clinicSettings.preview.virtualConsultation') }}: {{ selectedCurrencyOption.symbol }}{{ formData.virtualConsultationFee }}</span>
                </div>
                <div v-if="formData.virtualConsultationMeetingLink" class="flex items-center gap-3 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-xl">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-100 dark:bg-blue-800">
                    <UIcon name="i-lucide-link" class="h-4 w-4 text-blue-600 dark:text-blue-400" />
                  </div>
                  <span class="text-sm text-slate-700 dark:text-slate-300 truncate">
                    {{ t('clinicSettings.preview.virtualMeetingLink') }}:
                    <a :href="formData.virtualConsultationMeetingLink" target="_blank" rel="noopener" class="text-blue-600 dark:text-blue-300 underline">
                      {{ formData.virtualConsultationMeetingLink }}
                    </a>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Operating Hours Summary -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-indigo-500 to-purple-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-calendar-clock" class="h-5 w-5 text-white" />
                <div>
                  <h3 class="text-lg font-semibold text-white">{{ t('clinicSettings.schedule.title') }}</h3>
                  <p class="text-sm text-indigo-100">{{ t('clinicSettings.schedule.subtitle') }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">
              <div class="space-y-2">
                <div
                  v-for="day in weekDays"
                  :key="day.key"
                  class="flex items-center justify-between p-3 rounded-xl transition-colors"
                  :class="formData.workingHours[day.key] && formData.workingHours[day.key].toLowerCase() !== 'closed' 
                    ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800' 
                    : 'bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700'"
                >
                  <span class="text-sm font-medium text-slate-700 dark:text-slate-300">{{ day.label }}</span>
                  <span class="text-xs text-slate-600 dark:text-slate-400">
                    {{ formData.workingHours[day.key] || t('clinicSettings.schedule.closed') }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Social Media Links -->
          <div v-if="activeSocialLinks > 0" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-share-2" class="h-5 w-5 text-white" />
                <div>
                  <h3 class="text-lg font-semibold text-white">{{ t('clinicSettings.socialLinks.title') }}</h3>
                  <p class="text-sm text-blue-100">{{ t('clinicSettings.socialLinks.connected', { count: activeSocialLinks }) }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">
              <div class="space-y-2">
                <div v-if="formData.socialMedia.facebook" class="flex items-center gap-3 p-2 rounded-lg bg-blue-50 dark:bg-blue-900/20">
                  <UIcon name="i-lucide-facebook" class="h-4 w-4 text-blue-600 dark:text-blue-400" />
                  <span class="text-xs text-slate-600 dark:text-slate-300 truncate">Facebook</span>
                </div>
                <div v-if="formData.socialMedia.instagram" class="flex items-center gap-3 p-2 rounded-lg bg-pink-50 dark:bg-pink-900/20">
                  <UIcon name="i-lucide-instagram" class="h-4 w-4 text-pink-600 dark:text-pink-400" />
                  <span class="text-xs text-slate-600 dark:text-slate-300 truncate">Instagram</span>
                </div>
                <div v-if="formData.socialMedia.twitter" class="flex items-center gap-3 p-2 rounded-lg bg-sky-50 dark:bg-sky-900/20">
                  <UIcon name="i-lucide-twitter" class="h-4 w-4 text-sky-600 dark:text-sky-400" />
                  <span class="text-xs text-slate-600 dark:text-slate-300 truncate">X (Twitter)</span>
                </div>
                <div v-if="formData.socialMedia.linkedin" class="flex items-center gap-3 p-2 rounded-lg bg-blue-50 dark:bg-blue-900/20">
                  <UIcon name="i-lucide-linkedin" class="h-4 w-4 text-blue-700 dark:text-blue-400" />
                  <span class="text-xs text-slate-600 dark:text-slate-300 truncate">LinkedIn</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useClinicSettings } from '../composables/useClinicSettings';
import { useI18n } from 'vue-i18n';

const { request } = useAdminApi();
const toast = useToast();
const { t } = useI18n();
const { settings, pending: loading, error: loadError, refresh } = useClinicSettings();

useHead(() => ({
  title: t('clinicSettings.meta.title')
}));

interface WorkingHours {
  monday: string;
  tuesday: string;
  wednesday: string;
  thursday: string;
  friday: string;
  saturday: string;
  sunday: string;
}

interface SocialMedia {
  facebook: string;
  instagram: string;
  twitter: string;
  linkedin: string;
}

interface ClinicSettingsForm {
  clinicName: string;
  phone: string;
  email: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  currency: string;
  locale: string;
  logoUrl: string;
  logoImageId: string;
  workingHours: WorkingHours;
  socialMedia: SocialMedia;
  virtualConsultationFee: string;
  virtualConsultationMeetingLink: string;
  slotDurationMinutes: string;
  exchangeRates: Record<string, number>;
  paypalEnvironment: string;
  paypalClientId: string;
  paypalClientSecret: string;
  cloudflareAccountId: string;
  cloudflareApiToken: string;
}

const DEFAULT_CURRENCY = "AED";
const DEFAULT_LOCALE = "en-US"; // Always use English (US) locale

const DEFAULT_EXCHANGE_RATES: Record<string, number> = {
  USD: 1.0,
  JOD: 0.709,
  AED: 0.272,
  SAR: 0.267,
  QAR: 0.275,
  OMR: 2.597,
  KWD: 3.261,
  EUR: 1.085,
  GBP: 1.267
};

const weekDays = computed(() => [
  { key: "monday", label: t('clinicSettings.weekDays.monday') },
  { key: "tuesday", label: t('clinicSettings.weekDays.tuesday') },
  { key: "wednesday", label: t('clinicSettings.weekDays.wednesday') },
  { key: "thursday", label: t('clinicSettings.weekDays.thursday') },
  { key: "friday", label: t('clinicSettings.weekDays.friday') },
  { key: "saturday", label: t('clinicSettings.weekDays.saturday') },
  { key: "sunday", label: t('clinicSettings.weekDays.sunday') }
]);

const currencyOptions = [
  { label: "United Arab Emirates Dirham (AED)", value: "AED", symbol: "د.إ", locale: "en-AE" },
  { label: "Jordanian Dinar (JOD)", value: "JOD", symbol: "د.ا", locale: "ar-JO" },
  { label: "Saudi Riyal (SAR)", value: "SAR", symbol: "ر.س", locale: "ar-SA" },
  { label: "Qatari Riyal (QAR)", value: "QAR", symbol: "ر.ق", locale: "ar-QA" },
  { label: "Omani Rial (OMR)", value: "OMR", symbol: "ر.ع.", locale: "ar-OM" },
  { label: "Kuwaiti Dinar (KWD)", value: "KWD", symbol: "د.ك", locale: "ar-KW" },
  { label: "United States Dollar (USD)", value: "USD", symbol: "$", locale: "en-US" },
  { label: "Euro (EUR)", value: "EUR", symbol: "€", locale: "de-DE" },
  { label: "British Pound (GBP)", value: "GBP", symbol: "£", locale: "en-GB" }
];

const localeOptions = [
  { label: "English (United Arab Emirates)", value: "en-AE" },
  { label: "Arabic (Jordan)", value: "ar-JO" },
  { label: "Arabic (Saudi Arabia)", value: "ar-SA" },
  { label: "Arabic (Qatar)", value: "ar-QA" },
  { label: "Arabic (Oman)", value: "ar-OM" },
  { label: "Arabic (Kuwait)", value: "ar-KW" },
  { label: "English (United States)", value: "en-US" },
  { label: "English (United Kingdom)", value: "en-GB" },
  { label: "German (Germany)", value: "de-DE" }
];

const paypalEnvironmentOptions = [
  { label: "Sandbox (Testing)", value: "sandbox" },
  { label: "Live (Production)", value: "live" }
];

const formData = ref<ClinicSettingsForm>({
  clinicName: "",
  phone: "",
  email: "",
  address: "",
  city: "",
  state: "",
  zipCode: "",
  country: "",
  currency: DEFAULT_CURRENCY,
  locale: DEFAULT_LOCALE,
  logoUrl: "",
  logoImageId: "",
  workingHours: {
    monday: "",
    tuesday: "",
    wednesday: "",
    thursday: "",
    friday: "",
    saturday: "",
    sunday: ""
  },
  socialMedia: {
    facebook: "",
    instagram: "",
    twitter: "",
    linkedin: ""
  },
  virtualConsultationFee: "",
  virtualConsultationMeetingLink: "",
  slotDurationMinutes: "30",
  exchangeRates: { ...DEFAULT_EXCHANGE_RATES },
  paypalEnvironment: "sandbox",
  paypalClientId: "",
  paypalClientSecret: "",
  cloudflareAccountId: "",
  cloudflareApiToken: ""
});

const saving = ref(false);

const loadErrorMessage = computed(() => loadError.value ?? null);

const paypalEnvironment = computed(() => {
  return formData.value.paypalEnvironment?.trim() || settings.value?.paypalEnvironment || "";
});

const selectedCurrencyOption = computed(() => {
  return currencyOptions.find(option => option.value === formData.value.currency) ?? currencyOptions[0];
});

watch(
  settings,
  newSettings => {
    if (!newSettings) return;
    const resolvedCurrency = (newSettings.currency || DEFAULT_CURRENCY).toUpperCase();
    formData.value = {
      clinicName: newSettings.clinicName || "",
      phone: newSettings.phone || "",
      email: newSettings.email || "",
      address: newSettings.address || "",
      city: newSettings.city || "",
      state: newSettings.state || "",
      zipCode: newSettings.zipCode || "",
      country: newSettings.country || "",
      currency: resolvedCurrency,
      locale: DEFAULT_LOCALE, // Always use English (US) locale
      logoUrl: newSettings.logoUrl || "",
      logoImageId: newSettings.logoImageId || "",
      workingHours: {
        monday: newSettings.workingHours?.monday || "",
        tuesday: newSettings.workingHours?.tuesday || "",
        wednesday: newSettings.workingHours?.wednesday || "",
        thursday: newSettings.workingHours?.thursday || "",
        friday: newSettings.workingHours?.friday || "",
        saturday: newSettings.workingHours?.saturday || "",
        sunday: newSettings.workingHours?.sunday || ""
      },
      socialMedia: {
        facebook: newSettings.socialMedia?.facebook || "",
        instagram: newSettings.socialMedia?.instagram || "",
        twitter: newSettings.socialMedia?.twitter || "",
        linkedin: newSettings.socialMedia?.linkedin || ""
      },
      virtualConsultationFee: newSettings.virtualConsultationFee?.toString() || "",
      virtualConsultationMeetingLink: newSettings.virtualConsultationMeetingLink || "",
      slotDurationMinutes: (newSettings.slotDurationMinutes ?? 30).toString(),
      exchangeRates: newSettings.exchangeRates || { ...DEFAULT_EXCHANGE_RATES },
      paypalEnvironment: newSettings.paypalEnvironment || "sandbox",
      paypalClientId: newSettings.paypalClientId || "",
      paypalClientSecret: newSettings.paypalClientSecret || "",
      cloudflareAccountId: newSettings.cloudflareAccountId || "",
      cloudflareApiToken: newSettings.cloudflareApiToken || ""
    };
  },
  { immediate: true }
);

// Locale is always set to 'en-US' - no need to sync with currency
// Removed currency watch that auto-synced locale

const openDays = computed(() =>
  weekDays.value.filter(day => {
    const value = formData.value.workingHours[day.key as keyof WorkingHours];
    if (!value) return false;
    return value.toLowerCase() !== "closed";
  }).length
);

const activeSocialLinks = computed(() =>
  Object.values(formData.value.socialMedia).filter(link => link && link.trim().length).length
);

const primaryContact = computed(() => formData.value.phone || formData.value.email || "Not set");

const primaryAddress = computed(() => {
  const { address, city, state, zipCode, country } = formData.value;
  const parts = [address, city, state, zipCode, country].filter(part => part && part.trim().length);
  return parts.length ? parts.join(", ") : t('clinicSettings.preview.addressNotProvided');
});

const logoStatus = computed(() => formData.value.logoUrl ? "Uploaded" : "Not set");

const overviewMetrics = computed(() => {
  const slotDurationRaw = Number(formData.value.slotDurationMinutes || 30);
  const slotDuration = Number.isFinite(slotDurationRaw) && slotDurationRaw > 0 ? Math.round(slotDurationRaw) : 30;
  return [
    {
      label: t('clinicSettings.metrics.identity.label'),
      value: formData.value.clinicName ? t('clinicSettings.metrics.identity.configured') : t('clinicSettings.metrics.identity.notSet'),
      description: formData.value.clinicName || t('clinicSettings.metrics.identity.description'),
      icon: "i-lucide-building-2",
      iconBg: formData.value.clinicName ? "bg-violet-50" : "bg-gray-50",
      iconColor: formData.value.clinicName ? "text-violet-600" : "text-gray-400"
    },
    {
      label: t('clinicSettings.metrics.contact.label'),
      value: (formData.value.phone || formData.value.email) ? t('clinicSettings.metrics.contact.available') : t('clinicSettings.metrics.contact.missing'),
      description: formData.value.phone || formData.value.email || t('clinicSettings.metrics.contact.description'),
      icon: "i-lucide-phone",
      iconBg: (formData.value.phone || formData.value.email) ? "bg-emerald-50" : "bg-gray-50",
      iconColor: (formData.value.phone || formData.value.email) ? "text-emerald-600" : "text-gray-400"
    },
    {
      label: t('clinicSettings.metrics.slot.label'),
      value: t('clinicSettings.metrics.slot.value', { minutes: slotDuration }),
      description: t('clinicSettings.metrics.slot.description'),
      icon: "i-lucide-timer",
      iconBg: "bg-indigo-50",
      iconColor: "text-indigo-600"
    },
    {
      label: t('clinicSettings.metrics.hours.label'),
      value: t('clinicSettings.metrics.hours.value', { open: openDays.value, total: 7 }),
      description: t('clinicSettings.metrics.hours.description', { count: openDays.value }),
      icon: "i-lucide-calendar-check",
      iconBg: openDays.value > 0 ? "bg-blue-50" : "bg-gray-50",
      iconColor: openDays.value > 0 ? "text-blue-600" : "text-gray-400"
    }
  ];
});

async function saveSettings() {
  saving.value = true;
  try {
    const feeValue = formData.value.virtualConsultationFee
      ? Number(formData.value.virtualConsultationFee)
      : null;
    const slotDurationRaw = Number(formData.value.slotDurationMinutes);
    const normalizedSlotDuration = Number.isFinite(slotDurationRaw)
      ? Math.min(Math.max(Math.round(slotDurationRaw), 5), 240)
      : (settings.value?.slotDurationMinutes ?? 30);
    const paypalEnvironmentValue = formData.value.paypalEnvironment?.trim().toLowerCase() ?? "";
    const paypalClientIdValue = formData.value.paypalClientId?.trim() ?? "";
    const paypalClientSecretValue = formData.value.paypalClientSecret?.trim() ?? "";

    const payload = {
      ...formData.value,
      virtualConsultationFee: feeValue,
      virtualConsultationMeetingLink: formData.value.virtualConsultationMeetingLink.trim()
        ? formData.value.virtualConsultationMeetingLink.trim()
        : null,
      slotDurationMinutes: normalizedSlotDuration,
      exchangeRates: formData.value.exchangeRates,
      paypalEnvironment: paypalEnvironmentValue,
      paypalClientId: paypalClientIdValue,
      paypalClientSecret: paypalClientSecretValue,
      cloudflareAccountId: formData.value.cloudflareAccountId.trim(),
      cloudflareApiToken: formData.value.cloudflareApiToken.trim()
    };

    await request("/settings", {
      method: "PUT",
      body: payload
    });

    toast.add({
      title: t('clinicSettings.toasts.saveSuccess.title'),
      description: t('clinicSettings.toasts.saveSuccess.description'),
      color: "green",
      icon: "i-lucide-check-circle"
    });

    await refresh();
  } catch (err: any) {
    toast.add({
      title: t('clinicSettings.toasts.saveError.title'),
      description: err?.data?.message ?? err?.message ?? t('clinicSettings.toasts.saveError.description'),
      color: "red",
      icon: "i-lucide-alert-circle"
    });
  } finally {
    saving.value = false;
  }
}

function handleLogoUpload(data: { imageId: string; filename: string; publicUrl: string; variants: Record<string, string> }) {
  formData.value.logoUrl = data.publicUrl;
  formData.value.logoImageId = data.imageId;
  
  toast.add({
    title: t('clinicSettings.toasts.logoSuccess.title'),
    description: t('clinicSettings.toasts.logoSuccess.description'),
    color: "green",
    icon: "i-lucide-check-circle"
  });
}

function handleLogoError(error: string) {
  toast.add({
    title: t('clinicSettings.toasts.logoError.title'),
    description: error,
    color: "red",
    icon: "i-lucide-alert-circle"
  });
}

function resetForm() {
  const current = settings.value;
  if (!current) return;
  const resolvedCurrency = (current.currency || DEFAULT_CURRENCY).toUpperCase();
  formData.value = {
    clinicName: current.clinicName || "",
    phone: current.phone || "",
    email: current.email || "",
    address: current.address || "",
    city: current.city || "",
    state: current.state || "",
    zipCode: current.zipCode || "",
    country: current.country || "",
    currency: resolvedCurrency,
    locale: DEFAULT_LOCALE, // Always use English (US) locale
    logoUrl: current.logoUrl || "",
    logoImageId: current.logoImageId || "",
    workingHours: {
      monday: current.workingHours?.monday || "",
      tuesday: current.workingHours?.tuesday || "",
      wednesday: current.workingHours?.wednesday || "",
      thursday: current.workingHours?.thursday || "",
      friday: current.workingHours?.friday || "",
      saturday: current.workingHours?.saturday || "",
      sunday: current.workingHours?.sunday || ""
    },
    socialMedia: {
      facebook: current.socialMedia?.facebook || "",
      instagram: current.socialMedia?.instagram || "",
      twitter: current.socialMedia?.twitter || "",
      linkedin: current.socialMedia?.linkedin || ""
    },
    virtualConsultationFee: current.virtualConsultationFee?.toString() || "",
    virtualConsultationMeetingLink: current.virtualConsultationMeetingLink || "",
    slotDurationMinutes: (current.slotDurationMinutes ?? 30).toString(),
    exchangeRates: current.exchangeRates || { ...DEFAULT_EXCHANGE_RATES },
    paypalEnvironment: current.paypalEnvironment || "sandbox",
    paypalClientId: current.paypalClientId || "",
    paypalClientSecret: current.paypalClientSecret || "",
    cloudflareAccountId: current.cloudflareAccountId || "",
    cloudflareApiToken: current.cloudflareApiToken || ""
  };
}
</script>
