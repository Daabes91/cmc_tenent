'use client';

import Image from 'next/image';
import { useState, useEffect } from 'react';
import type { ProductImage } from '@/lib/types';

interface ProductImageGalleryProps {
  images: ProductImage[];
  productName: string;
  mainImageUrl?: string | null;
}

export function ProductImageGallery({ images, productName, mainImageUrl }: ProductImageGalleryProps) {
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // Prepare image array with main image prioritized
  const allImages = images?.length 
    ? images.sort((a, b) => (b.isMain ? 1 : 0) - (a.isMain ? 1 : 0))
    : mainImageUrl 
    ? [{ id: 0, imageUrl: mainImageUrl, isMain: true, altText: productName }]
    : [];

  const currentImage = allImages[selectedImageIndex];

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!isFullscreen) return;
      
      if (e.key === 'Escape') {
        setIsFullscreen(false);
      } else if (e.key === 'ArrowLeft') {
        setSelectedImageIndex((prev) => (prev > 0 ? prev - 1 : allImages.length - 1));
      } else if (e.key === 'ArrowRight') {
        setSelectedImageIndex((prev) => (prev < allImages.length - 1 ? prev + 1 : 0));
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isFullscreen, allImages.length]);

  if (!allImages.length) {
    return (
      <div className="space-y-4">
        <div className="aspect-square rounded-3xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center border border-slate-200 dark:border-slate-700">
          <div className="text-center">
            <div className="mx-auto h-24 w-24 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center mb-4">
              <svg className="h-12 w-12 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <p className="text-slate-500 dark:text-slate-400">No image available</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="space-y-4">
        {/* Main Image */}
        <div className="relative group">
          <div 
            className="relative aspect-square rounded-3xl overflow-hidden bg-white dark:bg-slate-800 shadow-2xl shadow-slate-900/10 dark:shadow-black/20 border border-slate-200/60 dark:border-slate-700/60 cursor-zoom-in"
            onClick={() => setIsFullscreen(true)}
          >
            {currentImage && (
              <Image
                src={currentImage.imageUrl}
                alt={currentImage.altText || productName}
                fill
                className={`object-cover transition-all duration-500 ${isLoading ? 'scale-110 blur-sm' : 'scale-100 group-hover:scale-105'}`}
                sizes="(max-width: 768px) 100vw, 50vw"
                priority={selectedImageIndex === 0}
                onLoad={() => setIsLoading(false)}
              />
            )}
            
            {/* Loading overlay */}
            {isLoading && (
              <div className="absolute inset-0 bg-slate-100 dark:bg-slate-800 animate-pulse" />
            )}
            
            {/* Zoom indicator */}
            <div className="absolute top-6 right-6 z-10 opacity-0 group-hover:opacity-100 transition-opacity">
              <div className="bg-black/50 backdrop-blur-sm text-white p-2 rounded-full">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7" />
                </svg>
              </div>
            </div>

            {/* Navigation arrows for multiple images */}
            {allImages.length > 1 && (
              <>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setSelectedImageIndex((prev) => (prev > 0 ? prev - 1 : allImages.length - 1));
                  }}
                  className="absolute left-4 top-1/2 -translate-y-1/2 z-10 bg-white/90 dark:bg-slate-800/90 backdrop-blur-sm text-slate-700 dark:text-slate-300 p-2 rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-opacity hover:bg-white dark:hover:bg-slate-800"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                  </svg>
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setSelectedImageIndex((prev) => (prev < allImages.length - 1 ? prev + 1 : 0));
                  }}
                  className="absolute right-4 top-1/2 -translate-y-1/2 z-10 bg-white/90 dark:bg-slate-800/90 backdrop-blur-sm text-slate-700 dark:text-slate-300 p-2 rounded-full shadow-lg opacity-0 group-hover:opacity-100 transition-opacity hover:bg-white dark:hover:bg-slate-800"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              </>
            )}
          </div>
        </div>

        {/* Thumbnail Gallery */}
        {allImages.length > 1 && (
          <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
            {allImages.map((image, index) => (
              <button
                key={image.id || index}
                onClick={() => {
                  setSelectedImageIndex(index);
                  setIsLoading(true);
                }}
                className={`flex-shrink-0 w-20 h-20 rounded-2xl overflow-hidden border-2 transition-all ${
                  selectedImageIndex === index
                    ? 'border-blue-500 shadow-lg shadow-blue-500/25 scale-105'
                    : 'border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600 hover:scale-105'
                }`}
              >
                <Image
                  src={image.imageUrl}
                  alt={image.altText || `${productName} view ${index + 1}`}
                  width={80}
                  height={80}
                  className="w-full h-full object-cover"
                />
              </button>
            ))}
          </div>
        )}

        {/* Image counter */}
        {allImages.length > 1 && (
          <div className="text-center">
            <span className="text-sm text-slate-500 dark:text-slate-400">
              {selectedImageIndex + 1} of {allImages.length}
            </span>
          </div>
        )}
      </div>

      {/* Fullscreen Modal */}
      {isFullscreen && (
        <div className="fixed inset-0 z-50 bg-black/95 backdrop-blur-sm flex items-center justify-center p-4">
          {/* Close button */}
          <button
            onClick={() => setIsFullscreen(false)}
            className="absolute top-6 right-6 z-10 bg-white/10 backdrop-blur-sm text-white p-3 rounded-full hover:bg-white/20 transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>

          {/* Navigation */}
          {allImages.length > 1 && (
            <>
              <button
                onClick={() => setSelectedImageIndex((prev) => (prev > 0 ? prev - 1 : allImages.length - 1))}
                className="absolute left-6 top-1/2 -translate-y-1/2 z-10 bg-white/10 backdrop-blur-sm text-white p-3 rounded-full hover:bg-white/20 transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <button
                onClick={() => setSelectedImageIndex((prev) => (prev < allImages.length - 1 ? prev + 1 : 0))}
                className="absolute right-6 top-1/2 -translate-y-1/2 z-10 bg-white/10 backdrop-blur-sm text-white p-3 rounded-full hover:bg-white/20 transition-colors"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </>
          )}

          {/* Main fullscreen image */}
          <div className="relative max-w-4xl max-h-full w-full h-full flex items-center justify-center">
            {currentImage && (
              <Image
                src={currentImage.imageUrl}
                alt={currentImage.altText || productName}
                width={1200}
                height={1200}
                className="max-w-full max-h-full object-contain"
                priority
              />
            )}
          </div>

          {/* Image info */}
          <div className="absolute bottom-6 left-1/2 -translate-x-1/2 text-center">
            <p className="text-white text-lg font-medium mb-2">{productName}</p>
            {allImages.length > 1 && (
              <p className="text-white/70 text-sm">
                {selectedImageIndex + 1} of {allImages.length} • Use arrow keys to navigate
              </p>
            )}
          </div>
        </div>
      )}
    </>
  );
}