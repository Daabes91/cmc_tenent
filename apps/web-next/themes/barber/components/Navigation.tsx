import Link from 'next/link'

interface NavigationProps {
  locale: string
}

export default function Navigation({ locale }: NavigationProps) {
  const navItems = [
    { href: `/${locale}`, label: 'Home' },
    { href: `/${locale}/services`, label: 'Services' },
    { href: `/${locale}/barbers`, label: 'Our Barbers' },
    { href: `/${locale}/gallery`, label: 'Gallery' },
    { href: `/${locale}/contact`, label: 'Contact' },
  ]

  return (
    <nav className="barber-navigation bg-neutral-900 border-b border-neutral-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-center space-x-2">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="barber-nav-link text-neutral-300 hover:text-amber-500 hover:bg-neutral-800 px-6 py-4 text-sm font-bold uppercase tracking-wider transition-all duration-200 border-b-2 border-transparent hover:border-amber-500"
            >
              {item.label}
            </Link>
          ))}
        </div>
      </div>
    </nav>
  )
}
