/**
 * Search Result Highlighting Utilities
 * 
 * Functions to highlight search terms in blog post content.
 */

/**
 * Highlight search terms in text
 * Returns HTML string with highlighted terms wrapped in <mark> tags
 */
export function highlightSearchTerm(text: string, searchTerm: string): string {
  if (!searchTerm || !text) {
    return text;
  }

  const escapedTerm = searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const regex = new RegExp(`(${escapedTerm})`, 'gi');
  
  return text.replace(regex, '<mark class="bg-yellow-200 dark:bg-yellow-900/50 px-1 rounded">$1</mark>');
}

/**
 * Extract a snippet of text around the search term
 * Returns a text excerpt with the search term in context
 */
export function extractSearchSnippet(
  text: string,
  searchTerm: string,
  maxLength: number = 150
): string {
  if (!searchTerm || !text) {
    return text.substring(0, maxLength) + (text.length > maxLength ? '...' : '');
  }

  const lowerText = text.toLowerCase();
  const lowerTerm = searchTerm.toLowerCase();
  const index = lowerText.indexOf(lowerTerm);

  if (index === -1) {
    return text.substring(0, maxLength) + (text.length > maxLength ? '...' : '');
  }

  // Calculate start and end positions to center the search term
  const halfLength = Math.floor(maxLength / 2);
  let start = Math.max(0, index - halfLength);
  let end = Math.min(text.length, start + maxLength);

  // Adjust start if we're at the end of the text
  if (end === text.length) {
    start = Math.max(0, end - maxLength);
  }

  // Find word boundaries
  if (start > 0) {
    const spaceIndex = text.indexOf(' ', start);
    if (spaceIndex !== -1 && spaceIndex < start + 20) {
      start = spaceIndex + 1;
    }
  }

  if (end < text.length) {
    const spaceIndex = text.lastIndexOf(' ', end);
    if (spaceIndex !== -1 && spaceIndex > end - 20) {
      end = spaceIndex;
    }
  }

  const snippet = text.substring(start, end);
  const prefix = start > 0 ? '...' : '';
  const suffix = end < text.length ? '...' : '';

  return prefix + snippet + suffix;
}

/**
 * Get a preview of where the search term was found
 */
export function getMatchPreview(
  post: { title: string; excerpt: string; content?: string },
  searchTerm: string
): { text: string; location: 'title' | 'excerpt' | 'content' } {
  const lowerTerm = searchTerm.toLowerCase();

  // Check title first
  if (post.title.toLowerCase().includes(lowerTerm)) {
    return {
      text: post.title,
      location: 'title',
    };
  }

  // Check excerpt
  if (post.excerpt.toLowerCase().includes(lowerTerm)) {
    return {
      text: extractSearchSnippet(post.excerpt, searchTerm, 150),
      location: 'excerpt',
    };
  }

  // Check content
  if (post.content && post.content.toLowerCase().includes(lowerTerm)) {
    return {
      text: extractSearchSnippet(post.content, searchTerm, 150),
      location: 'content',
    };
  }

  // Fallback to excerpt
  return {
    text: post.excerpt,
    location: 'excerpt',
  };
}
