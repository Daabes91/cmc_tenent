export type BlogStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export type Blog = {
  id: number;
  title: string;
  slug: string;
  excerpt: string | null;
  content: string;
  featuredImage: string | null;
  authorId: number | null;
  authorName: string | null;

  // SEO fields
  metaTitle: string | null;
  metaDescription: string | null;
  metaKeywords: string | null;
  ogTitle: string | null;
  ogDescription: string | null;
  ogImage: string | null;

  status: BlogStatus;
  publishedAt: string | null;
  viewCount: number;
  locale: string;
  createdAt: string;
  updatedAt: string;
};

export type BlogRequest = {
  title: string;
  slug: string;
  excerpt?: string;
  content: string;
  featuredImage?: string;

  // SEO fields
  metaTitle?: string;
  metaDescription?: string;
  metaKeywords?: string;
  ogTitle?: string;
  ogDescription?: string;
  ogImage?: string;

  locale?: string;
};
