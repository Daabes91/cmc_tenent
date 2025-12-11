<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100">
    <div class="max-w-5xl mx-auto px-4 py-12 space-y-10">
      <header class="space-y-2">
        <p class="text-sm font-medium text-primary-600 dark:text-primary-300 uppercase tracking-wide">Welcome</p>
        <h1 class="text-3xl sm:text-4xl font-bold">Get your clinic portal ready</h1>
        <p class="text-slate-600 dark:text-slate-300 max-w-2xl">
          You’ve completed payment. Let’s finish setting up your clinic and invite your team.
        </p>
      </header>

      <section class="grid gap-6 md:grid-cols-2">
        <div class="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-6 shadow-sm">
          <h2 class="text-xl font-semibold mb-2">Next steps</h2>
          <ul class="space-y-3 text-slate-700 dark:text-slate-200">
            <li class="flex items-start gap-3">
              <span class="mt-1 h-2.5 w-2.5 rounded-full bg-emerald-500"></span>
              <div>
                <p class="font-medium">Configure clinic profile</p>
                <p class="text-sm text-slate-500 dark:text-slate-400">Set name, contact info, timezone, and branding.</p>
              </div>
            </li>
            <li class="flex items-start gap-3">
              <span class="mt-1 h-2.5 w-2.5 rounded-full bg-emerald-500"></span>
              <div>
                <p class="font-medium">Add staff users</p>
                <p class="text-sm text-slate-500 dark:text-slate-400">Invite colleagues, assign roles, and set permissions.</p>
              </div>
            </li>
            <li class="flex items-start gap-3">
              <span class="mt-1 h-2.5 w-2.5 rounded-full bg-emerald-500"></span>
              <div>
                <p class="font-medium">Set up services & availability</p>
                <p class="text-sm text-slate-500 dark:text-slate-400">Create services, attach providers, and define schedules.</p>
              </div>
            </li>
          </ul>
        </div>

        <div class="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-6 shadow-sm">
          <h2 class="text-xl font-semibold mb-2">Quick actions</h2>
          <div class="space-y-3">
            <NuxtLink
              to="/clinic-settings"
              class="flex items-center justify-between rounded-lg border border-slate-200 dark:border-slate-700 px-4 py-3 hover:border-primary-500 hover:bg-primary-50/50 dark:hover:border-primary-400 dark:hover:bg-primary-500/10 transition"
            >
              <div>
                <p class="font-medium">Open clinic settings</p>
                <p class="text-sm text-slate-500 dark:text-slate-400">Branding, timezone, contact info</p>
              </div>
              <UIcon name="i-lucide-arrow-right" class="h-5 w-5 text-primary-500" />
            </NuxtLink>
            <NuxtLink
              to="/staff"
              class="flex items-center justify-between rounded-lg border border-slate-200 dark:border-slate-700 px-4 py-3 hover:border-primary-500 hover:bg-primary-50/50 dark:hover:border-primary-400 dark:hover:bg-primary-500/10 transition"
            >
              <div>
                <p class="font-medium">Manage staff</p>
                <p class="text-sm text-slate-500 dark:text-slate-400">Invite teammates, assign roles</p>
              </div>
              <UIcon name="i-lucide-users" class="h-5 w-5 text-primary-500" />
            </NuxtLink>
            <NuxtLink
              to="/services"
              class="flex items-center justify-between rounded-lg border border-slate-200 dark:border-slate-700 px-4 py-3 hover:border-primary-500 hover:bg-primary-50/50 dark:hover:border-primary-400 dark:hover:bg-primary-500/10 transition"
            >
              <div>
                <p class="font-medium">Configure services</p>
                <p class="text-sm text-slate-500 dark:text-slate-400">Add services, durations, and pricing</p>
              </div>
              <UIcon name="i-lucide-briefcase" class="h-5 w-5 text-primary-500" />
            </NuxtLink>
          </div>
        </div>
      </section>

      <section class="rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-6 shadow-sm">
        <h2 class="text-xl font-semibold mb-2">Need help?</h2>
        <p class="text-sm text-slate-600 dark:text-slate-300 mb-4">
          Check the docs or contact support if you need assistance finishing setup.
        </p>
        <div class="flex flex-wrap gap-3">
          <UButton to="/help" variant="ghost" color="primary">
            <template #leading><UIcon name="i-lucide-book-open" class="h-4 w-4" /></template>
            View docs
          </UButton>
          <UButton to="/support" color="primary">
            <template #leading><UIcon name="i-lucide-life-buoy" class="h-4 w-4" /></template>
            Contact support
          </UButton>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
// Lightweight onboarding landing page after payment confirmation.
</script>
