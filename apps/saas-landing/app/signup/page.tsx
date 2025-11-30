import { SignupForm } from '@/components/SignupForm';
import Header from '@/components/Header';

export const metadata = {
  title: 'Sign Up - Start Your Free Trial',
  description: 'Create your clinic portal in minutes. Start your free trial today.',
};

export default function SignupPage() {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-gray-950">
      <Header />
      <main className="flex-1 flex items-center justify-center py-12 px-4">
        <SignupForm />
      </main>
    </div>
  );
}
