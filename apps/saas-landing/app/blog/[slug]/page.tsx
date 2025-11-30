/**
 * Blog Post Detail Page
 * 
 * Displays a single blog post with full content, author information, and social sharing.
 */

import { Metadata } from 'next';
import { notFound } from 'next/navigation';
import Script from 'next/script';
import { getBlogPost, getAllBlogSlugs, getRelatedPosts } from '@/lib/blog';
import { BlogPost } from '@/components/blog/BlogPost';
import { RelatedPosts } from '@/components/blog/RelatedPosts';
import { BlogPostTracking } from '@/components/blog/BlogPostTracking';
import { generateBlogPostingSchema } from '@/lib/seo/structured-data';
import { BlogPostError, BlogPostParsingError } from '@/components/blog/BlogPostError';
import { ErrorBoundary } from '@/components/ErrorBoundary';

interface BlogPostPageProps {
  params: Promise<{
    slug: string;
  }>;
}

// Get site URL from environment or use default
const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000';

export async function generateStaticParams() {
  const slugs = getAllBlogSlugs();
  return slugs.map((slug) => ({
    slug,
  }));
}

export async function generateMetadata({ params }: BlogPostPageProps): Promise<Metadata> {
  const { slug } = await params;
  const post = await getBlogPost(slug);

  if (!post) {
    return {
      title: 'Post Not Found',
    };
  }

  const postUrl = `${SITE_URL}/blog/${slug}`;

  return {
    title: post.seo.title,
    description: post.seo.description,
    keywords: post.seo.keywords,
    authors: [{ name: post.author.name }],
    openGraph: {
      title: post.seo.title,
      description: post.seo.description,
      type: 'article',
      publishedTime: post.publishedAt,
      modifiedTime: post.updatedAt,
      authors: [post.author.name],
      images: [
        {
          url: post.featuredImage,
          width: 1200,
          height: 630,
          alt: post.title,
        },
      ],
      url: postUrl,
    },
    twitter: {
      card: 'summary_large_image',
      title: post.seo.title,
      description: post.seo.description,
      images: [post.featuredImage],
    },
    alternates: {
      canonical: postUrl,
    },
  };
}

export default async function BlogPostPage({ params }: BlogPostPageProps) {
  const { slug } = await params;
  
  try {
    const post = await getBlogPost(slug);

    if (!post || post.draft) {
      notFound();
    }

    // Check if post is scheduled for future
    const publishDate = new Date(post.publishedAt);
    const now = new Date();
    if (publishDate > now) {
      notFound();
    }

    // Get related posts with error handling
    let relatedPosts: any[] = [];
    try {
      relatedPosts = await getRelatedPosts(post, 3);
    } catch (error) {
      console.error('Error loading related posts:', error);
      relatedPosts = []; // Fallback to empty array
    }

    // Generate structured data for this blog post
    const blogPostingSchema = generateBlogPostingSchema(post);

    return (
      <ErrorBoundary section="blog post">
        {/* BlogPosting Structured Data */}
        <Script
          id="blog-posting-schema"
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify(blogPostingSchema),
          }}
        />
        
        <BlogPostTracking postTitle={post.title} postSlug={slug} />
        <BlogPost post={post} siteUrl={SITE_URL} />
        <div className="container mx-auto px-4 pb-16">
          <div className="max-w-3xl mx-auto">
            {relatedPosts.length > 0 && <RelatedPosts posts={relatedPosts} />}
          </div>
        </div>
      </ErrorBoundary>
    );
  } catch (error) {
    console.error(`Error loading blog post ${slug}:`, error);
    // Return error component instead of throwing
    return <BlogPostParsingError slug={slug} />;
  }
}
