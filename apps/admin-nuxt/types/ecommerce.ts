export type CarouselContentType = 'IMAGE' | 'PRODUCT' | 'CATEGORY' | 'BRAND' | 'OFFER' | 'VIEW_ALL_PRODUCTS';

export interface Carousel {
  id: number;
  name: string;
  slug: string;
  type: 'PRODUCT' | 'VIEW_ALL_PRODUCTS' | 'IMAGE' | 'CATEGORY' | 'BRAND' | 'OFFER' | 'TESTIMONIAL' | 'BLOG' | 'MIXED';
  placement: string;
  platform?: 'WEB' | 'MOBILE' | string;
  maxItems?: number | null;
  itemCount?: number;
  isActive?: boolean;
}

export interface CarouselItem {
  id: number;
  contentType: CarouselContentType;
  title?: string | null;
  subtitle?: string | null;
  imageUrl?: string | null;
  linkUrl?: string | null;
  ctaText?: string | null;
  sortOrder?: number | null;
  productId?: number | null;
  categoryId?: number | null;
  isActive?: boolean;
}

export interface ProductImage {
  id: number;
  imageUrl: string;
  altText?: string | null;
  isMain?: boolean | null;
  sortOrder?: number | null;
}

export interface Product {
  id: number;
  name: string;
  slug: string;
  sku?: string | null;
  description?: string | null;
  shortDescription?: string | null;
  price?: number | null;
  compareAtPrice?: number | null;
  currency?: string | null;
  status?: string;
  hasVariants?: boolean;
  isTaxable?: boolean;
  isVisible?: boolean;
  images?: ProductImage[];
  categoryIds?: number[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductPage {
  content: Product[];
  page: number;
  size: number;
  totalElements: number;
}

export interface Category {
  id: number;
  name: string;
  slug?: string;
  parentId?: number | null;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}
