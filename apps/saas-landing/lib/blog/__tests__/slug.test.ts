/**
 * Tests for slug generation utilities
 */

import { describe, it, expect } from 'vitest';
import {
  generateSlug,
  isValidSlug,
  sanitizeSlug,
  isUniqueSlug,
  makeUniqueSlug,
  extractSlugFromPath,
  generateFilePath,
} from '../slug';

describe('generateSlug', () => {
  it('should convert title to lowercase hyphenated slug', () => {
    expect(generateSlug('5 Ways to Reduce Patient No-Shows')).toBe(
      '5-ways-to-reduce-patient-no-shows'
    );
  });

  it('should remove special characters', () => {
    expect(generateSlug('HIPAA Compliance: What You Need to Know!')).toBe(
      'hipaa-compliance-what-you-need-to-know'
    );
  });

  it('should handle multiple spaces', () => {
    expect(generateSlug('Patient   Management    Tips')).toBe(
      'patient-management-tips'
    );
  });

  it('should remove leading and trailing hyphens', () => {
    expect(generateSlug('  -Test Post-  ')).toBe('test-post');
  });

  it('should collapse multiple consecutive hyphens', () => {
    expect(generateSlug('Test---Post')).toBe('test-post');
  });

  it('should handle empty string', () => {
    expect(() => generateSlug('')).toThrow();
  });

  it('should handle non-string input', () => {
    expect(() => generateSlug(null as any)).toThrow();
  });

  it('should handle numbers in title', () => {
    expect(generateSlug('10 Best Practices for 2024')).toBe(
      '10-best-practices-for-2024'
    );
  });

  it('should handle apostrophes and quotes', () => {
    expect(generateSlug("Doctor's Guide to Patient Care")).toBe(
      'doctors-guide-to-patient-care'
    );
  });
});

describe('isValidSlug', () => {
  it('should return true for valid slugs', () => {
    expect(isValidSlug('my-blog-post')).toBe(true);
    expect(isValidSlug('post-123')).toBe(true);
    expect(isValidSlug('5-ways-to-improve')).toBe(true);
  });

  it('should return false for invalid slugs', () => {
    expect(isValidSlug('My-Blog-Post')).toBe(false); // uppercase
    expect(isValidSlug('my_blog_post')).toBe(false); // underscore
    expect(isValidSlug('my--post')).toBe(false); // consecutive hyphens
    expect(isValidSlug('-my-post')).toBe(false); // leading hyphen
    expect(isValidSlug('my-post-')).toBe(false); // trailing hyphen
    expect(isValidSlug('')).toBe(false); // empty
    expect(isValidSlug('my post')).toBe(false); // space
  });
});

describe('sanitizeSlug', () => {
  it('should return valid slug unchanged', () => {
    expect(sanitizeSlug('my-blog-post')).toBe('my-blog-post');
  });

  it('should generate new slug for invalid input', () => {
    expect(sanitizeSlug('My Blog Post')).toBe('my-blog-post');
    expect(sanitizeSlug('Test_Post')).toBe('test_post'); // Underscores are valid in slugs
  });
});

describe('isUniqueSlug', () => {
  const existingSlugs = ['post-1', 'post-2', 'my-blog-post'];

  it('should return true for unique slug', () => {
    expect(isUniqueSlug('new-post', existingSlugs)).toBe(true);
  });

  it('should return false for existing slug', () => {
    expect(isUniqueSlug('post-1', existingSlugs)).toBe(false);
    expect(isUniqueSlug('my-blog-post', existingSlugs)).toBe(false);
  });
});

describe('makeUniqueSlug', () => {
  const existingSlugs = ['my-post', 'my-post-2', 'my-post-3'];

  it('should return slug unchanged if unique', () => {
    expect(makeUniqueSlug('new-post', existingSlugs)).toBe('new-post');
  });

  it('should append number if slug exists', () => {
    expect(makeUniqueSlug('my-post', existingSlugs)).toBe('my-post-4');
  });

  it('should find next available number', () => {
    const slugs = ['test', 'test-2'];
    expect(makeUniqueSlug('test', slugs)).toBe('test-3'); // Both test and test-2 exist, so next is test-3
  });
});

describe('extractSlugFromPath', () => {
  it('should extract slug from .mdx file path', () => {
    expect(extractSlugFromPath('content/blog/my-post.mdx')).toBe('my-post');
  });

  it('should extract slug from .md file path', () => {
    expect(extractSlugFromPath('content/blog/my-post.md')).toBe('my-post');
  });

  it('should handle path with multiple directories', () => {
    expect(extractSlugFromPath('/var/www/content/blog/my-post.mdx')).toBe(
      'my-post'
    );
  });
});

describe('generateFilePath', () => {
  it('should generate file path with default extension', () => {
    expect(generateFilePath('my-post')).toBe('content/blog/my-post.mdx');
  });

  it('should generate file path with custom extension', () => {
    expect(generateFilePath('my-post', 'md')).toBe('content/blog/my-post.md');
  });
});
