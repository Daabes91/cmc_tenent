/**
 * SEO Usage Examples
 * Demonstrates how to use the SEO infrastructure in different scenarios
 */

import { generatePageMetadata, generateBlogMetadata } from './metadata';
import { getKeywordsForPage } from '@/config/keywords.config';

/**
 * Example 1: Basic page metadata
 * Use this for static pages like About, Contact, etc.
 */
export const homePageMetadata = generatePageMetadata('/', {
  title: 'Home',
  description: 'Transform your clinic with Vireo Health - the all-in-one management platform',
});

/**
 * Example 2: Features page with custom keywords
 */
export const featuresPageMetadata = generatePageMetadata('/features', {
  title: 'Features',
  description: 'Discover powerful features for clinic management, appointment scheduling, and patient care',
  keywords: [
    'clinic management features',
    'appointment scheduling',
    'patient management',
    'medical billing',
  ],
});

/**
 * Example 3: Pricing page with custom image
 */
export const pricingPageMetadata = generatePageMetadata('/pricing', {
  title: 'Pricing',
  description: 'Affordable pricing plans for clinics of all sizes. Start your free trial today',
  image: '/images/pricing-og.png',
});

/**
 * Example 4: Blog post metadata
 */
export const blogPostExample = generateBlogMetadata('getting-started-clinic-management', {
  title: 'Getting Started with Clinic Management Software',
  description: 'A comprehensive guide to implementing clinic management software in your practice',
  publishedAt: '2024-01-15T10:00:00Z',
  updatedAt: '2024-01-20T15:30:00Z',
  author: 'Dr. Sarah Johnson',
  category: 'Guides',
  tags: ['clinic management', 'getting started', 'best practices'],
  image: '/images/blog/getting-started.jpg',
});

/**
 * Example 5: Dynamic metadata generation in a page component
 */
export async function generateDynamicMetadata(slug: string) {
  // In a real app, you would fetch data from an API or database
  const pageData = {
    title: 'Dynamic Page Title',
    description: 'This is a dynamically generated page',
  };

  return generatePageMetadata(`/pages/${slug}`, {
    title: pageData.title,
    description: pageData.description,
  });
}

/**
 * Example 6: Get keywords for a specific page
 */
export function getPageKeywords() {
  const homeKeywords = getKeywordsForPage('/');
  const featuresKeywords = getKeywordsForPage('/features');
  const pricingKeywords = getKeywordsForPage('/pricing');

  return {
    home: homeKeywords,
    features: featuresKeywords,
    pricing: pricingKeywords,
  };
}

/**
 * Example 7: No-index page (for pages you don't want indexed)
 */
export const privatePageMetadata = generatePageMetadata('/admin', {
  title: 'Admin Dashboard',
  description: 'Internal admin dashboard',
  noindex: true,
  nofollow: true,
});

/**
 * Example 8: Product page metadata
 */
export const productPageMetadata = generatePageMetadata('/products/clinic-pro', {
  title: 'Clinic Pro - Premium Management Solution',
  description: 'Our premium clinic management solution with advanced features',
  type: 'product',
  image: '/images/products/clinic-pro.jpg',
});

// Usage in Next.js App Router:
// 
// app/page.tsx:
// export const metadata = homePageMetadata;
//
// app/features/page.tsx:
// export const metadata = featuresPageMetadata;
//
// app/blog/[slug]/page.tsx:
// export async function generateMetadata({ params }) {
//   const post = await getPost(params.slug);
//   return generateBlogMetadata(params.slug, post);
// }
