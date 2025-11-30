/**
 * Blog Post Frontmatter Validation
 * 
 * Utilities for validating blog post frontmatter fields.
 */

import { BlogFrontmatter, BlogCategory } from './types';

export interface ValidationError {
  field: string;
  message: string;
  severity: 'error' | 'warning';
}

export interface ValidationResult {
  valid: boolean;
  errors: ValidationError[];
  warnings: ValidationError[];
}

const VALID_CATEGORIES: BlogCategory[] = [
  'practice-management',
  'patient-care',
  'technology',
  'compliance',
  'industry-news',
];

const SEO_TITLE_MIN_LENGTH = 40;
const SEO_TITLE_MAX_LENGTH = 60;
const SEO_DESCRIPTION_MIN_LENGTH = 150;
const SEO_DESCRIPTION_MAX_LENGTH = 160;
const EXCERPT_MIN_LENGTH = 150;
const EXCERPT_MAX_LENGTH = 200;

/**
 * Validate blog post frontmatter
 * 
 * Checks all required fields and validates their format and content.
 * Returns validation errors and warnings.
 * 
 * @param frontmatter - The frontmatter object to validate
 * @returns Validation result with errors and warnings
 */
export function validateFrontmatter(frontmatter: Partial<BlogFrontmatter>): ValidationResult {
  const errors: ValidationError[] = [];
  const warnings: ValidationError[] = [];

  // Validate required fields
  validateRequiredFields(frontmatter, errors);

  // Validate field formats and content
  if (frontmatter.title) {
    validateTitle(frontmatter.title, errors, warnings);
  }

  if (frontmatter.excerpt) {
    validateExcerpt(frontmatter.excerpt, errors, warnings);
  }

  if (frontmatter.author) {
    validateAuthor(frontmatter.author, errors);
  }

  if (frontmatter.publishedAt) {
    validateDate(frontmatter.publishedAt, 'publishedAt', errors);
  }

  if (frontmatter.updatedAt) {
    validateDate(frontmatter.updatedAt, 'updatedAt', errors);
  }

  if (frontmatter.category) {
    validateCategory(frontmatter.category, errors);
  }

  if (frontmatter.tags) {
    validateTags(frontmatter.tags, errors, warnings);
  }

  if (frontmatter.featuredImage) {
    validateFeaturedImage(frontmatter.featuredImage, errors, warnings);
  }

  if (frontmatter.seo) {
    validateSEO(frontmatter.seo, errors, warnings);
  }

  return {
    valid: errors.length === 0,
    errors,
    warnings,
  };
}

/**
 * Validate that all required fields are present
 */
function validateRequiredFields(frontmatter: Partial<BlogFrontmatter>, errors: ValidationError[]): void {
  const requiredFields: (keyof BlogFrontmatter)[] = [
    'title',
    'excerpt',
    'author',
    'publishedAt',
    'category',
    'tags',
    'featuredImage',
    'seo',
  ];

  for (const field of requiredFields) {
    if (!frontmatter[field]) {
      errors.push({
        field,
        message: `Missing required field: ${field}`,
        severity: 'error',
      });
    }
  }
}

/**
 * Validate title field
 */
function validateTitle(title: string, errors: ValidationError[], warnings: ValidationError[]): void {
  if (typeof title !== 'string') {
    errors.push({
      field: 'title',
      message: 'Title must be a string',
      severity: 'error',
    });
    return;
  }

  if (title.trim().length === 0) {
    errors.push({
      field: 'title',
      message: 'Title cannot be empty',
      severity: 'error',
    });
    return;
  }

  if (title.length < 10) {
    warnings.push({
      field: 'title',
      message: 'Title is very short (less than 10 characters)',
      severity: 'warning',
    });
  }

  if (title.length > 100) {
    warnings.push({
      field: 'title',
      message: 'Title is very long (more than 100 characters)',
      severity: 'warning',
    });
  }
}

/**
 * Validate excerpt field
 */
function validateExcerpt(excerpt: string, errors: ValidationError[], warnings: ValidationError[]): void {
  if (typeof excerpt !== 'string') {
    errors.push({
      field: 'excerpt',
      message: 'Excerpt must be a string',
      severity: 'error',
    });
    return;
  }

  if (excerpt.trim().length === 0) {
    errors.push({
      field: 'excerpt',
      message: 'Excerpt cannot be empty',
      severity: 'error',
    });
    return;
  }

  if (excerpt.length < EXCERPT_MIN_LENGTH) {
    warnings.push({
      field: 'excerpt',
      message: `Excerpt is shorter than recommended (${excerpt.length}/${EXCERPT_MIN_LENGTH} characters)`,
      severity: 'warning',
    });
  }

  if (excerpt.length > EXCERPT_MAX_LENGTH) {
    warnings.push({
      field: 'excerpt',
      message: `Excerpt is longer than recommended (${excerpt.length}/${EXCERPT_MAX_LENGTH} characters)`,
      severity: 'warning',
    });
  }
}

/**
 * Validate author field
 */
function validateAuthor(author: any, errors: ValidationError[]): void {
  if (typeof author !== 'object' || author === null) {
    errors.push({
      field: 'author',
      message: 'Author must be an object',
      severity: 'error',
    });
    return;
  }

  if (!author.name || typeof author.name !== 'string' || author.name.trim().length === 0) {
    errors.push({
      field: 'author.name',
      message: 'Author name is required and must be a non-empty string',
      severity: 'error',
    });
  }

  if (!author.role || typeof author.role !== 'string' || author.role.trim().length === 0) {
    errors.push({
      field: 'author.role',
      message: 'Author role is required and must be a non-empty string',
      severity: 'error',
    });
  }

  if (author.avatar && typeof author.avatar !== 'string') {
    errors.push({
      field: 'author.avatar',
      message: 'Author avatar must be a string (image path)',
      severity: 'error',
    });
  }
}

/**
 * Validate date field (publishedAt or updatedAt)
 */
function validateDate(date: string, fieldName: string, errors: ValidationError[]): void {
  if (typeof date !== 'string') {
    errors.push({
      field: fieldName,
      message: `${fieldName} must be a string in YYYY-MM-DD format`,
      severity: 'error',
    });
    return;
  }

  // Check format: YYYY-MM-DD
  const datePattern = /^\d{4}-\d{2}-\d{2}$/;
  if (!datePattern.test(date)) {
    errors.push({
      field: fieldName,
      message: `${fieldName} must be in YYYY-MM-DD format (e.g., "2024-01-15")`,
      severity: 'error',
    });
    return;
  }

  // Check if it's a valid date
  const parsedDate = new Date(date);
  if (isNaN(parsedDate.getTime())) {
    errors.push({
      field: fieldName,
      message: `${fieldName} is not a valid date`,
      severity: 'error',
    });
  }
}

/**
 * Validate category field
 */
function validateCategory(category: string, errors: ValidationError[]): void {
  if (typeof category !== 'string') {
    errors.push({
      field: 'category',
      message: 'Category must be a string',
      severity: 'error',
    });
    return;
  }

  if (!VALID_CATEGORIES.includes(category as BlogCategory)) {
    errors.push({
      field: 'category',
      message: `Invalid category. Must be one of: ${VALID_CATEGORIES.join(', ')}`,
      severity: 'error',
    });
  }
}

/**
 * Validate tags field
 */
function validateTags(tags: any, errors: ValidationError[], warnings: ValidationError[]): void {
  if (!Array.isArray(tags)) {
    errors.push({
      field: 'tags',
      message: 'Tags must be an array',
      severity: 'error',
    });
    return;
  }

  if (tags.length === 0) {
    errors.push({
      field: 'tags',
      message: 'At least one tag is required',
      severity: 'error',
    });
    return;
  }

  // Check each tag is a string
  tags.forEach((tag, index) => {
    if (typeof tag !== 'string' || tag.trim().length === 0) {
      errors.push({
        field: `tags[${index}]`,
        message: `Tag at index ${index} must be a non-empty string`,
        severity: 'error',
      });
    }
  });

  if (tags.length < 3) {
    warnings.push({
      field: 'tags',
      message: 'Consider adding more tags (3-6 recommended)',
      severity: 'warning',
    });
  }

  if (tags.length > 10) {
    warnings.push({
      field: 'tags',
      message: 'Too many tags (3-6 recommended)',
      severity: 'warning',
    });
  }
}

/**
 * Validate featuredImage field
 */
function validateFeaturedImage(featuredImage: string, errors: ValidationError[], warnings: ValidationError[]): void {
  if (typeof featuredImage !== 'string') {
    errors.push({
      field: 'featuredImage',
      message: 'Featured image must be a string (image path)',
      severity: 'error',
    });
    return;
  }

  if (featuredImage.trim().length === 0) {
    errors.push({
      field: 'featuredImage',
      message: 'Featured image path cannot be empty',
      severity: 'error',
    });
    return;
  }

  if (!featuredImage.startsWith('/images/')) {
    warnings.push({
      field: 'featuredImage',
      message: 'Featured image should be stored in /images/ directory',
      severity: 'warning',
    });
  }
}

/**
 * Validate SEO field
 */
function validateSEO(seo: any, errors: ValidationError[], warnings: ValidationError[]): void {
  if (typeof seo !== 'object' || seo === null) {
    errors.push({
      field: 'seo',
      message: 'SEO must be an object',
      severity: 'error',
    });
    return;
  }

  // Validate SEO title
  if (!seo.title || typeof seo.title !== 'string' || seo.title.trim().length === 0) {
    errors.push({
      field: 'seo.title',
      message: 'SEO title is required and must be a non-empty string',
      severity: 'error',
    });
  } else {
    if (seo.title.length < SEO_TITLE_MIN_LENGTH) {
      warnings.push({
        field: 'seo.title',
        message: `SEO title is shorter than recommended (${seo.title.length}/${SEO_TITLE_MIN_LENGTH} characters)`,
        severity: 'warning',
      });
    }
    if (seo.title.length > SEO_TITLE_MAX_LENGTH) {
      warnings.push({
        field: 'seo.title',
        message: `SEO title is longer than recommended (${seo.title.length}/${SEO_TITLE_MAX_LENGTH} characters)`,
        severity: 'warning',
      });
    }
  }

  // Validate SEO description
  if (!seo.description || typeof seo.description !== 'string' || seo.description.trim().length === 0) {
    errors.push({
      field: 'seo.description',
      message: 'SEO description is required and must be a non-empty string',
      severity: 'error',
    });
  } else {
    if (seo.description.length < SEO_DESCRIPTION_MIN_LENGTH) {
      warnings.push({
        field: 'seo.description',
        message: `SEO description is shorter than recommended (${seo.description.length}/${SEO_DESCRIPTION_MIN_LENGTH} characters)`,
        severity: 'warning',
      });
    }
    if (seo.description.length > SEO_DESCRIPTION_MAX_LENGTH) {
      warnings.push({
        field: 'seo.description',
        message: `SEO description is longer than recommended (${seo.description.length}/${SEO_DESCRIPTION_MAX_LENGTH} characters)`,
        severity: 'warning',
      });
    }
  }

  // Validate SEO keywords
  if (!seo.keywords || !Array.isArray(seo.keywords)) {
    errors.push({
      field: 'seo.keywords',
      message: 'SEO keywords must be an array',
      severity: 'error',
    });
  } else {
    if (seo.keywords.length === 0) {
      errors.push({
        field: 'seo.keywords',
        message: 'At least one SEO keyword is required',
        severity: 'error',
      });
    }

    if (seo.keywords.length < 5) {
      warnings.push({
        field: 'seo.keywords',
        message: 'Consider adding more SEO keywords (5-10 recommended)',
        severity: 'warning',
      });
    }

    if (seo.keywords.length > 15) {
      warnings.push({
        field: 'seo.keywords',
        message: 'Too many SEO keywords (5-10 recommended)',
        severity: 'warning',
      });
    }

    // Check each keyword is a string
    seo.keywords.forEach((keyword: any, index: number) => {
      if (typeof keyword !== 'string' || keyword.trim().length === 0) {
        errors.push({
          field: `seo.keywords[${index}]`,
          message: `Keyword at index ${index} must be a non-empty string`,
          severity: 'error',
        });
      }
    });
  }
}

/**
 * Format validation result as a human-readable string
 */
export function formatValidationResult(result: ValidationResult): string {
  const lines: string[] = [];

  if (result.valid) {
    lines.push('✓ Validation passed!');
  } else {
    lines.push('✗ Validation failed!');
  }

  if (result.errors.length > 0) {
    lines.push('\nErrors:');
    result.errors.forEach(error => {
      lines.push(`  ✗ ${error.field}: ${error.message}`);
    });
  }

  if (result.warnings.length > 0) {
    lines.push('\nWarnings:');
    result.warnings.forEach(warning => {
      lines.push(`  ⚠ ${warning.field}: ${warning.message}`);
    });
  }

  return lines.join('\n');
}

/**
 * Quick validation check - returns true if valid, throws error if invalid
 */
export function assertValidFrontmatter(frontmatter: Partial<BlogFrontmatter>): asserts frontmatter is BlogFrontmatter {
  const result = validateFrontmatter(frontmatter);
  if (!result.valid) {
    throw new Error(`Invalid frontmatter:\n${formatValidationResult(result)}`);
  }
}
