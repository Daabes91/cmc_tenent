"use client"

import type React from "react"

import { useState, useEffect } from "react"
import Image from "next/image"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import { withBasePath } from "@/lib/base-path"

type MenuItem = {
  icon: React.ReactNode
  title: string
  description: string
  href: string
}

type MenuColumn = {
  title: string
  items: MenuItem[]
}

type FeaturedItem = {
  title: string
  description: string
  ctaText: string
  ctaLink: string
  imageSrc: string
}

type MegaMenuProps = {
  data: {
    title: string
    columns: MenuColumn[]
    featured: FeaturedItem
  }
}

export default function MegaMenu({ data }: MegaMenuProps) {
  const [isVisible, setIsVisible] = useState(false)
  const featuredImage = withBasePath(data.featured.imageSrc || "/placeholder.svg")

  // Animation effect when menu appears
  useEffect(() => {
    setIsVisible(true)
  }, [])

  return (
    <div
      className={cn(
        "max-w-4xl w-full px-4 py-6 transition-all duration-300 transform",
        isVisible ? "opacity-100 translate-y-0" : "opacity-0 -translate-y-4",
      )}
    >
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Menu columns */}
        <div className="col-span-2 grid grid-cols-1 md:grid-cols-2 gap-8">
          {data.columns.map((column, idx) => (
            <div key={idx} className="space-y-4">
              <h3 className="text-sm font-medium text-slate-500 dark:text-gray-400 uppercase tracking-wider">{column.title}</h3>
              <ul className="space-y-4">
                {column.items.map((item, itemIdx) => (
                  <li key={itemIdx}>
                    <Link
                      href={item.href}
                      className="group flex items-start gap-3 rounded-lg p-2 transition-colors hover:bg-slate-100 dark:hover:bg-gray-900"
                    >
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-slate-100 text-primary dark:bg-gray-800 dark:text-primary/80 group-hover:bg-primary/10 dark:group-hover:bg-primary/20">
                        {item.icon}
                      </div>
                      <div>
                        <div className="font-medium text-slate-900 dark:text-white group-hover:text-primary dark:group-hover:text-primary/80">{item.title}</div>
                        <div className="text-sm text-slate-600 dark:text-gray-400">{item.description}</div>
                      </div>
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Featured section */}
        <div className="col-span-1">
          <div className="overflow-hidden rounded-lg border border-slate-200 bg-white dark:border-gray-800 dark:bg-gray-900">
            <div className="relative h-40">
              <Image
                src={featuredImage}
                alt={data.featured.title}
                fill
                sizes="(min-width: 1024px) 320px, 100vw"
                className="object-cover"
                priority
              />
              <div className="absolute inset-0 bg-gradient-to-t from-slate-900/30 to-transparent dark:from-gray-900"></div>
            </div>
            <div className="p-4">
              <h3 className="mb-1 font-medium text-slate-900 dark:text-white">{data.featured.title}</h3>
              <p className="mb-4 text-sm text-slate-600 dark:text-gray-400">{data.featured.description}</p>
              <Button
                asChild
                variant="outline"
                className="w-full border-slate-300 text-slate-700 hover:bg-slate-100 hover:text-slate-900 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-800 dark:hover:text-white"
              >
                <Link href={data.featured.ctaLink}>{data.featured.ctaText}</Link>
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
