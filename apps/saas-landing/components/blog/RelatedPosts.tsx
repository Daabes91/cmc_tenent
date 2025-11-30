/**
 * Related Posts Component
 * 
 * Displays related blog posts at the end of a blog post.
 * Shows posts with the same category or matching tags.
 */

import Link from 'next/link';
import Image from 'next/image';
import { BlogPost } from '@/lib/blog/types';

interface RelatedPostsProps {
  posts: BlogPost[];
}

export function RelatedPosts({ posts }: RelatedPostsProps) {
  if (posts.length === 0) {
    return null;
  }

  return (
    <section className="mt-16 pt-16 border-t">
      <div className="mb-8">
        <h2 className="text-3xl font-bold mb-2">Related Articles</h2>
        <p className="text-muted-foreground">
          Continue reading with these related posts
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {posts.map((post) => (
          <RelatedPostCard key={post.slug} post={post} />
        ))}
      </div>
    </section>
  );
}

function RelatedPostCard({ post }: { post: BlogPost }) {
  // Format category for display
  const categoryDisplay = post.category
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');

  return (
    <Link
      href={`/blog/${post.slug}`}
      className="group block bg-card rounded-lg overflow-hidden border hover:border-primary/50 transition-all hover:shadow-lg"
    >
      {/* Featured Image */}
      {post.featuredImage && (
        <div className="relative w-full aspect-[16/9] overflow-hidden">
          <Image
            src={post.featuredImage}
            alt={post.title}
            fill
            className="object-cover group-hover:scale-105 transition-transform duration-300"
          />
        </div>
      )}

      <div className="p-5">
        {/* Category Badge */}
        <div className="flex items-center gap-2 mb-3">
          <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded-full">
            {categoryDisplay}
          </span>
          <span className="text-xs text-muted-foreground">
            {post.readingTime} min read
          </span>
        </div>

        {/* Title */}
        <h3 className="font-semibold text-lg mb-2 line-clamp-2 group-hover:text-primary transition-colors">
          {post.title}
        </h3>

        {/* Excerpt */}
        <p className="text-sm text-muted-foreground line-clamp-2 mb-3">
          {post.excerpt}
        </p>

        {/* Author and Date */}
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          <span>{post.author.name}</span>
          <span>•</span>
          <time dateTime={post.publishedAt}>
            {new Date(post.publishedAt).toLocaleDateString('en-US', {
              month: 'short',
              day: 'numeric',
              year: 'numeric',
            })}
          </time>
        </div>
      </div>
    </Link>
  );
}
