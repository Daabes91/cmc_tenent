import { ProductCard } from '@/components/ProductCard';
import { ProductGrid } from '@/components/ProductGrid';
import type { PublicCarouselItem } from '@/lib/types';

// Mock product data for demonstration
const mockProduct = {
  id: 1,
  name: 'Premium Wireless Headphones',
  nameAr: 'سماعات لاسلكية فاخرة',
  slug: 'premium-wireless-headphones',
  shortDescription: 'High-quality wireless headphones with noise cancellation and premium sound quality.',
  shortDescriptionAr: 'سماعات لاسلكية عالية الجودة مع إلغاء الضوضاء وجودة صوت فائقة.',
  price: 299,
  compareAtPrice: 399,
  currency: 'USD',
  mainImageUrl: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&h=500&fit=crop',
  images: ['https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&h=500&fit=crop'],
  status: 'ACTIVE' as const
};

const mockProducts: PublicCarouselItem[] = [
  {
    id: 1,
    contentType: 'PRODUCT' as const,
    product: mockProduct
  },
  {
    id: 2,
    contentType: 'PRODUCT' as const,
    product: {
      ...mockProduct,
      id: 2,
      name: 'Smart Fitness Watch',
      nameAr: 'ساعة ذكية للياقة البدنية',
      slug: 'smart-fitness-watch',
      shortDescription: 'Advanced fitness tracking with heart rate monitoring and GPS.',
      shortDescriptionAr: 'تتبع متقدم للياقة البدنية مع مراقبة معدل ضربات القلب و GPS.',
      price: 199,
      compareAtPrice: null,
      mainImageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&h=500&fit=crop'
    }
  },
  {
    id: 3,
    contentType: 'PRODUCT' as const,
    product: {
      ...mockProduct,
      id: 3,
      name: 'Wireless Charging Pad',
      nameAr: 'لوحة شحن لاسلكية',
      slug: 'wireless-charging-pad',
      shortDescription: 'Fast wireless charging for all compatible devices.',
      shortDescriptionAr: 'شحن لاسلكي سريع لجميع الأجهزة المتوافقة.',
      price: 49,
      compareAtPrice: 69,
      mainImageUrl: 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?w=500&h=500&fit=crop'
    }
  }
];

export default function ProductDemoPage() {
  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 py-12">
      <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
        <div className="mb-12 text-center">
          <h1 className="text-4xl font-bold text-slate-900 dark:text-white mb-4">
            Modern Product Cards
          </h1>
          <p className="text-lg text-slate-600 dark:text-slate-400 max-w-2xl mx-auto">
            Redesigned product cards with modern aesthetics, smooth animations, and improved user experience.
          </p>
        </div>

        {/* Default Variant */}
        <section className="mb-16">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-8">Default Cards</h2>
          <ProductGrid 
            products={mockProducts} 
            locale="en" 
            variant="default" 
            columns={4}
          />
        </section>

        {/* Compact Variant */}
        <section className="mb-16">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-8">Compact Cards</h2>
          <ProductGrid 
            products={mockProducts} 
            locale="en" 
            variant="compact" 
            columns={4}
          />
        </section>

        {/* Featured Variant */}
        <section className="mb-16">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-8">Featured Cards</h2>
          <ProductGrid 
            products={mockProducts.slice(0, 2)} 
            locale="en" 
            variant="featured" 
            columns={2}
          />
        </section>

        {/* Individual Card Examples */}
        <section className="mb-16">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-8">Individual Card Variants</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div>
              <h3 className="text-lg font-semibold text-slate-700 dark:text-slate-300 mb-4">Default</h3>
              <ProductCard item={mockProducts[0]} locale="en" variant="default" />
            </div>
            <div>
              <h3 className="text-lg font-semibold text-slate-700 dark:text-slate-300 mb-4">Compact</h3>
              <ProductCard item={mockProducts[1]} locale="en" variant="compact" />
            </div>
            <div>
              <h3 className="text-lg font-semibold text-slate-700 dark:text-slate-300 mb-4">Featured</h3>
              <ProductCard item={mockProducts[2]} locale="en" variant="featured" />
            </div>
          </div>
        </section>

        {/* Loading State */}
        <section className="mb-16">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-8">Loading State</h2>
          <ProductGrid 
            products={[]} 
            locale="en" 
            variant="default" 
            columns={4}
            loading={true}
          />
        </section>

        {/* Features List */}
        <section className="bg-white dark:bg-slate-900 rounded-3xl p-8 border border-slate-200 dark:border-slate-700">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-6">Modern Design Features</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
                <svg className="w-4 h-4 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-slate-900 dark:text-white">Smooth Animations</h3>
                <p className="text-sm text-slate-600 dark:text-slate-400">Hover effects and micro-interactions</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-green-100 dark:bg-green-900 rounded-lg flex items-center justify-center">
                <svg className="w-4 h-4 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-slate-900 dark:text-white">Responsive Design</h3>
                <p className="text-sm text-slate-600 dark:text-slate-400">Works perfectly on all screen sizes</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-purple-100 dark:bg-purple-900 rounded-lg flex items-center justify-center">
                <svg className="w-4 h-4 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-slate-900 dark:text-white">Dark Mode Support</h3>
                <p className="text-sm text-slate-600 dark:text-slate-400">Beautiful in both light and dark themes</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-orange-100 dark:bg-orange-900 rounded-lg flex items-center justify-center">
                <svg className="w-4 h-4 text-orange-600 dark:text-orange-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 4V2a1 1 0 011-1h8a1 1 0 011 1v2m-9 0h10m-10 0a2 2 0 00-2 2v12a2 2 0 002 2h8a2 2 0 002-2V6a2 2 0 00-2-2" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-slate-900 dark:text-white">Multiple Variants</h3>
                <p className="text-sm text-slate-600 dark:text-slate-400">Default, compact, and featured layouts</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-red-100 dark:bg-red-900 rounded-lg flex items-center justify-center">
                <svg className="w-4 h-4 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-slate-900 dark:text-white">Enhanced UX</h3>
                <p className="text-sm text-slate-600 dark:text-slate-400">Better visual hierarchy and interactions</p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-indigo-100 dark:bg-indigo-900 rounded-lg flex items-center justify-center">
                <svg className="w-4 h-4 text-indigo-600 dark:text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div>
                <h3 className="font-semibold text-slate-900 dark:text-white">Loading States</h3>
                <p className="text-sm text-slate-600 dark:text-slate-400">Skeleton loading and empty states</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}