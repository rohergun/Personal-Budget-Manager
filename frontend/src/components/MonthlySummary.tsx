import { useRef, useState } from "react";
import { getMonthlySummary } from "../api/summary";
import { extractErrorMessage } from "../api/errors";
import type { MonthlySummaryResponse } from "../types/api";

const numberFormat = new Intl.NumberFormat(undefined, {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export function MonthlySummary() {
  const [month, setMonth] = useState("");
  const [summary, setSummary] = useState<MonthlySummaryResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // Only the latest request may update state, so a slow earlier month can't overwrite a newer one.
  const latestRequest = useRef(0);

  function handleMonthChange(value: string) {
    setMonth(value);
    if (!value) {
      return;
    }

    const requestId = ++latestRequest.current;
    setIsLoading(true);
    setError(null);

    getMonthlySummary(value)
      .then((data) => {
        if (requestId === latestRequest.current) setSummary(data);
      })
      .catch((err) => {
        if (requestId === latestRequest.current) setError(extractErrorMessage(err));
      })
      .finally(() => {
        if (requestId === latestRequest.current) setIsLoading(false);
      });
  }

  return (
    <div className="mt-8">
      <div className="flex items-center justify-between gap-4">
        <h2 className="text-base font-semibold text-slate-900">Monthly summary</h2>
        <input
          type="month"
          aria-label="Month"
          value={month}
          onChange={(event) => handleMonthChange(event.target.value)}
          className="rounded-lg border border-slate-200 px-3 py-1.5 text-sm text-slate-700"
        />
      </div>

      <div className="mt-3 rounded-lg bg-white p-4 text-sm shadow-sm ring-1 ring-slate-100">
        {!month ? (
          <p className="text-slate-500">Pick a month to see its summary.</p>
        ) : isLoading ? (
          <p className="text-slate-500">Loading summary…</p>
        ) : error ? (
          <p className="text-red-700">{error}</p>
        ) : summary ? (
          <dl className="grid grid-cols-3 gap-4">
            <div>
              <dt className="text-slate-500">Income</dt>
              <dd className="font-medium text-slate-900">{numberFormat.format(summary.totalIncome)}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Expenses</dt>
              <dd className="font-medium text-slate-900">{numberFormat.format(summary.totalExpenses)}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Net</dt>
              <dd className="font-medium text-slate-900">{numberFormat.format(summary.net)}</dd>
            </div>
          </dl>
        ) : null}
      </div>
    </div>
  );
}
