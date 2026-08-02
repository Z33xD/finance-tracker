export default function AuthLayout({ title, subtitle, children }) {
    return (
        <div className="flex min-h-screen bg-slate-50">
            {/* Brand panel — desktop only */}
            <aside className="relative hidden w-1/2 overflow-hidden bg-brand-950 lg:flex lg:flex-col lg:justify-between lg:p-12">
                <div aria-hidden="true" className="pointer-events-none absolute -top-24 -right-24 h-96 w-96 rounded-full bg-brand-500/20 blur-3xl" />
                <div aria-hidden="true" className="pointer-events-none absolute -bottom-32 -left-24 h-80 w-80 rounded-full bg-brand-400/10 blur-3xl" />

                <div className="relative flex items-center gap-3">
                    <span className="font-display text-5xl font-semibold tracking-tight text-white">
                        Finance Tracker
                    </span>
                </div>

                <div className="relative max-w-md">
                    <h1 className="font-display text-4xl leading-tight text-white">
                        Your money,<br />in its finest form.
                    </h1>
                    <p className="mt-4 leading-relaxed text-brand-200/80">
                        Track accounts, budgets, and transactions across currencies, with clarity and confidence.
                    </p>
                    <ul className="mt-8 space-y-4 text-sm text-brand-100/90">
                        <li className="flex items-center gap-3">
                            <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-500/20 ring-1 ring-inset ring-brand-400/40">
                                <span className="h-2 w-2 rounded-full bg-brand-300" />
                            </span>
                            Multi-currency accounts with live exchange rates
                        </li>
                        <li className="flex items-center gap-3">
                            <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-500/20 ring-1 ring-inset ring-brand-400/40">
                                <span className="h-2 w-2 rounded-full bg-brand-300" />
                            </span>
                            Smart budgets against real spending
                        </li>
                        <li className="flex items-center gap-3">
                            <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-brand-500/20 ring-1 ring-inset ring-brand-400/40">
                                <span className="h-2 w-2 rounded-full bg-brand-300" />
                            </span>
                            Secure, JWT-protected ledger
                        </li>
                    </ul>
                </div>

                <p className="relative text-xs text-brand-300/60"></p>
            </aside>

            {/* Form panel */}
            <main className="flex w-full items-center justify-center px-6 py-12 lg:w-1/2">
                <div className="w-full max-w-sm">
                    {/* Mobile brand mark */}
                    <div className="mb-8 flex items-center justify-center gap-2 lg:hidden">
                        <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600">
                            <span className="h-2.5 w-2.5 rounded-full border-2 border-white" />
                        </span>
                        <span className="font-display text-lg font-semibold tracking-tight text-slate-900">
                            Finance Tracker
                        </span>
                    </div>

                    <div className="card p-8">
                        <h1 className="text-center font-display text-2xl text-slate-900">{title}</h1>
                        {subtitle && (
                            <p className="mb-6 text-center text-sm text-slate-500">{subtitle}</p>
                        )}
                        {children}
                    </div>
                </div>
            </main>
        </div>
    );
}
