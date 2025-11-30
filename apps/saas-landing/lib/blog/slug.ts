/**
 * Blog Post Slug Generation
 * 
 * Utilities for generating SEO-friendly URL slugs from blog post titles.
 */

/**
 * Generate a URL-friendly slug from a blog post title
 * 
 * Rules:
 * - Convert to lowercase
 * - Replace spaces with hyphens
 * - Remove special characters (keep only alphanumeric and hyphens)
 * - Remove leading/trailing hyphens
 * - Collapse multiple consecutive hyphens into one
 * 
 * @param title - The blog post title
 * @returns SEO-friendly slug
 * 
 * @example
 * generateSlug("5 Ways to Reduce Patient No-Shows")
 * // Returns: "5-ways-to-reduce-patient-no-shows"
 * 
 * @example
 * generateSlug("HIPAA Compliance: What You Need to Know!")
 * // Returns: "hipaa-compliance-what-you-need-to-know"
 */
export function generateSlug(title: string): string {
  if (!title || typeof title !== 'string') {
    throw new Error('Title must be a non-empty string');
  }

  return title
    .toLowerCase() // Convert to lowercase
    .trim() // Remove leading/trailing whitespace
    .replace(/[^\w\s-]/g, '') // Remove special characters (keep alphanumeric, spaces, hyphens)
    .replace(/\s+/g, '-') // Replace spaces with hyphens
    .replace(/-+/g, '-') // Collapse multiple hyphens
    .replace(/^-+|-+$/g, ''); // Remove leading/trailing hyphens
}

/**
 * Validate if a slug is properly formatted
 * 
 * A valid slug:
 * - Contains only lowercase letters, numbers, and hyphens
 * - Does not start or end with a hyphen
 * - Does not contain consecutive hyphens
 * - Is not empty
 * 
 * @param slug - The slug to validate
 * @returns true if valid, false otherwise
 * 
 * @example
 * isValidSlug("my-blog-post")
 * // Returns: true
 * 
 * @example
 * isValidSlug("My-Blog-Post")
 * // Returns: false (contains uppercase)
 */
export function isValidSlug(slug: string): boolean {
  if (!slug || typeof slug !== 'string') {
    return false;
  }

  // Check if slug matches the pattern: lowercase alphanumeric and hyphens only
  const slugPattern = /^[a-z0-9]+(-[a-z0-9]+)*$/;
  return slugPattern.test(slug);
}

/**
 * Sanitize a slug to ensure it's valid
 * 
 * If the slug is already valid, returns it unchanged.
 * Otherwise, generates a new slug from the input.
 * 
 * @param slug - The slug to sanitize
 * @returns A valid slug
 * 
 * @example
 * sanitizeSlug("My-Blog-Post")
 * // Returns: "my-blog-post"
 */
export function sanitizeSlug(slug: string): string {
  if (isValidSlug(slug)) {
    return slug;
  }
  return generateSlug(slug);
}

/**
 * Check if a slug is unique among existing blog posts
 * 
 * @param slug - The slug to check
 * @param existingSlugs - Array of existing slugs
 * @returns true if unique, false if already exists
 */
export function isUniqueSlug(slug: string, existingSlugs: string[]): boolean {
  return !existingSlugs.includes(slug);
}

/**
 * Generate a unique slug by appending a number if necessary
 * 
 * @param baseSlug - The base slug to make unique
 * @param existingSlugs - Array of existing slugs
 * @returns A unique slug
 * 
 * @example
 * makeUniqueSlug("my-post", ["my-post", "my-post-2"])
 * // Returns: "my-post-3"
 */
export function makeUniqueSlug(baseSlug: string, existingSlugs: string[]): string {
  if (isUniqueSlug(baseSlug, existingSlugs)) {
    return baseSlug;
  }

  let counter = 2;
  let uniqueSlug = `${baseSlug}-${counter}`;

  while (!isUniqueSlug(uniqueSlug, existingSlugs)) {
    counter++;
    uniqueSlug = `${baseSlug}-${counter}`;
  }

  return uniqueSlug;
}

/**
 * Extract a slug from a file path
 * 
 * @param filePath - Path to the blog post file
 * @returns The slug extracted from the filename
 * 
 * @example
 * extractSlugFromPath("content/blog/my-post.mdx")
 * // Returns: "my-post"
 */
export function extractSlugFromPath(filePath: string): string {
  const filename = filePath.split('/').pop() || '';
  return filename.replace(/\.mdx?$/, '');
}

/**
 * Generate a file path from a slug
 * 
 * @param slug - The blog post slug
 * @param extension - File extension (default: 'mdx')
 * @returns Full file path for the blog post
 * 
 * @example
 * generateFilePath("my-post")
 * // Returns: "content/blog/my-post.mdx"
 */
export function generateFilePath(slug: string, extension: string = 'mdx'): string {
  return `content/blog/${slug}.${extension}`;
}
