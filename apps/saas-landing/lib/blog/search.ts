/**
 * Blog Search Functionality
 * 
 * Functions to search blog posts by keyword.
 */

import { BlogPost, BlogSearchResult } from './types';
import { getAllBlogPosts, toBlogListItem } from './get-posts';

/**
 * Search blog posts by keyword
 * Returns posts that match the keyword in title, excerpt, content, or tags
 */
export async function searchBlogPosts(keyword: string): Promise<BlogSearchResult[]> {
  if (!keyword || keyword.trim().length === 0) {
    return [];
  }
  
  const allPosts = await getAllBlogPosts(true); // Include content for searching
  const searchTerm = keyword.toLowerCase().trim();
  const results: BlogSearchResult[] = [];
  
  for (const post of allPosts) {
    const matchedIn: ('title' | 'excerpt' | 'content' | 'tags')[] = [];
    
    // Check title
    if (post.title.toLowerCase().includes(searchTerm)) {
      matchedIn.push('title');
    }
    
    // Check excerpt
    if (post.excerpt.toLowerCase().includes(searchTerm)) {
      matchedIn.push('excerpt');
    }
    
    // Check content
    if (post.content.toLowerCase().includes(searchTerm)) {
      matchedIn.push('content');
    }
    
    // Check tags
    if (post.tags.some(tag => tag.toLowerCase().includes(searchTerm))) {
      matchedIn.push('tags');
    }
    
    // If any matches found, add to results
    if (matchedIn.length > 0) {
      const listItem = toBlogListItem(post);
      results.push({
        ...listItem,
        matchedIn,
      });
    }
  }
  
  // Sort by relevance (title matches first, then excerpt, then content, then tags)
  return results.sort((a, b) => {
    const aScore = getRelevanceScore(a.matchedIn);
    const bScore = getRelevanceScore(b.matchedIn);
    return bScore - aScore;
  });
}

/**
 * Calculate relevance score based on where the match was found
 */
function getRelevanceScore(matchedIn: ('title' | 'excerpt' | 'content' | 'tags')[]): number {
  let score = 0;
  
  if (matchedIn.includes('title')) score += 100;
  if (matchedIn.includes('excerpt')) score += 50;
  if (matchedIn.includes('tags')) score += 25;
  if (matchedIn.includes('content')) score += 10;
  
  return score;
}

/**
 * Get search suggestions based on partial keyword
 */
export async function getSearchSuggestions(partial: string, limit: number = 5): Promise<string[]> {
  if (!partial || partial.trim().length < 2) {
    return [];
  }
  
  const allPosts = await getAllBlogPosts();
  const suggestions = new Set<string>();
  const searchTerm = partial.toLowerCase().trim();
  
  // Collect matching titles and tags
  for (const post of allPosts) {
    // Add matching title words
    const titleWords = post.title.toLowerCase().split(/\s+/);
    titleWords.forEach(word => {
      if (word.startsWith(searchTerm) && word.length > 2) {
        suggestions.add(word);
      }
    });
    
    // Add matching tags
    post.tags.forEach(tag => {
      if (tag.toLowerCase().startsWith(searchTerm)) {
        suggestions.add(tag);
      }
    });
    
    if (suggestions.size >= limit) break;
  }
  
  return Array.from(suggestions).slice(0, limit);
}
