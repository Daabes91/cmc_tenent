"use client"

import { useState, useRef, useEffect } from "react"
import DesktopNav from "./DesktopNav"
import MobileMenu from "./MobileMenu"
import Logo from "./Logo"
import HeaderActions from "./HeaderActions"
import {
  CalendarClock,
  Globe2,
  Stethoscope,
  ShieldCheck,
  CreditCard,
  LineChart,
  Users,
  Layers,
  BookText,
  LifeBuoy,
  FileText,
  PenSquare,
} from "lucide-react"
import {PATIENT_WEB_URL, API_DOCS_URL, SAAS_ADMIN_URL, SALES_EMAIL} from '@/lib/constants';

export default function Header() {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const [activeMegaMenu, setActiveMegaMenu] = useState<string | null>(null)
  const [indicatorStyle, setIndicatorStyle] = useState({ width: 0, left: 0, opacity: 0 })
  const [isHoveringMenu, setIsHoveringMenu] = useState(false)
  const menuTimeoutRef = useRef<NodeJS.Timeout | null>(null)
  const navRefs = useRef<Record<string, HTMLDivElement | null>>({})
  const megaMenuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    return () => {
      if (menuTimeoutRef.current) {
        clearTimeout(menuTimeoutRef.current)
      }
    }
  }, [])

  const handleMouseEnter = (menuId: string) => {
    if (menuTimeoutRef.current) {
      clearTimeout(menuTimeoutRef.current)
    }
    setActiveMegaMenu(menuId)

    const navElement = navRefs.current[menuId]
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
        setIndicatorStyle((prev) => ({ ...prev, opacity: 0 }))
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
      setIndicatorStyle((prev) => ({ ...prev, opacity: 0 }))
    }, 300)
  }

  const megaMenus = {
    products: {
      title: "Clinic Platform",
      columns: [
        {
          title: "Patient Experience",
          items: [
            {
              icon: <Globe2 className="h-5 w-5" />,
              title: "Website builder",
              description: "Launch a stunning clinic site with your branding",
              href: "#hero",
            },
            {
              icon: <CalendarClock className="h-5 w-5" />,
              title: "Online bookings",
              description: "24/7 scheduling with automated reminders",
              href: "#pricing",
            },
            {
              icon: <CreditCard className="h-5 w-5" />,
              title: "Trust & payments",
              description: "Showcase services and accept secure payments",
              href: "#features",
            },
          ],
        },
        {
          title: "Clinic Operations",
          items: [
            {
              icon: <Users className="h-5 w-5" />,
              title: "Admin workspace",
              description: "Manage patients, services, and staff in one place",
              href: SAAS_ADMIN_URL,
            },
            {
              icon: <LineChart className="h-5 w-5" />,
              title: "Automation & reminders",
              description: "Keep schedules organized and reduce no-shows",
              href: "#features",
            },
            {
              icon: <ShieldCheck className="h-5 w-5" />,
              title: "Virtual care",
              description: "Secure video visits with built-in payments",
              href: "#integrations",
            },
          ],
        },
      ],
      featured: {
        title: "See the website + admin in action",
        description: "Browse the live demo experience and imagine it with your branding and domain.",
        ctaText: "Open live demo",
        ctaLink: `${PATIENT_WEB_URL}?tenant=demo`,
        imageSrc: "/images/dashboard.png",
      },
    },
    resources: {
      title: "Resources",
      columns: [
        {
          title: "Getting started",
          items: [
            {
              icon: <Layers className="h-5 w-5" />,
              title: "Launch checklist",
              description: "Everything you need to go live this week",
              href: "#how-it-works",
            },
            {
              icon: <BookText className="h-5 w-5" />,
              title: "Playbooks",
              description: "Marketing, operations, and automation guides",
              href: "https://github.com/MohamedDjoudir/landing-page-template-2/tree/main/docs",
            },
            {
              icon: <PenSquare className="h-5 w-5" />,
              title: "Success stories",
              description: "See how clinics grow with a digital home",
              href: "#testimonials",
            },
          ],
        },
        {
          title: "Support & community",
          items: [
            {
              icon: <FileText className="h-5 w-5" />,
              title: "Feature overview",
              description: "Dive deeper into every module",
              href: "#features",
            },
            {
              icon: <LifeBuoy className="h-5 w-5" />,
              title: "Implementation help",
              description: "White-glove onboarding & migration",
              href: "mailto:support@cmc.health",
            },
            {
              icon: <Stethoscope className="h-5 w-5" />,
              title: "Clinic playbooks",
              description: "Webinars, demos, and best practices",
              href: "#blog",
            },
          ],
        },
      ],
      featured: {
        title: "Need to see more?",
        description: "Talk to our team for a guided tour and tailored launch plan.",
        ctaText: "Talk to us",
        ctaLink: `mailto:${SALES_EMAIL}`,
        imageSrc: "/images/webinar.png",
      },
    },
  }

  return (
    <header className="sticky top-0 z-40 border-0 bg-[#F8FCF7]/90 text-slate-900 backdrop-blur-md dark:border-gray-800 dark:bg-gray-950/80 dark:text-white transition-colors">
      <div className="container flex h-16 items-center justify-between px-4 md:px-8">
        <div className="flex items-center gap-6">
          <Logo />
          <DesktopNav
            activeMegaMenu={activeMegaMenu}
            setActiveMegaMenu={setActiveMegaMenu}
            indicatorStyle={indicatorStyle}
            setIndicatorStyle={setIndicatorStyle}
            navRefs={navRefs}
            menuTimeoutRef={menuTimeoutRef}
            isHoveringMenu={isHoveringMenu}
            setIsHoveringMenu={setIsHoveringMenu}
            megaMenuRef={megaMenuRef as React.RefObject<HTMLDivElement>}
            megaMenus={megaMenus}
          />
        </div>
        <HeaderActions isMenuOpen={isMenuOpen} setIsMenuOpen={setIsMenuOpen} />
      </div>
      <MobileMenu isMenuOpen={isMenuOpen} setIsMenuOpen={setIsMenuOpen} />
    </header>
  )
}
