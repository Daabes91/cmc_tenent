"use client";

import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";

type Status = "loading" | "success" | "error" | "missing";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/public";

export default function AppointmentConfirmPage() {
  const params = useSearchParams();
  const token = params.get("token");
  const [status, setStatus] = useState<Status>(token ? "loading" : "missing");
  const [message, setMessage] = useState<string>("");

  const confirmUrl = useMemo(() => {
    if (!token) return null;
    const base = API_URL.endsWith("/") ? API_URL.slice(0, -1) : API_URL;
    return `${base}/appointments/confirm?token=${encodeURIComponent(token)}`
  }, [token]);

  useEffect(() => {
    if (!confirmUrl) return;

    const confirm = async () => {
      try {
        const res = await fetch(confirmUrl, {
          headers: { Accept: "application/json" }
        });
        if (!res.ok) {
          throw new Error(`Request failed with status ${res.status}`);
        }
        setStatus("success");
        setMessage("Your appointment is confirmed. We look forward to seeing you.");
      } catch (err: any) {
        console.error("Confirmation failed", err);
        setStatus("error");
        setMessage("We could not confirm your appointment. Please contact the clinic.");
      }
    };

    confirm();
  }, [confirmUrl]);

  if (!token) {
    return (
      <Shell tone="error" title="Confirmation link missing" body="Please use the confirmation link from your email." />
    );
  }

  if (status === "loading") {
    return (
      <Shell tone="info" title="Confirming your appointment…" body="Please wait a moment while we confirm your visit." />
    );
  }

  if (status === "success") {
    return (
      <Shell
        tone="success"
        title="Appointment confirmed"
        body={message || "We look forward to seeing you at your visit."}
      />
    );
  }

  return (
    <Shell
      tone="error"
      title="Unable to confirm"
      body={message || "We could not confirm your appointment. Please contact the clinic for assistance."}
    />
  );
}

function Shell({
  title,
  body,
  tone
}: {
  title: string;
  body: string;
  tone: "success" | "error" | "info";
}) {
  const toneStyles = {
    success: {
      bg: "from-emerald-50 to-white",
      badge: "bg-emerald-100 text-emerald-700",
      iconBg: "bg-emerald-100 text-emerald-600"
    },
    error: {
      bg: "from-rose-50 to-white",
      badge: "bg-rose-100 text-rose-700",
      iconBg: "bg-rose-100 text-rose-600"
    },
    info: {
      bg: "from-sky-50 to-white",
      badge: "bg-sky-100 text-sky-700",
      iconBg: "bg-sky-100 text-sky-600"
    }
  }[tone];

  const icon =
    tone === "success" ? "✓" :
    tone === "error" ? "!" : "…";

  return (
    <div className={`min-h-screen flex items-center justify-center px-4 py-12 bg-gradient-to-b ${toneStyles.bg}`}>
      <div className="w-full max-w-2xl overflow-hidden rounded-3xl bg-white shadow-xl border border-slate-200">
        <div className="flex flex-col md:flex-row">
          <div className="flex-1 p-8 md:p-10">
            <div className="flex items-center gap-3 mb-4">
              <span className={`inline-flex h-10 w-10 items-center justify-center rounded-full text-lg font-semibold ${toneStyles.iconBg}`}>
                {icon}
              </span>
              <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold uppercase tracking-wide ${toneStyles.badge}`}>
                Appointment
              </span>
            </div>
            <h1 className="text-3xl font-bold text-slate-900 mb-3">{title}</h1>
            <p className="text-slate-600 leading-relaxed">{body}</p>
          </div>
          <div className="hidden md:block w-full md:w-64 bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white p-8">
            <div className="flex h-full flex-col justify-between">
              <div>
                <p className="text-sm text-slate-300">Your visit matters</p>
                <p className="mt-2 text-lg font-semibold">Thanks for confirming.</p>
              </div>
              <div className="mt-6 space-y-2 text-sm text-slate-200">
                <p className="flex items-center gap-2"><span className="text-slate-400">•</span> Please arrive 10 minutes early.</p>
                <p className="flex items-center gap-2"><span className="text-slate-400">•</span> Bring your ID and insurance.</p>
                <p className="flex items-center gap-2"><span className="text-slate-400">•</span> Contact us if you need to reschedule.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
