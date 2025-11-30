/**
 * Tests for frontmatter validation utilities
 */

import { describe, it, expect } from 'vitest';
import { validateFrontmatter, formatValidationResult } from '../validate';
import { BlogFrontmatter } from '../types';

const validFrontmatter: BlogFrontmatter = {
  title: 'Test Blog Post Title',
  excerpt: 'This is a test excerpt that is long enough to meet the minimum length requirement of 150 characters. It provides a good summary of the blog post content.',
  author: {
    name: 'Dr. Test Author',
    role: 'Healthcare Consultant',
    avatar: '/images/authors/test.jpg',
  },
  publishedAt: '2024-01-15',
  category: 'practice-management',
  tags: ['test', 'blog', 'healthcare'],
  featuredImage: '/images/blog/test.jpg',
  seo: {
    title: 'Test Blog Post: A Comprehensive Guide for Clinics',
    description:
      'This is a test SEO description that is long enough to meet the minimum length requirement of 150 characters for optimal search engine optimization.',
    keywords: ['test', 'blog', 'healthcare', 'clinic', 'management'],
  },
};

describe('validateFrontmatter', () => {
  describe('required fields', () => {
    it('should pass validation for valid frontmatter', () => {
      const result = validateFrontmatter(validFrontmatter);
      expect(result.valid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('should fail if title is missing', () => {
      const { title, ...incomplete } = validFrontmatter;
      const result = validateFrontmatter(incomplete);
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'title',
          message: 'Missing required field: title',
        })
      );
    });

    it('should fail if excerpt is missing', () => {
      const { excerpt, ...incomplete } = validFrontmatter;
      const result = validateFrontmatter(incomplete);
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'excerpt',
          message: 'Missing required field: excerpt',
        })
      );
    });

    it('should fail if author is missing', () => {
      const { author, ...incomplete } = validFrontmatter;
      const result = validateFrontmatter(incomplete);
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'author',
          message: 'Missing required field: author',
        })
      );
    });

    it('should fail if publishedAt is missing', () => {
      const { publishedAt, ...incomplete } = validFrontmatter;
      const result = validateFrontmatter(incomplete);
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'publishedAt',
          message: 'Missing required field: publishedAt',
        })
      );
    });

    it('should fail if category is missing', () => {
      const { category, ...incomplete } = validFrontmatter;
      const result = validateFrontmatter(incomplete);
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'category',
          message: 'Missing required field: category',
        })
      );
    });

    it('should fail if tags are missing', () => {
      const { tags, ...incomplete } = validFrontmatter;
      const result = validateFrontmatter(incomplete);
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'tags',
          message: 'Missing required field: tags',
        })
      );
    });

    it('should fail if featuredImage is missing', () => {
      const { featuredImage, ...incomplete } = validFrontmatter;
      const result = validateFrontmatter(incomplete);
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'featuredImage',
          message: 'Missing required field: featuredImage',
        })
      );
    });

    it('should fail if seo is missing', () => {
      const { seo, ...incomplete } = validFrontmatter;
      const result = validateFrontmatter(incomplete);
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'seo',
          message: 'Missing required field: seo',
        })
      );
    });
  });

  describe('title validation', () => {
    it('should fail if title is empty', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        title: '',
      });
      expect(result.valid).toBe(false);
      // Empty string is caught by validateRequiredFields first
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'title',
          message: 'Missing required field: title',
        })
      );
    });

    it('should warn if title is very short', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        title: 'Short',
      });
      expect(result.warnings).toContainEqual(
        expect.objectContaining({
          field: 'title',
          severity: 'warning',
        })
      );
    });
  });

  describe('author validation', () => {
    it('should fail if author name is missing', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        author: {
          name: '',
          role: 'Test Role',
        },
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'author.name',
        })
      );
    });

    it('should fail if author role is missing', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        author: {
          name: 'Test Author',
          role: '',
        },
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'author.role',
        })
      );
    });
  });

  describe('date validation', () => {
    it('should pass for valid date format', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        publishedAt: '2024-01-15',
      });
      expect(result.valid).toBe(true);
    });

    it('should fail for invalid date format', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        publishedAt: '01/15/2024',
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'publishedAt',
          message: expect.stringContaining('YYYY-MM-DD'),
        })
      );
    });

    it('should fail for invalid date', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        publishedAt: '2024-13-45',
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'publishedAt',
          message: expect.stringContaining('not a valid date'),
        })
      );
    });
  });

  describe('category validation', () => {
    it('should pass for valid categories', () => {
      const categories = [
        'practice-management',
        'patient-care',
        'technology',
        'compliance',
        'industry-news',
      ];

      categories.forEach((category) => {
        const result = validateFrontmatter({
          ...validFrontmatter,
          category: category as any,
        });
        expect(result.valid).toBe(true);
      });
    });

    it('should fail for invalid category', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        category: 'invalid-category' as any,
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'category',
          message: expect.stringContaining('Invalid category'),
        })
      );
    });
  });

  describe('tags validation', () => {
    it('should fail if tags is not an array', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        tags: 'not-an-array' as any,
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'tags',
          message: 'Tags must be an array',
        })
      );
    });

    it('should fail if tags array is empty', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        tags: [],
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'tags',
          message: 'At least one tag is required',
        })
      );
    });

    it('should warn if too few tags', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        tags: ['one', 'two'],
      });
      expect(result.warnings).toContainEqual(
        expect.objectContaining({
          field: 'tags',
          severity: 'warning',
        })
      );
    });
  });

  describe('SEO validation', () => {
    it('should fail if SEO title is missing', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        seo: {
          ...validFrontmatter.seo,
          title: '',
        },
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'seo.title',
        })
      );
    });

    it('should fail if SEO description is missing', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        seo: {
          ...validFrontmatter.seo,
          description: '',
        },
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'seo.description',
        })
      );
    });

    it('should fail if SEO keywords is not an array', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        seo: {
          ...validFrontmatter.seo,
          keywords: 'not-an-array' as any,
        },
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'seo.keywords',
          message: 'SEO keywords must be an array',
        })
      );
    });

    it('should fail if SEO keywords array is empty', () => {
      const result = validateFrontmatter({
        ...validFrontmatter,
        seo: {
          ...validFrontmatter.seo,
          keywords: [],
        },
      });
      expect(result.valid).toBe(false);
      expect(result.errors).toContainEqual(
        expect.objectContaining({
          field: 'seo.keywords',
          message: 'At least one SEO keyword is required',
        })
      );
    });
  });
});

describe('formatValidationResult', () => {
  it('should format successful validation', () => {
    const result = {
      valid: true,
      errors: [],
      warnings: [],
    };
    const formatted = formatValidationResult(result);
    expect(formatted).toContain('✓ Validation passed!');
  });

  it('should format validation with errors', () => {
    const result = {
      valid: false,
      errors: [
        { field: 'title', message: 'Title is required', severity: 'error' as const },
      ],
      warnings: [],
    };
    const formatted = formatValidationResult(result);
    expect(formatted).toContain('✗ Validation failed!');
    expect(formatted).toContain('Errors:');
    expect(formatted).toContain('title: Title is required');
  });

  it('should format validation with warnings', () => {
    const result = {
      valid: true,
      errors: [],
      warnings: [
        { field: 'tags', message: 'Consider adding more tags', severity: 'warning' as const },
      ],
    };
    const formatted = formatValidationResult(result);
    expect(formatted).toContain('Warnings:');
    expect(formatted).toContain('tags: Consider adding more tags');
  });
});
