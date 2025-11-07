const disallowedBasePaths = new Set([
  'admin',
  '/admin',
  'admin-panel',
  '/admin-panel',
]);

const shouldIgnoreBasePath = (value?: string | null) => {
  if (!value) return true;
  const trimmed = value.trim();
  if (!trimmed || trimmed === '/') return true;
  const normalized = trimmed.endsWith('/') ? trimmed.slice(0, -1) : trimmed;
  return disallowedBasePaths.has(normalized);
};

const rawBasePath = process.env.NEXT_PUBLIC_BASE_PATH ?? '';

// Normalize to ensure there's no trailing slash and always a leading slash when set.
const normalizedBasePath =
  !shouldIgnoreBasePath(rawBasePath)
    ? rawBasePath.endsWith('/')
      ? rawBasePath.slice(0, -1)
      : rawBasePath
    : '';

export const basePath = normalizedBasePath;

export const withBasePath = (path: string) => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  if (!basePath) return normalizedPath;
  return `${basePath}${normalizedPath}`;
};

export const stripBasePath = (pathname: string) => {
  if (!basePath) return pathname;
  if (pathname === basePath) {
    return '/';
  }
  return pathname.startsWith(basePath)
    ? pathname.slice(basePath.length) || '/'
    : pathname;
};

const BOOKING_SECTION_ID = 'booking-section';

export const BOOKING_SECTION_HASH = `#${BOOKING_SECTION_ID}`;

export const getLocalizedHomePath = (locale?: string) =>
  locale ? `/${locale}` : '/';

export const getBookingSectionPath = (locale?: string) =>
  `${getLocalizedHomePath(locale)}${BOOKING_SECTION_HASH}`;

export const getBookingSectionUrl = (locale?: string) =>
  `${withBasePath(getLocalizedHomePath(locale))}${BOOKING_SECTION_HASH}`;
