/**
 * Plugin to handle RTL/LTR direction setting based on locale
 * Sets dir attribute on document for proper text direction and toast positioning
 */
export default defineNuxtPlugin((nuxtApp) => {
  const { $i18n } = nuxtApp

  const setDirection = (currentLocale: string) => {
    const isRTL = currentLocale === 'ar'

    if (process.client) {
      document.documentElement.setAttribute('dir', isRTL ? 'rtl' : 'ltr')
      document.body.setAttribute('dir', isRTL ? 'rtl' : 'ltr')
      document.body.classList.toggle('rtl-mode', isRTL)
      document.body.classList.toggle('ltr-mode', !isRTL)
    }
  }

  if (process.client && $i18n?.locale) {
    // Set initial direction
    if ($i18n.locale.value) {
      setDirection($i18n.locale.value)
    }

    // Watch for locale changes
    watch($i18n.locale, setDirection, { immediate: true })
  }
})
