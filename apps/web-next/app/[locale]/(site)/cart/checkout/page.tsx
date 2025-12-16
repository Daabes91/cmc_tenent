'use client';

import {useEffect, useMemo, useState, type FormEvent} from 'react';
import {api} from '@/lib/api';
import type {CartResponse} from '@/lib/types';
import {Link} from '@/navigation';
import {useParams, useRouter} from 'next/navigation';
import {useAuth} from '@/hooks/useAuth';
import dynamic from 'next/dynamic';

const PayPalCheckoutIsolated = dynamic(() => import('@/components/PayPalCheckoutIsolated'), {
  ssr: false,
});

type FormState = {
  address1: string;
  address2: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  notes: string;
};

const paymentOptions = [
  {
    id: 'cod',
    label: 'COD (Cash on Delivery)',
    description: 'Hand over cash when your order arrives.',
  },
  {
    id: 'bank',
    label: 'Bank Transfer',
    description: 'Transfer to our bank account; we will confirm before shipping.',
  },
  {
    id: 'online',
    label: 'Online Payment (PayPal)',
    description: 'Pay securely with PayPal / cards.',
  },
];

export default function CheckoutPage() {
  const {locale} = useParams<{locale: string}>();
  const router = useRouter();
  const {user} = useAuth();

  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<string>('cod');
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [orderNumber, setOrderNumber] = useState<string | number | null>(null);
  const [orderId, setOrderId] = useState<number | null>(null);
  const [ready, setReady] = useState(false);

  const [form, setForm] = useState<FormState>({
    address1: '',
    address2: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'US',
    notes: '',
  });

  useEffect(() => {
    const load = async () => {
      try {
        const data = await api.getCart();
        setCart(data);
        if ((data as any)?.session_id) {
          setSessionId((data as any).session_id);
        }
      } catch (err: any) {
        console.error('Failed to load cart for checkout', err);
        setError(err?.message || 'Failed to load cart');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const formatMoney = (amount?: CartResponse['total_amount']) => {
    if (!amount) return '—';
    return amount.formatted || (amount.amount != null && amount.currency ? `${amount.amount} ${amount.currency}` : '—');
  };

  const paymentNote = useMemo(() => {
    switch (paymentMethod) {
      case 'cod':
        return 'Pay with cash upon delivery.';
      case 'bank':
        return 'After placing the order, we will send you bank transfer instructions.';
      case 'online':
        return 'After placing the order, continue to the online PayPal step.';
      default:
        return '';
    }
  }, [paymentMethod]);

  const handleChange = (field: keyof FormState, value: string) => {
    setForm((prev) => ({...prev, [field]: value}));
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!cart?.items?.length || !sessionId) {
      setError('Your cart is empty.');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const orderResp = await api.createOrderFromCart({
        sessionId,
        customerName: user?.name || 'Customer',
        customerEmail: user?.email || 'unknown@example.com',
        customerPhone: user?.phone || undefined,
        billingAddressLine1: form.address1 || 'N/A',
        billingAddressLine2: form.address2 || undefined,
        billingAddressCity: form.city || 'N/A',
        billingAddressState: form.state || undefined,
        billingAddressPostalCode: form.postalCode || '00000',
        billingAddressCountry: form.country || 'US',
        notes: `${paymentMethod.toUpperCase()}${form.notes ? ` | Notes: ${form.notes}` : ''}`,
      });

      const oid = (orderResp as any)?.order?.id;
      const orderNum = (orderResp as any)?.order?.orderNumber || (orderResp as any)?.order?.order_number || oid;
      if (!oid) {
        throw new Error(orderResp?.message || 'Unable to create order from cart');
      }
      setOrderId(oid);

      if (paymentMethod === 'online') {
        // Let PayPal flow handle completion.
        return;
      }

      setOrderNumber(orderNum ?? oid);
      setSuccess(true);
      setCart(null);
    } catch (err: any) {
      console.error('Checkout failed', err);
      setError(err?.message || 'Failed to submit your order');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 md:py-16">
      <div className="mb-6 flex items-center justify-between gap-4">
        <div>
          <p className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">Step 1</p>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">Checkout</h1>
        </div>
        <Link
          href={`/${locale || 'en'}/cart`}
          className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-blue-400 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-400 dark:hover:text-blue-200"
        >
          Back to cart
        </Link>
      </div>

      {loading && (
        <div className="rounded-xl border border-slate-200 bg-white p-6 text-slate-600 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
          Loading checkout…
        </div>
      )}

      {error && !loading && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-6 text-rose-700 dark:border-rose-900 dark:bg-rose-950 dark:text-rose-100">
          {error}
        </div>
      )}

      {!loading && !error && (!cart || !cart.items || cart.items.length === 0) && !success && (
        <div className="rounded-xl border border-slate-200 bg-white p-6 text-slate-600 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300">
          Your cart is empty. <Link href={`/${locale || 'en'}/cart`} className="text-blue-600 underline">Return to cart</Link>
        </div>
      )}

      {success && (
        <div className="mb-6 rounded-xl border border-emerald-200 bg-emerald-50 p-6 text-emerald-800 dark:border-emerald-900 dark:bg-emerald-950 dark:text-emerald-100">
          <h2 className="text-xl font-semibold">Congrats! Your order was submitted successfully.</h2>
          {orderNumber && <p className="mt-2 text-sm">Order reference: <span className="font-semibold">{orderNumber}</span></p>}
          <p className="mt-2 text-sm">{paymentNote}</p>
          <div className="mt-4 flex gap-3">
            <button
              onClick={() => router.push(`/${locale || 'en'}`)}
              className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700"
            >
              Continue shopping
            </button>
            <Link
              href={`/${locale || 'en'}/cart`}
              className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-blue-400 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-400 dark:hover:text-blue-200"
            >
              View cart
            </Link>
          </div>
        </div>
      )}

      {!loading && !error && cart && cart.items && cart.items.length > 0 && !success && (
        <div className="grid gap-6 lg:grid-cols-[2fr,1fr]">
          <form onSubmit={handleSubmit} className="space-y-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <div>
              <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Shipping address</h2>
              <p className="text-sm text-slate-500 dark:text-slate-400">Only address details are needed. Payment happens next.</p>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">Country</label>
                <input
                  value={form.country}
                  onChange={(e) => handleChange('country', e.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                />
              </div>
              <div className="space-y-2 md:col-span-2">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">Address line 1</label>
                <input
                  value={form.address1}
                  onChange={(e) => handleChange('address1', e.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                  required
                />
              </div>
              <div className="space-y-2 md:col-span-2">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">Address line 2 (optional)</label>
                <input
                  value={form.address2}
                  onChange={(e) => handleChange('address2', e.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                  placeholder="Apartment, suite, etc."
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">City</label>
                <input
                  value={form.city}
                  onChange={(e) => handleChange('city', e.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                  required
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">State/Province</label>
                <input
                  value={form.state}
                  onChange={(e) => handleChange('state', e.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">Postal code</label>
                <input
                  value={form.postalCode}
                  onChange={(e) => handleChange('postalCode', e.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                />
              </div>
            </div>

            <div className="space-y-3 rounded-lg border border-slate-200 bg-slate-50 p-4 dark:border-slate-800 dark:bg-slate-800/40">
              <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Payment method</h3>
              <div className="grid gap-3 md:grid-cols-3">
                {paymentOptions.map((option) => (
                  <label
                    key={option.id}
                    className={`cursor-pointer rounded-lg border p-3 text-sm transition ${
                      paymentMethod === option.id
                        ? 'border-blue-500 bg-blue-50 text-blue-900 dark:border-blue-400 dark:bg-blue-900/30 dark:text-blue-100'
                        : 'border-slate-200 bg-white text-slate-700 hover:border-blue-200 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200'
                    }`}
                  >
                    <input
                      type="radio"
                      name="payment-method"
                      value={option.id}
                      checked={paymentMethod === option.id}
                      onChange={() => setPaymentMethod(option.id)}
                      className="mr-2"
                    />
                    <span className="font-semibold">{option.label}</span>
                    <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">{option.description}</p>
                  </label>
                ))}
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-700 dark:text-slate-200">Notes (optional)</label>
              <textarea
                value={form.notes}
                onChange={(e) => handleChange('notes', e.target.value)}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
                rows={3}
                placeholder="Any delivery notes?"
              />
            </div>

            <div className="flex items-center justify-between border-t border-slate-200 pt-4 dark:border-slate-800">
              <div>
                <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">Total</p>
                <p className="text-sm text-slate-500 dark:text-slate-400">Includes all items in your cart.</p>
              </div>
              <p className="text-xl font-bold text-slate-900 dark:text-slate-100">{formatMoney(cart.total_amount)}</p>
            </div>

            <div className="space-y-3">
              {paymentMethod === 'online' && orderId && (
                <div className="rounded-lg border border-slate-200 p-4 dark:border-slate-800">
                  <p className="mb-2 text-sm font-semibold text-slate-900 dark:text-slate-100">Pay online</p>
                  <PayPalCheckoutIsolated
                    amount={cart.total_amount?.amount ?? undefined}
                    currency={cart.total_amount?.currency ?? undefined}
                    orderId={orderId ?? undefined}
                    returnUrl={`${typeof window !== 'undefined' ? window.location.origin : ''}/${locale || 'en'}/cart/paypal/return?orderId=${orderId}`}
                    cancelUrl={`${typeof window !== 'undefined' ? window.location.origin : ''}/${locale || 'en'}/cart?cancelled=true`}
                    onSuccess={() => {
                      setError(null);
                      setCart(null);
                      setSuccess(true);
                      setOrderNumber(orderId);
                    }}
                    onError={(msg) => setError(msg || 'Payment failed')}
                    onCancel={() => setError('Payment cancelled')}
                    onReady={() => setReady(true)}
                  />
                  {!ready && <p className="text-xs text-slate-500 dark:text-slate-400">Loading PayPal…</p>}
                </div>
              )}

              <div className="flex items-center justify-between text-sm text-slate-600 dark:text-slate-300">
                <span>{paymentNote}</span>
              </div>

              {paymentMethod !== 'online' && (
                <button
                  type="submit"
                  disabled={submitting}
                  className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60"
                >
                  {submitting ? 'Submitting…' : 'Place order'}
                </button>
              )}

              {paymentMethod === 'online' && !orderId && (
                <button
                  type="submit"
                  disabled={submitting}
                  className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-5 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:opacity-60"
                >
                  {submitting ? 'Preparing…' : 'Continue to online payment'}
                </button>
              )}
            </div>
          </form>

          <div className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100">Order summary</h2>
            <div className="divide-y divide-slate-200 text-sm dark:divide-slate-800">
              {cart.items.map((item) => (
                <div key={item.id} className="flex items-center justify-between py-3">
                  <div>
                    <p className="font-semibold text-slate-900 dark:text-slate-100">{item.product_name}</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      Qty: {item.quantity} • {formatMoney(item.unit_amount)}
                    </p>
                  </div>
                  <p className="font-semibold text-slate-900 dark:text-slate-100">{formatMoney(item.total_amount)}</p>
                </div>
              ))}
              <div className="flex items-center justify-between py-3 font-semibold text-slate-900 dark:text-slate-100">
                <span>Total</span>
                <span>{formatMoney(cart.total_amount)}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
