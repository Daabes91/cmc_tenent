<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-mint-500 to-mint-400 shadow-lg">
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
              <div class="bg-gradient-to-r from-mint-500 to-mint-400 px-6 py-4">
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

                  <UFormGroup :label="t('clinicSettings.form.favicon.label')" :hint="t('clinicSettings.form.favicon.hint')">
                    <ImageUpload
                      v-model="formData.faviconUrl"
                      :alt-text="t('clinicSettings.form.favicon.altText')"
                      :disabled="saving"
                      @upload-success="handleFaviconUpload"
                      @upload-error="handleFaviconError"
                    />
                    <p class="text-xs text-slate-500 dark:text-slate-400 mt-2">
                      {{ t('clinicSettings.form.favicon.helper') }}
                    </p>
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

            <!-- E-commerce Settings Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-purple-500 to-violet-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-shopping-bag" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.ecommerce.title') }}</h2>
                    <p class="text-sm text-purple-100">{{ t('clinicSettings.sections.ecommerce.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6">
                <div v-if="loading" class="space-y-3">
                  <USkeleton class="h-12 rounded-xl" />
                </div>
                <div v-else class="space-y-4">
                  <div class="flex items-start gap-4 p-4 bg-gradient-to-r from-purple-50 to-violet-50 dark:from-purple-900/20 dark:to-violet-900/20 border border-purple-200 dark:border-purple-800 rounded-xl">
                    <UIcon name="i-lucide-info" class="h-5 w-5 text-purple-600 dark:text-purple-400 mt-0.5 flex-shrink-0" />
                    <div class="flex-1">
                      <h4 class="font-semibold text-purple-900 dark:text-purple-100 mb-2">{{ t('clinicSettings.ecommerce.title') }}</h4>
                      <p class="text-sm text-purple-700 dark:text-purple-300 mb-3">
                        {{ t('clinicSettings.ecommerce.description') }}
                      </p>
                      <div class="flex items-center gap-3">
                        <UToggle 
                          v-model="formData.ecommerceEnabled" 
                          :disabled="saving"
                          size="lg"
                        />
                        <span class="text-sm font-medium text-purple-900 dark:text-purple-100">
                          {{ formData.ecommerceEnabled ? t('clinicSettings.ecommerce.enabled') : t('clinicSettings.ecommerce.disabled') }}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <UAlert
                    v-if="formData.ecommerceEnabled"
                    color="green"
                    icon="i-lucide-check-circle"
                    class="rounded-xl border border-green-200 bg-green-50 dark:bg-green-900/20 dark:border-green-800"
                  >
                    <template #title>{{ t('clinicSettings.ecommerce.enabledTitle') }}</template>
                    <template #description>
                      {{ t('clinicSettings.ecommerce.enabledDescription') }}
                    </template>
                  </UAlert>
                  
                  <UAlert
                    v-else
                    color="gray"
                    icon="i-lucide-eye-off"
                    class="rounded-xl border border-gray-200 bg-gray-50 dark:bg-gray-900/20 dark:border-gray-800"
                  >
                    <template #title>{{ t('clinicSettings.ecommerce.disabledTitle') }}</template>
                    <template #description>
                      {{ t('clinicSettings.ecommerce.disabledDescription') }}
                    </template>
                  </UAlert>
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


            <!-- Email Delivery Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-mail" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.email.title') }}</h2>
                    <p class="text-sm text-emerald-100">{{ t('clinicSettings.sections.email.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6 space-y-6">
                <div class="mt-6 rounded-xl border border-emerald-200 dark:border-emerald-800 bg-white dark:bg-slate-900/30 p-4 space-y-3">
                  <div class="flex items-center justify-between gap-2">
                    <div>
                      <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ t('clinicSettings.email.reminders.title') }}</p>
                      <p class="text-xs text-slate-500 dark:text-slate-400">
                        {{ t('clinicSettings.email.reminders.subtitle') }}
                      </p>
                    </div>
                    <UToggle v-model="formData.reminderEnabled" />
                  </div>

                  <div class="grid grid-cols-1 md:grid-cols-2 gap-3" :class="{ 'opacity-50 pointer-events-none': !formData.reminderEnabled }">
                    <UFormGroup
                      :label="t('clinicSettings.email.reminders.hoursBefore.label')"
                      :hint="t('clinicSettings.email.reminders.hoursBefore.hint')"
                    >
                      <UInput
                        v-model="formData.reminderHoursBefore"
                        type="number"
                        min="1"
                        max="168"
                        size="lg"
                        placeholder="24"
                      />
                    </UFormGroup>
                  </div>
                </div>
              </div>
            </div>

            <!-- Hero Media Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-pink-500 to-rose-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-image" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.heroMedia.title') }}</h2>
                    <p class="text-sm text-pink-100">{{ t('clinicSettings.sections.heroMedia.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6">
                <div v-if="loading" class="space-y-4">
                  <USkeleton class="h-12 rounded-xl" />
                  <USkeleton class="h-64 rounded-xl" />
                </div>
                <div v-else>
                  <HeroMediaConfiguration
                    :model-value="{
                      mediaType: formData.heroMediaType,
                      imageUrl: formData.heroImageUrl,
                      videoId: formData.heroVideoId
                    }"
                    @update:model-value="updateHeroMedia"
                  />
                </div>
            </div>
          </div>

            <!-- Why Choose Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-cyan-500 to-blue-600 px-6 py-4">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-star" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('clinicSettings.sections.whyChoose.title') }}</h2>
                    <p class="text-sm text-cyan-100">{{ t('clinicSettings.sections.whyChoose.subtitle') }}</p>
                  </div>
                </div>
              </div>
              <div class="p-6 space-y-6">
                <div v-if="loading" class="space-y-4">
                  <USkeleton class="h-12 rounded-xl" />
                  <USkeleton class="h-12 rounded-xl" />
                  <USkeleton class="h-32 rounded-xl" />
                </div>
                <div v-else class="space-y-8">
                  <div class="grid gap-6 md:grid-cols-2">
                    <UFormGroup :label="t('clinicSettings.whyChoose.title.en.label')" :hint="t('clinicSettings.whyChoose.title.en.hint')">
                      <UInput
                        v-model="formData.whyChooseTitleEn"
                        size="lg"
                        :placeholder="t('clinicSettings.whyChoose.title.en.placeholder')"
                        icon="i-lucide-type"
                      />
                    </UFormGroup>
                    <UFormGroup :label="t('clinicSettings.whyChoose.title.ar.label')" :hint="t('clinicSettings.whyChoose.title.ar.hint')">
                      <UInput
                        v-model="formData.whyChooseTitleAr"
                        size="lg"
                        dir="rtl"
                        :placeholder="t('clinicSettings.whyChoose.title.ar.placeholder')"
                        icon="i-lucide-type"
                      />
                    </UFormGroup>
                    <UFormGroup :label="t('clinicSettings.whyChoose.subtitle.en.label')">
                      <UInput
                        v-model="formData.whyChooseSubtitleEn"
                        size="lg"
                        :placeholder="t('clinicSettings.whyChoose.subtitle.en.placeholder')"
                        icon="i-lucide-align-left"
                      />
                    </UFormGroup>
                    <UFormGroup :label="t('clinicSettings.whyChoose.subtitle.ar.label')">
                      <UInput
                        v-model="formData.whyChooseSubtitleAr"
                        size="lg"
                        dir="rtl"
                        :placeholder="t('clinicSettings.whyChoose.subtitle.ar.placeholder')"
                        icon="i-lucide-align-right"
                      />
                    </UFormGroup>
                  </div>

                  <div class="space-y-4">
                    <div
                      v-for="(feature, index) in formData.whyChooseFeatures"
                      :key="`why-feature-${index}`"
                      class="rounded-2xl border border-slate-200/70 dark:border-slate-700/70 bg-slate-50/50 dark:bg-slate-800/40 p-5 space-y-4"
                    >
                      <div class="flex items-center justify-between">
                        <div class="flex items-center gap-3">
                          <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-cyan-600 text-white shadow-sm">
                            <UIcon name="i-lucide-sparkles" class="h-5 w-5" />
                          </div>
                          <div>
                            <p class="text-sm font-semibold text-slate-900 dark:text-slate-100">
                              {{ t('clinicSettings.whyChoose.featureTitle', { number: index + 1 }) }}
                            </p>
                            <p class="text-xs text-slate-500 dark:text-slate-400">
                              {{ t('clinicSettings.whyChoose.featureHint') }}
                            </p>
                          </div>
                        </div>
                        <UButton
                          v-if="formData.whyChooseFeatures.length > MIN_WHY_CHOOSE_FEATURES"
                          variant="ghost"
                          color="red"
                          size="xs"
                          icon="i-lucide-trash-2"
                          @click="removeWhyChooseFeature(index)"
                        >
                          {{ t('clinicSettings.whyChoose.actions.removeFeature') }}
                        </UButton>
                      </div>

                      <div class="grid gap-4 md:grid-cols-2">
                        <UFormGroup :label="t('clinicSettings.whyChoose.fields.key.label')" :hint="t('clinicSettings.whyChoose.fields.key.hint')">
                          <UInput
                            v-model="formData.whyChooseFeatures[index].key"
                            size="lg"
                            :placeholder="t('clinicSettings.whyChoose.fields.key.placeholder')"
                            icon="i-lucide-tag"
                          />
                        </UFormGroup>
                        <UFormGroup :label="t('clinicSettings.whyChoose.fields.icon.label')" :hint="t('clinicSettings.whyChoose.fields.icon.hint')">
                          <UInput
                            v-model="formData.whyChooseFeatures[index].icon"
                            size="lg"
                            :placeholder="t('clinicSettings.whyChoose.fields.icon.placeholder')"
                            icon="i-lucide-brush"
                          />
                        </UFormGroup>
                        <UFormGroup :label="t('clinicSettings.whyChoose.fields.title.en')" >
                          <UInput
                            v-model="formData.whyChooseFeatures[index].titleEn"
                            size="lg"
                            :placeholder="t('clinicSettings.whyChoose.fields.title.placeholder')"
                            icon="i-lucide-type"
                          />
                        </UFormGroup>
                        <UFormGroup :label="t('clinicSettings.whyChoose.fields.title.ar')" >
                          <UInput
                            v-model="formData.whyChooseFeatures[index].titleAr"
                            size="lg"
                            dir="rtl"
                            :placeholder="t('clinicSettings.whyChoose.fields.title.placeholder')"
                            icon="i-lucide-type"
                          />
                        </UFormGroup>
                        <UFormGroup :label="t('clinicSettings.whyChoose.fields.description.en')">
                          <UTextarea
                            v-model="formData.whyChooseFeatures[index].descriptionEn"
                            :placeholder="t('clinicSettings.whyChoose.fields.description.placeholder')"
                            class="min-h-[90px]"
                          />
                        </UFormGroup>
                        <UFormGroup :label="t('clinicSettings.whyChoose.fields.description.ar')">
                          <UTextarea
                            v-model="formData.whyChooseFeatures[index].descriptionAr"
                            dir="rtl"
                            :placeholder="t('clinicSettings.whyChoose.fields.description.placeholder')"
                            class="min-h-[90px]"
                          />
                        </UFormGroup>
                      </div>
                    </div>
                  </div>

                  <div class="flex justify-end">
                    <UButton
                      variant="outline"
                      color="blue"
                      icon="i-lucide-plus"
                      :disabled="formData.whyChooseFeatures.length >= MAX_WHY_CHOOSE_FEATURES"
                      @click="addWhyChooseFeature"
                    >
                      {{ t('clinicSettings.whyChoose.actions.addFeature') }}
                    </UButton>
                  </div>
                </div>
              </div>
            </div>

            <!-- Operating Hours Section -->
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
              <div class="bg-gradient-to-r from-mint-600 to-mint-400 px-6 py-4">
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

          <!-- Subscription Plan -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-emerald-600 to-mint-400 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-credit-card" class="h-5 w-5 text-white" />
                <div>
                  <h3 class="text-lg font-semibold text-white">{{ t('billing.plan.title') }}</h3>
                  <p class="text-sm text-emerald-100">{{ t('billing.plan.subtitle') }}</p>
                </div>
              </div>
            </div>
            <div class="p-6">
              <PlanCard
                :plan="billingPlanData"
                :loading="billingPlanLoading"
    @upgrade="showUpgradeModal = true"
    @cancel="showCancelModal = true"
    @resume="handleResumePlan"
    @update-payment="handleUpdatePaymentMethod"
              />
            </div>
          </div>

          <!-- Operating Hours Summary -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-mint-600 to-mint-400 px-6 py-4">
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

  <!-- Plan Upgrade Modal -->
  <PlanUpgradeModal
    v-model="showUpgradeModal"
    :current-tier="currentPlanTier"
    :available-tiers="availablePlanTiers"
    :loading="upgradeLoading"
    @confirm="handleUpgradeConfirm"
  />

  <!-- Cancel Confirmation Modal -->
  <CancelConfirmationModal
    v-model="showCancelModal"
    :effective-date="formattedRenewalDate"
    :loading="cancelLoading"
    @confirm="handleCancelConfirm"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useClinicSettings } from '../composables/useClinicSettings';
import { useI18n } from 'vue-i18n';
import { useBillingPlan } from '../composables/useBillingPlan';
import { usePlanCatalog } from '../composables/usePlanCatalog';

const { request } = useAdminApi();
const toast = useToast();
const { t } = useI18n();
const { settings, pending: loading, error: loadError, refresh } = useClinicSettings();

// Billing Plan Management
const {
  currentPlan: billingPlan,
  isLoading: billingPlanLoading,
  error: billingPlanError,
  fetchPlanDetails,
  upgradePlan,
  cancelPlan: cancelBillingPlan,
  updatePaymentMethod
} = useBillingPlan();
const { normalizedPlans: planCatalog, fallbackNormalized: fallbackPlanCatalog, fetchPlans: fetchPlanCatalog } = usePlanCatalog();

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

interface WhyChooseFeatureForm {
  key: string;
  icon: string;
  titleEn: string;
  titleAr: string;
  descriptionEn: string;
  descriptionAr: string;
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
  faviconUrl: string;
  faviconImageId: string;
  workingHours: WorkingHours;
  socialMedia: SocialMedia;
  virtualConsultationFee: string;
  virtualConsultationMeetingLink: string;
  slotDurationMinutes: string;
  exchangeRates: Record<string, number>;
  paypalEnvironment: string;
  paypalClientId: string;
  paypalClientSecret: string;
  emailFrom: string;
  emailFromName: string;
  emailEnabled: boolean;
  reminderEnabled: boolean;
  reminderHoursBefore: string;
  heroMediaType: string;
  heroImageUrl: string;
  heroVideoId: string;
  whyChooseTitleEn: string;
  whyChooseTitleAr: string;
  whyChooseSubtitleEn: string;
  whyChooseSubtitleAr: string;
  whyChooseFeatures: WhyChooseFeatureForm[];
  ecommerceEnabled: boolean;
}

const DEFAULT_CURRENCY = "AED";
const DEFAULT_LOCALE = "en-US"; // Always use English (US) locale

const DEFAULT_WHY_CHOOSE_TITLE_EN = "Why Choose Cliniqax's Clinic?";
const DEFAULT_WHY_CHOOSE_TITLE_AR = "لماذا تختار عيادة قدري؟";
const DEFAULT_WHY_CHOOSE_SUBTITLE_EN = "We combine cutting-edge technology with compassionate care to deliver exceptional dental experiences.";
const DEFAULT_WHY_CHOOSE_SUBTITLE_AR = "نجمع بين أحدث التقنيات والرعاية الإنسانية لنقدم تجربة أسنان استثنائية.";

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

const DEFAULT_PLAN_PERKS = [
  "Unlimited doctors and staff accounts",
  "Priority chat & email support",
  "Custom domains and branding",
  "Audit-ready activity logs"
];

type PlanCardStatus = 'active' | 'past_due' | 'canceled' | 'pending';

const normalizePlanStatus = (status?: string | null): PlanCardStatus => {
  if (!status) return 'pending';

  const normalized = status.toLowerCase().replace(/[\s-]+/g, '_');

  if (normalized === 'cancelled') {
    return 'canceled';
  }

  if (normalized === 'pending_change' || normalized === 'pending_cancellation' || normalized === 'pending_downgrade') {
    return 'pending';
  }

  if (normalized === 'trial' || normalized === 'trialing') {
    return 'active';
  }

  if (['active', 'past_due', 'canceled', 'pending'].includes(normalized as PlanCardStatus)) {
    return normalized as PlanCardStatus;
  }

  return 'pending';
};

function getDefaultWhyChooseFeatures(): WhyChooseFeatureForm[] {
  return [
    {
      key: "experts",
      icon: "shield-check",
      titleEn: "Expert Professionals",
      titleAr: "أطباء محترفون",
      descriptionEn: "Highly qualified dentists with years of specialized experience",
      descriptionAr: "أطباء أسنان مؤهلون يتمتعون بسنوات من الخبرة المتخصصة"
    },
    {
      key: "technology",
      icon: "beaker",
      titleEn: "Advanced Technology",
      titleAr: "تقنيات متقدمة",
      descriptionEn: "State-of-the-art equipment for precise diagnosis and treatment",
      descriptionAr: "أحدث الأجهزة للحصول على تشخيص وعلاج دقيق"
    },
    {
      key: "comfort",
      icon: "smile",
      titleEn: "Patient Comfort",
      titleAr: "راحة المرضى",
      descriptionEn: "Relaxing environment designed to ease dental anxiety",
      descriptionAr: "بيئة مريحة تساعد على تقليل القلق من علاج الأسنان"
    },
    {
      key: "affordable",
      icon: "wallet",
      titleEn: "Affordable Care",
      titleAr: "رعاية ميسورة",
      descriptionEn: "Flexible payment plans and insurance options available",
      descriptionAr: "خيارات دفع مرنة وتغطية تأمينية متاحة"
    }
  ];
}

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
  faviconUrl: "",
  faviconImageId: "",
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
  emailFrom: "",
  emailFromName: "",
  emailEnabled: false,
  reminderEnabled: false,
  reminderHoursBefore: "24",
  heroMediaType: "image",
  heroImageUrl: "",
  heroVideoId: "",
  whyChooseTitleEn: DEFAULT_WHY_CHOOSE_TITLE_EN,
  whyChooseTitleAr: DEFAULT_WHY_CHOOSE_TITLE_AR,
  whyChooseSubtitleEn: DEFAULT_WHY_CHOOSE_SUBTITLE_EN,
  whyChooseSubtitleAr: DEFAULT_WHY_CHOOSE_SUBTITLE_AR,
  whyChooseFeatures: getDefaultWhyChooseFeatures(),
  ecommerceEnabled: false
});

// Modal state
const showUpgradeModal = ref(false);
const showCancelModal = ref(false);
const upgradeLoading = ref(false);
const cancelLoading = ref(false);

// Billing plan computed properties
const billingPlanData = computed(() => {
  if (!billingPlan.value) return null;
  
  return {
    tenantId: billingPlan.value.tenantId,
    planTier: billingPlan.value.planTier,
    tierName: billingPlan.value.planTierName,
    price: billingPlan.value.price,
    currency: billingPlan.value.currency,
    billingCycle: billingPlan.value.billingCycle as 'MONTHLY' | 'ANNUAL',
    renewalDate: billingPlan.value.renewalDate || '',
    paymentMethodMask: billingPlan.value.paymentMethodMask,
    paymentMethodType: billingPlan.value.paymentMethodType,
    status: normalizePlanStatus(billingPlan.value.status),
    cancellationDate: billingPlan.value.cancellationDate,
    cancellationEffectiveDate: billingPlan.value.cancellationEffectiveDate,
    pendingPlanTier: billingPlan.value.pendingPlanTier,
    pendingPlanEffectiveDate: billingPlan.value.pendingPlanEffectiveDate,
    features: billingPlan.value.features
  };
});

const currentPlanTier = computed(() => billingPlan.value?.planTierName || 'Basic');

const formattedRenewalDate = computed(() => {
  const rd = billingPlan.value?.renewalDate;
  if (!rd) {
    return t('billing.plan.notAvailable');
  }

  const date = typeof rd === 'number' ? new Date(rd) : new Date(rd);
  if (Number.isNaN(date.getTime())) {
    return t('billing.plan.notAvailable');
  }

  return new Intl.DateTimeFormat(formData.value.locale || DEFAULT_LOCALE, {
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  }).format(date);
});

// Available plan tiers for upgrade
const availablePlanTiers = computed(() => {
  const currentTier = billingPlan.value?.planTier || 'BASIC';
  const tiers = planCatalog.value.length ? planCatalog.value : fallbackPlanCatalog;
  
  // Filter out current tier and lower tiers
  const tierOrder = ['BASIC', 'PROFESSIONAL', 'ENTERPRISE', 'CUSTOM'];
  const currentIndex = tierOrder.indexOf(currentTier);
  
  return tiers.filter(tier => {
    const tierIndex = tierOrder.indexOf(tier.value);
    return tierIndex > currentIndex;
  });
});

// Load plan details on mount
onMounted(() => {
  fetchPlanDetails();
  fetchPlanCatalog();
});

const MIN_WHY_CHOOSE_FEATURES = 1;
const MAX_WHY_CHOOSE_FEATURES = 8;

const createEmptyWhyChooseFeature = (): WhyChooseFeatureForm => ({
  key: "",
  icon: "",
  titleEn: "",
  titleAr: "",
  descriptionEn: "",
  descriptionAr: ""
});

const mapWhyChooseFeaturesFromSettings = (features?: {
  key?: string | null;
  icon?: string | null;
  title?: { en?: string | null; ar?: string | null } | null;
  description?: { en?: string | null; ar?: string | null } | null;
}[] | null): WhyChooseFeatureForm[] => {
  if (!features || !features.length) {
    return getDefaultWhyChooseFeatures();
  }

  return features.map((feature) => ({
    key: feature.key || "",
    icon: feature.icon || "",
    titleEn: feature.title?.en || "",
    titleAr: feature.title?.ar || "",
    descriptionEn: feature.description?.en || "",
    descriptionAr: feature.description?.ar || ""
  }));
};

const saving = ref(false);

const loadErrorMessage = computed(() => loadError.value ?? null);

const paypalEnvironment = computed(() => {
  return formData.value.paypalEnvironment?.trim() || settings.value?.paypalEnvironment || "";
});

const selectedCurrencyOption = computed(() => {
  return currencyOptions.find(option => option.value === formData.value.currency) ?? currencyOptions[0];
});

const emailConfigComplete = computed(() => {
  // Allow enabling email even if tenant fields are empty (env credentials will be used)
  return true;
});

const formatPlanDate = (input?: string | null) => {
  if (!input) {
    return t('clinicSettings.plan.notAvailable');
  }

  const date = new Date(input);
  if (Number.isNaN(date.getTime())) {
    return t('clinicSettings.plan.notAvailable');
  }

  return new Intl.DateTimeFormat(formData.value.locale || 'en-US', {
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  }).format(date);
};

// Plan management handlers
async function handleUpgradeConfirm(targetTier: string, billingCycle: string) {
  upgradeLoading.value = true;
  try {
    await upgradePlan(targetTier, billingCycle);
    showUpgradeModal.value = false;
  } catch (error) {
    console.error('Upgrade failed:', error);
  } finally {
    upgradeLoading.value = false;
  }
}

async function handleCancelConfirm(immediate: boolean, reason?: string) {
  cancelLoading.value = true;
  try {
    await cancelBillingPlan(immediate, reason);
    showCancelModal.value = false;
  } catch (error) {
    console.error('Cancellation failed:', error);
  } finally {
    cancelLoading.value = false;
  }
}

async function handleUpdatePaymentMethod() {
  try {
    await updatePaymentMethod();
  } catch (error) {
    console.error('Payment method update failed:', error);
  }
}

async function handleResumePlan() {
  try {
    await resumePlan();
  } catch (error) {
    console.error('Resume plan failed:', error);
  }
}

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
      faviconUrl: newSettings.faviconUrl || "",
      faviconImageId: newSettings.faviconImageId || "",
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
  emailFrom: newSettings.emailFrom || "",
  emailFromName: newSettings.emailFromName || "",
  emailEnabled: newSettings.emailEnabled ?? true,
      reminderEnabled: newSettings.reminderEnabled ?? false,
      reminderHoursBefore: newSettings.reminderHoursBefore?.toString() || "24",
      heroMediaType: newSettings.heroMediaType || "image",
      heroImageUrl: newSettings.heroImageUrl || "",
      heroVideoId: newSettings.heroVideoId || "",
      whyChooseTitleEn: newSettings.whyChoose?.title?.en || DEFAULT_WHY_CHOOSE_TITLE_EN,
      whyChooseTitleAr: newSettings.whyChoose?.title?.ar || DEFAULT_WHY_CHOOSE_TITLE_AR,
      whyChooseSubtitleEn: newSettings.whyChoose?.subtitle?.en || DEFAULT_WHY_CHOOSE_SUBTITLE_EN,
      whyChooseSubtitleAr: newSettings.whyChoose?.subtitle?.ar || DEFAULT_WHY_CHOOSE_SUBTITLE_AR,
      whyChooseFeatures: mapWhyChooseFeaturesFromSettings(newSettings.whyChoose?.features),
      ecommerceEnabled: newSettings.ecommerceEnabled ?? false
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
  // Validate email configuration
  if (formData.value.emailEnabled && !emailConfigComplete.value) {
    toast.add({
      title: t('clinicSettings.toasts.emailValidation.title'),
      description: t('clinicSettings.toasts.emailValidation.description'),
      color: "amber",
      icon: "i-lucide-alert-triangle"
    });
    return;
  }

  // Validate hero media configuration
  if (formData.value.heroMediaType === 'video' && !formData.value.heroVideoId) {
    toast.add({
      title: t('clinicSettings.toasts.heroMediaValidation.title'),
      description: t('clinicSettings.toasts.heroMediaValidation.description'),
      color: "amber",
      icon: "i-lucide-alert-triangle",
      timeout: 5000
    });
    return;
  }

  saving.value = true;
  
  // Show loading toast
  const loadingToast = toast.add({
    title: t('clinicSettings.toasts.saving.title'),
    description: t('clinicSettings.toasts.saving.description'),
    color: "blue",
    icon: "i-lucide-loader-2",
    timeout: 0 // Don't auto-dismiss
  });

  try {
    const {
      whyChooseTitleEn,
      whyChooseTitleAr,
      whyChooseSubtitleEn,
      whyChooseSubtitleAr,
      whyChooseFeatures,
      ...restFormData
    } = formData.value;

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

    const sanitize = (value: string) => value?.trim() || null;

    const buildFeaturePayload = (feature: WhyChooseFeatureForm, index: number) => {
      const key = sanitize(feature.key) || `feature-${index + 1}`;
      return {
        key,
        icon: sanitize(feature.icon),
        title: {
          en: sanitize(feature.titleEn),
          ar: sanitize(feature.titleAr)
        },
        description: {
          en: sanitize(feature.descriptionEn),
          ar: sanitize(feature.descriptionAr)
        }
      };
    };

    const sanitizedFeatures = (whyChooseFeatures || [])
      .map((feature, index) => buildFeaturePayload(feature, index))
      .filter(
        feature =>
          feature.title.en ||
          feature.title.ar ||
          feature.description.en ||
          feature.description.ar
      );

    const fallbackFeatures = getDefaultWhyChooseFeatures().map((feature, index) =>
      buildFeaturePayload(feature, index)
    );

    const payload = {
      ...restFormData,
      logoUrl: sanitize(formData.value.logoUrl),
      logoImageId: sanitize(formData.value.logoImageId),
      faviconUrl: sanitize(formData.value.faviconUrl),
      faviconImageId: sanitize(formData.value.faviconImageId),
      virtualConsultationFee: feeValue,
      virtualConsultationMeetingLink: formData.value.virtualConsultationMeetingLink.trim()
        ? formData.value.virtualConsultationMeetingLink.trim()
        : null,
      slotDurationMinutes: normalizedSlotDuration,
      exchangeRates: formData.value.exchangeRates,
      paypalEnvironment: paypalEnvironmentValue,
      paypalClientId: paypalClientIdValue,
      paypalClientSecret: paypalClientSecretValue,
      emailFrom: sanitize(formData.value.emailFrom),
      emailFromName: sanitize(formData.value.emailFromName),
      emailEnabled: formData.value.emailEnabled,
      reminderEnabled: formData.value.reminderEnabled,
      reminderHoursBefore: Number(formData.value.reminderHoursBefore) || 24,
      heroMediaType: formData.value.heroMediaType || "image",
      heroImageUrl: sanitize(formData.value.heroImageUrl),
      heroVideoId: sanitize(formData.value.heroVideoId),
      whyChoose: {
        title: {
          en: sanitize(whyChooseTitleEn),
          ar: sanitize(whyChooseTitleAr)
        },
        subtitle: {
          en: sanitize(whyChooseSubtitleEn),
          ar: sanitize(whyChooseSubtitleAr)
        },
        features: sanitizedFeatures.length ? sanitizedFeatures : fallbackFeatures
      }
    };

    await request("/settings", {
      method: "PUT",
      body: payload
    });

    // Remove loading toast
    toast.remove(loadingToast.id);

    // Show success toast with hero media specific message if applicable
    const hasHeroMedia = (payload.heroMediaType === 'image' && payload.heroImageUrl) || 
                         (payload.heroMediaType === 'video' && payload.heroVideoId);
    
    toast.add({
      title: t('clinicSettings.toasts.saveSuccess.title'),
      description: hasHeroMedia 
        ? t('clinicSettings.toasts.saveSuccessWithHeroMedia.description')
        : t('clinicSettings.toasts.saveSuccess.description'),
      color: "green",
      icon: "i-lucide-check-circle",
      timeout: 5000
    });

    await refresh();
  } catch (err: any) {
    // Remove loading toast
    if (loadingToast) {
      toast.remove(loadingToast.id);
    }

    // Determine if it's a hero media specific error
    const errorMessage = err?.data?.message ?? err?.message ?? t('clinicSettings.toasts.saveError.description');
    const isHeroMediaError = errorMessage.toLowerCase().includes('hero') || 
                             errorMessage.toLowerCase().includes('media') ||
                             errorMessage.toLowerCase().includes('youtube') ||
                             errorMessage.toLowerCase().includes('image');

    toast.add({
      title: isHeroMediaError 
        ? t('clinicSettings.toasts.heroMediaSaveError.title')
        : t('clinicSettings.toasts.saveError.title'),
      description: errorMessage,
      color: "red",
      icon: "i-lucide-alert-circle",
      timeout: 8000
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

function handleFaviconUpload(data: { imageId: string; filename: string; publicUrl: string; variants: Record<string, string> }) {
  formData.value.faviconUrl = data.publicUrl;
  formData.value.faviconImageId = data.imageId;

  toast.add({
    title: t('clinicSettings.toasts.faviconSuccess.title'),
    description: t('clinicSettings.toasts.faviconSuccess.description'),
    color: "green",
    icon: "i-lucide-check-circle"
  });
}

function handleFaviconError(error: string) {
  toast.add({
    title: t('clinicSettings.toasts.faviconError.title'),
    description: error,
    color: "red",
    icon: "i-lucide-alert-circle"
  });
}

function updateHeroMedia(value: { mediaType: string; imageUrl?: string | null; videoId?: string | null }) {
  formData.value.heroMediaType = value.mediaType || 'image';
  formData.value.heroImageUrl = value.imageUrl || '';
  formData.value.heroVideoId = value.videoId || '';
}

function addWhyChooseFeature() {
  formData.value.whyChooseFeatures.push(createEmptyWhyChooseFeature());
}

function removeWhyChooseFeature(index: number) {
  if (formData.value.whyChooseFeatures.length <= MIN_WHY_CHOOSE_FEATURES) {
    return;
  }
  formData.value.whyChooseFeatures.splice(index, 1);
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
    faviconUrl: current.faviconUrl || "",
    faviconImageId: current.faviconImageId || "",
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
    emailFrom: current.emailFrom || "",
    emailFromName: current.emailFromName || "",
    emailEnabled: current.emailEnabled ?? true,
    heroMediaType: current.heroMediaType || "image",
    heroImageUrl: current.heroImageUrl || "",
    heroVideoId: current.heroVideoId || "",
    whyChooseTitleEn: current.whyChoose?.title?.en || DEFAULT_WHY_CHOOSE_TITLE_EN,
    whyChooseTitleAr: current.whyChoose?.title?.ar || DEFAULT_WHY_CHOOSE_TITLE_AR,
    whyChooseSubtitleEn: current.whyChoose?.subtitle?.en || DEFAULT_WHY_CHOOSE_SUBTITLE_EN,
    whyChooseSubtitleAr: current.whyChoose?.subtitle?.ar || DEFAULT_WHY_CHOOSE_SUBTITLE_AR,
    whyChooseFeatures: mapWhyChooseFeaturesFromSettings(current.whyChoose?.features),
    ecommerceEnabled: current.ecommerceEnabled ?? false
  };
}
</script>
