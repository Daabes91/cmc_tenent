import * as React from "react";
import { headers } from 'next/headers';
import { getTenantTheme, getDefaultTheme } from '@/lib/theme';
import { TENANT_HEADER } from '@/lib/tenant';

type SiteLayoutProps = {
  children: React.ReactNode;
};

export default async function SiteLayout({ children }: SiteLayoutProps) {
  const headersList = await headers();
  const tenantSlug = headersList.get(TENANT_HEADER) || 'default';
  
  // Get tenant's theme
  const tenantTheme = await getTenantTheme(tenantSlug) || await getDefaultTheme();
  
  // Dynamic import of theme Header and Footer components
  const { Header } = await import(`@/themes/${tenantTheme.themeKey}/components/Header`)
    .catch(() => import('@/themes/default/components/Header'));
  
  const { Footer } = await import(`@/themes/${tenantTheme.themeKey}/components/Footer`)
    .catch(() => import('@/themes/default/components/Footer'));
  
  return (
    <>
      <Header />
      <main>{children}</main>
      <Footer />
    </>
  );
}
