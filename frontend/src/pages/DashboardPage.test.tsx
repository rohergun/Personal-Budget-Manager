import { act, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { getMonthlySummary } from "../api/summary";
import type { MonthlySummaryResponse } from "../types/api";
import { DashboardPage } from "./DashboardPage";

vi.mock("../api/summary");

const format = (value: number) =>
  new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(
    value,
  );

const summary: MonthlySummaryResponse = {
  month: "2026-09",
  totalIncome: 3000,
  totalExpenses: 3500,
  net: -500,
  byCategory: [
    { categoryId: "c1", categoryName: "Groceries", spent: 150, budgetLimit: 100 },
    { categoryId: "c2", categoryName: "Rent", spent: 1000, budgetLimit: null },
  ],
};

const monthLabel = (year: number, monthIndex: number) =>
  new Date(year, monthIndex, 1).toLocaleDateString(undefined, { month: "long", year: "numeric" });

async function pickMonth(year: number, monthIndex: number) {
  await userEvent.click(screen.getByRole("button", { expanded: false }));
  await userEvent.click(screen.getByRole("button", { name: monthLabel(year, monthIndex) }));
}

describe("DashboardPage", () => {
  // Only fake Date so the default month is stable; promises and timers stay real.
  beforeEach(() => {
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 8, 15));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("shows a loading state first", () => {
    vi.mocked(getMonthlySummary).mockReturnValue(new Promise(() => {}));

    render(<DashboardPage />);

    expect(screen.getByText("Loading your summary…")).toBeInTheDocument();
  });

  it("renders totals with expenses and negative net in red", async () => {
    vi.mocked(getMonthlySummary).mockResolvedValue(summary);

    render(<DashboardPage />);

    expect(await screen.findByText(format(3000))).toBeInTheDocument();
    expect(getMonthlySummary).toHaveBeenCalledWith("2026-09");
    expect(screen.getByRole("button", { name: monthLabel(2026, 8) })).toBeInTheDocument();
    expect(screen.getByText(format(3000))).toHaveClass("text-emerald-600");
    expect(screen.getByText(format(3500))).toHaveClass("text-red-600");
    expect(screen.getByText(format(-500))).toHaveClass("text-red-600");
  });

  it("highlights categories that are over budget", async () => {
    vi.mocked(getMonthlySummary).mockResolvedValue(summary);

    render(<DashboardPage />);

    expect(await screen.findByText(`${format(150)} / ${format(100)}`)).toHaveClass("text-red-600");
    expect(screen.getByText(format(1000))).toHaveClass("text-slate-600");
  });

  it("shows an empty state when there are no transactions", async () => {
    vi.mocked(getMonthlySummary).mockResolvedValue({ ...summary, byCategory: [] });

    render(<DashboardPage />);

    expect(await screen.findByText(/No transactions this month/)).toBeInTheDocument();
  });

  it("shows an error when the summary fails to load", async () => {
    vi.mocked(getMonthlySummary).mockRejectedValue(new Error("boom"));

    render(<DashboardPage />);

    expect(await screen.findByText("Something went wrong. Please try again.")).toBeInTheDocument();
  });

  it("reloads the whole dashboard for a picked month", async () => {
    vi.mocked(getMonthlySummary)
      .mockResolvedValueOnce(summary)
      .mockResolvedValueOnce({
        month: "2026-07",
        totalIncome: 1200,
        totalExpenses: 400,
        net: 800,
        byCategory: [{ categoryId: "c3", categoryName: "Travel", spent: 400, budgetLimit: null }],
      });

    render(<DashboardPage />);
    await screen.findByText(format(3000));

    await pickMonth(2026, 6);

    expect(getMonthlySummary).toHaveBeenLastCalledWith("2026-07");
    expect(await screen.findByText(format(1200))).toBeInTheDocument();
    expect(screen.getByText("Travel")).toBeInTheDocument();
    expect(screen.queryByText("Groceries")).not.toBeInTheDocument();
  });

  it("ignores a slower response for a previously picked month", async () => {
    let resolveCurrent!: (value: MonthlySummaryResponse) => void;
    vi.mocked(getMonthlySummary)
      .mockReturnValueOnce(new Promise((resolve) => (resolveCurrent = resolve)))
      .mockResolvedValueOnce({ ...summary, month: "2026-07", totalIncome: 1200 });

    render(<DashboardPage />);
    await pickMonth(2026, 6);
    expect(await screen.findByText(format(1200))).toBeInTheDocument();

    await act(async () => resolveCurrent(summary));

    expect(screen.getByText(format(1200))).toBeInTheDocument();
    expect(screen.queryByText(format(3000))).not.toBeInTheDocument();
  });
});
