import { ArrowLeftRight, LineChart, Target, Wallet } from "lucide-react";
import { Link } from "react-router-dom";

const FEATURES = [
  {
    icon: ArrowLeftRight,
    title: "Track every transaction",
    description: "Log income and expenses as they happen and organize them by category.",
  },
  {
    icon: Wallet,
    title: "Budget by category",
    description: "Set a spending limit per category and see exactly where you stand each month.",
  },
  {
    icon: Target,
    title: "Hit your financial goals",
    description: "Set savings targets and track your contributions until you get there.",
  },
  {
    icon: LineChart,
    title: "Monthly insights",
    description: "A clear breakdown of income, expenses, and net for the month, at a glance.",
  },
];

export function LandingPage() {
  return (
    <div className="min-h-screen bg-white">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-6 py-6">
        <div className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-500 text-sm font-bold text-white">
            B
          </div>
          <span className="text-lg font-semibold text-slate-900">BudgetManager</span>
        </div>
        <nav className="flex items-center gap-6">
          <Link to="/login" className="text-sm font-medium text-slate-600 hover:text-slate-900">
            Log in
          </Link>
          <Link
            to="/register"
            className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
          >
            Sign up
          </Link>
        </nav>
      </header>

      <section className="mx-auto max-w-4xl px-6 pb-20 pt-16 text-center">
        <h1 className="text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">
          Take control of your money
        </h1>
        <p className="mx-auto mt-4 max-w-2xl text-lg text-slate-600">
          Track spending, manage budgets, and work toward your financial goals all in one
          place. Built for individuals who want a clear picture of where their
          money goes.
        </p>
        <div className="mt-8 flex items-center justify-center gap-4">
          <Link
            to="/register"
            className="rounded-lg bg-brand-500 px-6 py-3 text-sm font-semibold text-white shadow-sm hover:bg-brand-600"
          >
            Get started free
          </Link>
          <Link
            to="/login"
            className="rounded-lg border border-slate-300 px-6 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            I already have an account
          </Link>
        </div>
      </section>

      <section className="border-t border-slate-100 bg-slate-50 py-16">
        <div className="mx-auto grid max-w-6xl grid-cols-1 gap-8 px-6 sm:grid-cols-2 lg:grid-cols-4">
          {FEATURES.map(({ icon: Icon, title, description }) => (
            <div key={title} className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-100">
              <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-lg bg-brand-50 text-brand-600">
                <Icon size={20} />
              </div>
              <h3 className="text-base font-semibold text-slate-900">{title}</h3>
              <p className="mt-2 text-sm text-slate-600">{description}</p>
            </div>
          ))}
        </div>
      </section>

    </div>
  );
}
