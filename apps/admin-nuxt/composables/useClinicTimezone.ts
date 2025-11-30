import { ref, computed } from 'vue'

export interface TimezoneInfo {
  timezone: string        // IANA timezone identifier (e.g., "Asia/Amman")
  abbreviation: string    // Timezone abbreviation (e.g., "EET", "EEST")
  offset: string          // Current UTC offset (e.g., "+03:00")
  currentTime: string     // Current time in clinic timezone (ISO-8601)
}

const clinicTimezone = ref<TimezoneInfo | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

/**
 * Composable for managing clinic timezone information across the admin dashboard.
 * Ensures all admins see appointment times in the clinic's timezone, not their browser timezone.
 */
export function useClinicTimezone() {
  const config = useRuntimeConfig()
  const resolvePublicApiBase = () => {
    const explicit = config.public.publicApiBase
    const adminBase = useApiBase() || ""
    const baseCandidate = explicit || adminBase
    if (!baseCandidate) return ""

    const normalized = baseCandidate.replace(/\/+$/, "")
    if (normalized.endsWith("/public")) return normalized
    return `${normalized}/public`
  }
  const publicApiBase = resolvePublicApiBase()

  /**
   * Fetch clinic timezone from backend API.
   * Should be called once during app initialization (e.g., in app.vue or layouts/default.vue)
   */
  const fetchClinicTimezone = async () => {
    if (clinicTimezone.value) {
      // Already loaded
      return clinicTimezone.value
    }

    isLoading.value = true
    error.value = null

    try {
      if (!publicApiBase) {
        throw new Error("Missing public API base URL")
      }
      const response = await $fetch<TimezoneInfo>(`${publicApiBase}/settings/timezone`)
      clinicTimezone.value = response
      return response
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch clinic timezone'
      console.error('Failed to fetch clinic timezone:', err)

      // Fallback to default timezone
      clinicTimezone.value = {
        timezone: 'Asia/Amman',
        abbreviation: 'EET',
        offset: '+03:00',
        currentTime: new Date().toISOString()
      }

      return clinicTimezone.value
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Get the clinic timezone identifier (e.g., "Asia/Amman")
   */
  const timezone = computed(() => clinicTimezone.value?.timezone || 'Asia/Amman')

  /**
   * Get the timezone abbreviation for display (e.g., "EET")
   */
  const abbreviation = computed(() => clinicTimezone.value?.abbreviation || 'EET')

  /**
   * Get the UTC offset (e.g., "+03:00")
   */
  const offset = computed(() => clinicTimezone.value?.offset || '+03:00')

  /**
   * Check if timezone info is loaded
   */
  const isLoaded = computed(() => clinicTimezone.value !== null)

  return {
    // State
    clinicTimezone: computed(() => clinicTimezone.value),
    isLoading: computed(() => isLoading.value),
    error: computed(() => error.value),
    isLoaded,

    // Getters
    timezone,
    abbreviation,
    offset,

    // Actions
    fetchClinicTimezone
  }
}
