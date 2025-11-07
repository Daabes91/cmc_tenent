'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { saveAuth, clearAuth, getUser, saveUser, isAuthenticated } from '@/lib/auth';
import type { PatientAuthResponse, AuthUser } from '@/lib/types';

export function useAuth() {
  const router = useRouter();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const currentUser = getUser();
    setUser(currentUser);
    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const data = await api.login(email, password);
    saveAuth(data);
    const storedUser: AuthUser = {
      ...data.patient,
      name: `${data.patient.firstName} ${data.patient.lastName}`.trim(),
    };
    setUser(storedUser);
    return data;
  };

  const signup = async (formData: {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    password: string;
  }) => {
    const data = await api.signup(formData);
    saveAuth(data);
    const storedUser: AuthUser = {
      ...data.patient,
      name: `${data.patient.firstName} ${data.patient.lastName}`.trim(),
    };
    setUser(storedUser);
    return data;
  };

  const logout = () => {
    clearAuth();
    setUser(null);
    router.push('/');
  };

  const updateUser = (updatedUser: Partial<AuthUser>) => {
    const current = user ?? ({} as AuthUser);
    const newUser: AuthUser = {
      ...current,
      ...updatedUser,
      name:
        updatedUser.name ??
        `${updatedUser.firstName ?? current.firstName ?? ''} ${updatedUser.lastName ?? current.lastName ?? ''}`.trim(),
    };
    setUser(newUser);
    saveUser(newUser);
  };

  return {
    user,
    loading,
    isAuthenticated: mounted ? isAuthenticated() : false,
    login,
    signup,
    logout,
    updateUser,
  };
}
