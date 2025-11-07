'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { useLocale, useTranslations } from 'next-intl';
import { api } from '@/lib/api';
import type { Blog } from '@/lib/types';

export default function BlogPageClient() {
  const locale = useLocale();
  const t = useTranslations('blogPage');
  const [blogs, setBlogs] = useState<Blog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchBlogs = async () => {
      try {
        const data = await api.getBlogs(locale);
        setBlogs(data);
      } catch (err) {
        console.error('[BlogPage] Failed to load blog posts:', err);
        setError(t('errors.load'));
      } finally {
        setLoading(false);
      }
    };

    fetchBlogs();
  }, [locale, t]);

  const formatDate = (rawValue?: string | number | null) => {
    if (!rawValue) return null;

    const toDate = (value: string | number) => {
      const parsed = typeof value === 'number' ? value : Number(value);
      if (!Number.isNaN(parsed)) {
        const milliseconds = parsed > 1e12 ? parsed : parsed * 1000;
        return new Date(milliseconds);
      }
      const asDate = new Date(String(value));
      return Number.isNaN(asDate.getTime()) ? null : asDate;
    };

    const date = toDate(rawValue);
    if (!date) return null;

    try {
      return date.toLocaleDateString(locale, {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    } catch {
      return date.toDateString();
    }
  };

  if (loading) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="inline-block h-12 w-12 animate-spin rounded-full border-4 border-blue-600 border-t-transparent dark:border-blue-400"></div>
              <p className="mt-4 font-medium text-slate-600 dark:text-slate-300">{t('loading')}</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (error) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8 py-20">
          <div className="rounded-2xl border-2 border-red-200 bg-red-50 p-6 text-red-700 shadow-md dark:border-red-900/60 dark:bg-red-950/40 dark:text-red-300">
            <div className="flex items-center gap-3">
              <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="font-semibold">{error || t('errors.load')}</span>
            </div>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
      {/* Header Section */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-800/80 dark:via-blue-900/80 dark:to-slate-900">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto">
            <h1 className="text-4xl md:text-5xl font-bold text-white">{t('header.title')}</h1>
            <p className="text-blue-50 mt-4 text-lg">{t('header.subtitle')}</p>
          </div>
        </div>
      </section>

      {/* Blog Posts Grid */}
      <section className="py-16">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          {blogs.length === 0 ? (
            <div className="text-center py-16">
              <div className="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900/40">
                <svg className="h-8 w-8 text-blue-600 dark:text-blue-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
                </svg>
              </div>
              <h3 className="mb-2 text-xl font-semibold text-slate-900 dark:text-slate-100">{t('empty.title')}</h3>
              <p className="text-slate-600 dark:text-slate-400">{t('empty.description')}</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-3">
              {blogs.map((blog) => (
                <Link
                  key={blog.id}
                  href={`/${locale}/blog/${blog.slug}`}
                  className="group overflow-hidden rounded-2xl border border-slate-100 bg-white shadow-md transition-all duration-300 hover:-translate-y-1 hover:border-blue-200 hover:shadow-xl dark:border-slate-800/70 dark:bg-slate-900/60 dark:shadow-blue-900/20 dark:hover:border-blue-700/70 dark:hover:shadow-blue-900/40 backdrop-blur"
                >
                  {blog.featuredImage && (
                    <div className="relative h-48 overflow-hidden bg-gradient-to-br from-blue-100 to-cyan-100 dark:from-blue-900/40 dark:to-cyan-900/40">
                      <img
                        src={blog.featuredImage}
                        alt={blog.title}
                        loading="lazy"
                        width="600"
                        height="320"
                        sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                        className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent" />
                    </div>
                  )}
                  {!blog.featuredImage && (
                    <div className="relative flex h-48 items-center justify-center bg-gradient-to-br from-blue-500 to-cyan-500 dark:from-blue-700 dark:to-cyan-700">
                      <svg className="h-16 w-16 text-white/40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
                      </svg>
                    </div>
                  )}

                  <div className="p-6">
                    <div className="mb-3 flex items-center gap-3 text-sm text-slate-500 dark:text-slate-400">
                      {(() => {
                        const displayDate = formatDate(blog.publishedAt ?? blog.createdAt ?? null);
                        if (!displayDate) return null;
                        return (
                        <div className="flex items-center gap-1">
                          <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                          <span>{displayDate}</span>
                        </div>
                        );
                      })()}
                      {blog.authorName && (
                        <div className="flex items-center gap-1">
                          <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                          </svg>
                          <span>{blog.authorName}</span>
                        </div>
                      )}
                      <div className="ml-auto flex items-center gap-1">
                        <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                        </svg>
                        <span>{blog.viewCount}</span>
                      </div>
                    </div>

                    <h2 className="mb-2 line-clamp-2 text-xl font-bold text-slate-900 transition-colors group-hover:text-blue-600 dark:text-slate-100 dark:group-hover:text-blue-400">
                      {blog.title}
                    </h2>

                    {blog.excerpt && (
                      <p className="mb-4 line-clamp-3 text-sm text-slate-600 dark:text-slate-300">
                        {blog.excerpt}
                      </p>
                    )}

                    <div className="flex items-center gap-1 text-sm font-medium text-blue-600 transition-all group-hover:gap-2 dark:text-blue-400">
                      <span>{t('cards.readMore')}</span>
                      <svg className="h-4 w-4 transition-transform group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
