import { ThemeLayoutProps } from '../types'
import Header from './components/Header'
import Footer from './components/Footer'
import Navigation from './components/Navigation'
import './styles/theme.css'

/**
 * Barber theme layout component
 * Modern barbershop design with bold, masculine styling
 */
export default function BarberLayout({ 
  children, 
  locale, 
  tenantSlug 
}: ThemeLayoutProps) {
  return (
    <div className="barber-theme min-h-screen flex flex-col bg-neutral-900">
      <Header locale={locale} tenantSlug={tenantSlug} />
      <Navigation locale={locale} />
      
      <main className="flex-grow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          {children}
        </div>
      </main>
      
      <Footer locale={locale} />
    </div>
  )
}
