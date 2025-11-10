import { ThemeLayoutProps } from '../types'
import Header from './components/Header'
import Footer from './components/Footer'

/**
 * Default theme layout component
 * Provides a basic, clean layout structure with header and footer
 */
export default function DefaultLayout({ 
  children, 
  locale, 
  tenantSlug 
}: ThemeLayoutProps) {
  return (
    <div className="min-h-screen flex flex-col">
      <Header locale={locale} tenantSlug={tenantSlug} />
      
      <main className="flex-grow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {children}
        </div>
      </main>
      
      <Footer locale={locale} />
    </div>
  )
}
