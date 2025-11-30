<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-teal-600 shadow-lg">
              <UIcon name="i-lucide-user" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ patientName || t('patients.common.loading') }}</h1>
              <div class="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-300">
                <span>{{ t('patients.details.header.id', { id: patient?.externalId ?? t('patients.common.notAssigned') }) }}</span>
                <span>•</span>
                <span>{{ t('patients.details.header.joined', { date: patientCreated }) }}</span>
                <span v-if="patientAge !== null">•</span>
                <span v-if="patientAge !== null">{{ t('patients.details.header.age', { count: formatNumber(patientAge) }) }}</span>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left" 
              to="/patients"
            >
              {{ t('patients.details.actions.back') }}
            </UButton>
            <UButton 
              color="emerald" 
              icon="i-lucide-save" 
              :loading="saving"
              @click="handleSave"
            >
              {{ t('patients.details.actions.save') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- Error State -->
      <div v-if="error" class="flex items-center justify-center py-12">
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-8 text-center max-w-md mx-auto">
          <div class="flex flex-col items-center gap-4">
            <div class="h-16 w-16 rounded-full bg-red-100 dark:bg-red-900/20 flex items-center justify-center">
              <UIcon name="i-lucide-alert-triangle" class="h-8 w-8 text-red-600 dark:text-red-400" />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t('patients.details.error.title') }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {{ error.message || t('patients.details.error.subtitle') }}
              </p>
            </div>
            <UButton
              color="emerald"
              icon="i-lucide-arrow-left"
              @click="router.push('/patients')"
            >
              {{ t('patients.details.actions.back') }}
            </UButton>
          </div>
        </div>
      </div>

      <div v-else class="grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <!-- Details Form -->
        <div class="space-y-6">
          <!-- Patient Details Form -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-user-round" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t('patients.details.form.title') }}</h2>
                  <p class="text-sm text-emerald-100">{{ t('patients.details.form.subtitle') }}</p>
                </div>
              </div>
            </div>

            <div v-if="pending" class="p-6 space-y-4">
              <USkeleton class="h-12 rounded-2xl" />
              <USkeleton class="h-12 rounded-2xl" />
              <USkeleton class="h-12 rounded-2xl" />
            </div>

            <form v-else @submit.prevent="handleSave" class="p-6">
              <div class="space-y-6">
                <div class="grid gap-6 md:grid-cols-2">
                  <UFormGroup :label="t('patients.details.form.fields.firstName')" required :error="errors.firstName">
                    <UInput
                      v-model="form.firstName"
                      size="lg"
                      :placeholder="t('patients.details.form.placeholders.firstName')"
                      icon="i-lucide-user"
                      @blur="validateField('firstName')"
                      @input="clearFieldError('firstName')"
                    />
                  </UFormGroup>

                  <UFormGroup :label="t('patients.details.form.fields.lastName')" required :error="errors.lastName">
                    <UInput
                      v-model="form.lastName"
                      size="lg"
                      :placeholder="t('patients.details.form.placeholders.lastName')"
                      icon="i-lucide-user"
                      @blur="validateField('lastName')"
                      @input="clearFieldError('lastName')"
                    />
                  </UFormGroup>
                </div>

                <div class="grid gap-6 md:grid-cols-2">
                  <UFormGroup :label="t('patients.details.form.fields.email')" :error="errors.email">
                    <UInput
                      v-model="form.email"
                      type="email"
                      size="lg"
                      :placeholder="t('patients.details.form.placeholders.email')"
                      icon="i-lucide-mail"
                      @blur="validateField('email')"
                      @input="clearFieldError('email')"
                    />
                    <template #description>{{ t('patients.details.form.descriptions.email') }}</template>
                  </UFormGroup>

                  <UFormGroup :label="t('patients.details.form.fields.phone')" :error="errors.phone">
                    <UInput
                      v-model="form.phone"
                      type="tel"
                      size="lg"
                      :placeholder="t('patients.details.form.placeholders.phone')"
                      icon="i-lucide-phone"
                    />
                  </UFormGroup>
                </div>

                <UFormGroup :label="t('patients.details.form.fields.dateOfBirth')" :hint="t('patients.details.form.hints.dateOfBirth')" :error="errors.dateOfBirth">
                  <UInput
                    v-model="form.dateOfBirth"
                    type="date"
                    size="lg"
                    icon="i-lucide-calendar"
                  />
                </UFormGroup>



                <UFormGroup :label="t('patients.details.form.fields.profileImage')" :hint="t('patients.details.form.hints.profileImage')" :error="errors.profileImageUrl">
                  <ImageUploadButton
                    v-model:imageUrl="form.profileImageUrl"
                    :placeholder="t('patients.details.form.placeholders.profileImage')"
                    :alt-text="`${form.firstName} ${form.lastName}`.trim() || t('patients.details.form.image.alt')"
                    :preview-title="t('patients.details.form.image.title')"
                    :preview-description="t('patients.details.form.image.description')"
                    preview-size="w-16 h-16 rounded-full"
                    @validate="validateField('profileImageUrl')"
                    @clear-error="clearFieldError('profileImageUrl')"
                @image-error="handleImageError"
                @image-load="handleImageLoad"
                @upload-error="(error) => errors.profileImageUrl = error"
                @upload-success="handleImageUploadSuccess"
              />
            </UFormGroup>

                <UFormGroup :label="t('patients.details.form.fields.notes')" :hint="t('patients.details.form.hints.notes')" :error="errors.notes">
                  <UTextarea
                    v-model="form.notes"
                    :placeholder="t('patients.details.form.placeholders.notes')"
                    :rows="4"
                    @input="clearFieldError('notes')"
                  />
                </UFormGroup>

                <UFormGroup :label="t('patients.details.form.fields.driveFolder')" :hint="t('patients.details.form.hints.driveFolder')" :error="errors.driveFolderUrl">
                  <div class="flex flex-col gap-3 md:flex-row md:items-center">
                    <UInput
                      v-model="form.driveFolderUrl"
                      size="lg"
                      :placeholder="t('patients.details.form.placeholders.driveFolder')"
                      icon="i-lucide-folder"
                      @blur="validateField('driveFolderUrl')"
                      @input="clearFieldError('driveFolderUrl')"
                    />
                    <UButton
                      v-if="driveFolderHref"
                      type="button"
                      color="gray"
                      variant="outline"
                      icon="i-lucide-link"
                      class="w-full md:w-auto"
                      @click="openDriveFolder"
                    >
                      {{ t('patients.details.form.actions.openDriveFolder') }}
                    </UButton>
                  </div>
                  <template #description>{{ t('patients.details.form.descriptions.driveFolder') }}</template>
                </UFormGroup>

                <UFormGroup label="Tags" hint="Add tags to categorize this patient">
                   <USelectMenu
                      v-model="form.tags"
                      :options="availableTags"
                      option-attribute="name"
                      value-attribute="id"
                      multiple
                      creatable
                      searchable
                      placeholder="Select or create tags"
                      @create="handleCreateTag"
                   >
                     <template #label>
                        <span v-if="form.tags.length" class="truncate">{{ form.tags.length }} tags selected</span>
                        <span v-else class="text-gray-500">Select or create tags</span>
                     </template>
                   </USelectMenu>
                </UFormGroup>
              </div>

              <!-- Form Actions -->
              <div class="flex justify-end gap-3 pt-6 border-t border-slate-200 dark:border-slate-700">
                <UButton
                  variant="ghost"
                  color="gray"
                  to="/patients"
                  :disabled="saving"
                >
                  {{ t('common.actions.cancel') }}
                </UButton>
                <UButton
                  type="submit"
                  color="emerald"
                  icon="i-lucide-save"
                  :loading="saving"
                >
                  {{ t('patients.details.actions.save') }}
                </UButton>
              </div>
            </form>
          </div>

          <!-- Patient Treatment Plans -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-mint-600 to-mint-400 px-6 py-4">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <UIcon name="i-lucide-clipboard-list" class="h-5 w-5 text-white" />
                  <div>
                    <h2 class="text-lg font-semibold text-white">{{ t('patients.details.treatmentPlans.title') }}</h2>
                    <p class="text-sm text-indigo-100">{{ t('patients.details.treatmentPlans.subtitle') }}</p>
                  </div>
                </div>
                <UBadge color="white" variant="solid" size="sm">
                  {{ t('patients.details.treatmentPlans.count', { count: formatNumber(patientPlans.length) }) }}
                </UBadge>
              </div>
            </div>
            <div class="p-6 space-y-4">
              <div v-if="plansLoading" class="space-y-4">
                <USkeleton v-for="i in 3" :key="i" class="h-20 rounded-xl" />
              </div>
              <div v-else-if="plansError" class="rounded-xl border border-dashed border-indigo-300 dark:border-indigo-600 p-6 text-center space-y-3">
                <div class="flex flex-col items-center gap-3">
                  <div class="h-14 w-14 rounded-full bg-indigo-100 dark:bg-indigo-900/30 flex items-center justify-center">
                    <UIcon name="i-lucide-alert-triangle" class="h-7 w-7 text-indigo-500 dark:text-indigo-300" />
                  </div>
                  <div>
                    <h3 class="text-base font-semibold text-slate-900 dark:text-white">{{ t('patients.details.treatmentPlans.error.title') }}</h3>
                    <p class="text-sm text-slate-600 dark:text-slate-300">
                      {{ t('patients.details.treatmentPlans.error.subtitle') }}
                    </p>
                  </div>
                  <UButton size="sm" color="indigo" variant="soft" icon="i-lucide-refresh-cw" @click="refreshPatientPlans">
                    {{ t('patients.details.treatmentPlans.actions.retry') }}
                  </UButton>
                </div>
              </div>
              <div v-else-if="!hasPatientPlans" class="rounded-xl border border-dashed border-slate-200 dark:border-slate-600 p-8 text-center space-y-4">
                <div class="flex flex-col items-center gap-3">
                  <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                    <UIcon name="i-lucide-folder-open" class="h-8 w-8 text-slate-400" />
                  </div>
                  <div>
                    <h3 class="text-base font-semibold text-slate-900 dark:text-white">{{ t('patients.details.treatmentPlans.empty.title') }}</h3>
                    <p class="text-sm text-slate-600 dark:text-slate-300">
                      {{ t('patients.details.treatmentPlans.empty.subtitle') }}
                    </p>
                  </div>
                  <UButton color="emerald" icon="i-lucide-plus" to="/treatment-plans">
                    {{ t('patients.details.treatmentPlans.actions.create') }}
                  </UButton>
                </div>
              </div>
              <div v-else class="space-y-4">
                <div
                  v-for="plan in patientPlans"
                  :key="plan.id"
                  class="rounded-xl border border-slate-200 dark:border-slate-600 p-5 space-y-4 hover:shadow-md transition-shadow duration-200"
                >
                  <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div class="space-y-2">
                      <div class="flex items-center gap-2 flex-wrap">
                        <h3 class="text-base font-semibold text-slate-900 dark:text-white">
                          {{ plan.treatmentTypeName }}
                        </h3>
                        <UBadge :color="planStatusColor(plan.status)" variant="soft">
                          {{ getPlanStatusLabel(plan.status) }}
                        </UBadge>
                      </div>
                      <p class="text-sm text-slate-500 dark:text-slate-400">
                        {{ t('patients.details.treatmentPlans.planWithDoctor', { doctor: plan.doctorName }) }}
                      </p>
                      <p class="text-xs text-slate-400 dark:text-slate-500">
                        {{ t('patients.details.treatmentPlans.created', { date: formatPatientDate(plan.createdAt) }) }}
                      </p>
                    </div>
                    <div class="flex flex-col md:items-end gap-3 w-full md:w-auto">
                      <div class="w-full md:w-56">
                        <p class="text-sm font-medium text-slate-900 dark:text-white">
                          {{ t('patients.details.treatmentPlans.visitsProgress', { completed: formatNumber(plan.completedVisits ?? 0), total: formatNumber(plan.plannedFollowups ?? 0) }) }}
                        </p>
                        <UProgress :value="getPlanVisitProgress(plan)" :color="getPlanProgressColor(plan)" class="mt-2" />
                      </div>
                      <div class="text-sm text-slate-600 dark:text-slate-300">
                        <span>{{ t('patients.details.treatmentPlans.balance.label') }}: </span>
                        <CurrencyValue
                          :amount="plan.convertedRemainingBalance || plan.remainingBalance"
                          :currency="plan.convertedCurrency || plan.currency"
                          variant="secondary"
                          size="sm"
                        />
                      </div>
                      <div class="flex gap-2">
                        <UButton
                          size="sm"
                          color="emerald"
                          variant="soft"
                          icon="i-lucide-eye"
                          :to="`/treatment-plans/${plan.id}`"
                        >
                          {{ t('patients.details.treatmentPlans.actions.view') }}
                        </UButton>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <aside class="space-y-6">
          <!-- Patient Avatar -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-6 text-center">
            <div class="relative inline-block">
              <UAvatar
                size="2xl"
                :alt="patientName"
                :src="patientImage || undefined"
                class="ring-4 ring-white dark:ring-slate-700 shadow-lg"
              />
              <div class="absolute -bottom-2 -right-2 h-8 w-8 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center">
                <UIcon name="i-lucide-user" class="h-4 w-4 text-white" />
              </div>
            </div>
            <h3 class="mt-4 text-lg font-semibold text-slate-900 dark:text-white">{{ patientName || t('patients.common.loading') }}</h3>
            <p class="text-sm text-slate-500 dark:text-slate-400">{{ t('patients.details.sidebar.patientId', { id: patient?.externalId ?? t('patients.common.notAssigned') }) }}</p>
          </div>

          <!-- Patient Balance -->
          <PatientBalanceCard 
            :treatment-plans="patientPlans" 
            :loading="plansLoading"
            :error="plansError"
            @retry="refreshPatientPlans"
          />

          <!-- Contact Information -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-slate-500 to-slate-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-contact" class="h-5 w-5 text-white" />
                <h3 class="text-lg font-semibold text-white">{{ t('patients.details.sidebar.contact.title') }}</h3>
              </div>
            </div>
            <div class="p-6">
              <ul class="space-y-4 text-sm text-slate-600 dark:text-slate-300">
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-sky-100 dark:bg-sky-900/20">
                    <UIcon name="i-lucide-mail" class="h-4 w-4 text-sky-600 dark:text-sky-400" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <a 
                      v-if="form.email" 
                      :href="`mailto:${form.email}`"
                      class="text-sky-600 dark:text-sky-400 hover:text-sky-700 dark:hover:text-sky-300 hover:underline transition-colors duration-200 break-all"
                      :title="t('patients.details.sidebar.contact.emailTooltip')"
                    >
                      {{ form.email }}
                    </a>
                    <span v-else class="text-slate-400 dark:text-slate-500">
                      {{ t('patients.details.sidebar.contact.emailMissing') }}
                    </span>
                  </div>
                </li>
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-100 dark:bg-emerald-900/20">
                    <UIcon name="i-lucide-phone" class="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <a 
                      v-if="form.phone" 
                      :href="`tel:${form.phone}`"
                      class="text-emerald-600 dark:text-emerald-400 hover:text-emerald-700 dark:hover:text-emerald-300 hover:underline transition-colors duration-200"
                      :title="t('patients.details.sidebar.contact.phoneTooltip')"
                    >
                      {{ form.phone }}
                    </a>
                    <span v-else class="text-slate-400 dark:text-slate-500">
                      {{ t('patients.details.sidebar.contact.phoneMissing') }}
                    </span>
                  </div>
                </li>
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-100 dark:bg-amber-900/20">
                    <UIcon name="i-lucide-cake" class="h-4 w-4 text-amber-600 dark:text-amber-400" />
                  </div>
                  <div class="flex flex-col">
                    <span>{{ patientBirthday !== '—' ? patientBirthday : t('patients.details.sidebar.contact.birthdayMissing') }}</span>
                    <span v-if="patientAge !== null" class="text-xs text-slate-400 dark:text-slate-500">
                      {{ t('patients.details.sidebar.contact.age', { count: formatNumber(patientAge) }) }}
                    </span>
                  </div>
                </li>
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-purple-100 dark:bg-purple-900/20">
                    <UIcon name="i-lucide-calendar" class="h-4 w-4 text-purple-600 dark:text-purple-400" />
                  </div>
                  <span>{{ t('patients.details.sidebar.contact.joined', { date: patientCreated }) }}</span>
                </li>
              </ul>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-mint-600 to-mint-400 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-zap" class="h-5 w-5 text-white" />
                <h3 class="text-lg font-semibold text-white">{{ t('patients.details.quickActions.title') }}</h3>
              </div>
            </div>
            <div class="p-6 space-y-3">
              <UButton block color="sky" variant="soft" icon="i-lucide-calendar-plus">
                {{ t('patients.details.quickActions.createPlan') }}
              </UButton>
              <UButton block color="emerald" variant="outline" icon="i-lucide-mail">
                {{ t('patients.details.quickActions.sendReminder') }}
              </UButton>
              <UButton block color="gray" variant="outline" icon="i-lucide-file-text">
                {{ t('patients.details.quickActions.viewHistory') }}
              </UButton>
            </div>
          </div>
        </aside>
      </div>


    </div>


  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watchEffect, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTreatmentPlans } from '~/composables/useTreatmentPlans'
import type { TreatmentPlan, TreatmentPlanStatus } from '~/composables/useTreatmentPlans'
import { useTagService, type Tag } from '@/services/tag.service'

const { t, locale } = useI18n()
const numberFormatter = computed(() => new Intl.NumberFormat(locale.value || undefined))
const formatNumber = (value: number) => numberFormatter.value.format(value)

definePageMeta({
  requiresAuth: true
})

const toast = useToast()
const router = useRouter()
const route = useRoute()
const { request } = useAdminApi()
const treatmentPlansApi = useTreatmentPlans()
const { listTags, createTag } = useTagService()

useHead(() => ({ title: t('patients.details.meta.headTitle') }))

const patientId = computed(() => Number(route.params.id))
if (Number.isNaN(patientId.value)) {
  throw createError({ statusCode: 404, statusMessage: t('patients.details.error.notFound') })
}

const { data, pending, error } = await useAsyncData(`admin-patient-${patientId.value}`, async () => {
  return await request(`/patients/${patientId.value}`)
})

const {
  data: patientPlansData,
  pending: plansLoading,
  error: plansError,
  refresh: refreshPatientPlans
} = await useAsyncData(`admin-patient-${patientId.value}-treatment-plans`, async () => {
  return await treatmentPlansApi.getAll({ patientId: patientId.value })
})

const patient = computed(() => data.value ?? null)
const patientName = computed(() => (patient.value ? `${patient.value.firstName} ${patient.value.lastName}`.trim() : ""))

const patientPlans = computed<TreatmentPlan[]>(() => patientPlansData.value ?? [])
const hasPatientPlans = computed(() => patientPlans.value.length > 0)

const planStatusLabels = computed<Record<TreatmentPlanStatus, string>>(() => ({
  PLANNED: t('treatmentPlans.common.status.planned'),
  IN_PROGRESS: t('treatmentPlans.common.status.inProgress'),
  COMPLETED: t('treatmentPlans.common.status.completed'),
  CANCELLED: t('treatmentPlans.common.status.cancelled')
}))

const planStatusColor = (status?: TreatmentPlanStatus | string) => {
  switch (status) {
    case 'PLANNED':
      return 'blue'
    case 'IN_PROGRESS':
      return 'amber'
    case 'COMPLETED':
      return 'green'
    case 'CANCELLED':
      return 'red'
    default:
      return 'gray'
  }
}

const getPlanStatusLabel = (status?: TreatmentPlanStatus | string) => {
  if (!status) return ''
  return planStatusLabels.value[status as TreatmentPlanStatus] ?? status
}

const formatCurrency = (amount: number | null | undefined, currency: string | undefined) => {
  return new Intl.NumberFormat(locale.value || undefined, {
    style: 'currency',
    currency: currency || 'JOD'
  }).format(amount ?? 0)
}

const getPlanVisitProgress = (plan: TreatmentPlan) => {
  const total = plan.plannedFollowups || 0
  if (!total) return 0
  const percent = (plan.completedVisits / total) * 100
  return Math.min(100, Math.max(0, percent))
}

const getPlanProgressColor = (plan: TreatmentPlan) => {
  const percentage = getPlanVisitProgress(plan)
  if (percentage >= 80) return 'green'
  if (percentage >= 50) return 'amber'
  return 'blue'
}

const joinDateFormatter = computed(
  () =>
    new Intl.DateTimeFormat(locale.value || undefined, {
      month: "short",
      day: "numeric",
      year: "numeric"
    })
)

const toValidDate = (input: string | number | null | undefined): Date | null => {
  if (input === null || input === undefined) return null

  const normalize = (value: number) => {
    const timestamp = value < 10000000000 ? value * 1000 : value
    const date = new Date(timestamp)
    return Number.isNaN(date.getTime()) || date.getTime() <= 0 ? null : date
  }

  if (typeof input === "number") {
    return normalize(input)
  }

  const value = String(input).trim()
  if (!value) return null

  if (/^\d+$/.test(value)) {
    return normalize(Number(value))
  }

  const date = new Date(value)
  return Number.isNaN(date.getTime()) || date.getTime() <= 0 ? null : date
}

const formatPatientDate = (dateInput: string | number | null | undefined): string => {
  const date = toValidDate(dateInput)
  if (!date) return "—"
  return joinDateFormatter.value.format(date)
}

const patientCreated = computed(() => {
  if (!patient.value?.createdAt) return "—"
  return formatPatientDate(patient.value.createdAt)
})

const patientBirthday = computed(() => {
  if (!patient.value?.dateOfBirth) return "—"
  return formatPatientDate(patient.value.dateOfBirth)
})

const patientAge = computed(() => {
  const birthDate = toValidDate(patient.value?.dateOfBirth ?? null)
  if (!birthDate) return null
  const age = Math.floor((Date.now() - birthDate.getTime()) / 31557600000)
  return age >= 0 && age <= 130 ? age : null
})

const patientImage = computed(() => {
  const fromForm = form.profileImageUrl?.trim()
  if (fromForm) return fromForm
  const fromPatient = patient.value?.profileImageUrl?.trim()
  return fromPatient || undefined
})

const form = reactive({
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  profileImageUrl: "",
  dateOfBirth: "",
  driveFolderUrl: "",
  notes: "",
  tags: [] as number[]
})

const availableTags = ref<Tag[]>([])

onMounted(async () => {
  try {
    availableTags.value = await listTags()
  } catch (e) {
    console.error("Failed to load tags", e)
  }
})

const handleCreateTag = async (name: string) => {
  try {
    const newTag = await createTag(name)
    if (!availableTags.value.some(tag => tag.id === newTag.id)) {
      availableTags.value.push(newTag)
    }
    if (!form.tags.includes(newTag.id)) {
      form.tags.push(newTag.id)
    }
  } catch (e) {
    toast.add({
      title: "Failed to create tag",
      color: "red"
    })
  }
}

watchEffect(() => {
  if (!patient.value) return
  form.firstName = patient.value.firstName ?? ""
  form.lastName = patient.value.lastName ?? ""
  form.email = patient.value.email ?? ""
  form.phone = patient.value.phone ?? ""
  form.profileImageUrl = patient.value.profileImageUrl ?? ""
  form.dateOfBirth = patient.value.dateOfBirth ? new Date(patient.value.dateOfBirth).toISOString().split('T')[0] : ""
  form.notes = patient.value.notes ?? ""
  form.driveFolderUrl = patient.value.driveFolderUrl ?? ""
  form.tags = patient.value.tags?.map((t: any) => t.id) ?? []
})

const saving = ref(false)
const errors = ref<Record<string, string>>({})

// Methods
// Validation functions
const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const isValidUrl = (url: string): boolean => {
  if (!url.trim()) return true // Optional field
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

const extractDriveFolderId = (value: string): string | null => {
  const input = value.trim()
  if (!input) return null

  try {
    const url = new URL(input)
    const pathMatch = url.pathname.match(/\/folders\/([\w-]+)/)
    if (pathMatch?.[1]) {
      return pathMatch[1]
    }
  } catch {
    // Not a full URL; fall back to ID detection below
  }

  const idMatch = input.match(/^[A-Za-z0-9_-]{10,}$/)
  return idMatch ? input : null
}

const normalizeDriveFolderValue = (value: string): string | null => {
  const id = extractDriveFolderId(value)
  if (id) {
    return `https://drive.google.com/drive/folders/${id}`
  }

  const trimmed = value.trim()
  if (!trimmed) return null

  try {
    return new URL(trimmed).toString()
  } catch {
    return null
  }
}

const driveFolderHref = computed(() => normalizeDriveFolderValue(form.driveFolderUrl) || null)

const openDriveFolder = () => {
  if (!driveFolderHref.value) return
  window.open(driveFolderHref.value, '_blank', 'noopener,noreferrer')
}

// Individual field validation
const validateField = (fieldName: string) => {
  switch (fieldName) {
    case 'firstName':
      if (!form.firstName.trim()) {
        errors.value.firstName = t('patients.details.validation.firstNameRequired')
      } else {
        delete errors.value.firstName
      }
      break
    case 'lastName':
      if (!form.lastName.trim()) {
        errors.value.lastName = t('patients.details.validation.lastNameRequired')
      } else {
        delete errors.value.lastName
      }
      break
    case 'email':
      if (form.email && !isValidEmail(form.email)) {
        errors.value.email = t('patients.details.validation.invalidEmail')
      } else {
        delete errors.value.email
      }
      break
    case 'profileImageUrl':
      if (form.profileImageUrl && !isValidUrl(form.profileImageUrl)) {
        errors.value.profileImageUrl = t('patients.details.validation.invalidUrl')
      } else {
        delete errors.value.profileImageUrl
      }
      break
    case 'driveFolderUrl':
      if (form.driveFolderUrl && !normalizeDriveFolderValue(form.driveFolderUrl)) {
        errors.value.driveFolderUrl = t('patients.details.validation.invalidDriveFolder')
      } else {
        delete errors.value.driveFolderUrl
      }
      break
    case 'notes':
      if (form.notes && form.notes.length > 2000) {
        errors.value.notes = t('patients.details.validation.notesTooLong')
      } else {
        delete errors.value.notes
      }
      break
  }
}

// Clear field error when user starts typing
const clearFieldError = (fieldName: string) => {
  if (errors.value[fieldName]) {
    delete errors.value[fieldName]
  }
}

// Image handling
const handleImageError = () => {
  errors.value.profileImageUrl = t('patients.details.validation.invalidUrl')
}

const handleImageLoad = () => {
  delete errors.value.profileImageUrl
}

const handleImageUploadSuccess = () => {
  delete errors.value.profileImageUrl
}

const validateForm = (): boolean => {
  // Validate all fields
  validateField('firstName')
  validateField('lastName')
  validateField('email')
  validateField('profileImageUrl')
  validateField('driveFolderUrl')
  validateField('notes')

  return Object.keys(errors.value).length === 0
}

async function handleSave() {
  if (!patient.value || !validateForm()) return
  saving.value = true

  const normalizedDriveFolderUrl = normalizeDriveFolderValue(form.driveFolderUrl)
  if (normalizedDriveFolderUrl || !form.driveFolderUrl.trim()) {
    form.driveFolderUrl = normalizedDriveFolderUrl ?? ""
  }

  const payload = {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    email: form.email.trim() || null,
    phone: form.phone.trim() || null,
    profileImageUrl: form.profileImageUrl.trim() || null,
    dateOfBirth: form.dateOfBirth ? form.dateOfBirth : null,
    driveFolderUrl: normalizedDriveFolderUrl,
    notes: form.notes.trim() || null,
    tagIds: form.tags
  }

  try {
    await request(`/patients/${patient.value.id}`, {
      method: "PUT",
      body: payload
    })

    toast.add({
      title: t('patients.details.toasts.updateSuccess.title'),
      description: t('patients.details.toasts.updateSuccess.description'),
      color: "green",
      icon: "i-lucide-check-circle"
    })
  } catch (error: any) {
    console.error('Failed to update patient:', error)

    if (error?.status === 404) {
      toast.add({
        title: t('patients.details.toasts.notFound.title'),
        description: t('patients.details.toasts.notFound.description'),
        color: "red",
        icon: "i-lucide-alert-circle"
      })
      await router.push('/patients')
      return
    }

    toast.add({
      title: t('patients.details.toasts.updateError.title'),
      description: error?.data?.message ?? error?.message ?? t('patients.details.toasts.updateError.description'),
      color: "red",
      icon: "i-lucide-alert-circle"
    })
  } finally {
    saving.value = false
  }
}

</script>
