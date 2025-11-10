import type { Metadata } from "next";
import "./globals.css";
import "@/themes/default/styles/theme.css";
import "@/themes/clinic/styles/theme.css";
import "@/themes/barber/styles/theme.css";
import { headers } from "next/headers";
import { ThemeProvider } from "@/components/providers/ThemeProvider";
import { DynamicHead } from "@/components/DynamicHead";
import { AnalyticsScript } from "@/components/AnalyticsScript";
import { SeoStructuredData } from "@/components/SeoStructuredData";
import { buildRootMetadata } from "@/lib/seo";

export async function generateMetadata(): Promise<Metadata> {
  return buildRootMetadata();
}

export default async function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  const headersList = await headers();
  const locale = headersList.get("x-next-intl-locale") ?? "en";
  const dir = locale === "ar" ? "rtl" : "ltr";

  return (
    <html lang={locale} dir={dir} suppressHydrationWarning>
      <body className="antialiased">
        <ThemeProvider>
          <DynamicHead locale={locale} />
          <SeoStructuredData locale={locale} />
          <AnalyticsScript />
          {children}
        </ThemeProvider>
      </body>
    </html>
  );
}
