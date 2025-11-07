'use client';

import { useState } from 'react';
import { useLocale } from 'next-intl';
import PayPalCheckoutSimple from '@/components/PayPalCheckoutSimple';
import PayPalCheckoutIsolated from '@/components/PayPalCheckoutIsolated';
import PayPalThemeNote from '@/components/PayPalThemeNote';
import { resetPayPalScript } from '@/utils/paypalUtils';

export default function PayPalTestPage() {
  const [showPayPal, setShowPayPal] = useState(false);
  const [useIsolated, setUseIsolated] = useState(true);
  const locale = useLocale();
  const isRTL = locale === 'ar';
  const [testData, setTestData] = useState({
    patientId: 1,
    doctorId: 1,
    serviceId: 1,
    slotId: '2024-01-15T10:00:00',
  });

  const mockProps = {
    amount: 50.00,
    currency: 'USD',
    ...testData,
    onSuccess: (orderID: string) => {
      console.log('Payment successful:', orderID);
      alert('Payment successful! Order ID: ' + orderID);
      setShowPayPal(false);
    },
    onError: (error: string) => {
      console.error('Payment error:', error);
      alert('Payment failed: ' + error);
    },
    onCancel: () => {
      console.log('Payment cancelled');
      alert('Payment cancelled');
      setShowPayPal(false);
    },
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 p-8">
      <div className="max-w-2xl mx-auto space-y-8">
        {/* Header */}
        <div className="text-center space-y-4">
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">
            PayPal Integration Test
          </h1>
          <p className="text-slate-600 dark:text-slate-400">
            Test the PayPal checkout with zoid error fixes
          </p>
        </div>

        {/* Version Toggle */}
        <div className="flex justify-center mb-4">
          <div className="inline-flex rounded-lg border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-800 p-1">
            <button
              onClick={() => setUseIsolated(false)}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${
                !useIsolated
                  ? 'bg-blue-500 text-white shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              Simple Version
            </button>
            <button
              onClick={() => setUseIsolated(true)}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${
                useIsolated
                  ? 'bg-green-500 text-white shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
              }`}
            >
              Isolated Version
            </button>
          </div>
        </div>

        {/* Controls */}
        <div className="flex flex-wrap gap-4 justify-center">
          <button
            onClick={() => setShowPayPal(!showPayPal)}
            className={`px-6 py-3 rounded-lg font-semibold transition-all ${
              showPayPal
                ? 'bg-red-500 hover:bg-red-600 text-white'
                : 'bg-blue-500 hover:bg-blue-600 text-white'
            }`}
          >
            {showPayPal ? 'Hide PayPal' : 'Show PayPal'}
          </button>

          <button
            onClick={() => {
              resetPayPalScript();
              setShowPayPal(false);
              setTimeout(() => setShowPayPal(true), 500);
            }}
            className="px-6 py-3 bg-orange-500 hover:bg-orange-600 text-white rounded-lg font-semibold transition-all"
          >
            Reset & Reload
          </button>

          <button
            onClick={() => {
              setTestData(prev => ({
                ...prev,
                patientId: prev.patientId + 1,
                slotId: new Date().toISOString(),
              }));
            }}
            className="px-6 py-3 bg-green-500 hover:bg-green-600 text-white rounded-lg font-semibold transition-all"
          >
            Change Props
          </button>
        </div>

        {/* Test Info */}
        <div className="bg-slate-100 dark:bg-slate-800 rounded-lg p-4">
          <h3 className="font-semibold mb-2">
            {locale === 'ar' ? 'بيانات الاختبار الحالية:' : 'Current Test Data:'}
          </h3>
          <div className="text-sm mb-2 flex flex-wrap gap-2">
            <span className={`inline-block px-2 py-1 rounded text-xs font-medium ${
              useIsolated 
                ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' 
                : 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
            }`}>
              {useIsolated 
                ? (locale === 'ar' ? 'النسخة المعزولة' : 'Isolated Version')
                : (locale === 'ar' ? 'النسخة البسيطة' : 'Simple Version')
              }
            </span>
            <span className="inline-block px-2 py-1 rounded text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200">
              {locale === 'ar' ? `العربية (${locale.toUpperCase()})` : `English (${locale.toUpperCase()})`}
            </span>
            <span className="inline-block px-2 py-1 rounded text-xs font-medium bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200">
              {isRTL ? 'RTL' : 'LTR'}
            </span>
          </div>
          <pre className="text-sm text-slate-600 dark:text-slate-400">
            {JSON.stringify({...testData, locale, isRTL}, null, 2)}
          </pre>
        </div>

        {/* Theme Information */}
        <PayPalThemeNote />

        {/* PayPal Component */}
        {showPayPal && (
          <div className="border-2 border-dashed border-slate-300 dark:border-slate-600 rounded-lg p-4">
            <h3 className="text-lg font-semibold mb-4 text-center">
              PayPal Checkout Test - {useIsolated ? 'Isolated' : 'Simple'} Version
            </h3>
            {useIsolated ? (
              <PayPalCheckoutIsolated {...mockProps} />
            ) : (
              <PayPalCheckoutSimple {...mockProps} />
            )}
          </div>
        )}

        {/* Instructions */}
        <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
          <h3 className="font-semibold text-blue-900 dark:text-blue-100 mb-2">Test Instructions:</h3>
          <ul className="text-sm text-blue-800 dark:text-blue-200 space-y-1">
            <li>• <strong>{locale === 'ar' ? 'النسخة المعزولة' : 'Isolated Version'}</strong>: {locale === 'ar' ? 'تستخدم نمط singleton لمنع أخطاء zoid' : 'Uses singleton pattern to prevent zoid errors'}</li>
            <li>• <strong>{locale === 'ar' ? 'النسخة البسيطة' : 'Simple Version'}</strong>: {locale === 'ar' ? 'تنفيذ أساسي للمقارنة' : 'Basic implementation for comparison'}</li>
            <li>• <strong>{locale === 'ar' ? 'دعم اللغة العربية' : 'Arabic Language Support'}</strong>: {locale === 'ar' ? 'PayPal يتكيف مع اللغة المحددة' : 'PayPal adapts to selected language'}</li>
            <li>• <strong>{locale === 'ar' ? 'دعم RTL' : 'RTL Support'}</strong>: {locale === 'ar' ? 'تخطيط من اليمين إلى اليسار للعربية' : 'Right-to-left layout for Arabic'}</li>
            <li>• {locale === 'ar' ? 'انقر "إظهار PayPal" لتهيئة المكون' : 'Click "Show PayPal" to initialize the component'}</li>
            <li>• {locale === 'ar' ? 'استخدم "إعادة تعيين وإعادة تحميل" إذا واجهت أخطاء zoid' : 'Use "Reset & Reload" if you encounter zoid errors'}</li>
            <li>• {locale === 'ar' ? 'تحقق من وحدة تحكم المتصفح للسجلات التفصيلية' : 'Check browser console for detailed logs and instance tracking'}</li>
          </ul>
        </div>
      </div>
    </div>
  );
}