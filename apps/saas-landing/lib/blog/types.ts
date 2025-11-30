/**
 * Blog System Types
 * 
 * Type definitions for the blog system including posts, categories, and metadata.
 */

export type BlogCategory = 
  | 'practice-management'
  | 'patient-care'
  | 'technology'
  | 'compliance'
  | 'industry-news';

export interface BlogAuthor {
  name: string;
  role: string;
  avatar?: string;
}

export interface BlogSEO {
  title: string;
  description: string;
  keywords: string[];
}

export interface BlogFrontmatter {
  title: string;
  excerpt: string;
  author: BlogAuthor;
  publishedAt: string;
  updatedAt?: string;
  category: BlogCategory;
  tags: string[];
  featuredImage: string;
  seo: BlogSEO;
  draft?: boolean;
}

export interface BlogPost extends BlogFrontmatter {
  slug: string;
  content: string;
  readingTime: number;
}

export interface BlogMetadata {
  posts: BlogPost[];
  categories: Record<BlogCategory, number>;
  totalPosts: number;
}

export interface BlogListItem {
  slug: string;
  title: string;
  excerpt: string;
  author: BlogAuthor;
  publishedAt: Date;
  category: BlogCategory;
  tags: string[];
  featuredImage: string;
  readingTime: number;
}

export interface BlogSearchResult extends BlogListItem {
  matchedIn: ('title' | 'excerpt' | 'content' | 'tags')[];
}
