import { useEffect, useState } from "react";
import { getMonthlySummary } from "../api/summary";
import { extractErrorMessage } from "../api/errors";
import { MonthPicker } from "../components/MonthPicker";
import type { MonthlySummaryResponse } from "../types/api";

const numberFormat = new Intl.NumberFormat(undefined, {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

function currentMonth(): string {
  const today = new Date();
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}`;
}

// Each result remembers which month it was requested for, so loading is simply
// "the latest result isn't for the selected month yet".
type SummaryResult =
  | { month: string; summary: MonthlySummaryResponse; error: null }
  | { month: string; summary: null; error: string };

export function DashboardPage() {
  const [month, setMonth] = useState(currentMonth);
  const [result, setResult] = useState<SummaryResult | null>(null);

  useEffect(() => {
    let ignore = false;

    getMonthlySummary(month)
      .then((summary) => {
        if (!ignore) setResult({ month, summary, error: null });
      })
      .catch((err) => {
        if (!ignore) setResult({ month, summary: null, error: extractErrorMessage(err) });
      });

    return () => {
      ignore = true;
    };
  }, [month]);

  return (
    <div>
      <div className="mb-8 flex items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold text-slate-900">Dashboard</h1>
        <MonthPicker value={month} onChange={setMonth} />
      </div>

      {result?.month !== month ? (
        <p className="text-sm text-slate-500">Loading your summary…</p>
      ) : result.error !== null ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {result.error}
        </div>
      ) : (
        <SummaryView summary={result.summary} />
      )}
    </div>
  );
}

function SummaryView({ summary }: { summary: MonthlySummaryResponse }) {
  return (
    <>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Income" value={summary.totalIncome} tone="positive" />
        <StatCard label="Expenses" value={summary.totalExpenses} tone="negative" />
        <StatCard label="Net" value={summary.net} tone={summary.net >= 0 ? "positive" : "negative"} />
      </div>

      <div className="mt-8">
        <h2 className="text-base font-semibold text-slate-900">Spending by category</h2>
        {summary.byCategory.length === 0 ? (
          <p className="mt-3 text-sm text-slate-500">
            No transactions this month. Once you log some, category spending will show up
            here.
          </p>
        ) : (
          <div className="mt-3 space-y-3">
            {summary.byCategory.map((category) => {
              const hasBudget = category.budgetLimit !== null && category.budgetLimit > 0;
              const percentage = hasBudget
                ? Math.min(100, (category.spent / category.budgetLimit!) * 100)
                : null;
              const isOverBudget = hasBudget && category.spent > category.budgetLimit!;

              return (
                <div
                  key={category.categoryId}
                  className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-100"
                >
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-medium text-slate-900">{category.categoryName}</span>
                    <span className={isOverBudget ? "text-red-600" : "text-slate-600"}>
                      {numberFormat.format(category.spent)}
                      {hasBudget && ` / ${numberFormat.format(category.budgetLimit!)}`}
                    </span>
                  </div>
                  {hasBudget && (
                    <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-slate-100">
                      <div
                        className={`h-full rounded-full ${isOverBudget ? "bg-red-500" : "bg-brand-500"}`}
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </>
  );
}

function StatCard({
  label,
  value,
  tone,
}: {
  label: string;
  value: number;
  tone: "positive" | "negative";
}) {
  return (
    <div className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-1 text-2xl font-semibold ${tone === "positive" ? "text-emerald-600" : "text-red-600"}`}>
        {numberFormat.format(value)}
      </p>
    </div>
  );
}
