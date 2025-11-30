/**
 * Blog Card Component
 * 
 * Displays a blog post card with title, excerpt, featured image, author, and date.
 */

import Image from 'next/image';
import Link from 'next/link';
import { BlogListItem } from '@/lib/blog/types';
import { Clock, Calendar } from 'lucide-react';
import { highlightSearchTerm } from '@/lib/blog/highlight';

interface BlogCardProps {
  post: BlogListItem;
  searchTerm?: string;
}

export function BlogCard({ post, searchTerm }: BlogCardProps) {
  const formattedDate = post.publishedAt.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <article className="group bg-white dark:bg-gray-900 border border-slate-200 dark:border-gray-800 rounded-xl overflow-hidden transition-all duration-300 hover:border-primary/30 hover:shadow-[0_30px_55px_rgba(24,226,153,0.15)]">
      {/* Featured Image */}
      <Link href={`/blog/${post.slug}`} className="block relative h-56 overflow-hidden">
        <Image
          src={post.featuredImage}
          alt={post.title}
          fill
          className="object-cover transition-transform duration-500 group-hover:scale-105"
          sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-gray-900 to-transparent opacity-60"></div>
        
        {/* Category Badge */}
        <div className="absolute top-4 left-4 bg-primary text-white text-xs font-medium px-3 py-1 rounded-full">
          {post.category.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase())}
        </div>
      </Link>

      {/* Content */}
      <div className="p-6">
        {/* Metadata */}
        <div className="flex items-center gap-4 text-sm text-slate-600 dark:text-gray-400 mb-3">
          <div className="flex items-center gap-1">
            <Calendar className="h-4 w-4" />
            <time dateTime={post.publishedAt.toISOString()}>
              {formattedDate}
            </time>
          </div>
          <span>•</span>
          <div className="flex items-center gap-1">
            <Clock className="h-4 w-4" />
            <span>{post.readingTime} min read</span>
          </div>
        </div>

        {/* Title */}
        <h3 className="text-xl font-bold mb-3 group-hover:text-primary transition-colors">
          <Link href={`/blog/${post.slug}`}>
            {searchTerm ? (
              <span dangerouslySetInnerHTML={{ __html: highlightSearchTerm(post.title, searchTerm) }} />
            ) : (
              post.title
            )}
          </Link>
        </h3>

        {/* Excerpt */}
        <p className="text-slate-600 dark:text-gray-400 text-sm mb-4 line-clamp-3">
          {searchTerm ? (
            <span dangerouslySetInnerHTML={{ __html: highlightSearchTerm(post.excerpt, searchTerm) }} />
          ) : (
            post.excerpt
          )}
        </p>

        {/* Author and Tags */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            {post.author.avatar && (
              <Image
                src={post.author.avatar}
                alt={post.author.name}
                width={32}
                height={32}
                className="rounded-full"
              />
            )}
            <div>
              <p className="text-sm font-medium text-slate-900 dark:text-white">
                {post.author.name}
              </p>
              <p className="text-xs text-slate-600 dark:text-gray-400">
                {post.author.role}
              </p>
            </div>
          </div>
        </div>

        {/* Tags */}
        {post.tags.length > 0 && (
          <div className="flex flex-wrap gap-2 mt-4">
            {post.tags.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className="text-xs bg-slate-100 dark:bg-gray-800 text-slate-700 dark:text-gray-300 px-2 py-1 rounded"
              >
                {searchTerm ? (
                  <span dangerouslySetInnerHTML={{ __html: highlightSearchTerm(tag, searchTerm) }} />
                ) : (
                  tag
                )}
              </span>
            ))}
          </div>
        )}
      </div>
    </article>
  );
}
