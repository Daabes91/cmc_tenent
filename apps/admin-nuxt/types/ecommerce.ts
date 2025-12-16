export type CarouselContentType = 'IMAGE' | 'PRODUCT' | 'CATEGORY' | 'BRAND' | 'OFFER' | 'VIEW_ALL_PRODUCTS';

export interface Carousel {
  id: number;
  name: string;
  nameAr?: string | null;
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
  titleAr?: string | null;
  subtitle?: string | null;
  subtitleAr?: string | null;
  imageUrl?: string | null;
  linkUrl?: string | null;
  ctaText?: string | null;
  ctaTextAr?: string | null;
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
  nameAr?: string | null;
  slug: string;
  sku?: string | null;
  description?: string | null;
  descriptionAr?: string | null;
  shortDescription?: string | null;
  shortDescriptionAr?: string | null;
  price?: number | null;
  compareAtPrice?: number | null;
  currency?: string | null;
  status?: string;
  productType?: string;
  hasVariants?: boolean;
  isTaxable?: boolean;
  isVisible?: boolean;
  images?: ProductImage[];
  variants?: any[];
  createdAt?: number;
  updatedAt?: number;
}

export interface ProductPage {
  content: Product[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    unpaged: boolean;
    paged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  first: boolean;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  numberOfElements: number;
  empty: boolean;
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

export interface Order {
  id: number;
  orderNumber: string;
  items?: OrderItem[];
  customerName?: string;
  customerEmail?: string;
  customerPhone?: string;
  status?: string;
  statusDisplayName?: string;
  subtotal?: number;
  taxAmount?: number;
  shippingAmount?: number;
  totalAmount?: number;
  currency?: string;
  notes?: string;
  totalItemCount?: number;
  fullBillingAddress?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderItem {
  id: number;
  productId?: number;
  variantId?: number;
  productName?: string;
  variantName?: string;
  sku?: string;
  quantity?: number;
  unitPrice?: number;
  totalPrice?: number;
  currency?: string;
}
