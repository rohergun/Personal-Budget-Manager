import { useEffect, useRef, useState } from "react";
import { CalendarDays, ChevronLeft, ChevronRight } from "lucide-react";

const MONTH_INDEXES = Array.from({ length: 12 }, (_, index) => index);

function toMonthValue(year: number, monthIndex: number): string {
  return `${year}-${String(monthIndex + 1).padStart(2, "0")}`;
}

function formatMonthLabel(year: number, monthIndex: number, month: "short" | "long"): string {
  return new Date(year, monthIndex, 1).toLocaleDateString(undefined, { month, year: "numeric" });
}

// Lets the user pick a month from a calendar-style grid; value is "yyyy-MM" or "" when unset.
export function MonthPicker({
  value,
  onChange,
}: {
  value: string;
  onChange: (value: string) => void;
}) {
  const today = new Date();
  const currentYear = today.getFullYear();
  const currentMonthIndex = today.getMonth();

  const [selectedYear, selectedMonthNumber] = value ? value.split("-").map(Number) : [null, null];
  const selectedMonthIndex = selectedMonthNumber === null ? null : selectedMonthNumber - 1;

  const [isOpen, setIsOpen] = useState(false);
  const [viewYear, setViewYear] = useState(selectedYear ?? currentYear);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    function handleClickOutside(event: MouseEvent) {
      if (!containerRef.current?.contains(event.target as Node)) setIsOpen(false);
    }
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") setIsOpen(false);
    }

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEscape);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [isOpen]);

  function toggle() {
    if (!isOpen) setViewYear(selectedYear ?? currentYear);
    setIsOpen(!isOpen);
  }

  function selectMonth(monthIndex: number) {
    onChange(toMonthValue(viewYear, monthIndex));
    setIsOpen(false);
  }

  return (
    <div ref={containerRef} className="relative">
      <button
        type="button"
        onClick={toggle}
        aria-haspopup="dialog"
        aria-expanded={isOpen}
        className="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-50"
      >
        <CalendarDays className="h-4 w-4 text-slate-400" />
        {selectedYear !== null ? formatMonthLabel(selectedYear, selectedMonthIndex!, "long") : "Pick a month"}
      </button>

      {isOpen && (
        <div
          role="dialog"
          aria-label="Choose a month"
          className="absolute right-0 z-10 mt-2 w-64 rounded-xl bg-white p-3 shadow-lg ring-1 ring-slate-200"
        >
          <div className="mb-2 flex items-center justify-between">
            <button
              type="button"
              onClick={() => setViewYear(viewYear - 1)}
              aria-label="Previous year"
              className="rounded-md p-1 text-slate-500 hover:bg-slate-100"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <span className="text-sm font-semibold text-slate-900">{viewYear}</span>
            <button
              type="button"
              onClick={() => setViewYear(viewYear + 1)}
              disabled={viewYear >= currentYear}
              aria-label="Next year"
              className="rounded-md p-1 text-slate-500 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-30 disabled:hover:bg-transparent"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>

          <div className="grid grid-cols-3 gap-1">
            {MONTH_INDEXES.map((monthIndex) => {
              const isSelected = viewYear === selectedYear && monthIndex === selectedMonthIndex;
              const isFuture =
                viewYear > currentYear || (viewYear === currentYear && monthIndex > currentMonthIndex);

              return (
                <button
                  key={monthIndex}
                  type="button"
                  onClick={() => selectMonth(monthIndex)}
                  disabled={isFuture}
                  aria-pressed={isSelected}
                  aria-label={formatMonthLabel(viewYear, monthIndex, "long")}
                  className={`rounded-md px-2 py-2 text-sm ${
                    isSelected
                      ? "bg-brand-500 font-medium text-white"
                      : "text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:text-slate-300 disabled:hover:bg-transparent"
                  }`}
                >
                  {new Date(viewYear, monthIndex, 1).toLocaleDateString(undefined, { month: "short" })}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
