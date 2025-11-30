'use client';

/**
 * Analytics Usage Examples
 * Demonstrates how to use analytics tracking in components
 */

import { trackCTAClick, trackFormSubmission, trackFeatureInteraction } from '@/lib/analytics';
import { useAnalytics } from '@/hooks/use-analytics';

/**
 * Example 1: CTA Button with Direct Tracking
 */
export function CTAButtonExample() {
  const handleClick = () => {
    trackCTAClick(
      'Get Started',      // CTA text
      'hero_section'      // Location on page
    );
  };

  return (
    <button 
      onClick={handleClick}
      className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700"
    >
      Get Started
    </button>
  );
}

/**
 * Example 2: CTA Button with Hook
 */
export function CTAButtonWithHook() {
  const { trackCTA } = useAnalytics();

  return (
    <button 
      onClick={() => trackCTA('Request Demo', 'pricing_section')}
      className="bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700"
    >
      Request Demo
    </button>
  );
}

/**
 * Example 3: Form with Tracking
 */
export function ContactFormExample() {
  const { trackForm } = useAnalytics();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    
    // Your form submission logic here
    const formData = new FormData(e.currentTarget);
    
    try {
      // Submit to API
      await fetch('/api/contact', {
        method: 'POST',
        body: formData,
      });
      
      // Track successful submission
      trackForm('contact_form', 'contact');
      
      alert('Form submitted successfully!');
    } catch (error) {
      console.error('Form submission failed:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <input
        type="text"
        name="name"
        placeholder="Your Name"
        className="w-full px-4 py-2 border rounded"
        required
      />
      <input
        type="email"
        name="email"
        placeholder="Your Email"
        className="w-full px-4 py-2 border rounded"
        required
      />
      <textarea
        name="message"
        placeholder="Your Message"
        className="w-full px-4 py-2 border rounded"
        rows={4}
        required
      />
      <button
        type="submit"
        className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700"
      >
        Send Message
      </button>
    </form>
  );
}

/**
 * Example 4: Feature Card with Interaction Tracking
 */
export function FeatureCardExample() {
  const { trackFeature } = useAnalytics();

  const handleClick = () => {
    trackFeature('appointment_scheduler', 'click');
  };

  const handleHover = () => {
    trackFeature('appointment_scheduler', 'hover');
  };

  return (
    <div
      onClick={handleClick}
      onMouseEnter={handleHover}
      className="p-6 border rounded-lg cursor-pointer hover:shadow-lg transition-shadow"
    >
      <h3 className="text-xl font-bold mb-2">Appointment Scheduler</h3>
      <p className="text-gray-600">
        Manage appointments with ease and reduce no-shows
      </p>
    </div>
  );
}

/**
 * Example 5: Multiple CTAs with Different Tracking
 */
export function HeroSectionExample() {
  return (
    <div className="text-center space-y-4">
      <h1 className="text-4xl font-bold">Transform Your Clinic Operations</h1>
      <p className="text-xl text-gray-600">
        Streamline appointments, billing, and patient management
      </p>
      
      <div className="flex gap-4 justify-center">
        <button
          onClick={() => trackCTAClick('Start Free Trial', 'hero')}
          className="bg-blue-600 text-white px-8 py-3 rounded-lg hover:bg-blue-700"
        >
          Start Free Trial
        </button>
        
        <button
          onClick={() => trackCTAClick('Schedule Demo', 'hero')}
          className="bg-white text-blue-600 border-2 border-blue-600 px-8 py-3 rounded-lg hover:bg-blue-50"
        >
          Schedule Demo
        </button>
      </div>
    </div>
  );
}

/**
 * Example 6: Newsletter Signup with Tracking
 */
export function NewsletterSignupExample() {
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    
    const formData = new FormData(e.currentTarget);
    const email = formData.get('email');
    
    try {
      await fetch('/api/newsletter', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      });
      
      trackFormSubmission('newsletter_signup', 'newsletter');
      
      alert('Successfully subscribed!');
    } catch (error) {
      console.error('Subscription failed:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex gap-2">
      <input
        type="email"
        name="email"
        placeholder="Enter your email"
        className="flex-1 px-4 py-2 border rounded"
        required
      />
      <button
        type="submit"
        className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
      >
        Subscribe
      </button>
    </form>
  );
}
