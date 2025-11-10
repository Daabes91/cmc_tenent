import { ThemeLayoutProps } from '../types'
import Header from './components/Header'
import Footer from './components/Footer'
import Navigation from './components/Navigation'
import './styles/theme.css'

/**
 * Clinic theme layout component
 * Professional medical clinic design with emphasis on trust and cleanliness
 */
export default function ClinicLayout({ 
  children, 
  locale, 
  tenantSlug 
}: ThemeLayoutProps) {
  return (
    <div className="clinic-theme min-h-screen flex flex-col bg-gray-50">
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
