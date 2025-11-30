/**
 * Related Posts Logic
 * 
 * Functions to find related blog posts based on category and tags.
 */

import { BlogPost } from './types';
import { getAllBlogPosts } from './get-posts';

/**
 * Get related posts for a given blog post
 * Returns posts with the same category or matching tags
 */
export async function getRelatedPosts(
  currentPost: BlogPost,
  limit: number = 3
): Promise<BlogPost[]> {
  const allPosts = await getAllBlogPosts();
  
  // Filter out the current post
  const otherPosts = allPosts.filter(post => post.slug !== currentPost.slug);
  
  if (otherPosts.length === 0) {
    return [];
  }
  
  // Calculate relevance score for each post
  const scoredPosts = otherPosts.map(post => ({
    post,
    score: calculateRelevanceScore(currentPost, post),
  }));
  
  // Sort by relevance score (highest first)
  scoredPosts.sort((a, b) => b.score - a.score);
  
  // Return top N posts
  return scoredPosts.slice(0, limit).map(item => item.post);
}

/**
 * Calculate relevance score between two posts
 * Higher score means more related
 */
function calculateRelevanceScore(post1: BlogPost, post2: BlogPost): number {
  let score = 0;
  
  // Same category: +10 points
  if (post1.category === post2.category) {
    score += 10;
  }
  
  // Matching tags: +5 points per tag
  const matchingTags = post1.tags.filter(tag => 
    post2.tags.some(t => t.toLowerCase() === tag.toLowerCase())
  );
  score += matchingTags.length * 5;
  
  // Same author: +3 points
  if (post1.author.name === post2.author.name) {
    score += 3;
  }
  
  // Recent posts get a slight boost (within 30 days)
  const daysDiff = Math.abs(
    new Date(post1.publishedAt).getTime() - new Date(post2.publishedAt).getTime()
  ) / (1000 * 60 * 60 * 24);
  
  if (daysDiff <= 30) {
    score += 2;
  }
  
  return score;
}

/**
 * Get posts from the same category
 */
export async function getPostsByCategory(
  category: string,
  excludeSlug?: string,
  limit?: number
): Promise<BlogPost[]> {
  const allPosts = await getAllBlogPosts();
  
  let filtered = allPosts.filter(post => post.category === category);
  
  if (excludeSlug) {
    filtered = filtered.filter(post => post.slug !== excludeSlug);
  }
  
  if (limit) {
    filtered = filtered.slice(0, limit);
  }
  
  return filtered;
}

/**
 * Get posts with matching tags
 */
export async function getPostsByTags(
  tags: string[],
  excludeSlug?: string,
  limit?: number
): Promise<BlogPost[]> {
  const allPosts = await getAllBlogPosts();
  
  const lowerTags = tags.map(t => t.toLowerCase());
  
  let filtered = allPosts.filter(post => 
    post.tags.some(tag => lowerTags.includes(tag.toLowerCase()))
  );
  
  if (excludeSlug) {
    filtered = filtered.filter(post => post.slug !== excludeSlug);
  }
  
  if (limit) {
    filtered = filtered.slice(0, limit);
  }
  
  return filtered;
}
