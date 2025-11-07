'use client';

import type { PatientAuthResponse, AuthUser } from './types';

export const AUTH_TOKEN_KEY = 'authToken';
export const USER_KEY = 'user';
export const TOKEN_EXPIRY_KEY = 'tokenExpiry';

export function saveAuth(data: PatientAuthResponse) {
  if (typeof window === 'undefined') return;

  const storedUser: AuthUser = {
    ...data.patient,
    name: `${data.patient.firstName} ${data.patient.lastName}`.trim(),
  };

  localStorage.setItem(AUTH_TOKEN_KEY, data.accessToken);
  localStorage.setItem(TOKEN_EXPIRY_KEY, data.accessTokenExpiresAt);
  localStorage.setItem(USER_KEY, JSON.stringify(storedUser));
}

export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

export function getUser(): AuthUser | null {
  if (typeof window === 'undefined') return null;

  const userStr = localStorage.getItem(USER_KEY);
  if (!userStr) return null;

  try {
    return JSON.parse(userStr);
  } catch {
    return null;
  }
}

export function saveUser(user: AuthUser) {
  if (typeof window === 'undefined') return;
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearAuth() {
  if (typeof window === 'undefined') return;

  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem(TOKEN_EXPIRY_KEY);
}

export function isTokenExpired(): boolean {
  if (typeof window === 'undefined') return true;

  const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY);
  if (!expiry) return true;

  try {
    const expiryDate = new Date(expiry);
    return expiryDate.getTime() < Date.now();
  } catch {
    return true;
  }
}

export function isAuthenticated(): boolean {
  const hasToken = !!getToken();
  if (!hasToken) return false;

  // Check if token is expired
  if (isTokenExpired()) {
    clearAuth();
    return false;
  }

  return true;
}
