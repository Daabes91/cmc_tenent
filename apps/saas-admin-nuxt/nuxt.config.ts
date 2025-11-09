// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },
  
  devServer: {
    port: 3002
  },

  app: {
    baseURL: '/saas-admin',
    head: {
      title: 'SAAS Manager Panel',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'SAAS Manager Admin Panel for Multi-Tenant Clinic Management' }
      ]
    }
  },

  modules: ['@nuxt/ui', '@nuxtjs/color-mode', '@nuxtjs/i18n'],

  css: ['~/assets/css/main.css'],

  colorMode: {
    preference: 'system',
    fallback: 'light',
    classSuffix: ''
  },

  i18n: {
    locales: [
      { code: 'en', iso: 'en-US', name: 'English', dir: 'ltr', file: 'en.json' },
      { code: 'ar', iso: 'ar', name: 'العربية', dir: 'rtl', file: 'ar.json' }
    ],
    defaultLocale: 'en',
    strategy: 'no_prefix',
    detectBrowserLanguage: {
      alwaysRedirect: false,
      fallbackLocale: 'en',
      redirectOn: 'root'
    },
    langDir: 'locales',
    lazy: true,
    vueI18n: './i18n.config.ts'
  },

  runtimeConfig: {
    apiBase: process.env.NUXT_PRIVATE_API_BASE ?? 'http://api:8080/saas',
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE ?? 'http://localhost:8080',
      saasApiBase: process.env.NUXT_PUBLIC_SAAS_API_BASE ?? 'http://localhost:8080/saas',
      appName: process.env.NUXT_PUBLIC_APP_NAME ?? 'SAAS Manager Panel',
      appUrl: process.env.NUXT_PUBLIC_APP_URL ?? 'http://localhost:3002',
      enableAnalytics: process.env.NUXT_PUBLIC_ENABLE_ANALYTICS === 'true',
      enableAuditLogs: process.env.NUXT_PUBLIC_ENABLE_AUDIT_LOGS === 'true',
      environment: process.env.NODE_ENV ?? 'development'
    }
  },

  vite: {
    optimizeDeps: {
      include: ['chart.js', 'vue-chartjs']
    }
  },

  typescript: {
    strict: true,
    typeCheck: false
  },

  compatibilityDate: '2024-04-03'
})
