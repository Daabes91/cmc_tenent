import { Metadata } from "next";
import Link from "next/link";
import Header from "@/components/Header";
import Footer from "@/components/footer";

export const metadata: Metadata = {
  title: "Privacy Policy | CMC Platform",
  description: "Learn how CMC Platform collects, uses, and protects your personal information.",
};

export default function PrivacyPolicy() {
  return (
    <div className="flex flex-col min-h-screen bg-slate-50 dark:bg-gray-950">
      <Header />
      <main className="flex-1 container mx-auto px-4 md:px-8 py-16 max-w-4xl">
        <div className="bg-white dark:bg-gray-900 rounded-lg shadow-sm p-8 md:p-12">
          <h1 className="text-4xl font-bold text-slate-900 dark:text-white mb-4">
            Privacy Policy
          </h1>
          <p className="text-slate-600 dark:text-gray-400 mb-8">
            Last updated: {new Date().toLocaleDateString("en-US", { year: "numeric", month: "long", day: "numeric" })}
          </p>

          <div className="prose prose-slate dark:prose-invert max-w-none">
            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                1. Introduction
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Welcome to CMC Platform ("we," "our," or "us"). We are committed to protecting your personal information and your right to privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our clinic management platform and services.
              </p>
              <p className="text-slate-700 dark:text-gray-300">
                By using our services, you agree to the collection and use of information in accordance with this policy. If you do not agree with our policies and practices, please do not use our services.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                2. Information We Collect
              </h2>
              
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                2.1 Personal Information
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We collect information that you provide directly to us, including:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Name, email address, and contact information</li>
                <li>Clinic or practice information</li>
                <li>Payment and billing information</li>
                <li>Account credentials and authentication data</li>
                <li>Patient health information (PHI) when using our healthcare features</li>
              </ul>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                2.2 Automatically Collected Information
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                When you access our services, we automatically collect:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Device information (IP address, browser type, operating system)</li>
                <li>Usage data (pages visited, features used, time spent)</li>
                <li>Cookies and similar tracking technologies</li>
                <li>Log files and analytics data</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                3. How We Use Your Information
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We use the information we collect to:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Provide, maintain, and improve our services</li>
                <li>Process transactions and send related information</li>
                <li>Send administrative information, updates, and security alerts</li>
                <li>Respond to your comments, questions, and customer service requests</li>
                <li>Monitor and analyze usage patterns and trends</li>
                <li>Detect, prevent, and address technical issues and security threats</li>
                <li>Comply with legal obligations and protect our legal rights</li>
                <li>Facilitate communication between healthcare providers and patients</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                4. HIPAA Compliance
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                As a healthcare technology platform, we are committed to compliance with the Health Insurance Portability and Accountability Act (HIPAA). We implement appropriate administrative, physical, and technical safeguards to protect Protected Health Information (PHI).
              </p>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We enter into Business Associate Agreements (BAAs) with covered entities and ensure that all PHI is:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Encrypted in transit and at rest</li>
                <li>Accessible only to authorized personnel</li>
                <li>Stored in secure, compliant infrastructure</li>
                <li>Subject to regular security audits and assessments</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                5. Information Sharing and Disclosure
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We do not sell your personal information. We may share your information in the following circumstances:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li><strong>With your consent:</strong> When you explicitly authorize us to share information</li>
                <li><strong>Service providers:</strong> With third-party vendors who perform services on our behalf</li>
                <li><strong>Legal requirements:</strong> When required by law or to protect our rights</li>
                <li><strong>Business transfers:</strong> In connection with a merger, acquisition, or sale of assets</li>
                <li><strong>Healthcare coordination:</strong> Between authorized healthcare providers for treatment purposes</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                6. Data Security
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We implement industry-standard security measures to protect your information:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>End-to-end encryption for data transmission</li>
                <li>Encrypted database storage</li>
                <li>Regular security audits and penetration testing</li>
                <li>Multi-factor authentication options</li>
                <li>Role-based access controls</li>
                <li>Regular backups and disaster recovery procedures</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                7. Your Privacy Rights
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Depending on your location, you may have the following rights:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li><strong>Access:</strong> Request a copy of your personal information</li>
                <li><strong>Correction:</strong> Request correction of inaccurate information</li>
                <li><strong>Deletion:</strong> Request deletion of your personal information</li>
                <li><strong>Portability:</strong> Request transfer of your data to another service</li>
                <li><strong>Opt-out:</strong> Opt out of marketing communications</li>
                <li><strong>Restriction:</strong> Request restriction of processing</li>
              </ul>
              <p className="text-slate-700 dark:text-gray-300 mt-4">
                To exercise these rights, please contact us at privacy@cmcplatform.com
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                8. Data Retention
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We retain your information for as long as necessary to provide our services and comply with legal obligations. Healthcare records are retained in accordance with applicable medical record retention laws and regulations.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                9. International Data Transfers
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Your information may be transferred to and processed in countries other than your country of residence. We ensure appropriate safeguards are in place to protect your information in accordance with this Privacy Policy.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                10. Children's Privacy
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Our services are not directed to individuals under 18. We do not knowingly collect personal information from children. If you believe we have collected information from a child, please contact us immediately.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                11. Changes to This Privacy Policy
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the "Last updated" date. We encourage you to review this Privacy Policy periodically.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                12. Contact Us
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                If you have questions or concerns about this Privacy Policy, please contact us:
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
