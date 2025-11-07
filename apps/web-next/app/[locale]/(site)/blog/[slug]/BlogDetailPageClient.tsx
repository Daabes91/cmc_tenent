'use client';

import { useEffect, useMemo, useState } from 'react';
import Link from 'next/link';
import { useLocale, useTranslations } from 'next-intl';
import { api } from '@/lib/api';
import type { Blog } from '@/lib/types';
import { getBookingSectionPath } from '@/utils/basePath';

type BlogDetailPageClientProps = {
  slug: string;
};

export default function BlogDetailPageClient({ slug }: BlogDetailPageClientProps) {
  const locale = useLocale();
  const t = useTranslations('blogDetail');
  const bookingSectionPath = getBookingSectionPath(locale);

  const [blog, setBlog] = useState<Blog | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchBlog = async () => {
      try {
        const data = await api.getBlogBySlug(slug);
        setBlog(data);
      } catch (err) {
        console.error('[BlogDetailPage] Failed to load blog post:', err);
        setError(t('errors.load'));
      } finally {
        setLoading(false);
      }
    };

    fetchBlog();
  }, [slug, t]);

  const formatDate = (rawValue?: string | number | null) => {
    if (!rawValue) return null;

    const parsed = typeof rawValue === 'number' ? rawValue : Number(rawValue);
    if (!Number.isNaN(parsed)) {
      const milliseconds = parsed > 1e12 ? parsed : parsed * 1000;
      const fromNumber = new Date(milliseconds);
      if (!Number.isNaN(fromNumber.getTime())) {
        return fromNumber.toLocaleDateString(locale, { year: 'numeric', month: 'long', day: 'numeric' });
      }
    }

    try {
      return new Date(String(rawValue)).toLocaleDateString(locale, {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    } catch {
      return String(rawValue);
    }
  };

  if (loading) {
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8 py-20">
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-blue-600 border-t-transparent dark:border-blue-400"></div>
              <p className="mt-4 text-slate-600 dark:text-slate-300 font-medium">{t('loading')}</p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  if (error || !blog) {
    const errorMessage = error || t('errors.notFound');
    return (
      <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8 py-20">
          <div className="p-6 rounded-2xl bg-red-50 border-2 border-red-200 text-red-700 shadow-md dark:border-red-900/60 dark:bg-red-950/40 dark:text-red-300">
            <div className="flex items-center gap-3">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="font-semibold">{errorMessage}</span>
            </div>
          </div>
          <div className="mt-6">
            <Link
              href={`/${locale}/blog`}
              className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 font-medium transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
              {t('back')}
            </Link>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
      {/* Back Button */}
      <div className="bg-white dark:bg-slate-900/60 border-b border-slate-200 dark:border-slate-800/70 backdrop-blur transition-colors">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8 py-4">
          <Link
            href={`/${locale}/blog`}
            className="inline-flex items-center gap-2 text-slate-600 hover:text-blue-600 dark:text-slate-300 dark:hover:text-blue-400 font-medium transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            {t('back')}
          </Link>
        </div>
      </div>

      {/* Featured Image */}
      {blog.featuredImage && (
        <div className="relative h-96 overflow-hidden bg-gradient-to-br from-blue-600 to-cyan-600 transition-colors dark:from-blue-700 dark:to-cyan-700">
          <img
            src={blog.featuredImage}
            alt={blog.title}
            loading="lazy"
            width="1200"
            height="480"
            sizes="100vw"
            className="h-full w-full object-cover opacity-90"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent dark:from-black/70" />
        </div>
      )}

      {/* Article Content */}
      <article className="py-12">
        <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8">
          {/* Header */}
          <header className="mb-8">
            <div className="flex items-center gap-4 text-sm text-slate-600 dark:text-slate-400 mb-4">
              {(() => {
                const displayDate = formatDate(blog.publishedAt ?? blog.createdAt ?? null);
                if (!displayDate) return null;
                return (
                <div className="flex items-center gap-1">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <span>{displayDate}</span>
                </div>
                );
              })()}
              {blog.authorName && (
                <div className="flex items-center gap-1">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span>{blog.authorName}</span>
                </div>
              )}
              {typeof blog.viewCount === 'number' && (
                <div className="flex items-center gap-1 ml-auto">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <span>{t('meta.views', { count: blog.viewCount })}</span>
                </div>
              )}
            </div>

            <h1 className="text-4xl md:text-5xl font-bold text-slate-900 dark:text-slate-100 mb-4 leading-tight transition-colors">
              {blog.title}
            </h1>

            {blog.excerpt && (
              <p className="text-xl text-slate-600 dark:text-slate-300 leading-relaxed transition-colors">
                {blog.excerpt}
              </p>
            )}
          </header>

          {/* Content */}
          <div className="bg-white dark:bg-slate-900/60 rounded-2xl shadow-lg dark:shadow-blue-900/20 p-8 md:p-12 backdrop-blur border border-slate-100 dark:border-slate-800/70 transition-colors">
            <div
              className="prose prose-lg prose-slate dark:prose-invert max-w-none
                prose-headings:font-bold prose-headings:text-slate-900 dark:prose-headings:text-slate-100
                prose-h2:text-3xl prose-h2:mt-8 prose-h2:mb-4
                prose-h3:text-2xl prose-h3:mt-6 prose-h3:mb-3
                prose-p:text-slate-700 dark:prose-p:text-slate-300 prose-p:leading-relaxed prose-p:mb-4
                prose-a:text-blue-600 dark:prose-a:text-blue-400 prose-a:no-underline hover:prose-a:underline
                prose-strong:text-slate-900 dark:prose-strong:text-slate-100 prose-strong:font-semibold
                prose-ul:my-4 prose-ol:my-4
                prose-li:text-slate-700 dark:prose-li:text-slate-300
                prose-img:rounded-xl prose-img:shadow-md"
              dangerouslySetInnerHTML={{ __html: blog.content.replace(/\n/g, '<br />') }}
            />
          </div>

          {/* Share Section */}
          <div className="mt-12 pt-8 border-t border-slate-200 dark:border-slate-800/70 transition-colors">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100 mb-2 transition-colors">{t('share.title')}</h3>
                <p className="text-sm text-slate-600 dark:text-slate-400 transition-colors">{t('share.subtitle')}</p>
              </div>
              <div className="flex items-center gap-3">
                <button
                  onClick={() => {
                    const url = window.location.href;
                    navigator.clipboard.writeText(url)
                      .then(() => alert(t('share.copied')))
                      .catch((clipboardError) => {
                        console.error('[BlogDetailPage] Failed to copy link:', clipboardError);
                        alert(t('share.copied'));
                      });
                  }}
                  className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 dark:bg-slate-800/60 dark:hover:bg-slate-700/60 text-slate-700 dark:text-slate-300 font-medium transition-colors"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                  </svg>
                  {t('share.copy')}
                </button>
              </div>
            </div>
          </div>

          {/* Call to Action */}
          <div className="mt-12 p-8 rounded-2xl bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-700 dark:to-cyan-700 text-white shadow-lg dark:shadow-blue-900/40 transition-colors">
            <div className="text-center max-w-2xl mx-auto">
              <h3 className="text-2xl font-bold mb-2">{t('cta.title')}</h3>
              <p className="text-blue-50 dark:text-blue-100 mb-6 transition-colors">
                {t('cta.subtitle')}
              </p>
              <Link
                href={bookingSectionPath}
                locale={false}
                className="inline-flex items-center gap-2 px-6 py-3 bg-white text-blue-600 font-semibold rounded-lg hover:bg-blue-50 dark:bg-slate-100 dark:hover:bg-slate-200 transition-colors"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                {t('cta.button')}
              </Link>
            </div>
          </div>
        </div>
      </article>
    </main>
  );
}
