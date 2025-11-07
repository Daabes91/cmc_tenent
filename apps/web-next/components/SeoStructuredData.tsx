import Script from 'next/script';
import { getClinicSettingsServer, getSafeClinicNameServer } from '@/lib/server-api';
import { getSiteUrl } from '@/lib/seo';

type SeoStructuredDataProps = {
  locale: string;
};

export async function SeoStructuredData({ locale }: SeoStructuredDataProps) {
  const clinicSettings = await getClinicSettingsServer();
  const clinicName = getSafeClinicNameServer(clinicSettings?.clinicName);
  const siteUrl = getSiteUrl();
  const phone = clinicSettings?.phone || '+000000000';
  const addressParts = [
    clinicSettings?.address,
    clinicSettings?.city,
    clinicSettings?.state,
    clinicSettings?.zipCode,
  ].filter(Boolean);

  const sameAs = Object.values(clinicSettings?.socialMedia || {}).filter(
    (value): value is string => Boolean(value && value.startsWith('http')),
  );

  const logoUrl = clinicSettings?.logoUrl
    ? clinicSettings.logoUrl.startsWith('http')
      ? clinicSettings.logoUrl
      : `${siteUrl}${clinicSettings.logoUrl.startsWith('/') ? clinicSettings.logoUrl : `/${clinicSettings.logoUrl}`}`
    : undefined;

  const organizationJsonLd = {
    '@context': 'https://schema.org',
    '@type': 'MedicalOrganization',
    name: clinicName,
    url: siteUrl,
    telephone: phone,
    image: logoUrl,
    address: addressParts.length
      ? {
          '@type': 'PostalAddress',
          streetAddress: clinicSettings?.address,
          addressLocality: clinicSettings?.city,
          addressRegion: clinicSettings?.state,
          postalCode: clinicSettings?.zipCode,
          addressCountry: clinicSettings?.country,
        }
      : undefined,
    sameAs: sameAs.length ? sameAs : undefined,
    medicalSpecialty: ['Dentistry', 'Orthodontics', 'CosmeticDentistry'],
    areaServed: clinicSettings?.country || 'Global',
  };

  const websiteJsonLd = {
    '@context': 'https://schema.org',
    '@type': 'WebSite',
    name: clinicName,
    url: siteUrl,
    inLanguage: locale,
    potentialAction: {
      '@type': 'SearchAction',
      target: `${siteUrl}/search?query={search_term_string}`,
      'query-input': 'required name=search_term_string',
    },
  };

  return (
    <>
      <Script id="schema-medical-organization" type="application/ld+json" strategy="afterInteractive">
        {JSON.stringify(organizationJsonLd)}
      </Script>
      <Script id="schema-website" type="application/ld+json" strategy="afterInteractive">
        {JSON.stringify(websiteJsonLd)}
      </Script>
    </>
  );
}
