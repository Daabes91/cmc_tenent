/**
 * Blog Listing Page
 * 
 * Displays all published blog posts with filtering, search, and pagination.
 */

import { Metadata } from 'next';
import { getAllBlogPosts } from '@/lib/blog';
import { BlogList } from '@/components/blog/BlogList';
import { BookOpen } from 'lucide-react';

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
      <div className="container mx-auto px-4 py-16 md:py-24">
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
