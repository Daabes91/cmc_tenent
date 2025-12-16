type LegalSection = {
  title: string;
  body: string;
  items?: string[];
};

type LegalPageProps = {
  title: string;
  intro: string;
  updated: string;
  sections: LegalSection[];
};

export function LegalPage({ title, intro, updated, sections }: LegalPageProps) {
  return (
    <main className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 transition-colors">
      <div className="mx-auto max-w-4xl px-4 md:px-6 lg:px-8 py-16 lg:py-24">
        <p className="text-xs font-semibold uppercase tracking-widest text-blue-600 dark:text-blue-300 mb-3">
          {updated}
        </p>
        <h1 className="text-2xl md:text-3xl font-bold text-slate-900 dark:text-slate-50 leading-tight">
          {title}
        </h1>
        <p className="mt-4 text-lg text-slate-600 dark:text-slate-300">{intro}</p>

        <div className="mt-12 space-y-10">
          {sections.map((section) => (
            <section
              key={section.title}
              className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white/80 dark:bg-slate-900/70 p-6 shadow-sm dark:shadow-blue-900/30 backdrop-blur"
            >
              <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100">
                {section.title}
              </h2>
              <p className="mt-3 text-slate-600 dark:text-slate-300">{section.body}</p>

              {section.items && section.items.length > 0 && (
                <ul className="mt-4 list-disc space-y-2 pl-5 text-slate-600 dark:text-slate-300">
                  {section.items.map((item) => (
                    <li key={item}>{item}</li>
                  ))}
                </ul>
              )}
            </section>
          ))}
        </div>
      </div>
    </main>
  );
}
