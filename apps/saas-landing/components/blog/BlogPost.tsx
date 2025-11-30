/**
 * Blog Post Component
 * 
 * Renders a complete blog post with MDX content, author info, and social sharing.
 */

'use client';

import Link from 'next/link';
import Image from 'next/image';
import { BlogPost as BlogPostType } from '@/lib/blog/types';
import { SocialShare } from './SocialShare';
import { BlogPostSchema } from './BlogPostSchema';
import { useMDXComponents } from '@/mdx-components';
import { MDXProvider } from '@mdx-js/react';

interface BlogPostProps {
  post: BlogPostType;
  siteUrl: string;
}

export function BlogPost({ post, siteUrl }: BlogPostProps) {
  const components = useMDXComponents({});
  const postUrl = `${siteUrl}/blog/${post.slug}`;
  
  // Format category for display
  const categoryDisplay = post.category
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
  
  return (
    <>
      <BlogPostSchema post={post} siteUrl={siteUrl} />
      <article className="container mx-auto px-4 py-16">
        <div className="max-w-3xl mx-auto">
        {/* Header */}
        <header className="mb-8">
          <div className="flex items-center gap-2 mb-4">
            <Link
              href={`/blog?category=${post.category}`}
              className="text-sm bg-primary/10 text-primary px-3 py-1 rounded-full hover:bg-primary/20 transition-colors"
            >
              {categoryDisplay}
            </Link>
            <span className="text-sm text-muted-foreground">
              {post.readingTime} min read
            </span>
          </div>
          
          <h1 className="text-4xl md:text-5xl font-bold mb-4 leading-tight">
            {post.title}
          </h1>
          
          <p className="text-xl text-muted-foreground mb-6">
            {post.excerpt}
          </p>
          
          <div className="flex items-center justify-between flex-wrap gap-4">
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-3">
                {post.author.avatar && (
                  <div className="relative w-12 h-12 rounded-full overflow-hidden">
                    <Image
                      src={post.author.avatar}
                      alt={post.author.name}
                      fill
                      className="object-cover"
                    />
                  </div>
                )}
                <div>
                  <div className="font-medium text-foreground">
                    {post.author.name}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    {post.author.role}
                  </div>
                </div>
              </div>
              <span className="text-muted-foreground">•</span>
              <time 
                dateTime={post.publishedAt}
                className="text-muted-foreground"
              >
                {new Date(post.publishedAt).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                })}
              </time>
            </div>
            
            <SocialShare
              url={postUrl}
              title={post.title}
              description={post.excerpt}
            />
          </div>
        </header>

        {/* Featured Image */}
        {post.featuredImage && (
          <div className="mb-8 rounded-lg overflow-hidden">
            <div className="relative w-full aspect-[2/1]">
              <Image
                src={post.featuredImage}
                alt={post.title}
                fill
                className="object-cover"
                priority
              />
            </div>
          </div>
        )}

        {/* Content */}
        <div className="prose prose-lg dark:prose-invert max-w-none">
          <MDXProvider components={components}>
            <div dangerouslySetInnerHTML={{ __html: post.content }} />
          </MDXProvider>
        </div>

        {/* Tags */}
        {post.tags && post.tags.length > 0 && (
          <div className="mt-8 pt-8 border-t">
            <h3 className="text-sm font-medium mb-3">Tags</h3>
            <div className="flex flex-wrap gap-2">
              {post.tags.map((tag) => (
                <Link
                  key={tag}
                  href={`/blog?tag=${encodeURIComponent(tag)}`}
                  className="text-sm bg-muted px-3 py-1 rounded-full hover:bg-muted/80 transition-colors"
                >
                  #{tag}
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* Author Bio */}
        <div className="mt-8 pt-8 border-t">
          <div className="flex items-start gap-4">
            {post.author.avatar && (
              <div className="relative w-16 h-16 rounded-full overflow-hidden flex-shrink-0">
                <Image
                  src={post.author.avatar}
                  alt={post.author.name}
                  fill
                  className="object-cover"
                />
              </div>
            )}
            <div>
              <h3 className="font-semibold text-lg mb-1">
                About {post.author.name}
              </h3>
              <p className="text-muted-foreground">
                {post.author.role}
              </p>
            </div>
          </div>
        </div>

        {/* Share Again */}
        <div className="mt-8 pt-8 border-t flex items-center justify-between flex-wrap gap-4">
          <div>
            <h3 className="font-semibold mb-2">Found this helpful?</h3>
            <p className="text-sm text-muted-foreground">
              Share it with your colleagues
            </p>
          </div>
          <SocialShare
            url={postUrl}
            title={post.title}
            description={post.excerpt}
          />
        </div>

        {/* Back to Blog */}
        <div className="mt-8">
          <Link
            href="/blog"
            className="text-primary hover:underline inline-flex items-center gap-2"
          >
            ← Back to all posts
          </Link>
        </div>
      </div>
    </article>
    </>
  );
}
