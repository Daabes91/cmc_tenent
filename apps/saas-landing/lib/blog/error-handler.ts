/**
 * Blog Error Handling Utilities
 * 
 * Centralized error handling for blog operations
 */

/**
 * Blog error types
 */
export enum BlogErrorType {
  NOT_FOUND = 'NOT_FOUND',
  PARSING_ERROR = 'PARSING_ERROR',
  INVALID_FRONTMATTER = 'INVALID_FRONTMATTER',
  FILE_READ_ERROR = 'FILE_READ_ERROR',
  UNKNOWN = 'UNKNOWN',
}

/**
 * Blog error class
 */
export class BlogError extends Error {
  constructor(
    message: string,
    public readonly type: BlogErrorType,
    public readonly slug?: string,
    public readonly originalError?: Error
  ) {
    super(message);
    this.name = 'BlogError';
  }
}

/**
 * Log blog error to monitoring service
 */
export function logBlogError(error: BlogError | Error, context?: Record<string, any>) {
  // Log to console
  console.error('Blog error:', {
    message: error.message,
    type: error instanceof BlogError ? error.type : 'UNKNOWN',
    slug: error instanceof BlogError ? error.slug : undefined,
    context,
  });

  // Log to analytics if available
  if (typeof window !== 'undefined' && (window as any).gtag) {
    (window as any).gtag('event', 'exception', {
      description: `Blog error: ${error.message}`,
      fatal: false,
    });
  }

  // In production, you might want to send to error tracking service
  // Example: Sentry.captureException(error);
}

/**
 * Handle blog post loading errors
 */
export function handleBlogPostError(error: unknown, slug: string): BlogError {
  if (error instanceof BlogError) {
    logBlogError(error);
    return error;
  }

  if (error instanceof Error) {
    const blogError = new BlogError(
      `Failed to load blog post: ${slug}`,
      BlogErrorType.UNKNOWN,
      slug,
      error
    );
    logBlogError(blogError);
    return blogError;
  }

  const blogError = new BlogError(
    `Unknown error loading blog post: ${slug}`,
    BlogErrorType.UNKNOWN,
    slug
  );
  logBlogError(blogError);
  return blogError;
}

/**
 * Validate blog post frontmatter
 */
export function validateBlogFrontmatter(data: any, slug: string): void {
  const requiredFields = ['title', 'excerpt', 'author', 'publishedAt', 'category', 'featuredImage'];
  const missingFields: string[] = [];

  for (const field of requiredFields) {
    if (!data[field]) {
      missingFields.push(field);
    }
  }

  if (missingFields.length > 0) {
    throw new BlogError(
      `Missing required frontmatter fields: ${missingFields.join(', ')}`,
      BlogErrorType.INVALID_FRONTMATTER,
      slug
    );
  }

  // Validate author structure
  if (!data.author.name || !data.author.role) {
    throw new BlogError(
      'Invalid author structure in frontmatter',
      BlogErrorType.INVALID_FRONTMATTER,
      slug
    );
  }

  // Validate category
  const validCategories = ['practice-management', 'patient-care', 'technology', 'compliance', 'industry-news'];
  if (!validCategories.includes(data.category)) {
    throw new BlogError(
      `Invalid category: ${data.category}. Must be one of: ${validCategories.join(', ')}`,
      BlogErrorType.INVALID_FRONTMATTER,
      slug
    );
  }

  // Validate tags
  if (!Array.isArray(data.tags)) {
    throw new BlogError(
      'Tags must be an array',
      BlogErrorType.INVALID_FRONTMATTER,
      slug
    );
  }
}

/**
 * Safe blog post fetch with error handling
 */
export async function safeBlogFetch<T>(
  operation: () => Promise<T>,
  fallback: T,
  errorContext?: string
): Promise<T> {
  try {
    return await operation();
  } catch (error) {
    console.error(`Blog fetch error${errorContext ? ` (${errorContext})` : ''}:`, error);
    
    if (error instanceof BlogError) {
      logBlogError(error);
    } else if (error instanceof Error) {
      logBlogError(new BlogError(
        error.message,
        BlogErrorType.UNKNOWN,
        undefined,
        error
      ));
    }

    return fallback;
  }
}
