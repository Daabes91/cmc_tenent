/**
 * Blog Post Error Component
 * 
 * Displays user-friendly error message when blog post fails to load
 */

import Link from 'next/link';
import { FileQuestion, ArrowLeft, Home } from 'lucide-react';

interface BlogPostErrorProps {
  slug: string;
  error?: string;
}

export function BlogPostError({ slug, error }: BlogPostErrorProps) {
  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-16">
      <div className="max-w-lg w-full text-center">
        <FileQuestion className="w-16 h-16 text-gray-400 mx-auto mb-6" />
        
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          Blog Post Not Found
        </h1>
        
        <p className="text-gray-600 mb-2">
          We couldn&apos;t find the blog post you&apos;re looking for.
        </p>
        
        {error && (
          <p className="text-sm text-gray-500 mb-6">
            Error: {error}
          </p>
        )}
        
        <p className="text-gray-600 mb-8">
          The post may have been moved, deleted, or the URL might be incorrect.
        </p>
        
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            href="/blog"
            className="inline-flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Blog
          </Link>
          
          <Link
            href="/"
            className="inline-flex items-center justify-center gap-2 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <Home className="w-4 h-4" />
            Go Home
          </Link>
        </div>
        
        <div className="mt-12 pt-8 border-t border-gray-200">
          <p className="text-sm text-gray-500 mb-4">
            Looking for something specific?
          </p>
          <Link
            href="/blog"
            className="text-blue-600 hover:text-blue-700 underline"
          >
            Browse all blog posts
          </Link>
        </div>
      </div>
    </div>
  );
}

/**
 * Blog Post Loading Error (for parsing errors)
 */
export function BlogPostParsingError({ slug }: { slug: string }) {
  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-16">
      <div className="max-w-lg w-full text-center">
        <FileQuestion className="w-16 h-16 text-orange-400 mx-auto mb-6" />
        
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          Content Unavailable
        </h1>
        
        <p className="text-gray-600 mb-8">
          We&apos;re having trouble displaying this blog post right now. 
          Our team has been notified and is working to fix the issue.
        </p>
        
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link
            href="/blog"
            className="inline-flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Blog
          </Link>
        </div>
      </div>
    </div>
  );
}
