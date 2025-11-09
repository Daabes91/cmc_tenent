export default defineNuxtPlugin((nuxtApp) => {
  // Get i18n instance from nuxtApp
  const i18n = nuxtApp.$i18n as any
  
  if (!i18n) return

  // Set initial direction
  const setDirection = (currentLocale: string) => {
    const dir = currentLocale === 'ar' ? 'rtl' : 'ltr'
    document.documentElement.setAttribute('dir', dir)
    document.documentElement.setAttribute('lang', currentLocale)
  }

  // Set direction on mount
  setDirection(i18n.locale.value)

  // Watch for locale changes
  watch(() => i18n.locale.value, (newLocale) => {
    setDirection(newLocale)
  })
})
