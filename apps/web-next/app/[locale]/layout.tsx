import * as React from "react";
import {notFound} from "next/navigation";
import {NextIntlClientProvider} from "next-intl";
import {setRequestLocale} from "next-intl/server";
import {locales, type Locale} from "@/i18n/request";
import {DirectionHandler} from "@/components/DirectionHandler";
import { headers } from "next/headers";
import { TENANT_HEADER } from "@/lib/tenant";
import { applyTranslationOverrides, fetchTenantTranslationOverrides } from "@/lib/translations";

export function generateStaticParams() {
  return locales.map((locale) => ({ locale }));
}

type LocaleLayoutProps = {
  children: React.ReactNode;
  params: Promise<{
    locale: string;
  }>;
};

export default async function LocaleLayout({ children, params }: LocaleLayoutProps) {
  const { locale } = await params;

  if (!locales.includes(locale as any)) {
    notFound();
  }

  setRequestLocale(locale);

  const baseMessages = (await import(`@/messages/${locale as Locale}.json`)).default;
  const tenantSlug = headers().get(TENANT_HEADER);
  const overrides = await fetchTenantTranslationOverrides(locale, tenantSlug);
  const messages = applyTranslationOverrides(baseMessages, overrides);

  return (
    <NextIntlClientProvider locale={locale} messages={messages}>
      <DirectionHandler />
      {children}
    </NextIntlClientProvider>
  );
}
