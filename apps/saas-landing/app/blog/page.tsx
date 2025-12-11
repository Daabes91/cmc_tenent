/**
 * Blog Listing Page
 * 
 * Displays all published blog posts with filtering, search, and pagination.
 */

import Link from 'next/link';
import Image from 'next/image';
import { Metadata } from 'next';
import { getAllBlogPosts } from '@/lib/blog';
import { BlogList } from '@/components/blog/BlogList';
import { BookOpen } from 'lucide-react';
import { withBasePath } from '@/lib/base-path';

export const metadata: Metadata = {
  title: 'Blog - Healthcare Practice Management Insights',
  description: 'Expert insights on clinic management, patient care, healthcare technology, and industry best practices.',
  openGraph: {
    title: 'Healthcare Insights Blog',
    description: 'Expert advice on practice management, patient care, and healthcare technology',
    type: 'website',
  },
};

export default async function BlogPage() {
  const posts = await getAllBlogPosts();

  return (
    <div className="min-h-screen bg-gradient-to-b from-white to-slate-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950">
      <div className="container mx-auto px-4 pt-8 md:pt-12">
        <div className="flex items-center justify-between gap-4 mb-10">
          <Link href="/" className="flex items-center gap-3">
            <div className="h-10 w-10 relative">
              <Image
                src={withBasePath('/brand-logo.png')}
                alt="Cliniqax"
                fill
                className="object-contain"
              />
            </div>
            <span className="text-lg font-semibold text-slate-900 dark:text-white">
              Cliniqax
            </span>
          </Link>
          <Link
            href="/"
            className="inline-flex items-center gap-2 rounded-full bg-primary px-4 py-2 text-sm font-semibold text-white shadow hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 dark:focus:ring-offset-gray-900"
          >
            ← Back to homepage
          </Link>
        </div>

        {/* Header */}
        <div className="max-w-3xl mx-auto text-center mb-16">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary/10 mb-6">
            <BookOpen className="h-8 w-8 text-primary" />
          </div>
          <h1 className="text-4xl md:text-5xl font-bold mb-4 text-slate-900 dark:text-white">
            Healthcare Insights Blog
          </h1>
          <p className="text-xl text-slate-600 dark:text-gray-400">
            Expert advice on practice management, patient care, and healthcare technology to help your clinic thrive
          </p>
        </div>

        {/* Blog List with Filtering and Pagination */}
        {posts.length === 0 ? (
          <div className="text-center py-16">
            <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-slate-100 dark:bg-gray-800 mb-6">
              <BookOpen className="h-10 w-10 text-slate-400" />
            </div>
            <h2 className="text-2xl font-bold mb-2 text-slate-900 dark:text-white">
              No articles yet
            </h2>
            <p className="text-slate-600 dark:text-gray-400">
              We&apos;re working on creating valuable content for you. Check back soon!
            </p>
          </div>
        ) : (
          <BlogList posts={posts} postsPerPage={9} />
        )}
      </div>
    </div>
  );
}
