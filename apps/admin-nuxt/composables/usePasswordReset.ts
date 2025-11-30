export interface ForgotPasswordRequest {
  email: string;
  tenantSlug: string;
}

export interface ValidateTokenRequest {
  token: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ForgotPasswordResponse {
  message: string;
}

export interface ValidateTokenResponse {
  valid: boolean;
  message?: string;
  tenantSlug?: string;
}

export interface ResetPasswordResponse {
  message: string;
}

export function usePasswordReset() {
  const apiBase = useApiBase();
  const { tenantSlug } = useTenantSlug();

  /**
   * Request a password reset for the given email and tenant
   */
  async function requestReset(email: string, tenantSlugParam: string): Promise<void> {
    try {
      const response = await $fetch<ForgotPasswordResponse>("/auth/forgot-password", {
        baseURL: apiBase,
        method: "POST",
        headers: tenantHeaders(tenantSlugParam),
        body: {
          email,
          tenantSlug: tenantSlugParam
        }
      });
      
      console.log('[PasswordReset] Request successful:', response.message);
    } catch (error: any) {
      console.error('[PasswordReset] Request failed:', error);
      throw normalizeError(error);
    }
  }

  /**
   * Validate a password reset token
   */
  async function validateToken(token: string): Promise<ValidateTokenResponse> {
    try {
      const response = await $fetch<ValidateTokenResponse>("/auth/validate-reset-token", {
        baseURL: apiBase,
        method: "POST",
        headers: tenantHeaders(),
        body: { token }
      });
      
      console.log('[PasswordReset] Token validation:', response.valid, 'tenant:', response.tenantSlug);
      return response;
    } catch (error: any) {
      console.error('[PasswordReset] Token validation failed:', error);
      throw normalizeError(error);
    }
  }

  /**
   * Reset password using a valid token
   */
  async function resetPassword(token: string, newPassword: string): Promise<void> {
    try {
      const response = await $fetch<ResetPasswordResponse>("/auth/reset-password", {
        baseURL: apiBase,
        method: "POST",
        headers: tenantHeaders(),
        body: {
          token,
          newPassword
        }
      });
      
      console.log('[PasswordReset] Password reset successful:', response.message);
    } catch (error: any) {
      console.error('[PasswordReset] Password reset failed:', error);
      throw normalizeError(error);
    }
  }

  function tenantHeaders(slugOverride?: string): Record<string, string> {
    const slug = slugOverride || tenantSlug.value;
    return slug ? { "X-Tenant-Slug": slug } : {};
  }

  function normalizeError(error: any): Error {
    if (error?.status === 400) {
      return new Error(error?.data?.message || "Invalid request. Please check your input.");
    }
    if (error?.status === 404) {
      return new Error("Service not found. Please try again later.");
    }
    if (error?.status === 429) {
      return new Error("Too many requests. Please wait a few minutes before trying again.");
    }
    if (error?.data?.message) {
      return new Error(error.data.message);
    }
    return error instanceof Error ? error : new Error("An unexpected error occurred. Please try again.");
  }

  return {
    requestReset,
    validateToken,
    resetPassword
  };
}
