'use client';

import {useCallback, useEffect, useMemo, useState} from 'react';
import Image from 'next/image';
import dynamic from 'next/dynamic';
import {useLocale, useTranslations} from 'next-intl';
import {Link} from '@/navigation';
import {api} from '@/lib/api';
import type {Service, InsuranceCompany, HeroMedia, WhyChooseContent, LocalizedText} from '@/lib/types';
import {useLocalizedPageTitle} from '@/hooks/usePageTitle';
import {YouTubeEmbed} from '@/components/YouTubeEmbed';
import {HeroSectionErrorBoundary} from '@/components/HeroSectionErrorBoundary';

const BookingSlider = dynamic(() => import('@/components/BookingSlider'), {
  ssr: false,
  loading: () => (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
      {[1, 2].map((item) => (
        <div
          key={item}
          className="animate-pulse rounded-3xl border border-blue-100/60 bg-white/70 p-6 shadow-sm dark:border-blue-900/40 dark:bg-slate-900/40"
        >
          <div className="h-6 w-32 rounded-full bg-blue-100 dark:bg-blue-950/40" />
          <div className="mt-4 h-12 rounded-2xl bg-slate-100 dark:bg-slate-800/60" />
          <div className="mt-3 h-12 rounded-2xl bg-slate-100 dark:bg-slate-800/60" />
          <div className="mt-3 h-12 rounded-2xl bg-slate-100 dark:bg-slate-800/60" />
        </div>
      ))}
    </div>
  ),
});

export default function Home() {
  const [services, setServices] = useState<Service[]>([]);
  const [loadingServices, setLoadingServices] = useState(true);
  const [insuranceCompanies, setInsuranceCompanies] = useState<InsuranceCompany[]>([]);
  const [loadingInsurance, setLoadingInsurance] = useState(true);
  const [heroMedia, setHeroMedia] = useState<HeroMedia>({ type: 'image' });
  const [loadingHeroMedia, setLoadingHeroMedia] = useState(true);
  const [heroMediaError, setHeroMediaError] = useState(false);
  const [whySection, setWhySection] = useState<WhyChooseContent | null>(null);
  const hero = useTranslations('hero');
  const servicesT = useTranslations('services');
  const booking = useTranslations('booking');
  const why = useTranslations('whyChoose');
  const insurance = useTranslations('insurance');
  const cta = useTranslations('cta');
  const common = useTranslations('common');
  const locale = useLocale();
  const isRTL = locale === 'ar';
  const getLocalizedText = useCallback(
    (text?: LocalizedText | null) => {
      if (!text) {
        return null;
      }
      const primary = locale === 'ar' ? text.ar : text.en;
      if (primary && primary.trim().length > 0) {
        return primary;
      }
      const fallback = locale === 'ar' ? text.en : text.ar;
      return fallback && fallback.trim().length > 0 ? fallback : null;
    },
    [locale]
  );

  // Default hero image URL
  const DEFAULT_HERO_IMAGE = 'https://images.unsplash.com/photo-1606811971618-4486d14f3f99?q=80&w=1200&auto=format&fit=crop';

  // Set localized page title
  useLocalizedPageTitle({
    en: 'Home',
    ar: 'الرئيسية'
  });

  useEffect(() => {
    const loadServices = async () => {
      try {
        const data = await api.getServices(locale);
        setServices(data);
      } catch (error) {
        console.error('Failed to load services:', error);
      } finally {
        setLoadingServices(false);
      }
    };

    const loadInsuranceCompanies = async () => {
      try {
        const data = await api.getInsuranceCompanies(locale);
        setInsuranceCompanies(data);
      } catch (error) {
        console.error('Failed to load insurance companies:', error);
      } finally {
        setLoadingInsurance(false);
      }
    };

    const loadHeroMedia = async () => {
      try {
        // Check cache first (5 minute cache)
        const cacheKey = 'hero-media-settings';
        const cacheExpiry = 5 * 60 * 1000; // 5 minutes
        
        if (typeof window !== 'undefined') {
          const cached = sessionStorage.getItem(cacheKey);
          const cacheTime = sessionStorage.getItem(`${cacheKey}-time`);
          
          if (cached && cacheTime) {
            const age = Date.now() - parseInt(cacheTime, 10);
            if (age < cacheExpiry) {
              const cachedData = JSON.parse(cached);
              setHeroMedia(cachedData);
              setLoadingHeroMedia(false);
              console.log('Using cached hero media settings');
              const cachedWhy = sessionStorage.getItem('why-choose-section');
              if (cachedWhy) {
                setWhySection(JSON.parse(cachedWhy));
              }
              return;
            }
          }
        }

        const settings = await api.getClinicSettings();
        
        console.log('Hero media settings loaded:', {
          heroMediaType: settings.heroMediaType,
          hasVideoId: !!settings.heroVideoId,
          hasImageUrl: !!settings.heroImageUrl,
          timestamp: new Date().toISOString(),
        });
        
        let mediaConfig: HeroMedia;
        
        // Extract hero media configuration from clinic settings
        if (settings.heroMediaType === 'video' && settings.heroVideoId) {
          mediaConfig = {
            type: 'video',
            videoId: settings.heroVideoId,
          };
        } else if (settings.heroMediaType === 'image' && settings.heroImageUrl) {
          mediaConfig = {
            type: 'image',
            imageUrl: settings.heroImageUrl,
          };
        } else {
          // Default to image type with default image
          console.log('No custom hero media configured, using default image');
          mediaConfig = {
            type: 'image',
            imageUrl: DEFAULT_HERO_IMAGE,
          };
        }
        
        setHeroMedia(mediaConfig);
        setWhySection(settings.whyChoose ?? null);
        
        // Cache the result
        if (typeof window !== 'undefined') {
          sessionStorage.setItem(cacheKey, JSON.stringify(mediaConfig));
          sessionStorage.setItem(`${cacheKey}-time`, Date.now().toString());
          sessionStorage.setItem('why-choose-section', JSON.stringify(settings.whyChoose ?? null));
        }
      } catch (error) {
        console.error('Failed to load hero media settings:', {
          error: error instanceof Error ? error.message : 'Unknown error',
          stack: error instanceof Error ? error.stack : undefined,
          timestamp: new Date().toISOString(),
        });
        
        // Fallback to default image on error
        setHeroMediaError(true);
        setHeroMedia({
          type: 'image',
          imageUrl: DEFAULT_HERO_IMAGE,
        });
        setWhySection(null);
      } finally {
        setLoadingHeroMedia(false);
      }
    };

    void loadServices();
    void loadInsuranceCompanies();
    void loadHeroMedia();
  }, [locale]);

  // Handle hash navigation for booking section
  useEffect(() => {
    const handleHashNavigation = () => {
      if (window.location.hash === '#booking-section') {
        // Small delay to ensure the page is fully loaded
        setTimeout(() => {
          const bookingSection = document.getElementById('booking-section');
          if (bookingSection) {
            bookingSection.scrollIntoView({ 
              behavior: 'smooth', 
              block: 'start' 
            });
          }
        }, 100);
      }
    };

    // Check hash on initial load
    handleHashNavigation();

    // Listen for hash changes
    window.addEventListener('hashchange', handleHashNavigation);

    return () => {
      window.removeEventListener('hashchange', handleHashNavigation);
    };
  }, []);

  const scrollToBooking = () => {
    const bookingSection = document.getElementById('booking-section');
    bookingSection?.scrollIntoView({behavior: 'smooth', block: 'start'});
  };

  // Handle YouTube video load errors by falling back to default image
  const handleVideoError = () => {
    console.error('YouTube video failed to load, falling back to default image', {
      videoId: heroMedia.type === 'video' ? heroMedia.videoId : 'N/A',
      timestamp: new Date().toISOString(),
    });
    
    setHeroMediaError(true);
    setHeroMedia({
      type: 'image',
      imageUrl: DEFAULT_HERO_IMAGE,
    });
  };

  // Handle image load errors by using default image
  const handleImageError = (e: React.SyntheticEvent<HTMLImageElement>) => {
    console.error('Hero image failed to load, falling back to default', {
      attemptedUrl: heroMedia.type === 'image' ? heroMedia.imageUrl : 'N/A',
      timestamp: new Date().toISOString(),
    });
    
    const target = e.target as HTMLImageElement;
    if (target.src !== DEFAULT_HERO_IMAGE) {
      target.src = DEFAULT_HERO_IMAGE;
      setHeroMediaError(true);
    }
  };

  const stats = useMemo(
    () => [
      {value: '15+', label: hero('stats.yearsExperience')},
      {value: '5K+', label: hero('stats.happyPatients')},
      {value: '98%', label: hero('stats.satisfactionRate')}
    ],
    [hero]
  );

  const inlineIconSpacing = isRTL ? 'mr-2' : 'ml-2';
  const arrowHoverShift = isRTL ? 'group-hover:-translate-x-1' : 'group-hover:translate-x-1';
  const arrowRotation = isRTL ? 'rotate-180' : '';

  const heroHighlights = [
    {
      icon: (
        <svg className="h-6 w-6 text-blue-600 dark:text-blue-300 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
      title: hero('features.support.title'),
      description: hero('features.support.description')
    },
    {
      icon: (
        <svg className="h-6 w-6 text-cyan-600 dark:text-cyan-300 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
      title: hero('features.team.title'),
      description: hero('features.team.description')
    }
  ];

  const fallbackWhyMap = useMemo(
    () => ({
      experts: {
        title: why('features.experts.title'),
        description: why('features.experts.description')
      },
      technology: {
        title: why('features.technology.title'),
        description: why('features.technology.description')
      },
      comfort: {
        title: why('features.comfort.title'),
        description: why('features.comfort.description')
      },
      affordable: {
        title: why('features.affordable.title'),
        description: why('features.affordable.description')
      }
    }),
    [why]
  );

  const renderWhyIcon = useCallback((key: string) => {
    switch (key) {
      case 'technology':
        return (
          <svg className="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
          </svg>
        );
      case 'comfort':
        return (
          <svg className="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        );
      case 'affordable':
        return (
          <svg className="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        );
      case 'experts':
      default:
        return (
          <svg className="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
          </svg>
        );
    }
  }, []);

  const fallbackWhyOrder = ['experts', 'technology', 'comfort', 'affordable'] as const;
  const fallbackWhyEntry = fallbackWhyMap[fallbackWhyOrder[0]];

  const derivedWhyFeatures = useMemo(() => {
    if (whySection?.features && whySection.features.length > 0) {
      return whySection.features.map((feature, index) => {
        const key = feature.key ?? `feature-${index}`;
        const fallbackEntry = fallbackWhyMap[key as keyof typeof fallbackWhyMap] ?? fallbackWhyEntry;
        return {
          key,
          icon: renderWhyIcon(key),
          title: getLocalizedText(feature.title) ?? fallbackEntry.title,
          description: getLocalizedText(feature.description) ?? fallbackEntry.description
        };
      });
    }

    return fallbackWhyOrder.map((key) => ({
      key,
      icon: renderWhyIcon(key),
      title: fallbackWhyMap[key].title,
      description: fallbackWhyMap[key].description
    }));
  }, [whySection, renderWhyIcon, getLocalizedText, fallbackWhyMap, fallbackWhyEntry, fallbackWhyOrder]);

  const whySectionTitle = getLocalizedText(whySection?.title) ?? why('title');
  const whySectionSubtitle = getLocalizedText(whySection?.subtitle) ?? why('subtitle');

  return (
    <>
      <section className="relative overflow-hidden bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-900 dark:via-slate-950 dark:to-slate-950 transition-colors duration-500">
        <div className="pointer-events-none absolute -right-32 -top-10 h-72 w-96 rounded-[63px] bg-gradient-to-br from-blue-100/40 to-cyan-100/40 dark:from-blue-900/30 dark:to-cyan-900/20 dark:opacity-70" />
        <div className="pointer-events-none absolute right-[-12rem] top-40 h-[54rem] w-[54rem] rotate-[-10deg] rounded-[123px] bg-gradient-to-br from-blue-50/30 to-cyan-50/30 dark:from-blue-950/30 dark:to-cyan-950/20 dark:opacity-60" />
        <div className="pointer-events-none absolute -left-40 bottom-[-12rem] h-[32rem] w-[32rem] rounded-full bg-gradient-to-br from-cyan-100/40 to-transparent dark:from-slate-900/40 dark:to-transparent" />

        <div className="mx-auto grid max-w-7xl items-center gap-12 px-4 py-16 md:px-6 lg:grid-cols-2 lg:px-8 lg:py-24">
          <div className="z-10">
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-blue-200/60 dark:border-blue-800/40 bg-blue-100/80 dark:bg-blue-900/40 px-4 py-2 text-sm font-semibold text-blue-700 dark:text-blue-300 backdrop-blur-sm shadow-sm dark:shadow-blue-900/40 transition-colors">
              <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                  clipRule="evenodd"
                />
              </svg>
              {hero('badge')}
            </div>
            <h1 className="text-5xl font-extrabold leading-tight tracking-tight text-slate-900 dark:text-slate-100 md:text-6xl lg:text-7xl">
              {hero('title')}{' '}
              <span className="bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-400 dark:to-cyan-400 bg-clip-text text-transparent">
                {common('clinicName')}
              </span>
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-slate-600 dark:text-slate-300">
              {hero('subtitle')}
            </p>

            <div className="mt-8 flex flex-col gap-4 sm:flex-row">
              <button
                onClick={scrollToBooking}
                className="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-blue-600 to-cyan-600 px-8 py-4 text-lg font-semibold text-white transition-all hover:from-blue-700 hover:to-cyan-700 hover:-translate-y-0.5 shadow-lg hover:shadow-xl dark:shadow-blue-900/40 dark:hover:shadow-blue-900/60"
              >
                {hero('bookAppointment')}
                <svg
                  className={`h-5 w-5 ${inlineIconSpacing} ${arrowRotation}`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </button>
              <Link
                href="/services"
                className="inline-flex items-center justify-center rounded-xl border-2 border-blue-200 dark:border-blue-800 bg-white/90 dark:bg-slate-900/60 px-8 py-4 text-lg font-semibold text-blue-700 dark:text-blue-300 transition-all hover:border-blue-300 dark:hover:border-blue-700 shadow hover:shadow-lg dark:shadow-blue-900/40 backdrop-blur"
              >
                {hero('ourServices')}
              </Link>
            </div>

            <div className="mt-12 grid max-w-2xl grid-cols-3 gap-6">
              {stats.map((stat) => (
                <div key={stat.label} className="text-center">
                  <div className="bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-400 dark:to-cyan-400 bg-clip-text text-4xl font-extrabold text-transparent md:text-5xl transition-colors">
                    {stat.value}
                  </div>
                  <div className="mt-2 text-sm font-medium text-slate-600 dark:text-slate-300">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>

          <div className="relative z-10">
            <HeroSectionErrorBoundary fallbackImage={DEFAULT_HERO_IMAGE}>
              {loadingHeroMedia ? (
                // Loading skeleton for hero media - responsive height
                <div className="relative">
                  <div className="animate-pulse h-[300px] sm:h-[400px] md:h-[450px] lg:h-[500px] w-full rounded-2xl sm:rounded-3xl border border-slate-100/60 bg-slate-200 dark:border-slate-800/60 dark:bg-slate-800 transition-colors duration-300" />
                </div>
              ) : heroMedia.type === 'video' && heroMedia.videoId && !heroMediaError ? (
                // YouTube video embed with error handling - responsive
                <div className="relative">
                  <YouTubeEmbed
                    videoId={heroMedia.videoId}
                    className="border border-slate-100/60 shadow-xl sm:shadow-2xl transition-all duration-300 dark:border-slate-800/60 dark:shadow-blue-900/50"
                    onError={handleVideoError}
                  />
                  {/* Fallback to image if video fails to render */}
                  {heroMediaError && (
                    <div className="absolute inset-0">
                      <Image
                        src={DEFAULT_HERO_IMAGE}
                        alt={hero('imageAlt')}
                        width={1200}
                        height={800}
                        priority
                        sizes="(max-width: 640px) 100vw, (max-width: 1024px) 90vw, 50vw"
                        className="h-full w-full rounded-2xl sm:rounded-3xl border border-slate-100/60 object-cover shadow-xl sm:shadow-2xl transition-all duration-300 dark:border-slate-800/60 dark:shadow-blue-900/50"
                      />
                      <div className="absolute inset-0 rounded-2xl sm:rounded-3xl bg-gradient-to-tr from-blue-600/10 via-blue-500/5 dark:from-blue-500/20 dark:via-blue-600/10 to-transparent transition-colors duration-300" />
                    </div>
                  )}
                </div>
              ) : (
                // Image display (default or custom) - responsive
                <div className="relative">
                  <Image
                    src={heroMedia.imageUrl || DEFAULT_HERO_IMAGE}
                    alt={hero('imageAlt')}
                    width={1200}
                    height={800}
                    priority
                    sizes="(max-width: 640px) 100vw, (max-width: 1024px) 90vw, 50vw"
                    className="h-full w-full rounded-2xl sm:rounded-3xl border border-slate-100/60 object-cover shadow-xl sm:shadow-2xl transition-all duration-300 dark:border-slate-800/60 dark:shadow-blue-900/50"
                    onError={handleImageError}
                  />
                  <div className="absolute inset-0 rounded-2xl sm:rounded-3xl bg-gradient-to-tr from-blue-600/10 via-blue-500/5 dark:from-blue-500/20 dark:via-blue-600/10 to-transparent transition-colors duration-300" />
                </div>
              )}
            </HeroSectionErrorBoundary>

            <div className="mt-6 sm:mt-8 grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
              <h2 className="sr-only">{hero('highlightsHeading')}</h2>
              {heroHighlights.map((highlight) => (
                <div
                  key={highlight.title}
                  className="rounded-xl sm:rounded-2xl border border-slate-100/70 dark:border-slate-800/60 bg-white/90 dark:bg-slate-900/70 p-4 sm:p-6 shadow-md sm:shadow-lg dark:shadow-blue-900/40 backdrop-blur transition-all duration-300"
                >
                  <div className="mb-3 sm:mb-4 flex h-10 w-10 sm:h-12 sm:w-12 items-center justify-center rounded-lg sm:rounded-xl bg-blue-100/80 dark:bg-blue-500/15 transition-colors duration-300">
                    {highlight.icon}
                  </div>
                  <h3 className="text-sm sm:text-base text-slate-900 dark:text-slate-100 font-bold transition-colors">{highlight.title}</h3>
                  <p className="mt-1 text-xs sm:text-sm text-slate-600 dark:text-slate-300 transition-colors">{highlight.description}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section
        id="booking-section"
        className="relative z-10 overflow-hidden bg-gradient-to-br from-slate-50 to-blue-50 dark:from-slate-950 dark:to-slate-900 py-16 transition-colors"
      >
        <div className="pointer-events-none absolute -left-20 top-12 h-56 w-56 rounded-full bg-gradient-to-br from-blue-200/40 to-transparent dark:from-blue-900/30" />
        <div className="pointer-events-none absolute right-[-6rem] bottom-[-6rem] h-72 w-72 rounded-full bg-gradient-to-tl from-cyan-200/40 to-transparent dark:from-cyan-900/30" />
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="mx-auto mb-8 max-w-2xl text-center">
            <h2 className="text-3xl font-bold text-slate-900 dark:text-slate-100 md:text-4xl">{booking('title')}</h2>
            <p className="mt-3 text-base text-slate-600 dark:text-slate-300">{booking('subtitle')}</p>
          </div>
          <BookingSlider />
        </div>
      </section>

      <section className="relative overflow-hidden bg-white dark:bg-slate-900 py-20 transition-colors">
        <div className="pointer-events-none absolute left-[-10rem] top-24 h-72 w-72 rounded-full bg-blue-100/30 dark:bg-blue-900/20 blur-3xl" />
        <div className="pointer-events-none absolute right-[-8rem] bottom-0 h-64 w-64 rounded-full bg-cyan-100/30 dark:bg-cyan-900/20 blur-3xl" />
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="mx-auto mb-12 max-w-3xl text-center">
            <h2 className="text-4xl font-bold text-slate-900 dark:text-slate-100 md:text-5xl">{servicesT('title')}</h2>
            <p className="mt-4 text-lg text-slate-600 dark:text-slate-300">{servicesT('subtitle')}</p>
          </div>

          {loadingServices ? (
            <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
              {[1, 2, 3, 4, 5, 6].map((i) => (
                <div
                  key={i}
                  className="animate-pulse rounded-2xl border border-blue-100/70 dark:border-blue-900/60 bg-gradient-to-br from-blue-50/80 to-cyan-50/80 dark:from-blue-950/40 dark:to-cyan-950/40 p-8 shadow-sm dark:shadow-blue-900/30 backdrop-blur"
                >
                  <div className="mb-4 h-14 w-14 rounded-xl bg-slate-300 dark:bg-slate-700" />
                  <div className="mb-2 h-6 w-3/4 rounded bg-slate-300 dark:bg-slate-700" />
                  <div className="h-4 w-full rounded bg-slate-200 dark:bg-slate-800" />
                  <div className="mt-2 h-4 w-5/6 rounded bg-slate-200 dark:bg-slate-800" />
                </div>
              ))}
            </div>
          ) : services.length > 0 ? (
            <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
              {services.map((service, index) => (
                <div
                  key={service.slug}
                  className="group rounded-2xl border border-blue-100/70 dark:border-blue-900/60 bg-gradient-to-br from-blue-50/80 to-cyan-50/80 dark:from-blue-950/30 dark:to-cyan-950/30 p-8 shadow-lg dark:shadow-blue-900/40 transition-all duration-300 hover:-translate-y-1 hover:shadow-2xl dark:hover:shadow-blue-900/60 backdrop-blur"
                >
                  <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-xl bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 text-white shadow-md dark:shadow-blue-900/40 transition-transform group-hover:scale-110">
                    <svg className="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      {index % 6 === 0 && (
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                      )}
                      {index % 6 === 1 && (
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
                      )}
                      {index % 6 === 2 && (
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                      )}
                      {index % 6 === 3 && (
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                      )}
                      {index % 6 === 4 && (
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                      )}
                      {index % 6 === 5 && (
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                      )}
                    </svg>
                  </div>
                  <h3 className="mb-2 text-xl font-bold text-slate-900 dark:text-slate-100">{service.name}</h3>
                  <p className="text-slate-600 dark:text-slate-300">{service.summary}</p>
                </div>
              ))}
            </div>
          ) : (
            <div className="py-12 text-center">
              <p className="text-lg text-slate-600 dark:text-slate-400">{servicesT('empty')}</p>
            </div>
          )}

          <div className="mt-12 text-center">
            <Link
              href="/services"
              className="group inline-flex items-center text-lg font-semibold text-blue-700 dark:text-blue-400 transition-colors hover:text-blue-800 dark:hover:text-blue-300"
            >
              {servicesT('exploreAll')}
              <svg
                className={`h-5 w-5 ${inlineIconSpacing} transition-transform ${arrowRotation} ${arrowHoverShift}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </Link>
          </div>
        </div>
      </section>

      <section className="relative overflow-hidden bg-white dark:bg-slate-950 py-20 transition-colors">
        <div className="pointer-events-none absolute inset-x-0 top-0 h-40 bg-gradient-to-b from-blue-100/30 to-transparent dark:from-blue-900/20" />
        <div className="pointer-events-none absolute inset-x-0 bottom-0 h-40 bg-gradient-to-t from-cyan-100/30 to-transparent dark:from-cyan-900/20" />
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="mx-auto mb-12 max-w-3xl text-center">
            <h2 className="text-4xl font-bold text-slate-900 dark:text-slate-100 md:text-5xl">{whySectionTitle}</h2>
            <p className="mt-4 text-lg text-slate-600 dark:text-slate-300">{whySectionSubtitle}</p>
          </div>

          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
            {derivedWhyFeatures.map((feature) => (
              <div
                key={feature.key}
                className="p-6 text-center rounded-2xl border border-slate-100/70 dark:border-slate-800/60 bg-white/80 dark:bg-slate-900/60 shadow-sm dark:shadow-blue-900/30 backdrop-blur transition-colors"
              >
                <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-blue-600 to-cyan-600 dark:from-blue-500 dark:to-cyan-500 shadow-lg dark:shadow-blue-900/40 transition-transform">
                  {feature.icon}
                </div>
                <h3 className="mb-2 text-lg font-bold text-slate-900 dark:text-slate-100">{feature.title}</h3>
                <p className="text-sm text-slate-600 dark:text-slate-300">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Insurance Companies Section */}
      <section className="py-16 bg-gradient-to-br from-slate-50 to-blue-50 dark:from-slate-950 dark:to-slate-900 transition-colors">
        <div className="mx-auto max-w-7xl px-4 md:px-6 lg:px-8">
          <div className="mx-auto mb-12 max-w-3xl text-center">
            <h2 className="text-3xl font-bold text-slate-900 dark:text-slate-100 md:text-4xl">{insurance('title')}</h2>
            <p className="mt-4 text-lg text-slate-600 dark:text-slate-300">{insurance('subtitle')}</p>
          </div>

          {loadingInsurance ? (
            <div className="grid grid-cols-2 gap-6 md:grid-cols-4 lg:grid-cols-6">
              {[1, 2, 3, 4, 5, 6].map((i) => (
                <div
                  key={i}
                  className="animate-pulse rounded-2xl border border-slate-100/70 dark:border-slate-800/60 bg-white/80 dark:bg-slate-900/60 p-6 shadow-sm dark:shadow-blue-900/30 backdrop-blur"
                >
                  <div className="h-16 w-full rounded-lg bg-slate-300 dark:bg-slate-700" />
                  <div className="mt-3 h-4 w-3/4 rounded bg-slate-200 dark:bg-slate-800" />
                </div>
              ))}
            </div>
          ) : insuranceCompanies.length > 0 ? (
            <div className="grid grid-cols-2 gap-6 md:grid-cols-4 lg:grid-cols-6">
              {insuranceCompanies.map((company) => (
                <div
                  key={company.id}
                  className="group rounded-2xl border border-slate-100/70 dark:border-slate-800/60 bg-white/90 dark:bg-slate-900/70 p-6 shadow-sm dark:shadow-blue-900/30 backdrop-blur transition-all duration-300 hover:-translate-y-1 hover:shadow-lg dark:hover:shadow-blue-900/50"
                >
                  {company.logoUrl ? (
                    <div className="relative mb-3 h-16 w-full overflow-hidden rounded-lg">
                      <Image
                        src={company.logoUrl}
                        alt={company.name}
                        fill
                        sizes="(max-width: 640px) 40vw, (max-width: 1024px) 20vw, 10vw"
                        className="object-contain transition-transform duration-300 group-hover:scale-105"
                      />
                    </div>
                  ) : (
                    <div className="mb-3 flex h-16 w-full items-center justify-center rounded-lg bg-gradient-to-br from-blue-100 to-cyan-100 dark:from-blue-900/40 dark:to-cyan-900/40">
                      <svg className="h-8 w-8 text-blue-600 dark:text-blue-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                      </svg>
                    </div>
                  )}
                  <h3 className="text-center text-sm font-semibold text-slate-900 dark:text-slate-100 transition-colors">
                    {company.name}
                  </h3>
                  {company.description && (
                    <p className="mt-1 text-center text-xs text-slate-600 dark:text-slate-400 line-clamp-2 transition-colors">
                      {company.description}
                    </p>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="py-12 text-center">
              <div className="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900/40">
                <svg className="h-8 w-8 text-blue-600 dark:text-blue-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
              <h3 className="mb-2 text-xl font-semibold text-slate-900 dark:text-slate-100">{insurance('empty.title')}</h3>
              <p className="text-slate-600 dark:text-slate-400">{insurance('empty.description')}</p>
            </div>
          )}
        </div>
      </section>

      <section className="bg-gradient-to-r from-blue-600 to-cyan-600 dark:from-blue-800/80 dark:via-blue-900/80 dark:to-slate-900 py-20 transition-colors">
        <div className="mx-auto max-w-7xl px-4 text-center md:px-6 lg:px-8">
          <h2 className="mb-6 text-4xl font-bold text-white md:text-5xl">{cta('title')}</h2>
          <p className="mx-auto mb-8 max-w-2xl text-xl text-blue-50 dark:text-blue-100">{cta('subtitle')}</p>
          <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
            <button
              onClick={scrollToBooking}
              className="inline-flex items-center justify-center rounded-xl bg-white/95 px-8 py-4 text-lg font-semibold text-blue-700 transition-all hover:bg-blue-50 shadow-lg hover:shadow-xl dark:bg-blue-100/15 dark:text-blue-100 dark:hover:bg-blue-100/25 dark:shadow-blue-900/50 dark:hover:shadow-blue-900/70"
            >
              {cta('scheduleAppointment')}
              <svg
                className={`h-5 w-5 ${inlineIconSpacing} ${arrowRotation}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </button>
            <Link
              href="/doctors"
              className="inline-flex items-center justify-center rounded-xl border-2 border-white/90 px-8 py-4 text-lg font-semibold text-white transition-all hover:bg-white/15 dark:border-white/70"
            >
              {cta('meetDentists')}
            </Link>
          </div>
        </div>
      </section>
    </>
  );
}
