import Link from 'next/link'

interface NavigationProps {
  locale: string
}

export default function Navigation({ locale }: NavigationProps) {
  const navItems = [
    { href: `/${locale}`, label: 'Home' },
    { href: `/${locale}/about`, label: 'About Us' },
    { href: `/${locale}/services`, label: 'Services' },
    { href: `/${locale}/doctors`, label: 'Our Doctors' },
    { href: `/${locale}/contact`, label: 'Contact' },
  ]

  return (
    <nav className="clinic-navigation bg-blue-600 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-center space-x-1">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="clinic-nav-link text-white hover:bg-blue-700 px-4 py-3 text-sm font-medium transition-colors duration-200 border-b-2 border-transparent hover:border-white"
            >
              {item.label}
            </Link>
          ))}
        </div>
      </div>
    </nav>
  )
}
