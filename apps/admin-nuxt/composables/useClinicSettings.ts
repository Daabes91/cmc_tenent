import { computed, watch } from "vue";

type ExchangeRatesMap = Record<string, number>;

type LocalizedTextPayload = {
  en?: string | null;
  ar?: string | null;
} | null;

type WhyChooseFeaturePayload = {
  key?: string | null;
  icon?: string | null;
  title?: LocalizedTextPayload;
  description?: LocalizedTextPayload;
} | null;

type ClinicSettingsPayload = {
  id?: number;
  clinicName?: string | null;
  tagline?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  city?: string | null;
  state?: string | null;
  zipCode?: string | null;
  country?: string | null;
  workingHours?: {
    monday?: string | null;
    tuesday?: string | null;
    wednesday?: string | null;
    thursday?: string | null;
    friday?: string | null;
    saturday?: string | null;
    sunday?: string | null;
  } | null;
  socialMedia?: {
    facebook?: string | null;
    instagram?: string | null;
    twitter?: string | null;
    linkedin?: string | null;
  } | null;
  currency?: string | null;
  locale?: string | null;
  currencySymbol?: string | null;
  logoUrl?: string | null;
  logoImageId?: string | null;
  faviconUrl?: string | null;
  faviconImageId?: string | null;
  virtualConsultationFee?: number | null;
  virtualConsultationMeetingLink?: string | null;
  slotDurationMinutes?: number | null;
  paypalEnvironment?: string | null;
  paypalClientId?: string | null;
  paypalClientSecret?: string | null;
  exchangeRates?: ExchangeRatesMap | null;
  emailFrom?: string | null;
  emailFromName?: string | null;
  emailEnabled?: boolean | null;
  reminderEnabled?: boolean | null;
  reminderHoursBefore?: number | null;
  heroMediaType?: string | null;
  heroImageUrl?: string | null;
  heroVideoId?: string | null;
  whyChoose?: {
    title?: LocalizedTextPayload;
    subtitle?: LocalizedTextPayload;
    features?: WhyChooseFeaturePayload[];
  } | null;
};

type UseClinicSettingsOptions = {
  immediate?: boolean;
};

const DEFAULT_EXCHANGE_RATES: ExchangeRatesMap = {
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

const DEFAULT_SETTINGS: Required<Pick<ClinicSettingsPayload, "currency" | "locale">> & {
  currencySymbol: string;
  exchangeRates: ExchangeRatesMap;
} = {
  currency: "SAR",
  locale: "ar-SA",
  currencySymbol: "ر.س",
  exchangeRates: DEFAULT_EXCHANGE_RATES
};

function resolveCurrencySymbol(code: string) {
  try {
    const formatter = new Intl.NumberFormat(undefined, {
      style: "currency",
      currency: code,
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });
    const parts = formatter.formatToParts(1);
    const symbol = parts.find(part => part.type === "currency")?.value;
    return symbol || code;
  } catch {
    return code;
  }
}

export function useClinicSettings(options: UseClinicSettingsOptions = {}) {
  const { immediate = true } = options;
  const { request } = useAdminApi();

  const nuxtApp = useNuxtApp();
  const promiseKey = "__clinicSettingsPromise";

  const dataState = useState<ClinicSettingsPayload | null>("clinic-settings:data", () => null);
  const pendingState = useState<boolean>("clinic-settings:pending", () => false);
  const errorState = useState<string | null>("clinic-settings:error", () => null);
  const getPromise = () =>
    (nuxtApp as Record<string, any>)[promiseKey] as Promise<ClinicSettingsPayload | null> | null;
  const setPromise = (value: Promise<ClinicSettingsPayload | null> | null) => {
    (nuxtApp as Record<string, any>)[promiseKey] = value;
  };

  const fetchSettings = async (force = false): Promise<ClinicSettingsPayload | null> => {
    if (!force && dataState.value) {
      return dataState.value;
    }

    const existingPromise = getPromise();
    if (existingPromise) {
      return existingPromise;
    }

    const fetchPromise = (async () => {
      pendingState.value = true;
      errorState.value = null;
      try {
        const payload = await request<ClinicSettingsPayload>("/settings");
        dataState.value = payload;
        return payload;
      } catch (err) {
        errorState.value = err instanceof Error ? err.message : String(err);
        throw err;
      } finally {
        pendingState.value = false;
        setPromise(null);
      }
    })();

    setPromise(fetchPromise);
    return fetchPromise;
  };

  if (immediate && !dataState.value && !getPromise()) {
    fetchSettings().catch(() => {
      // errors are captured in errorState; suppress unhandled rejection
    });
  }

  const refresh = async () => {
    return fetchSettings(true);
  };

  const settings = computed(() => {
    const payload = dataState.value ?? {};
    const currency = (payload.currency || DEFAULT_SETTINGS.currency).toUpperCase();
    const locale = payload.locale || DEFAULT_SETTINGS.locale;
    const symbol =
      payload.currencySymbol ||
      resolveCurrencySymbol(currency);
    const exchangeRates = payload.exchangeRates && Object.keys(payload.exchangeRates).length
      ? payload.exchangeRates
      : DEFAULT_SETTINGS.exchangeRates;
    const paypalEnvironment = payload.paypalEnvironment?.trim() || null;
    const paypalClientId = payload.paypalClientId?.trim() || null;
    const paypalClientSecret = payload.paypalClientSecret?.trim() || null;
    const emailFrom = payload.emailFrom?.trim() || null;
    const emailFromName = payload.emailFromName?.trim() || null;
    const emailEnabled = payload.emailEnabled ?? true;

    // Ensure clinic name is properly handled for branding
    const clinicName = payload.clinicName?.trim() || null;
    const logoUrl = payload.logoUrl?.trim() || null;
    const faviconUrl = payload.faviconUrl?.trim() || null;
    const faviconImageId = payload.faviconImageId?.trim() || null;

    // Hero media fields
    const heroMediaType = payload.heroMediaType?.trim() || 'image';
    const heroImageUrl = payload.heroImageUrl?.trim() || null;
    const heroVideoId = payload.heroVideoId?.trim() || null;
    const whyChoose = payload.whyChoose ?? null;

    return {
      ...payload,
      clinicName,
      logoUrl,
      faviconUrl,
      faviconImageId,
      currency,
      locale,
      currencySymbol: symbol,
      exchangeRates,
      paypalEnvironment,
      paypalClientId,
      paypalClientSecret,
      emailFrom,
      emailFromName,
      emailEnabled,
      heroMediaType,
      heroImageUrl,
      heroVideoId,
      whyChoose
    };
  });

  const currencyFormatter = computed(() => {
    const locale = settings.value.locale || DEFAULT_SETTINGS.locale;
    const currency = settings.value.currency || DEFAULT_SETTINGS.currency;
    return new Intl.NumberFormat(locale, {
      style: "currency",
      currency,
      notation: "compact"
    });
  });

  const standardCurrencyFormatter = computed(() => {
    const locale = settings.value.locale || DEFAULT_SETTINGS.locale;
    const currency = settings.value.currency || DEFAULT_SETTINGS.currency;
    return new Intl.NumberFormat(locale, {
      style: "currency",
      currency
    });
  });

  const formatCurrency = (amount: number | null | undefined, opts?: Intl.NumberFormatOptions) => {
    const value = Number(amount ?? 0);
    const locale = settings.value.locale || DEFAULT_SETTINGS.locale;
    const currency = settings.value.currency || DEFAULT_SETTINGS.currency;
    if (!opts || Object.keys(opts).length === 0) {
      return standardCurrencyFormatter.value.format(value);
    }

    if (
      opts.notation === "compact" &&
      opts.minimumFractionDigits === undefined &&
      opts.maximumFractionDigits === undefined
    ) {
      return currencyFormatter.value.format(value);
    }

    const formatter = new Intl.NumberFormat(locale, {
      style: "currency",
      currency,
      ...opts
    });

    return formatter.format(value);
  };

  const pending = computed(() => pendingState.value);
  const error = computed(() => errorState.value);

  function reset() {
    dataState.value = null;
    errorState.value = null;
    setPromise(null);
  }

  const { tenantSlug } = useTenantSlug();
  watch(
    () => tenantSlug.value,
    () => {
      reset();
      fetchSettings(true).catch(() => {
        /* handled via error state */
      });
    }
  );

  return {
    settings,
    pending,
    error,
    refresh,
    reset,
    currencyFormatter,
    formatCurrency,
    standardCurrencyFormatter
  };
}
