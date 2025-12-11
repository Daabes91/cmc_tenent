import { Metadata } from "next";
import Link from "next/link";
import Header from "@/components/Header";
import Footer from "@/components/footer";

export const metadata: Metadata = {
  title: "Cookie Policy | CMC Platform",
  description: "Learn about how CMC Platform uses cookies and similar tracking technologies.",
};

export default function CookiePolicy() {
  return (
    <div className="flex flex-col min-h-screen bg-slate-50 dark:bg-gray-950">
      <Header />
      <main className="flex-1 container mx-auto px-4 md:px-8 py-16 max-w-4xl">
        <div className="bg-white dark:bg-gray-900 rounded-lg shadow-sm p-8 md:p-12">
          <h1 className="text-4xl font-bold text-slate-900 dark:text-white mb-4">
            Cookie Policy
          </h1>
          <p className="text-slate-600 dark:text-gray-400 mb-8">
            Last updated: {new Date().toLocaleDateString("en-US", { year: "numeric", month: "long", day: "numeric" })}
          </p>

          <div className="prose prose-slate dark:prose-invert max-w-none">
            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                1. What Are Cookies?
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Cookies are small text files that are placed on your device when you visit our website. They help us provide you with a better experience by remembering your preferences and understanding how you use our services.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                2. Types of Cookies We Use
              </h2>
              
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                2.1 Essential Cookies
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                These cookies are necessary for the website to function properly. They enable core functionality such as:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>User authentication and security</li>
                <li>Session management</li>
                <li>Load balancing</li>
                <li>Form submission</li>
              </ul>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                2.2 Performance Cookies
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                These cookies help us understand how visitors interact with our website by collecting anonymous information:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Pages visited and time spent</li>
                <li>Error messages encountered</li>
                <li>Loading times and performance metrics</li>
                <li>User navigation patterns</li>
              </ul>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                2.3 Functional Cookies
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                These cookies enable enhanced functionality and personalization:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Language preferences</li>
                <li>Theme settings (light/dark mode)</li>
                <li>User interface customizations</li>
                <li>Remember me functionality</li>
              </ul>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                2.4 Analytics Cookies
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We use analytics services like Google Analytics to understand usage patterns:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Visitor demographics and interests</li>
                <li>Traffic sources and referrals</li>
                <li>Feature usage and engagement</li>
                <li>Conversion tracking</li>
              </ul>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                2.5 Marketing Cookies
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                These cookies track your activity to deliver relevant advertisements:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Retargeting and remarketing</li>
                <li>Ad performance measurement</li>
                <li>Social media integration</li>
                <li>Third-party advertising networks</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                3. Third-Party Cookies
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We use services from trusted third parties that may set cookies on your device:
              </p>
              <div className="bg-slate-50 dark:bg-gray-800 p-4 rounded-lg mb-4">
                <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 space-y-2">
                  <li><strong>Google Analytics:</strong> Website analytics and reporting</li>
                  <li><strong>PayPal:</strong> Payment processing and fraud prevention</li>
                  <li><strong>Stripe:</strong> Payment processing and security</li>
                  <li><strong>Cloudflare:</strong> Security and performance optimization</li>
                  <li><strong>Social Media Platforms:</strong> Social sharing and login features</li>
                </ul>
              </div>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                4. Cookie Duration
              </h2>
              
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                Session Cookies
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                These temporary cookies are deleted when you close your browser. They are used for essential functions like maintaining your login session.
              </p>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                Persistent Cookies
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                These cookies remain on your device for a set period or until you delete them. They remember your preferences across sessions.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                5. Managing Your Cookie Preferences
              </h2>
              
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                Browser Settings
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Most browsers allow you to control cookies through their settings:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li><strong>Chrome:</strong> Settings → Privacy and security → Cookies</li>
                <li><strong>Firefox:</strong> Options → Privacy & Security → Cookies</li>
                <li><strong>Safari:</strong> Preferences → Privacy → Cookies</li>
                <li><strong>Edge:</strong> Settings → Privacy → Cookies</li>
              </ul>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                Opt-Out Tools
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                You can opt out of specific tracking:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Google Analytics: <a href="https://tools.google.com/dlpage/gaoptout" className="text-primary hover:underline" target="_blank" rel="noopener noreferrer">Browser Add-on</a></li>
                <li>Network Advertising Initiative: <a href="http://optout.networkadvertising.org/" className="text-primary hover:underline" target="_blank" rel="noopener noreferrer">NAI Opt-Out</a></li>
                <li>Digital Advertising Alliance: <a href="http://optout.aboutads.info/" className="text-primary hover:underline" target="_blank" rel="noopener noreferrer">DAA Opt-Out</a></li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                6. Impact of Disabling Cookies
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                If you disable cookies, some features of our website may not function properly:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>You may not be able to log in or stay logged in</li>
                <li>Your preferences and settings will not be saved</li>
                <li>Some interactive features may not work</li>
                <li>We won't be able to personalize your experience</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                7. Do Not Track Signals
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Some browsers include a "Do Not Track" (DNT) feature. Currently, there is no industry standard for how to respond to DNT signals. We do not currently respond to DNT signals, but we respect your privacy choices and provide cookie management options.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                8. Updates to This Cookie Policy
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We may update this Cookie Policy from time to time to reflect changes in our practices or for legal, operational, or regulatory reasons. We will notify you of any material changes by posting the updated policy on this page.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                9. Contact Us
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                If you have questions about our use of cookies, please contact us:
              </p>
              <div className="bg-slate-50 dark:bg-gray-800 p-4 rounded-lg">
                <p className="text-slate-700 dark:text-gray-300">
                  <strong>Email:</strong> privacy@cmcplatform.com<br />
                  <strong>Address:</strong> CMC Platform Privacy Team<br />
                  [Your Business Address]
                </p>
              </div>
            </section>
          </div>

          <div className="mt-12 pt-8 border-t border-slate-200 dark:border-gray-700">
            <Link
              href="/"
              className="text-primary hover:text-primary/80 font-medium inline-flex items-center"
            >
              ← Back to Home
            </Link>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
}
