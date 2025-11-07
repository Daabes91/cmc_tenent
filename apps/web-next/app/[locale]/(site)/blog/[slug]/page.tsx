import type { Metadata } from 'next';
import { buildLocalizedMetadata } from '@/lib/seo';
import { getBlogBySlugServer } from '@/lib/server-api';
import BlogDetailPageClient from './BlogDetailPageClient';

type PageProps = {
  params: { locale: string; slug: string };
};

export async function generateMetadata({ params }: PageProps): Promise<Metadata> {
  const post = await getBlogBySlugServer(params.slug);

  const title = post?.metaTitle || post?.title || 'Dental blog article';
  const description =
    post?.metaDescription ||
    post?.excerpt ||
    'Read expert-written articles about oral health, cosmetic dentistry, and orthodontics.';

  const keywords = post
    ? [post.title, 'dental blog', 'oral health tips']
    : ['dental blog', 'oral health'];

  return buildLocalizedMetadata({
    locale: params.locale,
    path: `/blog/${params.slug}`,
    title,
    description,
    keywords,
    image: post?.ogImage || post?.featuredImage || undefined,
  });
}

export default function BlogDetailPage({ params }: PageProps) {
  return <BlogDetailPageClient slug={params.slug} />;
}
