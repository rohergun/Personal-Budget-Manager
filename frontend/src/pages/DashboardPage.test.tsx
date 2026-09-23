import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
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

describe("DashboardPage", () => {
  it("shows a loading state first", () => {
    vi.mocked(getMonthlySummary).mockReturnValue(new Promise(() => {}));

    render(<DashboardPage />);

    expect(screen.getByText("Loading your summary…")).toBeInTheDocument();
  });

  it("renders totals with expenses and negative net in red", async () => {
    vi.mocked(getMonthlySummary).mockResolvedValue(summary);

    render(<DashboardPage />);

    expect(await screen.findByRole("heading", { name: "Dashboard" })).toBeInTheDocument();
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

    expect(await screen.findByText(/No transactions yet this month/)).toBeInTheDocument();
  });

  it("shows an error when the summary fails to load", async () => {
    vi.mocked(getMonthlySummary).mockRejectedValue(new Error("boom"));

    render(<DashboardPage />);

    expect(await screen.findByText("Something went wrong. Please try again.")).toBeInTheDocument();
  });
});
