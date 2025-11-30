'use client';

import Link from "next/link"
import { Laptop, Users, BarChart3, Settings, HelpCircle, FileText, Zap } from "lucide-react"
import { cn } from "@/lib/utils"
import MegaMenu from "../mega-menu"
import { RefObject } from "react"
import {useLanguage} from '@/contexts/LanguageContext';

interface DesktopNavProps {
  activeMegaMenu: string | null
  setActiveMegaMenu: (menu: string | null) => void
  indicatorStyle: { width: number; left: number; opacity: number }
  setIndicatorStyle: React.Dispatch<React.SetStateAction<{ width: number; left: number; opacity: number }>>
  navRefs: RefObject<Record<string, HTMLDivElement | null>>
  menuTimeoutRef: RefObject<NodeJS.Timeout | null>
  isHoveringMenu: boolean
  setIsHoveringMenu: (hover: boolean) => void
  megaMenuRef: RefObject<HTMLDivElement>
  megaMenus: any
}

export default function DesktopNav({
  activeMegaMenu,
  setActiveMegaMenu,
  indicatorStyle,
  setIndicatorStyle,
  navRefs,
  menuTimeoutRef,
  isHoveringMenu,
  setIsHoveringMenu,
  megaMenuRef,
  megaMenus,
}: DesktopNavProps) {
  const {language} = useLanguage();
  const labels = {
    en: {
      platform: 'Platform',
      resources: 'Resources',
      features: 'Features',
      how: 'How it works',
      pricing: 'Pricing',
      testimonials: 'Testimonials',
    },
    ar: {
      platform: 'المنصة',
      resources: 'الموارد',
      features: 'الميزات',
      how: 'كيف تعمل',
      pricing: 'الأسعار',
      testimonials: 'آراء العملاء',
    },
  } as const;

  const text = labels[language];
  // Handler functions (copied from index, but adapted for props)
  const handleMouseEnter = (menuId: string) => {
    if (menuTimeoutRef.current) {
      clearTimeout(menuTimeoutRef.current)
    }
    setActiveMegaMenu(menuId)
    const navElement = navRefs.current?.[menuId]
    if (navElement) {
      const rect = navElement.getBoundingClientRect()
      const parentRect = navElement.parentElement?.getBoundingClientRect() || { left: 0 }
      setIndicatorStyle({
        width: rect.width,
        left: rect.left - parentRect.left,
        opacity: 1,
      })
    }
  }

  const handleMouseLeave = () => {
    if (menuTimeoutRef.current) {
      clearTimeout(menuTimeoutRef.current)
    }
    if (!isHoveringMenu) {
      menuTimeoutRef.current = setTimeout(() => {
        setActiveMegaMenu(null)
        setIndicatorStyle((prev: { width: number; left: number; opacity: number }) => ({ ...prev, opacity: 0 }))
      }, 300)
    }
  }

  const handleMenuMouseEnter = () => {
    if (menuTimeoutRef.current) {
      clearTimeout(menuTimeoutRef.current)
    }
    setIsHoveringMenu(true)
  }

  const handleMenuMouseLeave = () => {
    setIsHoveringMenu(false)
    menuTimeoutRef.current = setTimeout(() => {
      setActiveMegaMenu(null)
      setIndicatorStyle((prev: any) => ({ ...prev, opacity: 0 }))
    }, 300)
  }

  // Calculate arrow position for the open menu (centered under the active nav link)
  let arrowLeft = indicatorStyle.left + indicatorStyle.width / 2 - 14 // 14px is half the arrow width (28px)

  return (
    <nav className="hidden md:flex items-center gap-6 relative">
      {/* Animated gradient arrow indicator (flipped horizontally, sits on top border of menu) */}
      {activeMegaMenu && (
        <div
          className="pointer-events-none absolute z-50"
          style={{
            top: '100%',  
            left: `${arrowLeft}px`,
            width: "18px",
            height: "13px",
            transition: 'left 300ms cubic-bezier(0.4,0,0.2,1), top 300ms cubic-bezier(0.4,0,0.2,1)',
            background: "linear-gradient(135deg,#18e299,#578bfa)",
            clipPath: "polygon(50% 0%, 0 100%, 100% 100%)",
          }}
        />
      )}
      <div
        className="relative"
        onMouseEnter={() => handleMouseEnter("products")}
        onMouseLeave={handleMouseLeave}
        ref={el => { navRefs.current && (navRefs.current.products = el) }}
      >
        <button
          className={cn(
            "flex items-center gap-1 text-sm font-medium px-2 py-1 rounded hover:text-primary dark:hover:text-white transition",
            activeMegaMenu === "products" ? "text-primary dark:text-white" : "text-slate-600 dark:text-gray-300",
          )}
        >
          {text.platform}
        </button>
      </div>
      <div
        className="relative"
        onMouseEnter={() => handleMouseEnter("resources")}
        onMouseLeave={handleMouseLeave}
        ref={el => { navRefs.current && (navRefs.current.resources = el) }}
      >
        <button
          className={cn(
            "flex items-center gap-1 text-sm font-medium px-2 py-1 rounded hover:text-primary dark:hover:text-white transition",
            activeMegaMenu === "resources" ? "text-primary dark:text-white" : "text-slate-600 dark:text-gray-300",
          )}
        >
          {text.resources}
        </button>
      </div>
      <Link href="#features" className="text-sm font-medium text-slate-600 hover:text-primary dark:text-gray-300 dark:hover:text-white px-2 py-1 rounded transition">
        {text.features}
      </Link>
      <Link href="#how-it-works" className="text-sm font-medium text-slate-600 hover:text-primary dark:text-gray-300 dark:hover:text-white px-2 py-1 rounded transition">
        {text.how}
      </Link>
      <Link href="#pricing" className="text-sm font-medium text-slate-600 hover:text-primary dark:text-gray-300 dark:hover:text-white px-2 py-1 rounded transition">
        {text.pricing}
      </Link>
      
      {/* Mega Menu */}
      {activeMegaMenu && (
        <div
          ref={megaMenuRef}
          className="absolute left-0 right-0 bg-white/95 dark:bg-[rgba(17,15,31,0.95)] backdrop-blur-xl flex justify-center min-w-max px-8 py-8"
          style={{
            top: "calc(100% + 12px)",
            zIndex: 50,
          }}
          onMouseEnter={handleMenuMouseEnter}
          onMouseLeave={handleMenuMouseLeave}
        >
          <div
            className="absolute top-0 left-0 w-full h-1 bg-mintlify-gradient rounded-t-lg"
            style={{zIndex: 1}}
          />
          <div style={{ position: "relative", zIndex: 2, width: "100%" }}>
            <MegaMenu data={megaMenus[activeMegaMenu as keyof typeof megaMenus]} />
          </div>
        </div>
      )}
    </nav>
  )
}
