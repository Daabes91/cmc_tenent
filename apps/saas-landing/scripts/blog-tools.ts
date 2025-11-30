#!/usr/bin/env node

/**
 * Blog Tools CLI
 * 
 * Command-line utilities for blog post management:
 * - Generate slugs from titles
 * - Validate blog post frontmatter
 * - Create new blog posts from template
 */

import fs from 'fs';
import path from 'path';
import matter from 'gray-matter';
import { generateSlug, isValidSlug, makeUniqueSlug } from '../lib/blog/slug';
import { validateFrontmatter, formatValidationResult } from '../lib/blog/validate';
import { getAllBlogSlugs } from '../lib/blog/get-posts';

const BLOG_CONTENT_DIR = path.join(process.cwd(), 'content', 'blog');
const TEMPLATE_PATH = path.join(BLOG_CONTENT_DIR, '_template.mdx');

/**
 * Display help information
 */
function showHelp(): void {
  console.log(`
Blog Tools CLI

Usage:
  npm run blog:generate-slug <title>     Generate a slug from a title
  npm run blog:validate <file>           Validate a blog post file
  npm run blog:create <title>            Create a new blog post from template
  npm run blog:list                      List all blog posts
  npm run blog:help                      Show this help message

Examples:
  npm run blog:generate-slug "5 Ways to Reduce Patient No-Shows"
  npm run blog:validate content/blog/my-post.mdx
  npm run blog:create "Getting Started with Clinic Management"
  npm run blog:list
  `);
}

/**
 * Generate and display a slug from a title
 */
function handleGenerateSlug(title: string): void {
  if (!title) {
    console.error('Error: Title is required');
    console.log('Usage: npm run blog:generate-slug "Your Blog Post Title"');
    process.exit(1);
  }

  try {
    const slug = generateSlug(title);
    const existingSlugs = getAllBlogSlugs();
    const isUnique = !existingSlugs.includes(slug);

    console.log(`\nGenerated slug: ${slug}`);
    console.log(`Valid: ${isValidSlug(slug) ? '✓' : '✗'}`);
    console.log(`Unique: ${isUnique ? '✓' : '✗'}`);

    if (!isUnique) {
      const uniqueSlug = makeUniqueSlug(slug, existingSlugs);
      console.log(`\nSuggested unique slug: ${uniqueSlug}`);
    }

    console.log(`\nFile path: content/blog/${slug}.mdx`);
  } catch (error) {
    console.error('Error generating slug:', error);
    process.exit(1);
  }
}

/**
 * Validate a blog post file
 */
function handleValidate(filePath: string): void {
  if (!filePath) {
    console.error('Error: File path is required');
    console.log('Usage: npm run blog:validate content/blog/my-post.mdx');
    process.exit(1);
  }

  try {
    // Read the file
    const fullPath = path.resolve(process.cwd(), filePath);
    if (!fs.existsSync(fullPath)) {
      console.error(`Error: File not found: ${filePath}`);
      process.exit(1);
    }

    const fileContents = fs.readFileSync(fullPath, 'utf8');
    const { data } = matter(fileContents);

    // Validate frontmatter
    const result = validateFrontmatter(data);

    console.log(`\nValidating: ${filePath}`);
    console.log(formatValidationResult(result));

    if (!result.valid) {
      process.exit(1);
    }
  } catch (error) {
    console.error('Error validating file:', error);
    process.exit(1);
  }
}

/**
 * Create a new blog post from template
 */
function handleCreate(title: string): void {
  if (!title) {
    console.error('Error: Title is required');
    console.log('Usage: npm run blog:create "Your Blog Post Title"');
    process.exit(1);
  }

  try {
    // Generate slug
    const slug = generateSlug(title);
    const existingSlugs = getAllBlogSlugs();
    const finalSlug = makeUniqueSlug(slug, existingSlugs);

    // Check if template exists
    if (!fs.existsSync(TEMPLATE_PATH)) {
      console.error('Error: Template file not found at content/blog/_template.mdx');
      process.exit(1);
    }

    // Read template
    const template = fs.readFileSync(TEMPLATE_PATH, 'utf8');

    // Replace title in template
    const updatedTemplate = template.replace(
      /title: "Your Blog Post Title Here"/g,
      `title: "${title}"`
    );

    // Create new file
    const newFilePath = path.join(BLOG_CONTENT_DIR, `${finalSlug}.mdx`);
    fs.writeFileSync(newFilePath, updatedTemplate);

    console.log(`\n✓ Created new blog post:`);
    console.log(`  File: content/blog/${finalSlug}.mdx`);
    console.log(`  Title: ${title}`);
    console.log(`  Slug: ${finalSlug}`);
    console.log(`\nNext steps:`);
    console.log(`  1. Edit the file and fill in the frontmatter`);
    console.log(`  2. Write your content`);
    console.log(`  3. Validate: npm run blog:validate content/blog/${finalSlug}.mdx`);
    console.log(`  4. Preview: npm run dev (then visit /blog/${finalSlug})`);
  } catch (error) {
    console.error('Error creating blog post:', error);
    process.exit(1);
  }
}

/**
 * List all blog posts
 */
function handleList(): void {
  try {
    const slugs = getAllBlogSlugs();

    if (slugs.length === 0) {
      console.log('\nNo blog posts found in content/blog/');
      return;
    }

    console.log(`\nFound ${slugs.length} blog post(s):\n`);

    slugs.forEach((slug, index) => {
      const filePath = path.join(BLOG_CONTENT_DIR, `${slug}.mdx`);
      const fileContents = fs.readFileSync(filePath, 'utf8');
      const { data } = matter(fileContents);

      const isDraft = data.draft === true;
      const status = isDraft ? '[DRAFT]' : '[PUBLISHED]';

      console.log(`${index + 1}. ${status} ${data.title || slug}`);
      console.log(`   Slug: ${slug}`);
      console.log(`   Category: ${data.category || 'N/A'}`);
      console.log(`   Published: ${data.publishedAt || 'N/A'}`);
      console.log('');
    });
  } catch (error) {
    console.error('Error listing blog posts:', error);
    process.exit(1);
  }
}

/**
 * Main CLI handler
 */
function main(): void {
  const args = process.argv.slice(2);
  const command = args[0];
  const param = args.slice(1).join(' ');

  switch (command) {
    case 'generate-slug':
      handleGenerateSlug(param);
      break;
    case 'validate':
      handleValidate(param);
      break;
    case 'create':
      handleCreate(param);
      break;
    case 'list':
      handleList();
      break;
    case 'help':
    case '--help':
    case '-h':
      showHelp();
      break;
    default:
      console.error(`Unknown command: ${command}`);
      showHelp();
      process.exit(1);
  }
}

// Run CLI
main();
