import type React from "react";
import "./globals.css";
import "./mobile-responsive.css";
import { ThemeProvider } from "@/components/theme-provider";
import { LanguageProvider } from "@/contexts/LanguageContext";
import { GoogleAnalytics } from "@/components/GoogleAnalytics";

export const metadata = {
  title: "SaasPro - Modern SaaS Platform",
  description: "Streamline your workflow with our powerful SaaS solution",
  generator: "Mohamed Djoudir",
  verification: {
    google: process.env.NEXT_PUBLIC_GSC_VERIFICATION || '',
  },
  openGraph: {
    title: "SaasPro - Modern SaaS Platform",
    description: "Streamline your workflow with our powerful SaaS solution",
    images: [
      {
        url: '/image.png',
        width: 1200,
        height: 630,
        alt: 'SaasPro',
      }
    ],
    type: 'website',
    siteName: 'SaasPro',
  },
  twitter: {
    card: 'summary_large_image',
    title: "SaasPro - Modern SaaS Platform",
    description: "Streamline your workflow with our powerful SaaS solution",
    images: ['/image.png'],
  },
  icons: {
    icon: "/favicon-32x32.png",
    shortcut: "/favicon-32x32.png",
    apple: "/apple-touch-icon.png",
  },
  manifest: "/site.webmanifest",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
  }>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <link rel="icon" href="/favicon-32x32.png" type="image/png" sizes="32x32" />
        <link rel="icon" href="/favicon-96x96.png" type="image/png" sizes="96x96" />
        <link rel="apple-touch-icon" href="/apple-touch-icon.png" sizes="180x180" />
        <link rel="manifest" href="/site.webmanifest" />
      </head>
      <body className="font-sans antialiased">
        <GoogleAnalytics />
        <LanguageProvider>
          <ThemeProvider
            attribute="class"
            defaultTheme="system"
            enableSystem
          >
            {children}
          </ThemeProvider>
        </LanguageProvider>
      </body>
    </html>
  );
}
