import { TENANT_HEADER } from './tenant';

const API_BASE_URL =
  process.env.NEXT_SERVER_API_BASE_URL ||
  process.env.NEXT_SERVER_API_URL ||
  'http://localhost:8080';

export async function fetchTenantTranslationOverrides(locale: string, tenantSlug?: string | null) {
  try {
    const url = new URL(`${API_BASE_URL}/public/translations`);
    url.searchParams.set('locale', locale);

    const response = await fetch(url.toString(), {
      headers: {
        'Content-Type': 'application/json',
        ...(tenantSlug ? { [TENANT_HEADER]: tenantSlug } : {})
      },
      cache: 'no-store'
    });

    if (!response.ok) {
      return {};
    }

    return (await response.json()) as Record<string, string>;
  } catch {
    return {};
  }
}

export function applyTranslationOverrides<T extends Record<string, any>>(
  baseMessages: T,
  overrides: Record<string, string>
): T {
  if (!overrides || Object.keys(overrides).length === 0) {
    return baseMessages;
  }

  const clone = structuredClone(baseMessages);

  const setValue = (obj: any, path: string, value: string) => {
    const parts = path.split('.');
    let current = obj;
    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      const isLast = i === parts.length - 1;
      if (isLast) {
        current[part] = value;
      } else {
        if (current[part] === undefined || typeof current[part] !== 'object') {
          current[part] = {};
        }
        current = current[part];
      }
    }
  };

  Object.entries(overrides).forEach(([path, value]) => {
    if (path && value !== undefined) {
      setValue(clone, path, value);
    }
  });

  return clone;
}
