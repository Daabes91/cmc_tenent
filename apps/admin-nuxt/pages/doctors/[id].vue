<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 shadow-lg">
              <UIcon name="i-lucide-user-pen" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ t('doctors.edit.header.title') }}</h1>
              <p class="text-sm text-slate-600 dark:text-slate-300">
                {{
                  doctor?.fullNameEn
                    ? t('doctors.edit.header.subtitleWithName', { name: doctor.fullNameEn })
                    : t('doctors.edit.header.subtitle')
                }}
              </p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left" 
              to="/doctors"
            >
              {{ t('doctors.edit.actions.back') }}
            </UButton>
            <UButton 
              color="blue" 
              icon="i-lucide-save" 
              :loading="submitting"
              @click="handleSubmit"
            >
              {{ t('doctors.edit.actions.update') }}
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-6 py-8">

      <!-- Loading State -->
      <div v-if="loading" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-6">
        <div class="animate-pulse space-y-4">
          <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-1/4"></div>
          <div class="h-10 bg-slate-300 dark:bg-slate-600 rounded"></div>
          <div class="h-4 bg-slate-300 dark:bg-slate-600 rounded w-1/3"></div>
          <div class="h-10 bg-slate-300 dark:bg-slate-600 rounded"></div>
        </div>
      </div>

      <!-- Form -->
      <div v-else-if="doctor" class="space-y-6">
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-blue-500 to-indigo-600 px-6 py-4">
            <div class="flex items-center gap-3">
              <UIcon name="i-lucide-stethoscope" class="h-5 w-5 text-white" />
              <div>
                <h2 class="text-lg font-semibold text-white">{{ t('doctors.edit.form.title') }}</h2>
                <p class="text-sm text-blue-100">{{ t('doctors.edit.form.subtitle') }}</p>
              </div>
            </div>
          </div>
          <form @submit.prevent="handleSubmit" class="p-6">
          <div class="space-y-6">
            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('doctors.edit.form.fields.fullNameEn')" required :error="errors.fullNameEn">
                <UInput
                  v-model="form.fullNameEn"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.fullNameEn')"
                  icon="i-lucide-user"
                  @blur="validateField('fullNameEn')"
                  @input="clearFieldError('fullNameEn')"
                />
              </UFormGroup>

              <UFormGroup :label="t('doctors.edit.form.fields.fullNameAr')" :error="errors.fullNameAr">
                <UInput
                  v-model="form.fullNameAr"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.fullNameAr')"
                  icon="i-lucide-user"
                  dir="rtl"
                  @blur="validateField('fullNameAr')"
                  @input="clearFieldError('fullNameAr')"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('doctors.edit.form.fields.email')" :error="errors.email">
                <UInput
                  v-model="form.email"
                  type="email"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.email')"
                  icon="i-lucide-mail"
                  @blur="validateField('email')"
                  @input="clearFieldError('email')"
                />
              </UFormGroup>

              <UFormGroup :label="t('doctors.edit.form.fields.phone')" :error="errors.phone">
                <UInput
                  v-model="form.phone"
                  type="tel"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.phone')"
                  icon="i-lucide-phone"
                  @blur="validateField('phone')"
                  @input="clearFieldError('phone')"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">

              <UFormGroup :label="t('doctors.edit.form.fields.specialtyEn')" :error="errors.specialtyEn">
                <UInput
                  v-model="form.specialtyEn"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.specialtyEn')"
                  icon="i-lucide-briefcase"
                  @blur="validateField('specialtyEn')"
                  @input="clearFieldError('specialtyEn')"
                />
              </UFormGroup>

              <UFormGroup :label="t('doctors.edit.form.fields.specialtyAr')" :error="errors.specialtyAr">
                <UInput
                  v-model="form.specialtyAr"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.specialtyAr')"
                  icon="i-lucide-briefcase"
                  dir="rtl"
                  @blur="validateField('specialtyAr')"
                  @input="clearFieldError('specialtyAr')"
                />
              </UFormGroup>
            </div>

            <UFormGroup :label="t('doctors.edit.form.fields.profileImage')" :hint="t('doctors.edit.form.hints.profileImage')" :error="errors.imageUrl">
              <ImageUploadButton
                v-model:imageUrl="form.imageUrl"
                :placeholder="t('doctors.edit.form.placeholders.imageUrl')"
                :alt-text="form.fullNameEn || t('doctors.edit.form.imageUpload.altText')"
                :preview-title="t('doctors.edit.form.imageUpload.title')"
                :preview-description="t('doctors.edit.form.imageUpload.description')"
                preview-size="w-16 h-16 rounded-full"
                @validate="validateField('imageUrl')"
                @clear-error="clearFieldError('imageUrl')"
                @image-error="handleImageError"
                @image-load="handleImageLoad"
                @upload-error="(error) => errors.imageUrl = error"
                @upload-success="handleImageUploadSuccess"
                @update:imageUrl="(url) => { form.imageUrl = url }"
              />
            </UFormGroup>

            <div class="grid gap-6 md:grid-cols-1">
              <UFormGroup :label="t('doctors.edit.form.fields.bioEn')" :hint="t('doctors.edit.form.hints.bioEn')" :error="errors.bioEn">
                <UTextarea
                  v-model="form.bioEn"
                  :rows="4"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.bioEn')"
                  @blur="validateField('bioEn')"
                  @input="clearFieldError('bioEn')"
                />
              </UFormGroup>
              
              <UFormGroup :label="t('doctors.edit.form.fields.bioAr')" :hint="t('doctors.edit.form.hints.bioAr')" :error="errors.bioAr">
                <UTextarea
                  v-model="form.bioAr"
                  :rows="4"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.bioAr')"
                  dir="rtl"
                  @blur="validateField('bioAr')"
                  @input="clearFieldError('bioAr')"
                />
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">

              <UFormGroup :label="t('doctors.edit.form.fields.languages')" :hint="t('doctors.edit.form.hints.languages')">
                <div class="rounded-lg border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-800 p-4">
                  <div class="flex flex-wrap gap-3">
                    <UCheckbox
                      v-for="option in languageOptions"
                      :key="option.value"
                      v-model="form.locales"
                      :value="option.value"
                      :label="option.label"
                      color="blue"
                      class="inline-flex"
                    />
                  </div>
                </div>
              </UFormGroup>

              <UFormGroup :label="t('doctors.edit.form.fields.services')" :hint="t('doctors.edit.form.hints.services')">
                <div class="rounded-lg border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-800 p-4">
                  <div v-if="serviceOptions.length > 0" class="max-h-64 overflow-y-auto">
                    <div class="flex flex-wrap gap-3">
                      <UCheckbox
                        v-for="option in serviceOptions"
                        :key="option.value"
                        v-model="form.serviceIds"
                        :value="option.value"
                        :label="option.label"
                        color="blue"
                        class="inline-flex"
                      />
                    </div>
                  </div>

                  <!-- Empty State -->
                  <div v-else class="flex flex-col items-center justify-center py-12 text-center">
                    <div class="flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-700 mb-4">
                      <UIcon name="i-lucide-package-x" class="h-8 w-8 text-slate-400" />
                    </div>
                    <h3 class="text-sm font-semibold text-slate-900 dark:text-white mb-1">{{ t('doctors.edit.form.emptyServices.title') }}</h3>
                    <p class="text-xs text-slate-500 dark:text-slate-400">
                      {{ t('doctors.edit.form.emptyServices.description') }}
                    </p>
                  </div>
                </div>
              </UFormGroup>
            </div>

            <div class="grid gap-6 md:grid-cols-2">
              <UFormGroup :label="t('doctors.edit.form.fields.displayOrder')" :hint="t('doctors.edit.form.hints.displayOrder')" :error="errors.displayOrder">
                <UInput
                  v-model.number="form.displayOrder"
                  type="number"
                  min="0"
                  step="1"
                  size="lg"
                  :placeholder="t('doctors.edit.form.placeholders.displayOrder')"
                  icon="i-lucide-hash"
                  @blur="validateField('displayOrder')"
                  @input="clearFieldError('displayOrder')"
                />
              </UFormGroup>

              <UFormGroup :label="t('doctors.edit.form.fields.visibility')" :hint="t('doctors.edit.form.hints.visibility')">
                <div class="flex items-center justify-between p-4 rounded-xl border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-800">
                  <div class="flex-1">
                    <span class="text-sm font-medium text-slate-900 dark:text-white">
                      {{ t('doctors.edit.form.fields.activeProfile') }}
                    </span>
                    <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                      {{ t('doctors.edit.form.hints.activeProfileDescription') }}
                    </p>
                  </div>
                  <UToggle
                    v-model="form.isActive"
                    color="green"
                    size="lg"
                  />
                </div>
              </UFormGroup>
            </div>

            <!-- Form Actions -->
            <div class="flex justify-end gap-3 pt-6 border-t border-slate-200 dark:border-slate-700">
              <UButton
                variant="ghost"
                color="gray"
                to="/doctors"
                :disabled="submitting"
              >
                {{ t('common.actions.cancel') }}
              </UButton>
              <UButton
                type="submit"
                color="blue"
                icon="i-lucide-save"
                :loading="submitting"
                :disabled="!isFormValid"
              >
                {{ t('doctors.edit.actions.update') }}
              </UButton>
            </div>
        </div>
          </form>
        </div>

        <!-- Availability Management -->
        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-calendar-clock" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t('doctors.edit.availability.title') }}</h2>
                  <p class="text-sm text-emerald-100">{{ t('doctors.edit.availability.subtitle') }}</p>
                </div>
              </div>
              <UBadge color="white" variant="solid" size="sm">
                {{ t('doctors.edit.availability.count', { count: formatNumber(availabilities.length) }) }}
              </UBadge>
            </div>
          </div>
          <div class="p-6 space-y-6">
            <div class="space-y-4">
              <h3 class="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wide">
                {{ t('doctors.edit.availability.form.title') }}
              </h3>
              <form @submit.prevent="handleCreateAvailability" class="grid gap-4 lg:grid-cols-4">
                <UFormGroup
                  class="lg:col-span-2"
                  :label="t('doctors.edit.availability.form.type.label')"
                  :hint="t('doctors.edit.availability.form.type.hint')"
                >
                  <USelect
                    v-model="availabilityForm.type"
                    :options="availabilityTypeOptions"
                    value-attribute="value"
                    label-attribute="label"
                    size="lg"
                  />
                </UFormGroup>

                <UFormGroup
                  v-if="isWeeklyAvailability"
                  :label="t('doctors.edit.availability.form.dayOfWeek.label')"
                  :hint="t('doctors.edit.availability.form.dayOfWeek.hint')"
                >
                  <USelect
                    v-model="availabilityForm.dayOfWeek"
                    :options="dayOfWeekOptions"
                    value-attribute="value"
                    label-attribute="label"
                    size="lg"
                  />
                </UFormGroup>

                <UFormGroup
                  v-if="isOneTimeAvailability"
                  :label="t('doctors.edit.availability.form.date.label')"
                  :hint="t('doctors.edit.availability.form.date.hint')"
                >
                  <UInput
                    v-model="availabilityForm.date"
                    type="date"
                    size="lg"
                  />
                </UFormGroup>

                <UFormGroup
                  :label="t('doctors.edit.availability.form.startTime.label')"
                  :hint="t('doctors.edit.availability.form.startTime.hint')"
                >
                  <UInput
                    v-model="availabilityForm.startTime"
                    type="time"
                    size="lg"
                    step="300"
                  />
                </UFormGroup>

                <UFormGroup
                  :label="t('doctors.edit.availability.form.endTime.label')"
                  :hint="t('doctors.edit.availability.form.endTime.hint')"
                >
                  <UInput
                    v-model="availabilityForm.endTime"
                    type="time"
                    size="lg"
                    step="300"
                  />
                </UFormGroup>

                <div class="lg:col-span-4 flex flex-col gap-3">
                  <UAlert
                    v-if="availabilityFormError"
                    color="red"
                    variant="soft"
                    icon="i-lucide-alert-triangle"
                    class="rounded-xl"
                  >
                    {{ availabilityFormError }}
                  </UAlert>
                  <div class="flex justify-end">
                    <UButton
                      type="submit"
                      color="emerald"
                      icon="i-lucide-plus"
                      :loading="availabilitySubmitting"
                      :disabled="!canSubmitAvailability || availabilitySubmitting"
                    >
                      {{ t('doctors.edit.availability.form.submit') }}
                    </UButton>
                  </div>
                </div>
              </form>
            </div>

            <div class="space-y-4">
              <div class="flex items-center justify-between">
                <h3 class="text-sm font-semibold text-slate-900 dark:text-white uppercase tracking-wide">
                  {{ t('doctors.edit.availability.list.title') }}
                </h3>
                <UButton
                  v-if="availabilityError"
                  color="emerald"
                  variant="soft"
                  icon="i-lucide-refresh-cw"
                  :loading="availabilityLoading"
                  @click="refreshAvailability"
                >
                  {{ t('doctors.edit.availability.list.retry') }}
                </UButton>
              </div>

              <div v-if="availabilityLoading" class="space-y-3">
                <USkeleton v-for="i in 3" :key="`availability-skeleton-${i}`" class="h-16 rounded-xl" />
              </div>

              <div v-else-if="availabilityError" class="rounded-xl border border-dashed border-emerald-200 dark:border-emerald-800 p-6 text-center space-y-2">
                <UIcon name="i-lucide-alert-octagon" class="mx-auto h-8 w-8 text-emerald-500 dark:text-emerald-300" />
                <p class="text-sm text-slate-600 dark:text-slate-300">
                  {{ t('doctors.edit.availability.list.error') }}
                </p>
              </div>

              <div v-else-if="sortedAvailabilities.length === 0" class="rounded-xl border border-dashed border-emerald-200 dark:border-emerald-800 p-6 text-center space-y-3">
                <div class="flex items-center justify-center">
                  <UIcon name="i-lucide-calendar-x" class="h-10 w-10 text-emerald-400 dark:text-emerald-300" />
                </div>
                <div>
                  <h4 class="text-sm font-semibold text-slate-900 dark:text-white">
                    {{ t('doctors.edit.availability.list.empty.title') }}
                  </h4>
                  <p class="text-xs text-slate-600 dark:text-slate-400">
                    {{ t('doctors.edit.availability.list.empty.description') }}
                  </p>
                </div>
              </div>

              <div v-else class="space-y-4">
                <div
                  v-for="availability in sortedAvailabilities"
                  :key="availability.id"
                  class="rounded-xl border border-slate-200 dark:border-slate-600 p-5 flex flex-col gap-4 md:flex-row md:items-center md:justify-between hover:border-emerald-300 dark:hover:border-emerald-500 transition-colors duration-150"
                >
                  <div class="space-y-2">
                    <div class="flex flex-wrap items-center gap-2 text-sm text-slate-700 dark:text-slate-200">
                      <UBadge color="emerald" variant="soft">
                        {{ typeLabelMap[availability.type] ?? availability.type }}
                      </UBadge>
                      <span v-if="availability.type === 'WEEKLY'">
                        {{ formatAvailabilityDay(availability.dayOfWeek) }}
                      </span>
                      <span v-else>
                        {{ formatAvailabilityDate(availability.date) }}
                      </span>
                      <span class="text-slate-400">•</span>
                      <span class="font-medium text-slate-900 dark:text-white">
                        {{ availability.startTime }} - {{ availability.endTime }}
                      </span>
                    </div>
                    <p class="text-xs text-slate-500 dark:text-slate-400">
                      {{ t('doctors.edit.availability.list.updatedAt', { date: formatAvailabilityTimestamp(availability.updatedAt) || formatAvailabilityTimestamp(availability.createdAt) || 'N/A' }) }}
                    </p>
                  </div>
                  <div class="flex items-center gap-2 self-start md:self-auto">
                    <UButton
                      color="red"
                      variant="soft"
                      icon="i-lucide-trash-2"
                      :loading="deletingAvailabilityId === availability.id"
                      @click="handleDeleteAvailability(availability.id)"
                    >
                      {{ t('doctors.edit.availability.list.remove') }}
                    </UButton>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
          <div class="bg-gradient-to-r from-purple-500 to-fuchsia-600 px-6 py-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-clipboard-heart" class="h-5 w-5 text-white" />
                <div>
                  <h2 class="text-lg font-semibold text-white">{{ t('doctors.edit.treatmentPlans.title') }}</h2>
                  <p class="text-sm text-fuchsia-100">{{ t('doctors.edit.treatmentPlans.subtitle') }}</p>
                </div>
              </div>
              <UBadge color="white" variant="solid" size="sm">
                {{ t('doctors.edit.treatmentPlans.count', { count: formatNumber(doctorPlans.length) }) }}
              </UBadge>
            </div>
          </div>
          <div class="p-6 space-y-4">
            <div v-if="plansLoading" class="space-y-4">
              <USkeleton v-for="i in 3" :key="`doctor-plan-skeleton-${i}`" class="h-20 rounded-xl" />
            </div>
            <div v-else-if="plansError" class="rounded-xl border border-dashed border-fuchsia-300 dark:border-fuchsia-700 p-6 text-center space-y-3">
              <div class="flex flex-col items-center gap-3">
                <div class="h-14 w-14 rounded-full bg-fuchsia-100 dark:bg-fuchsia-900/30 flex items-center justify-center">
                  <UIcon name="i-lucide-alert-triangle" class="h-7 w-7 text-fuchsia-500 dark:text-fuchsia-300" />
                </div>
                <div>
                  <h3 class="text-base font-semibold text-slate-900 dark:text-white">{{ t('doctors.edit.treatmentPlans.error.title') }}</h3>
                  <p class="text-sm text-slate-600 dark:text-slate-300">
                    {{ t('doctors.edit.treatmentPlans.error.subtitle') }}
                  </p>
                </div>
                <UButton size="sm" color="purple" variant="soft" icon="i-lucide-refresh-cw" @click="refreshDoctorPlans">
                  {{ t('doctors.edit.treatmentPlans.actions.retry') }}
                </UButton>
              </div>
            </div>
            <div v-else-if="doctorPlans.length === 0" class="rounded-xl border border-dashed border-slate-200 dark:border-slate-600 p-8 text-center space-y-4">
              <div class="flex flex-col items-center gap-3">
                <div class="h-16 w-16 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center">
                  <UIcon name="i-lucide-heart-off" class="h-8 w-8 text-slate-400" />
                </div>
                <div>
                  <h3 class="text-base font-semibold text-slate-900 dark:text-white">{{ t('doctors.edit.treatmentPlans.empty.title') }}</h3>
                  <p class="text-sm text-slate-600 dark:text-slate-300">
                    {{ t('doctors.edit.treatmentPlans.empty.subtitle') }}
                  </p>
                </div>
              </div>
            </div>
            <div v-else class="space-y-4">
              <div
                v-for="plan in paginatedDoctorPlans"
                :key="plan.id"
                class="rounded-xl border border-slate-200 dark:border-slate-600 p-5 space-y-4 hover:shadow-md transition-shadow duration-200"
              >
                <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                  <div class="space-y-2">
                    <div class="flex items-center gap-2 flex-wrap">
                      <h3 class="text-base font-semibold text-slate-900 dark:text-white">
                        {{ plan.patientName }}
                      </h3>
                      <UBadge :color="planStatusColor(plan.status)" variant="soft">
                        {{ getPlanStatusLabel(plan.status) }}
                      </UBadge>
                      <UBadge color="gray" variant="soft">
                        {{ plan.treatmentTypeName }}
                      </UBadge>
                    </div>
                    <p class="text-sm text-slate-500 dark:text-slate-400">
                      {{ t('doctors.edit.treatmentPlans.planWithPatient', { patient: plan.patientName }) }}
                    </p>
                    <p class="text-xs text-slate-400 dark:text-slate-500">
                      {{ t('doctors.edit.treatmentPlans.created', { date: formatPlanDate(plan.createdAt) }) }}
                    </p>
                  </div>
                  <div class="flex flex-col md:items-end gap-3 w-full md:w-auto">
                    <div class="w-full md:w-56">
                      <p class="text-sm font-medium text-slate-900 dark:text-white">
                        {{ t('doctors.edit.treatmentPlans.visitsProgress', { completed: formatNumber(plan.completedVisits ?? 0), total: formatNumber(plan.plannedFollowups ?? 0) }) }}
                      </p>
                      <UProgress :value="getPlanVisitProgress(plan)" :color="getPlanProgressColor(plan)" class="mt-2" />
                    </div>
                    <p class="text-sm text-slate-600 dark:text-slate-300">
                      {{ t('doctors.edit.treatmentPlans.balance', { amount: formatCurrency(plan.remainingBalance, plan.currency) }) }}
                    </p>
                    <div class="flex gap-2">
                      <UButton
                        size="sm"
                        color="blue"
                        variant="soft"
                        icon="i-lucide-eye"
                        :to="`/treatment-plans/${plan.id}`"
                      >
                        {{ t('doctors.edit.treatmentPlans.actions.view') }}
                      </UButton>
                    </div>
                  </div>
                </div>
              </div>
              <div
                v-if="hasPlanPagination && !plansLoading && !plansError"
                class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between pt-4 border-t border-slate-200 dark:border-slate-700"
              >
                <span class="text-sm text-slate-600 dark:text-slate-300">
                  {{ t('doctors.edit.treatmentPlans.pagination.summary', { start: formatNumber(planRange.start), end: formatNumber(planRange.end), total: formatNumber(planRange.total) }) }}
                </span>
                <div class="flex items-center gap-2">
                  <UButton
                    size="xs"
                    variant="ghost"
                    color="gray"
                    icon="i-lucide-chevron-left"
                    :disabled="!canGoPrevPlanPage"
                    @click="goToPrevPlanPage"
                  >
                    {{ t('doctors.edit.treatmentPlans.pagination.previous') }}
                  </UButton>
                  <UBadge color="purple" variant="soft">
                    {{ formatNumber(plansPage) }} / {{ formatNumber(totalPlanPages) }}
                  </UBadge>
                  <UButton
                    size="xs"
                    variant="ghost"
                    color="gray"
                    icon="i-lucide-chevron-right"
                    trailing
                    :disabled="!canGoNextPlanPage"
                    @click="goToNextPlanPage"
                  >
                    {{ t('doctors.edit.treatmentPlans.pagination.next') }}
                  </UButton>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-6 text-center">
        <UIcon name="i-lucide-alert-triangle" class="mx-auto h-12 w-12 text-red-400" />
        <h3 class="mt-2 text-sm font-medium text-slate-900 dark:text-white">{{ t('doctors.edit.notFound.title') }}</h3>
        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
          {{ t('doctors.edit.notFound.description') }}
        </p>
        <div class="mt-6">
          <UButton
            color="blue"
            to="/doctors"
          >
            {{ t('doctors.edit.notFound.action') }}
          </UButton>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup lang="ts">
import type { AdminServiceSummary } from "@/types/services";
import type { DoctorAdmin, DoctorAvailability } from "@/types/doctors";
import { computed, reactive, ref, watch, watchEffect } from "vue";
import { useI18n } from "vue-i18n";
import { useTreatmentPlans } from "~/composables/useTreatmentPlans";
import type { TreatmentPlan, TreatmentPlanStatus } from "~/composables/useTreatmentPlans";

definePageMeta({
  requiresAuth: true
})

const { t, locale } = useI18n();

const numberFormatter = computed(() => new Intl.NumberFormat(locale.value || undefined));
const formatNumber = (value: number | string | null | undefined) => {
  const numeric = Number(value ?? 0);
  if (Number.isNaN(numeric)) {
    return numberFormatter.value.format(0);
  }
  return numberFormatter.value.format(numeric);
};

useHead(() => ({
  title: t('doctors.edit.meta.title')
}))

const toast = useToast()
const route = useRoute()
const router = useRouter()
const { fetcher, request } = useAdminApi()
const treatmentPlansApi = useTreatmentPlans()

const doctorId = computed(() => Number(route.params.id));

if (Number.isNaN(doctorId.value)) {
  throw createError({ statusCode: 404, statusMessage: t('doctors.edit.notFound.title') });
}

// Load doctor data
const { data, pending: loading, error } = await useAsyncData(
  `admin-doctor-${doctorId.value}`,
  async () => {
    return await request<DoctorAdmin>(`/doctors/${doctorId.value}`);
  }
);

// Load services data
const { data: servicesData } = await useAsyncData("admin-doctor-services", () =>
  fetcher<AdminServiceSummary[]>("/services", [])
);

const doctor = computed(() => data.value ?? null);

const {
  data: availabilityData,
  pending: availabilityLoading,
  error: availabilityError,
  refresh: refreshAvailability
} = await useAsyncData(`admin-doctor-${doctorId.value}-availability`, async () => {
  return await request<DoctorAvailability[]>(`/doctors/${doctorId.value}/availability`);
});

const availabilities = computed<DoctorAvailability[]>(() => availabilityData.value ?? []);

const dayOfWeekOrder: Record<string, number> = {
  MONDAY: 1,
  TUESDAY: 2,
  WEDNESDAY: 3,
  THURSDAY: 4,
  FRIDAY: 5,
  SATURDAY: 6,
  SUNDAY: 7
};

const toMinutes = (value: string | null | undefined) => {
  if (!value || !/^\d{2}:\d{2}$/.test(value)) return Number.MAX_SAFE_INTEGER;
  const [hours, minutes] = value.split(":").map(Number);
  return (hours * 60) + minutes;
};

const normalizeDay = (value: string | null | undefined) => (value ?? "").toUpperCase();

const sortedAvailabilities = computed(() => {
  return [...availabilities.value].sort((a, b) => {
    if (a.type !== b.type) {
      return a.type === "WEEKLY" ? -1 : 1;
    }

    if (a.type === "WEEKLY") {
      const aDay = dayOfWeekOrder[normalizeDay(a.dayOfWeek)] ?? 8;
      const bDay = dayOfWeekOrder[normalizeDay(b.dayOfWeek)] ?? 8;
      if (aDay !== bDay) {
        return aDay - bDay;
      }
      return toMinutes(a.startTime) - toMinutes(b.startTime);
    }

    if (a.type === "ONE_TIME") {
      const aDate = a.date ?? "";
      const bDate = b.date ?? "";
      if (aDate !== bDate) {
        return aDate.localeCompare(bDate);
      }
      return toMinutes(a.startTime) - toMinutes(b.startTime);
    }

    return 0;
  });
});

const availabilityTypeOptions = computed(() => [
  { label: t("doctors.edit.availability.form.typeOptions.weekly"), value: "WEEKLY" },
  { label: t("doctors.edit.availability.form.typeOptions.oneTime"), value: "ONE_TIME" }
]);

const dayOfWeekOptions = computed(() => [
  { label: t("clinicSettings.weekDays.monday"), value: "MONDAY" },
  { label: t("clinicSettings.weekDays.tuesday"), value: "TUESDAY" },
  { label: t("clinicSettings.weekDays.wednesday"), value: "WEDNESDAY" },
  { label: t("clinicSettings.weekDays.thursday"), value: "THURSDAY" },
  { label: t("clinicSettings.weekDays.friday"), value: "FRIDAY" },
  { label: t("clinicSettings.weekDays.saturday"), value: "SATURDAY" },
  { label: t("clinicSettings.weekDays.sunday"), value: "SUNDAY" }
]);

const availabilityForm = reactive({
  type: "WEEKLY",
  dayOfWeek: "MONDAY",
  date: "",
  startTime: "",
  endTime: ""
});

const availabilityFormError = ref<string | null>(null);
const availabilitySubmitting = ref(false);
const deletingAvailabilityId = ref<number | null>(null);

const ensureDateString = () => new Date().toISOString().split("T")[0];

watch(() => availabilityForm.type, (type) => {
  if (type === "WEEKLY") {
    availabilityForm.dayOfWeek = availabilityForm.dayOfWeek || "MONDAY";
    availabilityForm.date = "";
  } else if (type === "ONE_TIME") {
    availabilityForm.date = availabilityForm.date || ensureDateString();
    availabilityForm.dayOfWeek = "";
  }
});

const isWeeklyAvailability = computed(() => availabilityForm.type === "WEEKLY");
const isOneTimeAvailability = computed(() => availabilityForm.type === "ONE_TIME");

const hasValidTimeRange = computed(() => {
  if (!availabilityForm.startTime || !availabilityForm.endTime) {
    return false;
  }
  return toMinutes(availabilityForm.startTime) < toMinutes(availabilityForm.endTime);
});

const canSubmitAvailability = computed(() => {
  if (!availabilityForm.type || !availabilityForm.startTime || !availabilityForm.endTime) {
    return false;
  }
  if (!hasValidTimeRange.value) {
    return false;
  }
  if (isWeeklyAvailability.value) {
    return Boolean(availabilityForm.dayOfWeek);
  }
  if (isOneTimeAvailability.value) {
    return Boolean(availabilityForm.date);
  }
  return true;
});

const typeLabelMap = computed<Record<string, string>>(() => ({
  WEEKLY: t("doctors.edit.availability.labels.weekly"),
  ONE_TIME: t("doctors.edit.availability.labels.oneTime")
}));

const availabilityDateFormatter = computed(() =>
  new Intl.DateTimeFormat(locale.value || undefined, {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric"
  })
);

const formatAvailabilityDate = (value: string | null | undefined) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return availabilityDateFormatter.value.format(date);
};

const formatAvailabilityDay = (value: string | null | undefined) => {
  const key = normalizeDay(value);
  const match = dayOfWeekOptions.value.find(option => option.value === key);
  return match?.label ?? value ?? "";
};

const availabilityMetaFormatter = computed(() =>
  new Intl.DateTimeFormat(locale.value || undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  })
);

const formatAvailabilityTimestamp = (value: string | null | undefined) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return availabilityMetaFormatter.value.format(date);
};

const resetAvailabilityForm = () => {
  availabilityForm.startTime = "";
  availabilityForm.endTime = "";
  if (isWeeklyAvailability.value) {
    availabilityForm.dayOfWeek = "MONDAY";
  } else if (isOneTimeAvailability.value) {
    availabilityForm.date = ensureDateString();
  }
};

watch(
  () => [
    availabilityForm.type,
    availabilityForm.dayOfWeek,
    availabilityForm.date,
    availabilityForm.startTime,
    availabilityForm.endTime
  ],
  () => {
    availabilityFormError.value = null;
  }
);

const handleCreateAvailability = async () => {
  availabilityFormError.value = null;

  if (!canSubmitAvailability.value) {
    availabilityFormError.value = t("doctors.edit.availability.validation.required");
    return;
  }

  if (!hasValidTimeRange.value) {
    availabilityFormError.value = t("doctors.edit.availability.validation.timeOrder");
    return;
  }

  const payload: Record<string, string> = {
    type: availabilityForm.type,
    startTime: availabilityForm.startTime,
    endTime: availabilityForm.endTime
  };

  if (isWeeklyAvailability.value) {
    payload.dayOfWeek = availabilityForm.dayOfWeek;
  }

  if (isOneTimeAvailability.value) {
    payload.date = availabilityForm.date;
  }

  try {
    availabilitySubmitting.value = true;
    await request<DoctorAvailability>(`/doctors/${doctorId.value}/availability`, {
      method: "POST",
      body: payload
    });

    toast.add({
      title: t("doctors.edit.toasts.availabilityCreated"),
      description: t("doctors.edit.toasts.availabilityCreatedDescription"),
      color: "green",
      icon: "i-lucide-check-circle"
    });

    resetAvailabilityForm();
    await refreshAvailability();
  } catch (err: any) {
    availabilityFormError.value = err?.data?.message ?? err?.message ?? t("doctors.edit.availability.validation.genericError");
    toast.add({
      title: t("doctors.edit.toasts.availabilityError"),
      description: availabilityFormError.value,
      color: "red",
      icon: "i-lucide-alert-circle"
    });
  } finally {
    availabilitySubmitting.value = false;
  }
};

const handleDeleteAvailability = async (availabilityId: number) => {
  try {
    deletingAvailabilityId.value = availabilityId;
    await request(`/doctors/${doctorId.value}/availability/${availabilityId}`, {
      method: "DELETE"
    });

    toast.add({
      title: t("doctors.edit.toasts.availabilityDeleted"),
      description: t("doctors.edit.toasts.availabilityDeletedDescription"),
      color: "green",
      icon: "i-lucide-trash-2"
    });

    await refreshAvailability();
  } catch (err: any) {
    toast.add({
      title: t("doctors.edit.toasts.availabilityError"),
      description: err?.data?.message ?? err?.message ?? t("doctors.edit.availability.validation.genericError"),
      color: "red",
      icon: "i-lucide-alert-circle"
    });
  } finally {
    deletingAvailabilityId.value = null;
  }
};

const {
  data: doctorPlansData,
  pending: plansLoading,
  error: plansError,
  refresh: refreshDoctorPlans
} = await useAsyncData(`admin-doctor-${doctorId.value}-treatment-plans`, async () => {
  return await treatmentPlansApi.getAll({ doctorId: doctorId.value })
})

const doctorPlans = computed<TreatmentPlan[]>(() => doctorPlansData.value ?? [])
const plansPageSize = 5
const plansPage = ref(1)

const totalPlanPages = computed(() => {
  if (!doctorPlans.value.length) return 1
  return Math.max(1, Math.ceil(doctorPlans.value.length / plansPageSize))
})

const paginatedDoctorPlans = computed(() => {
  if (!doctorPlans.value.length) return []
  const start = (plansPage.value - 1) * plansPageSize
  return doctorPlans.value.slice(start, start + plansPageSize)
})

const planRange = computed(() => {
  if (!doctorPlans.value.length) {
    return { start: 0, end: 0, total: 0 }
  }
  const start = (plansPage.value - 1) * plansPageSize + 1
  const end = Math.min(start + plansPageSize - 1, doctorPlans.value.length)
  return { start, end, total: doctorPlans.value.length }
})

const hasPlanPagination = computed(() => doctorPlans.value.length > plansPageSize)
const canGoPrevPlanPage = computed(() => plansPage.value > 1)
const canGoNextPlanPage = computed(() => plansPage.value < totalPlanPages.value)

const goToPrevPlanPage = () => {
  if (canGoPrevPlanPage.value) {
    plansPage.value -= 1
  }
}

const goToNextPlanPage = () => {
  if (canGoNextPlanPage.value) {
    plansPage.value += 1
  }
}

watch(doctorPlans, (plans) => {
  const maxPage = Math.max(1, Math.ceil((plans?.length ?? 0) / plansPageSize))
  if (plansPage.value > maxPage) {
    plansPage.value = maxPage
  }
  if (plansPage.value < 1) {
    plansPage.value = 1
  }
}, { immediate: true })

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

const planDateFormatter = computed(() =>
  new Intl.DateTimeFormat(locale.value || undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  })
)

const toValidPlanDate = (input: string | number | null | undefined): Date | null => {
  if (input === null || input === undefined) return null

  const normalize = (value: number) => {
    const timestamp = value < 10000000000 ? value * 1000 : value
    const date = new Date(timestamp)
    return Number.isNaN(date.getTime()) || date.getTime() <= 0 ? null : date
  }

  if (typeof input === 'number') {
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

const formatPlanDate = (value: string | number | null | undefined) => {
  const date = toValidPlanDate(value)
  if (!date) return '—'
  return planDateFormatter.value.format(date)
}

const serviceOptions = computed(() => {
  const useArabic = locale.value?.startsWith("ar");

  return (servicesData.value ?? []).map(service => {
    const primary = useArabic ? service.nameAr : service.nameEn;
    const secondary = useArabic ? service.nameEn : service.nameAr;
    const fallback = service.slug;
    const label = (primary || secondary || fallback || "").trim() || fallback;
    return {
      label,
      value: service.id.toString()
    };
  }).sort((a, b) => a.label.localeCompare(b.label, locale.value || undefined));
});

const form = reactive({
  fullNameEn: "",
  fullNameAr: "",
  email: "",
  phone: "",
  specialtyEn: "",
  specialtyAr: "",
  imageUrl: "",
  bioEn: "",
  bioAr: "",
  locales: [] as string[],
  serviceIds: [] as string[],
  displayOrder: 0,
  isActive: true
});

const errors = ref<Record<string, string>>({});
const submitting = ref(false);

// Validation functions
const validateEmail = (email: string): boolean => {
  if (!email.trim()) return true; // Optional field
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const validateUrl = (url: string): boolean => {
  if (!url.trim()) return true; // Optional field
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

const validatePhone = (phone: string): boolean => {
  if (!phone.trim()) return true; // Optional field
  const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/
  return phoneRegex.test(phone.replace(/\s/g, ''))
}

// Individual field validation
const validateField = (fieldName: string) => {
  switch (fieldName) {
    case 'fullNameEn':
      if (!form.fullNameEn.trim()) {
        errors.value.fullNameEn = t('doctors.edit.validation.fullNameRequired')
      } else {
        delete errors.value.fullNameEn
      }
      break
    case 'email':
      if (form.email && !validateEmail(form.email)) {
        errors.value.email = t('doctors.edit.validation.invalidEmail')
      } else {
        delete errors.value.email
      }
      break
    case 'phone':
      if (form.phone && !validatePhone(form.phone)) {
        errors.value.phone = t('doctors.edit.validation.invalidPhone')
      } else {
        delete errors.value.phone
      }
      break
    case 'imageUrl':
      if (form.imageUrl && !validateUrl(form.imageUrl)) {
        errors.value.imageUrl = t('doctors.edit.validation.invalidUrl')
      } else {
        delete errors.value.imageUrl
      }
      break
    case 'displayOrder':
      if (form.displayOrder < 0) {
        errors.value.displayOrder = t('doctors.edit.validation.invalidDisplayOrder')
      } else {
        delete errors.value.displayOrder
      }
      break
  }
}

// Form validation
const validateForm = (): boolean => {
  // Validate all fields
  validateField('fullNameEn')
  validateField('email')
  validateField('phone')
  validateField('imageUrl')
  validateField('displayOrder')

  return Object.keys(errors.value).length === 0
}

// Clear field error when user starts typing
const clearFieldError = (fieldName: string) => {
  if (errors.value[fieldName]) {
    delete errors.value[fieldName]
  }
}

// Form validation state
const isFormValid = computed(() => {
  return form.fullNameEn.trim() && Object.keys(errors.value).length === 0
})

// Image handling
const handleImageError = () => {
  errors.value.imageUrl = t('doctors.edit.validation.invalidUrl')
}

const handleImageLoad = () => {
  delete errors.value.imageUrl
}

const handleImageUploadSuccess = () => {
  delete errors.value.imageUrl
}

const languageOptions = computed(() => [
  { label: t('doctors.common.languages.english'), value: "en" },
  { label: t('doctors.common.languages.arabic'), value: "ar" },
  { label: t('doctors.common.languages.russian'), value: "ru" }
]);

// Populate form when doctor data loads
watchEffect(() => {
  if (!doctor.value) return;
  form.fullNameEn = doctor.value.fullNameEn ?? (doctor.value as any).fullName ?? "";
  form.fullNameAr = doctor.value.fullNameAr ?? "";
  form.email = doctor.value.email ?? "";
  form.phone = doctor.value.phone ?? "";
  form.specialtyEn = doctor.value.specialtyEn ?? (doctor.value as any).specialty ?? "";
  form.specialtyAr = doctor.value.specialtyAr ?? "";
  form.bioEn = doctor.value.bioEn ?? (doctor.value as any).bio ?? "";
  form.bioAr = doctor.value.bioAr ?? "";
  form.imageUrl = doctor.value.imageUrl ?? "";
  form.locales = doctor.value.locales ?? [];
  form.serviceIds = doctor.value.services?.map(service => service.id.toString()) ?? [];
  form.displayOrder = doctor.value.displayOrder ?? 0;
  form.isActive = doctor.value.isActive ?? true;
});

const handleSubmit = async () => {
  console.log('handleSubmit called');
  console.log('validateForm():', validateForm());
  console.log('doctor.value:', doctor.value);
  
  if (!validateForm() || !doctor.value) {
    console.log('Validation failed or no doctor data');
    return;
  }

  try {
    submitting.value = true;
    console.log('Starting form submission');

    const data = {
      fullNameEn: form.fullNameEn.trim(),
      fullNameAr: form.fullNameAr.trim() || null,
      email: form.email.trim() || null,
      phone: form.phone.trim() || null,
      specialtyEn: form.specialtyEn.trim() || null,
      specialtyAr: form.specialtyAr.trim() || null,
      imageUrl: form.imageUrl.trim() || null,
      bioEn: form.bioEn.trim() || null,
      bioAr: form.bioAr.trim() || null,
      locales: form.locales,
      serviceIds: form.serviceIds.map(id => Number(id)),
      displayOrder: form.displayOrder,
      isActive: form.isActive
    };

    console.log('Making API request to update doctor');
    await request(`/doctors/${doctor.value.id}`, {
      method: "PUT",
      body: data
    });
    console.log('API request successful');

    console.log('About to show success toast');
    toast.add({
      title: "Doctor Updated Successfully",
      description: "The doctor profile has been updated successfully.",
      color: "green",
      icon: "i-lucide-check-circle"
    });
    console.log('Success toast added');
    
    // Small delay to ensure toast is displayed before redirect
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Redirect to doctors list
    await router.push('/doctors')
  } catch (error: any) {
    console.error('Failed to update doctor:', error);
    
    // Handle server validation errors
    if (error.status === 422 && error.data?.errors) {
      // Map server errors to form fields
      Object.keys(error.data.errors).forEach(key => {
        errors.value[key] = error.data.errors[key]
      })
    } else if (error.status === 404) {
      console.log('About to show 404 toast');
      toast.add({
        title: "Doctor Not Found",
        description: "The doctor you're looking for doesn't exist.",
        color: "red",
        icon: "i-lucide-alert-circle"
      });
      console.log('404 toast added');
      await router.push('/doctors')
    } else {
      console.log('About to show error toast');
      toast.add({
        title: "Update Failed",
        description: error.message || "Failed to update doctor profile. Please try again.",
        color: "red",
        icon: "i-lucide-alert-circle"
      });
      console.log('Error toast added');
    }
  } finally {
    submitting.value = false;
  }
}


</script>
