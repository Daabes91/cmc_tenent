/**
 * Blog List Component
 * 
 * Client-side component that handles filtering, search, and pagination.
 */

'use client';

import { useState, useMemo } from 'react';
import { BlogPost, BlogCategory, BlogListItem } from '@/lib/blog/types';
import { BlogCard } from './BlogCard';
import { CategoryFilter } from './CategoryFilter';
import { SearchBar } from './SearchBar';
import { Pagination } from './Pagination';

interface BlogListProps {
  posts: BlogPost[];
  postsPerPage?: number;
}

export function BlogList({ posts, postsPerPage = 9 }: BlogListProps) {
  const [selectedCategory, setSelectedCategory] = useState<BlogCategory | 'all'>('all');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [currentPage, setCurrentPage] = useState(1);

  // Calculate category counts
  const categories = useMemo(() => {
    const counts: Record<BlogCategory, number> = {
      'practice-management': 0,
      'patient-care': 0,
      'technology': 0,
      'compliance': 0,
      'industry-news': 0,
    };

    posts.forEach(post => {
      counts[post.category]++;
    });

    return counts;
  }, [posts]);

  // Filter posts by category and search
  const filteredPosts = useMemo(() => {
    let filtered = posts;

    // Filter by category
    if (selectedCategory !== 'all') {
      filtered = filtered.filter(post => post.category === selectedCategory);
    }

    // Filter by search keyword
    if (searchKeyword.trim()) {
      const keyword = searchKeyword.toLowerCase().trim();
      filtered = filtered.filter(post => {
        return (
          post.title.toLowerCase().includes(keyword) ||
          post.excerpt.toLowerCase().includes(keyword) ||
          post.tags.some(tag => tag.toLowerCase().includes(keyword))
        );
      });
    }

    return filtered;
  }, [posts, selectedCategory, searchKeyword]);

  // Convert posts to list items
  const listItems = useMemo(() => {
    return filteredPosts.map(post => ({
      slug: post.slug,
      title: post.title,
      excerpt: post.excerpt,
      author: post.author,
      publishedAt: new Date(post.publishedAt),
      category: post.category,
      tags: post.tags,
      featuredImage: post.featuredImage,
      readingTime: post.readingTime,
    } as BlogListItem));
  }, [filteredPosts]);

  // Calculate pagination
  const totalPages = Math.ceil(listItems.length / postsPerPage);
  const startIndex = (currentPage - 1) * postsPerPage;
  const endIndex = startIndex + postsPerPage;
  const paginatedPosts = listItems.slice(startIndex, endIndex);

  // Reset to page 1 when filters change
  const handleCategoryChange = (category: BlogCategory | 'all') => {
    setSelectedCategory(category);
    setCurrentPage(1);
  };

  const handleSearch = (keyword: string) => {
    setSearchKeyword(keyword);
    setCurrentPage(1);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    // Scroll to top of blog list
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="space-y-8">
      {/* Search Bar */}
      <div className="flex justify-center">
        <SearchBar onSearch={handleSearch} placeholder="Search healthcare articles..." />
      </div>

      {/* Category Filter */}
      <div className="flex justify-center">
        <CategoryFilter
          categories={categories}
          selectedCategory={selectedCategory}
          onCategoryChange={handleCategoryChange}
        />
      </div>

      {/* Results Count */}
      <div className="text-center text-sm text-slate-600 dark:text-gray-400">
        {searchKeyword && (
          <p>
            Found {listItems.length} {listItems.length === 1 ? 'article' : 'articles'} 
            {searchKeyword && ` for "${searchKeyword}"`}
          </p>
        )}
      </div>

      {/* Blog Posts Grid */}
      {paginatedPosts.length === 0 ? (
        <div className="text-center py-16 px-4">
          <div className="max-w-md mx-auto">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-100 dark:bg-gray-800 mb-4">
              <svg
                className="w-8 h-8 text-slate-400 dark:text-gray-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-2">
              No articles found
            </h3>
            <p className="text-slate-600 dark:text-gray-400 mb-6">
              {searchKeyword ? (
                <>
                  We couldn&apos;t find any articles matching &quot;<strong>{searchKeyword}</strong>&quot;.
                  Try different keywords or browse all articles.
                </>
              ) : (
                'Try adjusting your filter criteria to see more results.'
              )}
            </p>
            {(searchKeyword || selectedCategory !== 'all') && (
              <button
                onClick={() => {
                  setSearchKeyword('');
                  setSelectedCategory('all');
                  setCurrentPage(1);
                }}
                className="inline-flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors"
              >
                <svg
                  className="w-4 h-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                  />
                </svg>
                Clear all filters
              </button>
            )}
          </div>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {paginatedPosts.map((post) => (
              <BlogCard key={post.slug} post={post} searchTerm={searchKeyword} />
            ))}
          </div>

          {/* Pagination */}
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </div>
  );
}
