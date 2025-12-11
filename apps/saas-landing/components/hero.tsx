'use client';

import { useMemo, useState } from 'react';
import Image from "next/image";
import { ArrowRight, ImageOff, PlayCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { PATIENT_WEB_URL, SALES_EMAIL } from '@/lib/constants';
import { useLanguage } from '@/contexts/LanguageContext';
import { AnimatedBackground } from './AnimatedBackground';
import { Card3D } from './Card3D';
import { LogoMarquee } from './LogoMarquee';
import { useIntersectionAnimation } from '@/hooks/use-intersection-animation';
import { healthcareCopy } from '@/lib/content/healthcare-copy';
import { useAnalytics } from '@/hooks/use-analytics';
import { withBasePath } from '@/lib/base-path';

export default function Hero() {
  const { language } = useLanguage();
  const copy = healthcareCopy[language].hero;
  const isRTL = language === 'ar';
  const heroVideoUrl = process.env.NEXT_PUBLIC_HERO_VIDEO_URL;
  const videoLabel = language === 'ar' ? 'مشاهدة عرض الفيديو' : 'Watch video tour';
  const videoSrc = useMemo(() => {
    if (!heroVideoUrl) return null;
    const hasQuery = heroVideoUrl.includes('?');
    return `${heroVideoUrl}${hasQuery ? '&' : '?'}rel=0&modestbranding=1`;
  }, [heroVideoUrl]);
  const heroImageSrc = useMemo(() => withBasePath('/images/hero.png'), []);

  // Analytics tracking
  const { trackCTA } = useAnalytics();

  // Image error state
  const [imageError, setImageError] = useState(false);
  const [showVideo, setShowVideo] = useState(false);

  // Intersection animation hooks for staggered entrance
  const [badgeRef, badgeVisible] = useIntersectionAnimation({ threshold: 0.1 });
  const [headlineRef, headlineVisible] = useIntersectionAnimation({ threshold: 0.1 });
  const [descriptionRef, descriptionVisible] = useIntersectionAnimation({ threshold: 0.1 });
  const [buttonsRef, buttonsVisible] = useIntersectionAnimation({ threshold: 0.1 });
  const [imageRef, imageVisible] = useIntersectionAnimation({ threshold: 0.1 });
  const [trustRef, trustVisible] = useIntersectionAnimation({ threshold: 0.1 });

  // Handle image load error
  const handleImageError = () => {
    console.error('Hero image failed to load');
    setImageError(true);
  };

  return (
    <section 
      className="relative overflow-hidden py-12 sm:py-16 md:py-24 lg:py-32"
      aria-labelledby="hero-heading"
    >
      {/* Animated Background with SVG waves */}
      <AnimatedBackground />

      {/* Centered Content Container */}
      <div className="container relative z-10 mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col items-center space-y-8 sm:space-y-10 md:space-y-10 text-center">
          
          {/* Badge Component */}
          <div
            ref={badgeRef}
            className={`transition-all duration-900 ease-out ${
              badgeVisible ? 'opacity-100 translate-x-0' : `opacity-0 ${isRTL ? 'translate-x-24' : '-translate-x-24'}`
            }`}
            role="status"
            aria-label={copy.badge}
          >
            <div className="inline-flex items-center gap-1 rounded-full border border-primary/20 bg-primary/5 px-3 py-1.5 sm:px-4 sm:py-2 text-xs sm:text-sm font-medium backdrop-blur-sm">
              <span className="text-primary" aria-hidden="true">{copy.badge}</span>
            </div>
          </div>

          {/* Headline with Gradient Underline */}
          <div
            ref={headlineRef}
            className={`max-w-4xl space-y-3 sm:space-y-4 transition-all duration-900 ease-out ${
              headlineVisible ? 'opacity-100 translate-x-0' : `opacity-0 ${isRTL ? 'translate-x-24' : '-translate-x-24'}`
            }`}
            style={{ transitionDelay: headlineVisible ? '400ms' : '0ms' }}
          >
            <h1 
              id="hero-heading"
              className="text-lg font-bold leading-tight tracking-tight sm:text-2xl md:text-2xl lg:text-3xl xl:text-4xl px-4 sm:px-0"
            >
              {copy.headline.prefix}{' '}
              <span className="relative inline-block">
                <span className="relative z-10 text-primary text-lg sm:text-xl md:text-2xl">{copy.headline.highlight}</span>
                <span className={`absolute inset-x-0 -bottom-1 sm:-bottom-2 h-2 sm:h-3 ${isRTL ? 'bg-gradient-to-l' : 'bg-gradient-to-r'} from-primary to-mintlify-blue opacity-30 blur-sm`} aria-hidden="true" />
                <span className={`absolute inset-x-0 -bottom-1 sm:-bottom-2 h-2 sm:h-3 ${isRTL ? 'bg-gradient-to-l' : 'bg-gradient-to-r'} from-primary to-mintlify-blue opacity-50`} aria-hidden="true" />
              </span>
            </h1>
          </div>

          {/* Description Text */}
          <div
            ref={descriptionRef}
            className={`max-w-2xl px-3 sm:px-0 transition-all duration-900 ease-out ${
              descriptionVisible ? 'opacity-100 translate-x-0' : `opacity-0 ${isRTL ? 'translate-x-24' : '-translate-x-24'}`
            }`}
            style={{ transitionDelay: descriptionVisible ? '800ms' : '0ms' }}
          >
            <p className="text-xs leading-relaxed text-muted-foreground sm:text-xs md:text-sm">
              {copy.description}
            </p>
          </div>

          {/* CTA Buttons */}
          <nav
            ref={buttonsRef}
            className={`flex flex-col gap-3 sm:gap-4 sm:flex-row w-full sm:w-auto px-4 sm:px-0 transition-all duration-900 ease-out ${
              buttonsVisible ? 'opacity-100 translate-x-0' : `opacity-0 ${isRTL ? 'translate-x-24' : '-translate-x-24'}`
            }`}
            style={{ transitionDelay: buttonsVisible ? '1200ms' : '0ms' }}
            aria-label={language === 'ar' ? 'إجراءات رئيسية' : 'Primary actions'}
          >
            <Button 
              size="lg" 
              className="h-11 sm:h-12 bg-primary px-6 sm:px-8 text-sm sm:text-base font-semibold hover:bg-primary/90 focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:outline-none transition-all w-full sm:w-auto"
              asChild
            >
              <a 
                href="/landing/signup"
                aria-label={copy.ctaPrimary}
                onClick={() => trackCTA(copy.ctaPrimary, 'hero_section')}
              >
                {copy.ctaPrimary}
                <ArrowRight className={`${isRTL ? 'mr-2' : 'ml-2'} h-4 w-4 sm:h-5 sm:w-5 ${isRTL ? 'rotate-180' : ''}`} aria-hidden="true" />
              </a>
            </Button>
            <Button
              size="lg"
              variant="outline"
              className="h-11 sm:h-12 px-6 sm:px-8 text-sm sm:text-base font-semibold focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:outline-none transition-all w-full sm:w-auto bg-[#CFECD3] text-primary hover:bg-[#CFECD3]/90 hover:text-primary"
              asChild
            >
              <a 
                href={`mailto:${SALES_EMAIL}`}
                aria-label={`${copy.ctaSecondary} ${language === 'ar' ? '(يفتح تطبيق البريد الإلكتروني)' : '(opens email client)'}`}
                className="w-full text-[#00A33B]"
                onClick={() => trackCTA(copy.ctaSecondary, 'hero_section')}
              >
                {copy.ctaSecondary}
              </a>
            </Button>
            {videoSrc && (
              <Button
                type="button"
                size="lg"
                variant="ghost"
                className="h-11 sm:h-12 px-6 sm:px-8 text-sm sm:text-base font-semibold text-primary hover:bg-primary/10 focus:ring-2 focus:ring-primary focus:ring-offset-2 focus:outline-none transition-all w-full sm:w-auto"
                onClick={() => {
                  setShowVideo(true);
                  trackCTA(videoLabel, 'hero_section');
                }}
              >
                {videoLabel}
                <PlayCircle className={`${isRTL ? 'mr-2' : 'ml-2'} h-5 w-5`} aria-hidden="true" />
              </Button>
            )}
          </nav>

          {/* Hero Image with 3D Card Effect */}
          <figure
            ref={imageRef}
            className={`w-full max-w-6xl px-4 sm:px-0 transition-all duration-900 ease-out ${
              imageVisible ? 'opacity-100 scale-100' : 'opacity-0 scale-80'
            }`}
            style={{ transitionDelay: imageVisible ? '1500ms' : '0ms' }}
            aria-label={language === 'ar' ? 'معاينة لوحة التحكم' : 'Dashboard preview'}
          >
            <Card3D className="overflow-hidden rounded-lg sm:rounded-xl md:rounded-2xl border border-border/50 shadow-xl sm:shadow-2xl">
              {imageError ? (
                // Fallback UI for failed image loads
                <div 
                  className="flex flex-col items-center justify-center bg-muted/30 backdrop-blur-sm p-12 sm:p-16 md:p-24 aspect-[3/2]"
                  role="img"
                  aria-label={language === 'ar' 
                    ? 'تعذر تحميل صورة لوحة التحكم'
                    : 'Dashboard preview unavailable'}
                >
                  <ImageOff className="h-16 w-16 sm:h-20 sm:w-20 md:h-24 md:w-24 text-muted-foreground/40 mb-4" aria-hidden="true" />
                  <p className="text-sm sm:text-base text-muted-foreground/60 text-center max-w-md">
                    {language === 'ar' 
                      ? 'تعذر تحميل صورة لوحة التحكم'
                      : 'Dashboard preview unavailable'}
                  </p>
                </div>
              ) : showVideo && videoSrc ? (
                <div className="relative aspect-[3/2] w-full">
                  <iframe
                    src={videoSrc}
                    title={language === 'ar' ? 'فيديو توضيحي للمنصة' : 'Platform walkthrough video'}
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                    className="absolute inset-0 h-full w-full border-0"
                  />
                  <button
                    type="button"
                    onClick={() => setShowVideo(false)}
                    className="absolute top-3 right-3 rounded-full bg-black/60 px-3 py-1 text-xs font-semibold text-white shadow hover:bg-black/75 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
                  >
                    {language === 'ar' ? 'إغلاق' : 'Close'}
                  </button>
                </div>
              ) : (
                <picture>
                  <Image
                    src={heroImageSrc}
                    alt={language === 'ar' 
                      ? 'معاينة لوحة تحكم SaaS تعرض الميزات الرئيسية والواجهة'
                      : 'SaaS Dashboard Preview showing main features and interface'}
                    width={1366}
                    height={709}
                    className="h-auto w-full"
                    priority
                    placeholder="blur"
                    blurDataURL="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAUCAIAAABj86gYAAAAAXNSR0IArs4c6QAAAERlWElmTU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAAAAA6ABAAMAAAABAAEAAKACAAQAAAABAAAAIKADAAQAAAABAAAAFAAAAAD4J3y1AAAD2klEQVQ4EYWVzW4cRRDHp6d7pmdnvLvYfCVGAmyBOCAUceMAQjnAEyAkngDxEEi5wYkXAHHOI3BJpIgLEpdEXCJyARGRi1dmP7zzPc2vuncWyxJOr3emu7rqX1X/qlqrJ8/+ePD7L5+cfKRiNZ0eGK2LPHfODYOLwlJRrBTbAWkkXx3LUUXKi3dadd8+Xv3pInWUTg/ibKonSZqgoeqm2TTVzE7A03Esliru+6Fr+27ogeBo08RFw7apXCduOA7DgDLRCDw6kfp7e3bntx8iZz54+d33ijffP3zL5lYcgNU0rTGiKj60xpj9s2pxGBeKLU58MpNJhr4ghsV2TBIBN33fk+J5vSZNp9xL2Qs61gYtpHGsNhdba63WJKGaob371/1Pk1umVsYY6ML45OT1Ip+M8P59yR3nsqy7qPvm1x/Py3+stnc+/OpG8aIi3poMtCaEOI7xJlw7t6krExvJ3kcn4TriEA69LLwVRPaDGx2pNNG+fA4CbZJgTgbR2dmiaZr5bHp0dOgJjbD59u5PT8/OUeqHYbUtUQslvkwS2LdvvfP2a69Gsfhwvbr36PFiubaJ2dbtd19+fvNoLg5u3ngF41jHQulIK0BsyQZZkVmebde1XT+GL0FrmoZP6C2JgE6UlmPBeUhLigxFEJVTQ48YKCJwrjyACuRg4RkDdUxDKbySLs5YmCeGHlacefFBJCwD1LZtmrC0j0gMIBt+IV0bU1d1VuShANwBtHeR+vbz+FcfUhuCebR48vXD73WU0GJamc/e+PiL09td3wNHmsHIsy+9G5bPQ/zsrkf5lbdYRU4tyuXPTx+SjrUpx9Pp8ensGE7Wm4vlcpXnE7zM5zOa9Yr9c4+QjI5yg+sads5YP5YRLSSlLcuSyaB9YXk2PdB+YkiKMX4udFDYlZBXRZXblgpMMgvFNLLvnahtGa++KLK26Zquy2yKImmzqeoal8x/WVU2TSls0ze5zUC7KKvQLzAIjiROxPQVZpdDEwadq9qqXtVUOmXoUMK9lFiKzMxJM9GbcowGRpbIxnsuApq00Laq0SzyTMLnz1MEetcNm3a7WK2n+fT4cH59SRthZMCXFTIl9lADCYMEoWK5umjbbnSMDlGq1BimLOUHCofB/f8+dyEHtRA+e4Mlo5JlFkJB9A5oX1EDsu7qdbUeJKAD/zMUDK97hkT33gyBMnXMY5JcaUTxZhIzm80S5Yt/Hex/dzvoUbAbpvA/YBTKBEkLEDes+rKGuLxctMJxv9kf9xJR8iu+d/9BDfXeRvQChu+TNJXOQcQvWLjxJjv0oHhZHm73EmE5iv4FprwCYzhqznUAAAAASUVORK5CYII="
                    onError={handleImageError}
                  />
                </picture>
              )}
            </Card3D>
          </figure>

          {/* Trust Section with Logo Marquee */}
          <aside
            ref={trustRef}
            className={`w-full max-w-4xl space-y-4 sm:space-y-6 px-4 sm:px-0 transition-all duration-900 ease-out ${
              trustVisible ? 'opacity-100 translate-x-0' : `opacity-0 ${isRTL ? 'translate-x-24' : '-translate-x-24'}`
            }`}
            style={{ transitionDelay: trustVisible ? '1000ms' : '0ms' }}
            aria-label={language === 'ar' ? 'الشركات الموثوقة' : 'Trusted companies'}
          >
            <p className="text-xs sm:text-sm font-medium uppercase tracking-wider text-muted-foreground">
              {copy.trustLabel}
            </p>
            <div className={`transition-opacity duration-500 ${trustVisible ? 'opacity-100' : 'opacity-0'}`}>
              <LogoMarquee logos={copy.companyLogos} speed={30} />
            </div>
          </aside>
        </div>
      </div>
    </section>
  );
}
