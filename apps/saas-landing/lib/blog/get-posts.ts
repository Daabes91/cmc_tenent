/**
 * Blog Post Fetching Utilities
 * 
 * Functions to fetch and process blog posts from MDX files.
 */

import fs from 'fs';
import path from 'path';
import matter from 'gray-matter';
import readingTime from 'reading-time';
import { BlogPost, BlogFrontmatter, BlogListItem, BlogMetadata, BlogCategory, BlogAuthor } from './types';
import { BlogError, BlogErrorType, validateBlogFrontmatter, handleBlogPostError } from './error-handler';
import { withBasePath } from '@/lib/base-path';

const BLOG_CONTENT_DIR = path.join(process.cwd(), 'content', 'blog');
const PUBLIC_DIR = path.join(process.cwd(), 'public');
const AUTHOR_PLACEHOLDER = '/images/authors/placeholder.svg';
const BLOG_PLACEHOLDER = '/images/blog/placeholder.svg';

/**
 * Get all blog post slugs from the content directory
 */
export function getAllBlogSlugs(): string[] {
  try {
    if (!fs.existsSync(BLOG_CONTENT_DIR)) {
      return [];
    }
    
    const files = fs.readdirSync(BLOG_CONTENT_DIR);
    return files
      .filter(file => file.endsWith('.mdx') || file.endsWith('.md'))
      .map(file => file.replace(/\.mdx?$/, ''));
  } catch (error) {
    console.error('Error reading blog directory:', error);
    return [];
  }
}

/**
 * Get a single blog post by slug
 */
export async function getBlogPost(slug: string): Promise<BlogPost | null> {
  try {
    const filePath = path.join(BLOG_CONTENT_DIR, `${slug}.mdx`);
    
    if (!fs.existsSync(filePath)) {
      // Try .md extension
      const mdFilePath = path.join(BLOG_CONTENT_DIR, `${slug}.md`);
      if (!fs.existsSync(mdFilePath)) {
        throw new BlogError(
          `Blog post not found: ${slug}`,
          BlogErrorType.NOT_FOUND,
          slug
        );
      }
      return parseBlogPost(mdFilePath, slug);
    }
    
    return parseBlogPost(filePath, slug);
  } catch (error) {
    if (error instanceof BlogError && error.type === BlogErrorType.NOT_FOUND) {
      // Return null for not found errors (expected behavior)
      return null;
    }
    
    // Log other errors
    handleBlogPostError(error, slug);
    return null;
  }
}

/**
 * Parse a blog post file and extract metadata
 */
function parseBlogPost(filePath: string, slug: string): BlogPost {
  try {
    const fileContents = fs.readFileSync(filePath, 'utf8');
    const { data, content } = matter(fileContents);
    
    // Validate frontmatter
    validateBlogFrontmatter(data, slug);
    
    const frontmatter = data as BlogFrontmatter;

    // Normalize asset paths to include the base path in production
    const resolveImage = (value: string | undefined | null, fallback: string) => {
      if (!value || !value.startsWith('/')) {
        return withBasePath(fallback);
      }
      const normalizedPath = value.startsWith('/') ? value.substring(1) : value;
      const absPath = path.join(PUBLIC_DIR, normalizedPath);
      if (fs.existsSync(absPath)) {
        return withBasePath(value);
      }
      return withBasePath(fallback);
    };

    const normalizedAuthor: BlogAuthor = {
      ...frontmatter.author,
      avatar: resolveImage(frontmatter.author?.avatar, AUTHOR_PLACEHOLDER),
    };

    const normalizedFeaturedImage = resolveImage(frontmatter.featuredImage, BLOG_PLACEHOLDER);
    
    // Calculate reading time
    const stats = readingTime(content);
    
    return {
      slug,
      ...frontmatter,
      author: normalizedAuthor,
      featuredImage: normalizedFeaturedImage,
      content,
      readingTime: Math.ceil(stats.minutes),
    };
  } catch (error) {
    if (error instanceof BlogError) {
      throw error;
    }
    
    throw new BlogError(
      `Failed to parse blog post: ${slug}`,
      BlogErrorType.PARSING_ERROR,
      slug,
      error instanceof Error ? error : undefined
    );
  }
}

/**
 * Get all published blog posts (excludes drafts and scheduled posts)
 */
export async function getAllBlogPosts(includeContent = false): Promise<BlogPost[]> {
  try {
    const slugs = getAllBlogSlugs();
    const posts: BlogPost[] = [];
    
    for (const slug of slugs) {
      try {
        const post = await getBlogPost(slug);
        if (post && !post.draft && isPublished(post.publishedAt)) {
          if (!includeContent) {
            // Remove content to reduce payload size for listing pages
            post.content = '';
          }
          posts.push(post);
        }
      } catch (error) {
        // Log error but continue processing other posts
        console.error(`Error loading post ${slug}:`, error);
        continue;
      }
    }
    
    // Sort by publication date (newest first)
    return posts.sort((a, b) => 
      new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime()
    );
  } catch (error) {
    console.error('Error getting all blog posts:', error);
    // Return empty array as fallback
    return [];
  }
}

/**
 * Check if a post is published (not scheduled for future)
 */
function isPublished(publishedAt: string): boolean {
  const publishDate = new Date(publishedAt);
  const now = new Date();
  return publishDate <= now;
}

/**
 * Get blog posts by category
 */
export async function getBlogPostsByCategory(category: BlogCategory): Promise<BlogPost[]> {
  const allPosts = await getAllBlogPosts();
  return allPosts.filter(post => post.category === category);
}

/**
 * Get blog posts by tag
 */
export async function getBlogPostsByTag(tag: string): Promise<BlogPost[]> {
  const allPosts = await getAllBlogPosts();
  return allPosts.filter(post => 
    post.tags.some(t => t.toLowerCase() === tag.toLowerCase())
  );
}

/**
 * Get blog metadata including post counts by category
 */
export async function getBlogMetadata(): Promise<BlogMetadata> {
  const posts = await getAllBlogPosts();
  
  const categories: Record<BlogCategory, number> = {
    'practice-management': 0,
    'patient-care': 0,
    'technology': 0,
    'compliance': 0,
    'industry-news': 0,
  };
  
  posts.forEach(post => {
    categories[post.category]++;
  });
  
  return {
    posts,
    categories,
    totalPosts: posts.length,
  };
}

/**
 * Convert BlogPost to BlogListItem (for listing pages)
 */
export function toBlogListItem(post: BlogPost): BlogListItem {
  return {
    slug: post.slug,
    title: post.title,
    excerpt: post.excerpt,
    author: post.author,
    publishedAt: new Date(post.publishedAt),
    category: post.category,
    tags: post.tags,
    featuredImage: post.featuredImage,
    readingTime: post.readingTime,
  };
}

/**
 * Get paginated blog posts
 */
export async function getPaginatedBlogPosts(
  page: number = 1,
  postsPerPage: number = 10
): Promise<{ posts: BlogPost[]; totalPages: number; currentPage: number }> {
  const allPosts = await getAllBlogPosts();
  const totalPages = Math.ceil(allPosts.length / postsPerPage);
  const startIndex = (page - 1) * postsPerPage;
  const endIndex = startIndex + postsPerPage;
  
  return {
    posts: allPosts.slice(startIndex, endIndex),
    totalPages,
    currentPage: page,
  };
}
