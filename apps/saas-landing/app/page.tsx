"use client";

import { useEffect, useState, useRef } from "react";
import dynamic from "next/dynamic";
import Script from "next/script";
import Header from "@/components/Header";
import Hero from "@/components/hero";
import { 
  generateOrganizationSchema, 
  generateSoftwareApplicationSchema 
} from "@/lib/seo/structured-data";

// Scroll restoration component
const ScrollRestoration = () => {
  useEffect(() => {
    // Set scroll to top
    window.scrollTo(0, 0);

    // Disable browser's automatic scroll restoration
    if ("scrollRestoration" in history) {
      history.scrollRestoration = "manual";
    }

    const handleBeforeUnload = () => {
      window.scrollTo(0, 0);
    };

    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, []);

  return null;
};

// Lazy load components
const SocialProof = dynamic(() => import("@/components/social-proof"), {
  ssr: false,
});

const Features = dynamic(() => import("@/components/features"), {
  ssr: false,
});

const HowItWorks = dynamic(() => import("../components/how-it-works"), {
  ssr: false,
});

const Testimonials = dynamic(() => import("@/components/testimonials"), {
  ssr: false,
});

const Pricing = dynamic(() => import("@/components/pricing"), {
  ssr: false,
});

const ComparisonTable = dynamic(() => import("@/components/comparison-table"), {
  ssr: false,
});

const Integrations = dynamic(() => import("@/components/integrations"), {
  ssr: false,
});

const Security = dynamic(() => import("@/components/security"), {
  ssr: false,
});

const Faq = dynamic(() => import("@/components/faq"), {
  ssr: false,
});

const BlogPreview = dynamic(() => import("@/components/blog-preview"), {
  ssr: false,
});

const Cta = dynamic(() => import("@/components/cta"), {
  ssr: false,
});

const Footer = dynamic(() => import("@/components/footer"), {
  ssr: false,
});

// LazyLoad wrapper component
function LazyLoad({ children }: { children: React.ReactNode }) {
  const ref = useRef<HTMLDivElement>(null);
  const [inView, setInView] = useState(false);

  useEffect(() => {
    const obs = new IntersectionObserver(
      ([e]) => {
        if (e.isIntersecting) {
          setInView(true);
          obs.disconnect();
        }
      },
      { rootMargin: "200px" }
    );
    if (ref.current) obs.observe(ref.current);
    return () => obs.disconnect();
  }, []);

  return <div ref={ref}>{inView ? children : null}</div>;
}

export default function LandingPage() {
  // Generate structured data
  const organizationSchema = generateOrganizationSchema();
  const softwareSchema = generateSoftwareApplicationSchema();

  return (
    <div className="flex flex-col bg-slate-50 text-slate-900 dark:bg-gray-950 dark:text-gray-100 min-h-[11700px] transition-colors">
      {/* Structured Data for SEO */}
      <Script
        id="organization-schema"
        type="application/ld+json"
        dangerouslySetInnerHTML={{
          __html: JSON.stringify(organizationSchema),
        }}
      />
      <Script
        id="software-schema"
        type="application/ld+json"
        dangerouslySetInnerHTML={{
          __html: JSON.stringify(softwareSchema),
        }}
      />
      
      <ScrollRestoration />
      <Header />
      <Hero />
      <LazyLoad>
        <SocialProof />
      </LazyLoad>
      <LazyLoad>
        <Features />
      </LazyLoad>
      <LazyLoad>
        <HowItWorks />
      </LazyLoad>
      <LazyLoad>
        <Testimonials />
      </LazyLoad>
      <LazyLoad>
        <Pricing />
      </LazyLoad>
      <LazyLoad>
        <ComparisonTable />
      </LazyLoad>
      <LazyLoad>
        <Integrations />
      </LazyLoad>
      <LazyLoad>
        <Security />
      </LazyLoad>
      <LazyLoad>
        <Faq />
      </LazyLoad>
      <LazyLoad>
        <BlogPreview />
      </LazyLoad>
      <LazyLoad>
        <Cta />
      </LazyLoad>
      <LazyLoad>
        <Footer />
      </LazyLoad>
    </div>
  );
}
