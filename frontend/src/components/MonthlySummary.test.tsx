import { act, fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { getMonthlySummary } from "../api/summary";
import type { MonthlySummaryResponse } from "../types/api";
import { MonthlySummary } from "./MonthlySummary";

vi.mock("../api/summary");

const format = (value: number) =>
  new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(
    value,
  );

const summary: MonthlySummaryResponse = {
  month: "2026-08",
  totalIncome: 2000,
  totalExpenses: 1200,
  net: 800,
  byCategory: [],
};

// jsdom doesn't support typing into month inputs, so set the value directly.
const pickMonth = (value: string) =>
  fireEvent.change(screen.getByLabelText("Month"), { target: { value } });

describe("MonthlySummary", () => {
  it("asks for a month before fetching anything", () => {
    render(<MonthlySummary />);

    expect(screen.getByText("Pick a month to see its summary.")).toBeInTheDocument();
    expect(getMonthlySummary).not.toHaveBeenCalled();
  });

  it("fetches and shows the totals for the picked month", async () => {
    vi.mocked(getMonthlySummary).mockResolvedValue(summary);

    render(<MonthlySummary />);
    pickMonth("2026-08");

    expect(getMonthlySummary).toHaveBeenCalledWith("2026-08");
    expect(await screen.findByText(format(2000))).toBeInTheDocument();
    expect(screen.getByText(format(1200))).toBeInTheDocument();
    expect(screen.getByText(format(800))).toBeInTheDocument();
  });

  it("shows a loading state while fetching", () => {
    vi.mocked(getMonthlySummary).mockReturnValue(new Promise(() => {}));

    render(<MonthlySummary />);
    pickMonth("2026-08");

    expect(screen.getByText("Loading summary…")).toBeInTheDocument();
  });

  it("shows an error when the summary fails to load", async () => {
    vi.mocked(getMonthlySummary).mockRejectedValue(new Error("boom"));

    render(<MonthlySummary />);
    pickMonth("2026-08");

    expect(await screen.findByText("Something went wrong. Please try again.")).toBeInTheDocument();
  });

  it("ignores a slower response for a previously picked month", async () => {
    let resolveFirst!: (value: MonthlySummaryResponse) => void;
    vi.mocked(getMonthlySummary)
      .mockReturnValueOnce(new Promise((resolve) => (resolveFirst = resolve)))
      .mockResolvedValueOnce({ ...summary, month: "2026-07", totalIncome: 500 });

    render(<MonthlySummary />);
    pickMonth("2026-08");
    pickMonth("2026-07");

    expect(await screen.findByText(format(500))).toBeInTheDocument();

    await act(async () => resolveFirst(summary));

    expect(screen.queryByText(format(2000))).not.toBeInTheDocument();
  });
});
