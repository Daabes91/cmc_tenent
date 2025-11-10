import Link from 'next/link';

export default function NoTenantPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-900 px-4">
      <div className="max-w-md w-full px-6 py-8 bg-white dark:bg-slate-800 shadow-lg rounded-lg text-center border border-slate-200 dark:border-slate-700">
        <div className="mb-6">
          <svg
            className="mx-auto h-16 w-16 text-red-500 dark:text-red-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>
        
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100 mb-4">
          Tenant Not Found
        </h1>
        
        <p className="text-slate-600 dark:text-slate-300 mb-6">
          The clinic or organization you're trying to access could not be found or is currently inactive.
        </p>
        
        <div className="space-y-3">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Please check the URL and try again, or contact support if you believe this is an error.
          </p>
          
          <div className="pt-4">
            <Link
              href="/"
              className="inline-block px-6 py-3 bg-blue-600 dark:bg-blue-500 text-white font-medium rounded-lg hover:bg-blue-700 dark:hover:bg-blue-600 transition-colors shadow-sm"
            >
              Go to Home
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
