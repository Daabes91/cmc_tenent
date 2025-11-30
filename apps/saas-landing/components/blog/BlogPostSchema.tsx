/**
 * Blog Post Schema Component
 * 
 * Adds structured data (schema.org) for blog posts to improve SEO.
 */

import { BlogPost } from '@/lib/blog/types';

interface BlogPostSchemaProps {
  post: BlogPost;
  siteUrl: string;
}

export function BlogPostSchema({ post, siteUrl }: BlogPostSchemaProps) {
  const postUrl = `${siteUrl}/blog/${post.slug}`;
  
  const schema = {
    '@context': 'https://schema.org',
    '@type': 'BlogPosting',
    headline: post.title,
    description: post.excerpt,
    image: post.featuredImage,
    datePublished: post.publishedAt,
    dateModified: post.updatedAt || post.publishedAt,
    author: {
      '@type': 'Person',
      name: post.author.name,
      jobTitle: post.author.role,
      ...(post.author.avatar && { image: post.author.avatar }),
    },
    publisher: {
      '@type': 'Organization',
      name: 'Clinic Management Platform',
      logo: {
        '@type': 'ImageObject',
        url: `${siteUrl}/logo.png`,
      },
    },
    mainEntityOfPage: {
      '@type': 'WebPage',
      '@id': postUrl,
    },
    articleSection: post.category
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' '),
    keywords: post.tags.join(', '),
    wordCount: post.content.split(/\s+/).length,
    timeRequired: `PT${post.readingTime}M`,
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(schema) }}
    />
  );
}
