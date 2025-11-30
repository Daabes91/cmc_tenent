<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50/30 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900">
    <!-- Header Section -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-xl border-b border-slate-200/60 dark:bg-slate-900/80 dark:border-slate-700/60">
      <div class="max-w-7xl mx-auto px-6 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-mint-500 to-mint-300 shadow-lg">
              <UIcon name="i-lucide-calendar-check" class="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-slate-900 dark:text-white">{{ pageTitle || 'Loading…' }}</h1>
              <div class="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-300">
                <UBadge :color="statusColor" variant="soft" size="sm">
                  <UIcon name="i-lucide-badge-check" class="h-3 w-3" />
                  {{ formatStatus(appointment?.status) || t("appointments.detail.badges.status") }}
                </UBadge>
                <UBadge :color="bookingModeColor" variant="soft" size="sm">
                  <UIcon :name="bookingModeIcon" class="h-3 w-3" />
                  {{ bookingModeLabel || t("appointments.detail.badges.bookingMode") }}
                </UBadge>
                <UBadge v-if="appointment?.treatmentPlanId" color="indigo" variant="soft" size="sm">
                  <UIcon name="i-lucide-activity" class="h-3 w-3" />
                  {{ t("appointments.detail.badges.treatmentVisit", { number: appointment.followUpVisitNumber }) }}
                </UBadge>
              </div>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <UButton 
              variant="ghost" 
              color="gray" 
              icon="i-lucide-arrow-left" 
              to="/appointments"
            >
              {{ t("appointments.detail.actions.back") }}
            </UButton>
            <UButton 
              color="emerald" 
              icon="i-lucide-check" 
              :loading="approving"
              :disabled="!canApprove"
              @click="handleApprove"
            >
              {{ t("appointments.detail.actions.approve") }}
            </UButton>
            <UButton 
              color="red" 
              variant="soft"
              icon="i-lucide-x" 
              :loading="declining"
              :disabled="!canDecline"
              @click="handleDecline"
            >
              {{ t("appointments.detail.actions.decline") }}
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
              <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t("appointments.detail.alerts.loadTitle") }}</h3>
              <p class="text-sm text-slate-600 dark:text-slate-300 mt-1">
                {{ error.message || t("appointments.detail.alerts.loadDescription") }}
              </p>
            </div>
            <UButton
              color="violet"
              icon="i-lucide-arrow-left"
              @click="router.push('/appointments')"
            >
              {{ t("appointments.detail.actions.back") }}
            </UButton>
          </div>
        </div>
      </div>

      <div v-else class="grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <!-- Details Form -->
        <div class="space-y-6">
          <div v-if="pending" class="space-y-6">
            <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-6 space-y-4">
              <USkeleton class="h-12 rounded-2xl" />
              <USkeleton class="h-12 rounded-2xl" />
              <USkeleton class="h-12 rounded-2xl" />
            </div>
          </div>

          <UForm v-else :state="form" @submit.prevent="handleSave">
            <div class="space-y-6">
              <!-- Assignment Details -->
              <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
                <div class="bg-gradient-to-r from-mint-500 to-mint-300 px-6 py-4">
                  <div class="flex items-center gap-3">
                    <UIcon name="i-lucide-calendar-check" class="h-5 w-5 text-white" />
                    <div>
                      <h2 class="text-lg font-semibold text-white">{{ t("appointments.detail.cards.assignment.title") }}</h2>
                      <p class="text-sm text-violet-100">{{ t("appointments.detail.cards.assignment.subtitle") }}</p>
                    </div>
                  </div>
                </div>

                <div class="p-6">

                  <div class="space-y-6">
                    <div class="grid gap-6 md:grid-cols-2">
                      <UFormGroup :label="t('appointments.detail.fields.patient')" required>
                        <USelect
                          v-model="form.patientId"
                          :options="patientOptions"
                          option-attribute="label"
                          value-attribute="value"
                          :placeholder="t('appointments.detail.placeholders.selectPatient')"
                          size="lg"
                        />
                      </UFormGroup>
                      <UFormGroup :label="t('appointments.detail.fields.service')" required>
                        <USelect
                          v-model="form.serviceId"
                          :options="serviceOptions"
                          option-attribute="label"
                          value-attribute="value"
                          :placeholder="t('appointments.detail.placeholders.selectService')"
                          size="lg"
                        />
                      </UFormGroup>
                    </div>

                    <div class="grid gap-6 md:grid-cols-2">
                      <UFormGroup :label="t('appointments.detail.fields.doctor')" required>
                        <USelect
                          v-model="form.doctorId"
                          :options="doctorOptions"
                          option-attribute="label"
                          value-attribute="value"
                          :placeholder="t('appointments.detail.placeholders.selectDoctor')"
                          size="lg"
                        />
                        <template #help v-if="!doctorOptions.length">
                          <div class="flex items-center gap-2 text-amber-600">
                            <UIcon name="i-lucide-alert-triangle" class="h-3.5 w-3.5" />
                            <span>{{ t("appointments.detail.messages.noDoctorForService") }}</span>
                          </div>
                        </template>
                      </UFormGroup>
                      <UFormGroup :label="t('appointments.detail.fields.bookingMode')" required>
                        <USelect
                          v-model="form.bookingMode"
                          :options="bookingModeOptions"
                          option-attribute="label"
                          value-attribute="value"
                          size="lg"
                        />
                      </UFormGroup>
                    </div>

                    <div class="grid gap-6 md:grid-cols-2">
                      <UFormGroup :label="t('appointments.detail.fields.scheduledTime')" :hint="t('appointments.detail.hints.localTime')" required>
                        <UInput 
                          v-model="form.scheduledAt" 
                          type="datetime-local" 
                          :step="scheduleStepSeconds" 
                          size="lg"
                          icon="i-lucide-calendar-clock"
                        />
                      </UFormGroup>
                      <UFormGroup :label="t('appointments.detail.fields.slotDuration')" :hint="t('appointments.detail.hints.slotDuration')">
                        <UInput
                          v-model="form.slotDurationMinutes"
                          type="number"
                          min="5"
                          max="240"
                          step="5"
                          size="lg"
                          icon="i-lucide-timer"
                        />
                      </UFormGroup>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Visit Tracking Card -->
              <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
                <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
                  <div class="flex items-center gap-3">
                    <UIcon name="i-lucide-clipboard-check" class="h-5 w-5 text-white" />
                    <div>
                      <h2 class="text-lg font-semibold text-white">{{ t("appointments.detail.cards.tracking.title") }}</h2>
                      <p class="text-sm text-emerald-100">
                        {{ appointment?.treatmentPlanId
                          ? t("appointments.detail.cards.tracking.subtitlePlan")
                          : t("appointments.detail.cards.tracking.subtitleStandard") }}
                      </p>
                    </div>
                  </div>
                </div>

                <div class="p-6">

                  <div class="space-y-6">
                    <div v-if="appointment?.treatmentPlanId" class="rounded-xl border border-amber-200 bg-amber-50 dark:bg-amber-900/20 dark:border-amber-800 p-4">
                      <div class="flex items-start gap-3">
                        <UIcon name="i-lucide-info" class="mt-0.5 h-4 w-4 text-amber-600 dark:text-amber-400 flex-shrink-0" />
                        <div class="text-sm text-amber-800 dark:text-amber-200">
                          <p class="font-medium mb-1">{{ t("appointments.detail.cards.tracking.planNotice.title") }}</p>
                          <p>{{ t("appointments.detail.cards.tracking.planNotice.description") }}</p>
                        </div>
                      </div>
                    </div>

                    <div class="grid gap-6 md:grid-cols-2">
                      <UFormGroup :label="appointment?.treatmentPlanId ? t('appointments.detail.fields.basicPaymentFlag') : t('appointments.detail.fields.paymentCollected')">
                        <div class="flex items-center gap-3 p-4 bg-slate-50 dark:bg-slate-700 rounded-xl">
                          <UToggle
                            v-model="form.paymentCollected"
                            size="lg"
                            color="emerald"
                            :disabled="Boolean(appointment?.treatmentPlanId)"
                          />
                          <div>
                            <p class="text-sm font-medium text-slate-900 dark:text-white">
                              {{ form.paymentCollected ? t("appointments.detail.labels.markedPaid") : t("appointments.detail.labels.notPaid") }}
                            </p>
                            <p v-if="appointment?.treatmentPlanId" class="text-xs text-slate-500 dark:text-slate-400">
                              {{ t("appointments.detail.hints.paymentHandledInPlan") }}
                            </p>
                          </div>
                        </div>
                      </UFormGroup>
                      <UFormGroup :label="t('appointments.detail.fields.attendance')">
                        <URadioGroup
                          v-model="form.attendanceStatus"
                          :options="attendanceOptions"
                          class="grid gap-3"
                        />
                      </UFormGroup>
                    </div>

                    <div v-if="isStandaloneAppointment" class="space-y-6">
                      <div class="grid gap-6 md:grid-cols-2">
                        <UFormGroup :label="t('appointments.detail.fields.paymentAmount')">
                          <UInput
                            v-model="form.paymentAmount"
                            type="number"
                            step="0.01"
                            min="0"
                            icon="i-lucide-wallet"
                            :disabled="!form.paymentCollected"
                          />
                        </UFormGroup>
                        <UFormGroup :label="t('appointments.detail.fields.paymentCurrency')">
                          <USelect
                            v-model="form.paymentCurrency"
                            :options="currencyOptions"
                            option-attribute="label"
                            value-attribute="value"
                            :disabled="!form.paymentCollected"
                          />
                        </UFormGroup>
                      </div>
                      <div class="grid gap-6 md:grid-cols-2">
                        <UFormGroup :label="t('appointments.detail.fields.paymentMethod')">
                          <USelect
                            v-model="form.paymentMethod"
                            :options="paymentMethodOptions"
                            option-attribute="label"
                            value-attribute="value"
                            :disabled="!form.paymentCollected"
                          />
                        </UFormGroup>
                        <UFormGroup :label="t('appointments.detail.fields.paymentDate')">
                          <UInput
                            v-model="form.paymentDate"
                            type="datetime-local"
                            :disabled="!form.paymentCollected"
                          />
                        </UFormGroup>
                      </div>
                      <div class="grid gap-6 md:grid-cols-2">
                        <UFormGroup :label="t('appointments.detail.fields.paymentReference')">
                          <UInput
                            v-model="form.paymentReference"
                            :disabled="!form.paymentCollected"
                          />
                        </UFormGroup>
                        <UFormGroup :label="t('appointments.detail.fields.paymentNotes')">
                          <UTextarea
                            v-model="form.paymentNotes"
                            :rows="3"
                            :disabled="!form.paymentCollected"
                          />
                        </UFormGroup>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Treatment Plan Card -->
              <div v-if="appointment?.treatmentPlanId" class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
                <div class="bg-gradient-to-r from-mint-600 to-mint-400 px-6 py-4">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                      <UIcon name="i-lucide-clipboard-list" class="h-5 w-5 text-white" />
                      <div>
                        <h2 class="text-lg font-semibold text-white">{{ t("appointments.detail.cards.treatment.title") }}</h2>
                        <p class="text-sm text-indigo-100">{{ t("appointments.detail.cards.treatment.subtitle") }}</p>
                      </div>
                    </div>
                    <UBadge color="white" variant="soft" size="sm">
                      <UIcon name="i-lucide-activity" class="mr-1 h-3 w-3" />
                      {{ t("appointments.detail.cards.treatment.visitNumber", { number: appointment.followUpVisitNumber || '—' }) }}
                    </UBadge>
                  </div>
                </div>

                <div class="p-6">

                  <div class="space-y-4">
                    <div class="flex items-center justify-between rounded-xl border border-indigo-100 bg-indigo-50/50 dark:bg-indigo-900/20 dark:border-indigo-800 p-4">
                      <div class="flex items-start gap-3">
                        <UIcon name="i-lucide-file-text" class="mt-1 h-5 w-5 text-indigo-600 dark:text-indigo-400" />
                        <div>
                          <p class="text-sm font-semibold text-slate-900 dark:text-white">{{ t("appointments.detail.cards.treatment.linked.title") }}</p>
                          <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{{ t("appointments.detail.cards.treatment.linked.subtitle") }}</p>
                        </div>
                      </div>
                      <UButton
                        color="indigo"
                        variant="soft"
                        size="sm"
                        icon="i-lucide-arrow-right"
                        @click="navigateTo(`/treatment-plans/${appointment.treatmentPlanId}`)"
                      >
                        {{ t("appointments.detail.cards.treatment.linked.action") }}
                      </UButton>
                    </div>

                    <div class="rounded-xl bg-slate-50 dark:bg-slate-700 p-4">
                      <div class="flex items-start gap-3">
                        <UIcon name="i-lucide-info" class="mt-0.5 h-4 w-4 text-slate-600 dark:text-slate-400" />
                        <div class="text-sm text-slate-700 dark:text-slate-300">
                          <p class="font-medium mb-2">{{ t("appointments.detail.cards.treatment.guide.title") }}</p>
                          <ul class="space-y-1 text-xs text-slate-600 dark:text-slate-400">
                            <li v-if="appointment.status === 'COMPLETED' && !isVisitRecordedForAppointment">
                              • {{ t("appointments.detail.cards.treatment.guide.completeRecorded") }}
                            </li>
                            <li v-else-if="isVisitRecordedForAppointment">
                              • {{ t("appointments.detail.cards.treatment.guide.alreadyRecorded") }}
                            </li>
                            <li v-else>
                              • {{ t("appointments.detail.cards.treatment.guide.completeFirst") }}
                            </li>
                            <li>• {{ t("appointments.detail.cards.treatment.guide.paymentNote") }}</li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Internal Notes Card -->
              <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
                <div class="bg-gradient-to-r from-amber-500 to-orange-600 px-6 py-4">
                  <div class="flex items-center gap-3">
                    <UIcon name="i-lucide-file-text" class="h-5 w-5 text-white" />
                    <div>
                      <h2 class="text-lg font-semibold text-white">{{ t("appointments.detail.cards.notes.title") }}</h2>
                      <p class="text-sm text-amber-100">{{ t("appointments.detail.cards.notes.subtitle") }}</p>
                    </div>
                  </div>
                </div>

                <div class="p-6">
                  <UFormGroup :label="t('appointments.detail.placeholders.notes')">
                    <UTextarea
                      v-model="form.notes"
                      :rows="4"
                      size="lg"
                      :placeholder="t('appointments.detail.placeholders.notes')"
                    />
                  </UFormGroup>
                </div>
              </div>

              <!-- Form Actions -->
              <div class="flex justify-end gap-3 pt-6">
                <UButton
                  variant="ghost"
                  color="gray"
                  to="/appointments"
                  :disabled="saving"
                >
                  Cancel
                </UButton>
                <UButton
                  type="submit"
                  color="violet"
                  icon="i-lucide-save"
                  :loading="saving"
                >
                  Save Changes
                </UButton>
              </div>
            </div>
          </UForm>
        </div>

        <!-- Sidebar -->
        <aside class="space-y-6">
          <!-- Appointment Avatar -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 p-6 text-center">
            <div class="relative inline-block">
              <div class="h-20 w-20 rounded-full bg-gradient-to-br from-mint-500 to-mint-300 flex items-center justify-center shadow-lg">
                <UIcon name="i-lucide-calendar-check" class="h-10 w-10 text-white" />
              </div>
              <div class="absolute -bottom-2 -right-2 h-8 w-8 rounded-full bg-gradient-to-br from-mint-500 to-mint-300 flex items-center justify-center">
                <UIcon name="i-lucide-clock" class="h-4 w-4 text-white" />
              </div>
            </div>
            <h3 class="mt-4 text-lg font-semibold text-slate-900 dark:text-white">{{ pageTitle || 'Loading…' }}</h3>
            <p class="text-sm text-slate-500 dark:text-slate-400">{{ formatDateTime(appointment?.scheduledAt) || 'Not scheduled' }}</p>
          </div>

          <!-- Quick Info -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-blue-500 to-cyan-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-info" class="h-5 w-5 text-white" />
                <h3 class="text-lg font-semibold text-white">{{ t("appointments.detail.cards.quickInfo.title") }}</h3>
              </div>
            </div>
            <div class="p-6">

              <ul class="space-y-4 text-sm text-slate-600 dark:text-slate-300">
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-100 dark:bg-emerald-900/20">
                    <UIcon name="i-lucide-user" class="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
                  </div>
                  <div class="flex flex-col">
                    <span class="text-xs text-slate-500 dark:text-slate-400">{{ t("appointments.detail.labels.patient") }}</span>
                    <span class="font-medium text-slate-900 dark:text-white">{{ appointment?.patient?.name || t("appointments.detail.labels.notAssigned") }}</span>
                  </div>
                </li>
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-100 dark:bg-blue-900/20">
                    <UIcon name="i-lucide-stethoscope" class="h-4 w-4 text-blue-600 dark:text-blue-400" />
                  </div>
                  <div class="flex flex-col">
                    <span class="text-xs text-slate-500 dark:text-slate-400">{{ t("appointments.detail.labels.doctor") }}</span>
                    <span class="font-medium text-slate-900 dark:text-white">{{ appointment?.doctor?.name || t("appointments.detail.labels.notAssigned") }}</span>
                  </div>
                </li>
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-mint-100 dark:bg-mint-900/20">
                    <UIcon name="i-lucide-clipboard-list" class="h-4 w-4 text-mint-600 dark:text-mint-400" />
                  </div>
                  <div class="flex flex-col">
                    <span class="text-xs text-slate-500 dark:text-slate-400">{{ t("appointments.detail.labels.service") }}</span>
                    <span class="font-medium text-slate-900 dark:text-white">{{ appointment?.service?.name || t("appointments.detail.labels.notAssigned") }}</span>
                  </div>
                </li>
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-100 dark:bg-amber-900/20">
                    <UIcon name="i-lucide-clock" class="h-4 w-4 text-amber-600 dark:text-amber-400" />
                  </div>
                  <div class="flex flex-col">
                    <span class="text-xs text-slate-500 dark:text-slate-400">{{ t("appointments.detail.labels.scheduledTime") }}</span>
                    <span class="font-medium text-slate-900 dark:text-white">{{ formatDateTime(appointment?.scheduledAt) }}</span>
                  </div>
                </li>
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-green-100 dark:bg-green-900/20">
                    <UIcon name="i-lucide-badge-check" class="h-4 w-4 text-green-600 dark:text-green-400" />
                  </div>
                  <div class="flex flex-col">
                    <span class="text-xs text-slate-500 dark:text-slate-400">{{ t("appointments.detail.labels.status") }}</span>
                    <UBadge :color="statusColor" variant="soft" size="sm" class="w-fit">
                      {{ formatStatus(appointment?.status) }}
                    </UBadge>
                  </div>
                </li>
                <li class="flex items-center gap-3">
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg bg-cyan-100 dark:bg-cyan-900/20">
                    <UIcon name="i-lucide-wallet" class="h-4 w-4 text-cyan-600 dark:text-cyan-400" />
                  </div>
                  <div class="flex flex-col">
                    <span class="text-xs text-slate-500 dark:text-slate-400">{{ t("appointments.detail.labels.payment") }}</span>
                    <div class="flex flex-col gap-1">
                      <UBadge :color="appointment?.paymentCollected ? 'green' : 'gray'" variant="soft" size="sm" class="w-fit">
                        {{ appointment?.paymentCollected ? t("appointments.payment.paid") : t("appointments.payment.unpaid") }}
                      </UBadge>
                      <span
                        v-if="appointment?.paymentCollected && appointment?.paymentAmount !== null"
                        class="text-xs text-slate-500 dark:text-slate-400"
                      >
                        {{ formatCurrencyValue(appointment?.paymentAmount ?? null, appointment?.paymentCurrency ?? null) }}
                      </span>
                      <span
                        v-if="appointment?.paymentMethod"
                        class="text-xs text-slate-500 dark:text-slate-400"
                      >
                        {{ t("appointments.detail.labels.paymentMethod") }}: {{ paymentMethodLabel(appointment?.paymentMethod) }}
                      </span>
                      <span
                        v-if="appointment?.paymentDate"
                        class="text-xs text-slate-500 dark:text-slate-400"
                      >
                        {{ t("appointments.detail.labels.paymentDate") }}: {{ formatDateTime(appointment?.paymentDate) }}
                      </span>
                      <span
                        v-if="appointment?.paymentReference"
                        class="text-xs text-slate-500 dark:text-slate-400"
                      >
                        {{ t("appointments.detail.labels.paymentReference") }}: {{ appointment?.paymentReference }}
                      </span>
                    </div>
                  </div>
                </li>
              </ul>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-sm border border-slate-200/60 dark:border-slate-700/60 overflow-hidden">
            <div class="bg-gradient-to-r from-emerald-500 to-teal-600 px-6 py-4">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-zap" class="h-5 w-5 text-white" />
                <h3 class="text-lg font-semibold text-white">{{ t("appointments.detail.cards.quickActions.title") }}</h3>
              </div>
            </div>
            <div class="p-6 space-y-3">
              <UButton
                block
                color="emerald"
                variant="soft"
                icon="i-lucide-check-circle"
                :disabled="!canApprove || approving"
                :loading="approving"
                @click="handleApprove"
              >
                {{ t("appointments.detail.actions.approveFull") }}
              </UButton>
              <UButton
                block
                color="red"
                variant="soft"
                icon="i-lucide-x-circle"
                :disabled="!canDecline || declining"
                :loading="declining"
                @click="handleDecline"
              >
                {{ t("appointments.detail.actions.declineFull") }}
              </UButton>
              <UButton
                block
                color="gray"
                variant="outline"
                icon="i-lucide-trash-2"
                :loading="deleting"
                @click="deleteConfirmOpen = true"
              >
                {{ t("appointments.detail.actions.delete") }}
              </UButton>
              <UButton
                v-if="appointment?.treatmentPlanId"
                block
                color="indigo"
                variant="outline"
                icon="i-lucide-clipboard-list"
                @click="navigateTo(`/treatment-plans/${appointment.treatmentPlanId}`)"
              >
                View Treatment Plan
              </UButton>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <UModal v-model="deleteConfirmOpen">
      <UCard>
        <template #header>
          <div>
            <p class="text-sm font-medium text-red-600">{{ t("appointments.detail.modal.title") }}</p>
            <h3 class="text-lg font-semibold text-slate-900 dark:text-white">{{ t("appointments.detail.modal.subtitle") }}</h3>
          </div>
        </template>
        <p class="text-sm text-slate-600 dark:text-slate-300">
          {{ t("appointments.detail.modal.body") }}
        </p>
        <div class="mt-6 flex justify-end gap-3">
          <UButton variant="ghost" color="gray" :disabled="deleting" @click="deleteConfirmOpen = false">
            {{ t("common.actions.cancel") }}
          </UButton>
          <UButton color="red" :loading="deleting" @click="handleDelete">
            {{ t("common.actions.delete") }}
          </UButton>
        </div>
      </UCard>
    </UModal>
  </div>
</template>

<script setup lang="ts">
import type { AppointmentAdminDetail } from "@/types/appointments";
import type { PatientAdmin } from "@/types/patients";
import type { DoctorAdmin } from "@/types/doctors";
import type { AdminServiceSummary } from "@/types/services";
import { computed, ref, reactive, watchEffect } from "vue";
import { useAppointmentTime } from "../../composables/useAppointmentTime";

const toast = useToast();
const router = useRouter();
const route = useRoute();
const { t, locale } = useI18n();
const { fetcher, request } = useAdminApi();
const runtimeConfig = useRuntimeConfig();
const defaultCurrency =
  (runtimeConfig.public?.defaultCurrency as string | undefined)?.toUpperCase?.() || "JOD";
const selectPaddingUi = {
  trailing: {
    padding: {
      sm: "pe-12",
      md: "pe-12",
      lg: "pe-12"
    }
  }
};
const bookingModeOptions = computed(() => [
  { value: "CLINIC_VISIT", label: t("appointments.modes.clinic") },
  { value: "VIRTUAL_CONSULTATION", label: t("appointments.modes.virtual") }
]);
const currencyOptions = computed(() => [
  { value: "JOD", label: t("treatmentPlans.create.form.currency.options.jod") },
  { value: "USD", label: t("treatmentPlans.create.form.currency.options.usd") },
  { value: "AED", label: t("treatmentPlans.create.form.currency.options.aed") }
]);
const paymentMethodOptions = computed(() => [
  { value: "CASH", label: t("treatmentPlans.common.paymentMethods.cash") },
  { value: "POS", label: t("treatmentPlans.common.paymentMethods.pos") },
  { value: "BANK_TRANSFER", label: t("treatmentPlans.common.paymentMethods.bankTransfer") },
  { value: "PAYPAL", label: t("treatmentPlans.common.paymentMethods.paypal") },
  { value: "OTHER", label: t("treatmentPlans.common.paymentMethods.other") }
]);

const attendanceOptions = computed(() => [
  { value: "PENDING", label: t("appointments.attendance.pending") },
  { value: "ATTENDED", label: t("appointments.attendance.attended") },
  { value: "NO_SHOW", label: t("appointments.attendance.noShow") }
]);

useHead(() => ({
  title: t("appointments.detail.meta.title")
}));

const { settings: clinicSettings } = useClinicSettings();
const defaultSlotDurationMinutes = computed(() => clinicSettings.value?.slotDurationMinutes ?? 30);

// Use the appointment time composable for consistent time handling
const { 
  formatAppointmentTime, 
  formatForInput, 
  parseInputForAPI, 
  validateAppointmentTime,
  timezone,
  abbreviation 
} = useAppointmentTime();

const appointmentId = computed(() => Number(route.params.id));
if (Number.isNaN(appointmentId.value)) {
  throw createError({ statusCode: 404, statusMessage: t("appointments.detail.errors.notFound") });
}

const { data: patientsData } = await useAsyncData("admin-appointment-patients", () =>
  fetcher<PatientAdmin[]>("/patients", [])
);
const { data: doctorsData } = await useAsyncData("admin-appointment-doctors", () =>
  fetcher<DoctorAdmin[]>("/doctors", [])
);
const { data: servicesData } = await useAsyncData("admin-appointment-services", () =>
  fetcher<AdminServiceSummary[]>("/services", [])
);

const { data, pending, error, refresh } = await useAsyncData(
  `admin-appointment-${appointmentId.value}`,
  () => request<AppointmentAdminDetail>(`/appointments/${appointmentId.value}`)
);

const appointment = computed(() => data.value ?? null);

function extractItems<T>(value: any): T[] {
  if (Array.isArray(value)) {
    return value as T[];
  }
  if (value && Array.isArray((value as any).content)) {
    return (value as any).content as T[];
  }
  if (value && Array.isArray((value as any).data)) {
    return (value as any).data as T[];
  }
  return [];
}

const patients = computed(() => extractItems<PatientAdmin>(patientsData.value));
const doctors = computed(() => extractItems<DoctorAdmin>(doctorsData.value));
const services = computed(() => extractItems<AdminServiceSummary>(servicesData.value));
const isGuestAppointment = computed(() => Boolean(appointment.value && !appointment.value.patient));

const form = reactive({
  patientId: null as string | null,
  doctorId: null as string | null,
  serviceId: null as string | null,
  bookingMode: "CLINIC_VISIT",
  scheduledAt: "",
  slotDurationMinutes: defaultSlotDurationMinutes.value.toString(),
  notes: "",
  paymentCollected: false,
  attendanceStatus: "PENDING" as "PENDING" | "ATTENDED" | "NO_SHOW",
  paymentAmount: "",
  paymentCurrency: defaultCurrency,
  paymentMethod: "CASH",
  paymentDate: "",
  paymentReference: "",
  paymentNotes: ""
});

watchEffect(() => {
  if (!appointment.value) return;
  form.patientId = appointment.value.patient?.id != null ? appointment.value.patient.id.toString() : "guest";
  form.doctorId = appointment.value.doctor?.id != null ? appointment.value.doctor.id.toString() : null;
  form.serviceId = appointment.value.service?.id != null ? appointment.value.service.id.toString() : null;
  form.bookingMode = appointment.value.bookingMode ?? "CLINIC_VISIT";
  form.scheduledAt = formatForInput(appointment.value.scheduledAt);
  form.notes = appointment.value.notes ?? "";
  const slotDuration = appointment.value.slotDurationMinutes ?? defaultSlotDurationMinutes.value;
  form.slotDurationMinutes = slotDuration ? slotDuration.toString() : defaultSlotDurationMinutes.value.toString();
  form.paymentCollected = Boolean(appointment.value.paymentCollected);
  const attendance = appointment.value.patientAttended;
  form.attendanceStatus = attendance === null || attendance === undefined
    ? "PENDING"
    : attendance
      ? "ATTENDED"
      : "NO_SHOW";
  if (appointment.value.treatmentPlanId) {
    form.paymentAmount = "";
    form.paymentCurrency = defaultCurrency;
    form.paymentMethod = "CASH";
    form.paymentDate = "";
    form.paymentReference = "";
    form.paymentNotes = "";
  } else {
    form.paymentAmount = appointment.value.paymentAmount != null
      ? appointment.value.paymentAmount.toString()
      : "";
    form.paymentCurrency = appointment.value.paymentCurrency ?? defaultCurrency;
    form.paymentMethod = appointment.value.paymentMethod ?? "CASH";
    form.paymentDate = formatForInput(appointment.value.paymentDate);
    form.paymentReference = appointment.value.paymentReference ?? "";
    form.paymentNotes = appointment.value.paymentNotes ?? "";
  }
});

const scheduleStepSeconds = computed(() => {
  const raw = form.slotDurationMinutes || defaultSlotDurationMinutes.value.toString();
  const parsed = Number(raw);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return 300;
  }
  const alignment = Math.max(5, Math.min(60, Math.round(parsed)));
  return alignment * 60;
});

const patientOptions = computed(() =>
  {
    const optionList = patients.value.map(patient => ({
      label: `${patient.firstName} ${patient.lastName}`,
      value: patient.id.toString()
    }));

    if (appointment.value?.patient?.id != null) {
      const id = appointment.value.patient.id.toString();
      const alreadyPresent = optionList.some(option => option.value === id);
      if (!alreadyPresent) {
        optionList.unshift({
          label: appointment.value.patient.name || t("appointments.detail.labels.patient"),
          value: id
        });
      }
    } else if (isGuestAppointment.value) {
      optionList.unshift({
        label: t("appointments.detail.labels.guestPatient"),
        value: "guest"
      });
    }

    return optionList;
  }
);

const serviceOptions = computed(() =>
  services.value.map(service => ({
    label: service.nameEn ?? service.nameAr ?? service.slug,
    value: service.id.toString()
  }))
);

const doctorOptions = computed(() => {
  const selectedService = form.serviceId ? Number(form.serviceId) : null;
  return doctors.value
    .filter(doctor => (selectedService ? doctor.services.some(service => service.id === selectedService) : true))
    .map(doctor => ({
      label: doctor.fullNameEn || doctor.fullNameAr || (doctor as any).fullName,
      value: doctor.id.toString()
    }));
});

const isStandaloneAppointment = computed(() => !appointment.value?.treatmentPlanId);

const saving = ref(false);
const approving = ref(false);
const declining = ref(false);
const deleting = ref(false);
const deleteConfirmOpen = ref(false);

const canApprove = computed(() => appointment.value?.status === "SCHEDULED");
const canDecline = computed(() => appointment.value ? appointment.value.status === "SCHEDULED" || appointment.value.status === "CONFIRMED" : false);

// Placeholder for visit recorded check - would need treatment plan data to verify
// For now, we'll show guidance text instead of checking actual state
const isVisitRecordedForAppointment = computed(() => false);

const statusColor = computed(() => {
  switch (appointment.value?.status) {
    case "CONFIRMED":
      return "emerald";
    case "COMPLETED":
      return "blue";
    case "CANCELLED":
      return "red";
    case "SCHEDULED":
    default:
      return "violet";
  }
});

const bookingModeColor = computed(() =>
  appointment.value?.bookingMode === "VIRTUAL_CONSULTATION" ? "blue" : "violet"
);

const bookingModeIcon = computed(() =>
  appointment.value?.bookingMode === "VIRTUAL_CONSULTATION" ? "i-lucide-video" : "i-lucide-building-2"
);

const bookingModeLabel = computed(() => formatMode(appointment.value?.bookingMode));

const pageTitle = computed(() => {
  if (appointment.value?.patient?.name) {
    const doctorName = appointment.value?.doctor?.name ?? t("appointments.detail.labels.tbdDoctor");
    return t("appointments.detail.pageTitle.withNames", {
      patient: appointment.value.patient.name,
      doctor: doctorName
    });
  }
  return t("appointments.detail.pageTitle.default");
});

const statusLabels = computed(() => ({
  SCHEDULED: t("appointments.status.scheduled"),
  CONFIRMED: t("appointments.status.confirmed"),
  COMPLETED: t("appointments.status.completed"),
  CANCELLED: t("appointments.status.cancelled")
}));

const modeLabels = computed(() => ({
  VIRTUAL_CONSULTATION: t("appointments.modes.virtual"),
  CLINIC_VISIT: t("appointments.modes.clinic")
}));



function formatStatus(status: string | undefined) {
  if (!status) return "";
  return statusLabels.value[status as keyof typeof statusLabels.value] ?? status;
}

function formatMode(mode: string | undefined) {
  return modeLabels.value[mode as keyof typeof modeLabels.value] ?? modeLabels.value.CLINIC_VISIT;
}

function attendanceStatusLabelFromForm(status: "PENDING" | "ATTENDED" | "NO_SHOW") {
  if (status === "ATTENDED") return t("appointments.attendance.attended");
  if (status === "NO_SHOW") return t("appointments.attendance.noShow");
  return t("appointments.attendance.pending");
}

function attendanceStatusBadgeColor(status: "PENDING" | "ATTENDED" | "NO_SHOW") {
  if (status === "ATTENDED") return "green";
  if (status === "NO_SHOW") return "red";
  return "gray";
}

function formatDateTime(dateTimeString: string | number | undefined) {
  if (!dateTimeString) return t("appointments.detail.labels.notScheduled");
  // Use the new appointment time composable for consistent formatting
  const formatted = formatAppointmentTime(dateTimeString);
  if (formatted === "Invalid date") {
    return t("appointments.detail.labels.invalidDate");
  }
  return formatted;
}

function formatCurrencyValue(amount?: number | null, currency?: string | null) {
  if (amount === null || amount === undefined) {
    return "—";
  }
  const numericAmount = Number(amount);
  if (!Number.isFinite(numericAmount)) {
    return "—";
  }
  const safeCurrency = currency && currency.trim().length > 0
    ? currency.trim().toUpperCase().slice(0, 3)
    : defaultCurrency;
  try {
    return new Intl.NumberFormat(locale.value || undefined, {
      style: "currency",
      currency: safeCurrency
    }).format(numericAmount);
  } catch (error) {
    return `${numericAmount.toFixed(2)} ${safeCurrency}`;
  }
}

function paymentMethodLabel(method?: string | null) {
  if (!method) return null;
  const option = paymentMethodOptions.value.find(item => item.value === method);
  return option?.label ?? method;
}

async function handleSave() {
  if (!appointment.value) return;
  
  // Validate appointment time if provided
  if (form.scheduledAt && !validateAppointmentTime(form.scheduledAt)) {
    toast.add({
      title: t("appointments.detail.toasts.validationError.title"),
      description: t("appointments.detail.toasts.validationError.invalidTime"),
      color: "red"
    });
    return;
  }
  
  // Validate payment date if provided
  if (form.paymentDate && !validateAppointmentTime(form.paymentDate)) {
    toast.add({
      title: t("appointments.detail.toasts.validationError.title"),
      description: t("appointments.detail.toasts.validationError.invalidPaymentDate"),
      color: "red"
    });
    return;
  }
  
  saving.value = true;
  try {
    const attendanceValue = form.attendanceStatus === "PENDING"
      ? null
      : form.attendanceStatus === "ATTENDED";
    const standalone = !appointment.value.treatmentPlanId;
    const patientIdValue =
      form.patientId && form.patientId !== "guest"
        ? Number(form.patientId)
        : null;
    let paymentAmount: number | null = null;
    if (form.paymentAmount !== "" && form.paymentAmount !== null && form.paymentAmount !== undefined) {
      const parsed = Number(form.paymentAmount);
      paymentAmount = Number.isFinite(parsed) ? parsed : null;
    }
    const normalizedCurrency = form.paymentCurrency
      ? form.paymentCurrency.trim().toUpperCase().slice(0, 3)
      : null;
    const parsedSlotDuration = Number(form.slotDurationMinutes);
    const slotDurationMinutes = Number.isFinite(parsedSlotDuration)
      ? Math.min(Math.max(Math.round(parsedSlotDuration), 5), 240)
      : defaultSlotDurationMinutes.value;
    const paymentPayload = standalone
      ? {
          paymentAmount: form.paymentCollected && paymentAmount !== null ? paymentAmount : null,
          paymentMethod: form.paymentCollected ? form.paymentMethod || null : null,
          paymentCurrency: form.paymentCollected ? normalizedCurrency : null,
          paymentDate: form.paymentCollected && form.paymentDate
            ? (() => {
                try {
                  return parseInputForAPI(form.paymentDate);
                } catch (error) {
                  console.error('Error parsing payment date:', error);
                  throw new Error(t("appointments.detail.timeErrors.invalidPaymentDate"));
                }
              })()
            : null,
          paymentReference: form.paymentCollected ? form.paymentReference.trim() || null : null,
          paymentNotes: form.paymentCollected ? form.paymentNotes.trim() || null : null
        }
      : {
          paymentAmount: null,
          paymentMethod: null,
          paymentCurrency: null,
          paymentDate: null,
          paymentReference: null,
          paymentNotes: null
        };
    await request(`/appointments/${appointment.value.id}`, {
      method: "PUT",
      body: {
        patientId: patientIdValue,
        doctorId: form.doctorId ? Number(form.doctorId) : null,
        serviceId: form.serviceId ? Number(form.serviceId) : null,
        scheduledAt: form.scheduledAt ? (() => {
          try {
            return parseInputForAPI(form.scheduledAt);
          } catch (error) {
            console.error('Error parsing scheduled time:', error);
            throw new Error(t("appointments.detail.timeErrors.invalidScheduledTime"));
          }
        })() : null,
        bookingMode: form.bookingMode,
        notes: form.notes.trim() || null,
        paymentCollected: form.paymentCollected,
        patientAttended: attendanceValue,
        slotDurationMinutes,
        ...paymentPayload
      }
    });
    toast.add({ title: t("appointments.detail.toasts.updateSuccess") });
    await refresh();
  } catch (error: any) {
    toast.add({
      title: t("appointments.detail.toasts.updateError.title"),
      description: error?.data?.message ?? error?.message ?? t("common.errors.unexpected"),
      color: "red"
    });
  } finally {
    saving.value = false;
  }
}

async function handleApprove() {
  if (!appointment.value || !canApprove.value) return;
  approving.value = true;
  try {
    await request(`/appointments/${appointment.value.id}/approve`, { method: "POST" });
    toast.add({ title: t("appointments.detail.toasts.approveSuccess") });
    await refresh();
  } catch (error: any) {
    toast.add({
      title: t("appointments.detail.toasts.approveError.title"),
      description: error?.data?.message ?? error?.message ?? t("common.errors.unexpected"),
      color: "red"
    });
  } finally {
    approving.value = false;
  }
}

async function handleDecline() {
  if (!appointment.value || !canDecline.value) return;
  declining.value = true;
  try {
    await request(`/appointments/${appointment.value.id}/decline`, { method: "POST" });
    toast.add({ title: t("appointments.detail.toasts.declineSuccess") });
    await refresh();
  } catch (error: any) {
    toast.add({
      title: t("appointments.detail.toasts.declineError.title"),
      description: error?.data?.message ?? error?.message ?? t("common.errors.unexpected"),
      color: "red"
    });
  } finally {
    declining.value = false;
  }
}

async function handleDelete() {
  if (!appointment.value) return;
  deleting.value = true;
  try {
    await request(`/appointments/${appointment.value.id}`, { method: "DELETE" });
    toast.add({ title: t("appointments.detail.toasts.deleteSuccess") });
    router.push("/appointments");
  } catch (error: any) {
    toast.add({
      title: t("appointments.detail.toasts.deleteError.title"),
      description: error?.data?.message ?? error?.message ?? t("common.errors.unexpected"),
      color: "red"
    });
  } finally {
    deleting.value = false;
  }
}
</script>
