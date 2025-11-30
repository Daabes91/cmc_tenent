import type { FetchOptions } from "ofetch";
import type { BillingPlanResponse, CancelPlanPayload } from "~/types/billing";

type ApiErrorPayload = {
  field?: string | null;
  message: string;
};

type ApiResponseEnvelope<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  meta?: Record<string, any>;
  links?: Record<string, string>;
  errors?: ApiErrorPayload[] | null;
};

export function useAdminApi() {
  const baseURL = useApiBase();
  const auth = useAuth();
  const { tenantSlug } = useTenantSlug();

  const normalizePath = (path: string) => {
    const normalizedBase = (baseURL || '').replace(/\/+$/, '');
    const baseHasAdmin = normalizedBase.endsWith('/admin');
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;

    // Avoid double "/admin" when the base URL already contains it
    if (baseHasAdmin && normalizedPath.startsWith('/admin')) {
      return normalizedPath.replace(/^\/admin/, '') || '/';
    }

    return normalizedPath;
  };

  const authorizedRequest = async <T>(
    path: string,
    options: FetchOptions = {},
    retry = true
  ): Promise<T> => {
    try {
      // Always get fresh authorization header at request time
      const response = await $fetch<ApiResponseEnvelope<T>>(normalizePath(path), {
        baseURL,
        credentials: "include",
        headers: {
          "X-Tenant-Slug": tenantSlug.value,
          ...auth.authorizationHeader(),
          ...(options.headers ?? {})
        },
        ...options
      });

      if (response && typeof response.success === "boolean") {
        if (!response.success) {
          const error = new Error(response.message || "Request failed");
          (error as any).code = response.code;
          (error as any).errors = response.errors;
          (error as any).data = response;
          throw error;
        }
        return response.data;
      }

      return response as unknown as T;
    } catch (error: any) {
      if (retry && error?.status === 401) {
        try {
          // Refresh tokens - this will update the auth state
          await auth.refresh();
          // Retry the request - it will get the NEW token from authorizationHeader()
          return await authorizedRequest<T>(path, options, false);
        } catch (refreshError: any) {
          if (refreshError?.status === 401) {
            await auth.logout();
          }
          throw refreshError;
        }
      }
      throw error;
    }
  };

  const fetcher = async <T>(
    path: string,
    fallback: T,
    options: FetchOptions = {}
  ): Promise<T> => {
    try {
      return await authorizedRequest<T>(path, options);
    } catch (error) {
      console.warn(
        `[admin-api] Falling back to mock data for "${path}" —`,
        (error as Error).message ?? error
      );
      return fallback;
    }
  };

  const getBillingPlan = () => authorizedRequest<BillingPlanResponse>("/billing/plan");

  const cancelBillingPlan = (payload: CancelPlanPayload = {}) =>
    authorizedRequest<void>("/billing/plan/cancel", {
      method: "POST",
      body: JSON.stringify(payload)
    });

  return { fetcher, request: authorizedRequest, getBillingPlan, cancelBillingPlan };
}
