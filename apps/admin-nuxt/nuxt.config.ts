import { fileURLToPath } from "node:url";

const defaultPublicApiBase = (process.env.NUXT_PUBLIC_API_BASE ?? "https://lionfish-app-mf2v9.ondigitalocean.app/api/admin").replace(/\/+$/, "");
const defaultPrivateApiBase = (
  process.env.NUXT_PRIVATE_API_BASE ??
  process.env.NUXT_PUBLIC_API_BASE ??
  "https://lionfish-app-mf2v9.ondigitalocean.app/api/admin"
).replace(/\/+$/, "");
const defaultPublicBase = (process.env.NUXT_PUBLIC_PUBLIC_API_BASE ?? "https://lionfish-app-mf2v9.ondigitalocean.app/api/public").replace(/\/+$/, "");

const tailwindConfigAlias = fileURLToPath(new URL("./tailwind-config", import.meta.url));

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },
  devServer: {
    port: 3000
  },
  app: {
    baseURL: '/admin-panel',
    head: {
      meta: [
        { 
          name: 'viewport', 
          content: 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover' 
        }
      ],
      link: [
        { rel: 'icon', type: 'image/png', href: 'https://imagedelivery.net/K88oXEK4nwOFUDLZaSq1vg/6c79054b-5ecc-4a97-be03-441518f70200/public' }
      ]
    }
  },
  modules: ["@nuxt/ui", "@nuxtjs/color-mode", "@nuxtjs/i18n"],
  css: ["~/assets/css/base.css"],
  alias: {
    "#tailwind-config": tailwindConfigAlias
  },
  colorMode: {
    preference: "system",
    fallback: "light",
    classSuffix: ""
  },
  i18n: {
    locales: [
      { code: "en", iso: "en-US", name: "English", dir: "ltr", file: "en.json" },
      { code: "ar", iso: "ar", name: "العربية", dir: "rtl", file: "ar.json" }
    ],
    defaultLocale: "en",
    strategy: "no_prefix",
    detectBrowserLanguage: {
      alwaysRedirect: false,
      fallbackLocale: "en",
      redirectOn: "root"
    },
    langDir: "locales",
    lazy: true,
    vueI18n: "./i18n.config.ts"
  },
  runtimeConfig: {
    apiBase: defaultPrivateApiBase,
    public: {
      apiBase: defaultPublicApiBase,
      publicApiBase: defaultPublicBase,
      defaultTenantSlug: process.env.NUXT_PUBLIC_DEFAULT_TENANT?.toLowerCase() ?? "default",
      webUrl: process.env.NUXT_PUBLIC_WEB_URL ?? "http://localhost:3001"
    }
  },
  vite: {
    optimizeDeps: {
      include: ['@tiptap/vue-3', '@tiptap/starter-kit']
    }
  },
  compatibilityDate: '2024-04-03'
});
