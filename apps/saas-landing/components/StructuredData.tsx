/**
 * Structured Data Component
 * 
 * Renders JSON-LD structured data for SEO.
 * Use this component to add schema markup to any page.
 */

import React from 'react';
import {
  OrganizationSchema,
  SoftwareApplicationSchema,
  BlogPostingSchema,
} from '@/lib/seo/structured-data';

interface StructuredDataProps {
  data: OrganizationSchema | SoftwareApplicationSchema | BlogPostingSchema | Array<OrganizationSchema | SoftwareApplicationSchema | BlogPostingSchema>;
}

export function StructuredData({ data }: StructuredDataProps) {
  const jsonLd = Array.isArray(data) ? data : [data];

  return (
    <>
      {jsonLd.map((schema, index) => (
        <script
          key={index}
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify(schema),
          }}
        />
      ))}
    </>
  );
}
