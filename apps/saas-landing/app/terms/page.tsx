import { Metadata } from "next";
import Link from "next/link";
import Header from "@/components/Header";
import Footer from "@/components/footer";

export const metadata: Metadata = {
  title: "Terms of Service | CMC Platform",
  description: "Read the terms and conditions for using CMC Platform's clinic management services.",
};

export default function TermsOfService() {
  return (
    <div className="flex flex-col min-h-screen bg-slate-50 dark:bg-gray-950">
      <Header />
      <main className="flex-1 container mx-auto px-4 md:px-8 py-16 max-w-4xl">
        <div className="bg-white dark:bg-gray-900 rounded-lg shadow-sm p-8 md:p-12">
          <h1 className="text-4xl font-bold text-slate-900 dark:text-white mb-4">
            Terms of Service
          </h1>
          <p className="text-slate-600 dark:text-gray-400 mb-8">
            Last updated: {new Date().toLocaleDateString("en-US", { year: "numeric", month: "long", day: "numeric" })}
          </p>

          <div className="prose prose-slate dark:prose-invert max-w-none">
            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                1. Agreement to Terms
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                By accessing or using CMC Platform ("Service," "Platform," "we," "us," or "our"), you agree to be bound by these Terms of Service ("Terms"). If you disagree with any part of these terms, you may not access the Service.
              </p>
              <p className="text-slate-700 dark:text-gray-300">
                These Terms apply to all visitors, users, and others who access or use the Service, including healthcare providers, clinic administrators, and patients.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                2. Description of Service
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                CMC Platform provides a comprehensive clinic management solution that includes:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Multi-tenant clinic websites with custom domains</li>
                <li>Administrative dashboards for clinic management</li>
                <li>Patient portals and appointment booking systems</li>
                <li>Electronic health records (EHR) management</li>
                <li>Billing and payment processing</li>
                <li>Communication tools for patient-provider interaction</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                3. User Accounts
              </h2>
              
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                3.1 Account Creation
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                To use certain features of the Service, you must register for an account. You agree to:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Provide accurate, current, and complete information</li>
                <li>Maintain and promptly update your account information</li>
                <li>Maintain the security of your password and account</li>
                <li>Accept responsibility for all activities under your account</li>
                <li>Notify us immediately of any unauthorized use</li>
              </ul>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                3.2 Account Types
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Different account types have different rights and responsibilities:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li><strong>Clinic Administrators:</strong> Full access to clinic management features</li>
                <li><strong>Healthcare Providers:</strong> Access to patient records and clinical tools</li>
                <li><strong>Staff Members:</strong> Limited access based on assigned roles</li>
                <li><strong>Patients:</strong> Access to personal health information and booking</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                4. Subscription and Payment
              </h2>
              
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                4.1 Subscription Plans
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We offer various subscription plans with different features and pricing. By subscribing, you agree to pay all fees associated with your chosen plan.
              </p>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                4.2 Billing
              </h3>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Subscriptions are billed in advance on a monthly or annual basis</li>
                <li>Payment is due immediately upon subscription or renewal</li>
                <li>We use third-party payment processors (PayPal, Stripe)</li>
                <li>All fees are non-refundable except as required by law</li>
                <li>We reserve the right to change pricing with 30 days' notice</li>
              </ul>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                4.3 Cancellation
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                You may cancel your subscription at any time. Cancellation will take effect at the end of your current billing period. You will retain access to the Service until that date.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                5. Acceptable Use Policy
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                You agree not to:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Violate any laws or regulations</li>
                <li>Infringe on intellectual property rights</li>
                <li>Upload malicious code or viruses</li>
                <li>Attempt to gain unauthorized access to the Service</li>
                <li>Interfere with or disrupt the Service</li>
                <li>Use the Service for any illegal or unauthorized purpose</li>
                <li>Harass, abuse, or harm other users</li>
                <li>Impersonate any person or entity</li>
                <li>Share your account credentials with others</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                6. Healthcare-Specific Terms
              </h2>
              
              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                6.1 HIPAA Compliance
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Healthcare providers using our Service must comply with HIPAA and other applicable healthcare regulations. We provide HIPAA-compliant infrastructure, but you are responsible for your own compliance practices.
              </p>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                6.2 Medical Disclaimer
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                CMC Platform is a management tool and does not provide medical advice. Healthcare providers are solely responsible for all medical decisions and patient care. We are not liable for any medical outcomes.
              </p>

              <h3 className="text-xl font-semibold text-slate-900 dark:text-white mb-3 mt-6">
                6.3 Professional Licensing
              </h3>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Healthcare providers must maintain all required licenses and certifications. You represent that you are legally authorized to provide healthcare services in your jurisdiction.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                7. Intellectual Property
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                The Service and its original content, features, and functionality are owned by CMC Platform and are protected by international copyright, trademark, and other intellectual property laws.
              </p>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                You retain ownership of content you upload to the Service. By uploading content, you grant us a license to use, store, and display that content as necessary to provide the Service.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                8. Data and Privacy
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Your use of the Service is also governed by our Privacy Policy. We collect, use, and protect your data as described in that policy. By using the Service, you consent to our data practices.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                9. Service Availability
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We strive to provide reliable service but do not guarantee uninterrupted access. We may:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Perform scheduled maintenance with advance notice</li>
                <li>Make emergency updates or repairs</li>
                <li>Modify or discontinue features with notice</li>
                <li>Suspend service for violations of these Terms</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                10. Limitation of Liability
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                To the maximum extent permitted by law, CMC Platform shall not be liable for any indirect, incidental, special, consequential, or punitive damages, including loss of profits, data, or other intangible losses resulting from:
              </p>
              <ul className="list-disc pl-6 text-slate-700 dark:text-gray-300 mb-4 space-y-2">
                <li>Your use or inability to use the Service</li>
                <li>Unauthorized access to your data</li>
                <li>Errors or omissions in the Service</li>
                <li>Any third-party content or conduct</li>
              </ul>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                11. Indemnification
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                You agree to indemnify and hold harmless CMC Platform from any claims, damages, losses, liabilities, and expenses arising from your use of the Service or violation of these Terms.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                12. Termination
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We may terminate or suspend your account immediately, without prior notice, for any reason, including breach of these Terms. Upon termination, your right to use the Service will immediately cease.
              </p>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                You may request a data export within 30 days of termination. After this period, we may delete your data in accordance with our data retention policies.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                13. Dispute Resolution
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                Any disputes arising from these Terms or the Service shall be resolved through binding arbitration in accordance with the rules of [Arbitration Organization]. You waive your right to participate in class action lawsuits.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                14. Governing Law
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                These Terms shall be governed by and construed in accordance with the laws of [Your Jurisdiction], without regard to its conflict of law provisions.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                15. Changes to Terms
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                We reserve the right to modify these Terms at any time. We will notify you of any changes by posting the new Terms on this page and updating the "Last updated" date. Your continued use of the Service after changes constitutes acceptance of the new Terms.
              </p>
            </section>

            <section className="mb-8">
              <h2 className="text-2xl font-semibold text-slate-900 dark:text-white mb-4">
                16. Contact Information
              </h2>
              <p className="text-slate-700 dark:text-gray-300 mb-4">
                If you have questions about these Terms, please contact us:
              </p>
              <div className="bg-slate-50 dark:bg-gray-800 p-4 rounded-lg">
                <p className="text-slate-700 dark:text-gray-300">
                  <strong>Email:</strong> legal@cmcplatform.com<br />
                  <strong>Address:</strong> CMC Platform Legal Department<br />
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
