import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { MonthPicker } from "./MonthPicker";

const monthLabel = (year: number, monthIndex: number) =>
  new Date(year, monthIndex, 1).toLocaleDateString(undefined, { month: "long", year: "numeric" });

describe("MonthPicker", () => {
  // Only fake Date so "future months are disabled" is stable; promises and timers stay real.
  beforeEach(() => {
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 8, 15));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("shows a placeholder and keeps the calendar closed until clicked", () => {
    render(<MonthPicker value="" onChange={vi.fn()} />);

    expect(screen.getByRole("button", { name: "Pick a month" })).toBeInTheDocument();
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("emits the picked month as yyyy-MM and closes", async () => {
    const onChange = vi.fn();
    render(<MonthPicker value="" onChange={onChange} />);

    await userEvent.click(screen.getByRole("button", { name: "Pick a month" }));
    await userEvent.click(screen.getByRole("button", { name: monthLabel(2026, 2) }));

    expect(onChange).toHaveBeenCalledWith("2026-03");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("disables future months and the next-year arrow in the current year", async () => {
    render(<MonthPicker value="" onChange={vi.fn()} />);

    await userEvent.click(screen.getByRole("button", { name: "Pick a month" }));

    expect(screen.getByRole("button", { name: monthLabel(2026, 8) })).toBeEnabled();
    expect(screen.getByRole("button", { name: monthLabel(2026, 9) })).toBeDisabled();
    expect(screen.getByRole("button", { name: "Next year" })).toBeDisabled();
  });

  it("navigates to earlier years", async () => {
    const onChange = vi.fn();
    render(<MonthPicker value="" onChange={onChange} />);

    await userEvent.click(screen.getByRole("button", { name: "Pick a month" }));
    await userEvent.click(screen.getByRole("button", { name: "Previous year" }));
    await userEvent.click(screen.getByRole("button", { name: monthLabel(2025, 11) }));

    expect(onChange).toHaveBeenCalledWith("2025-12");
  });

  it("shows the selected month on the button and highlights it in the grid", async () => {
    render(<MonthPicker value="2025-06" onChange={vi.fn()} />);

    await userEvent.click(screen.getByRole("button", { name: monthLabel(2025, 5) }));

    expect(screen.getByText("2025")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: monthLabel(2025, 5), pressed: true })).toBeInTheDocument();
  });

  it("closes on Escape and on outside click", async () => {
    render(<MonthPicker value="" onChange={vi.fn()} />);
    const trigger = screen.getByRole("button", { name: "Pick a month" });

    await userEvent.click(trigger);
    await userEvent.keyboard("{Escape}");
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();

    await userEvent.click(trigger);
    await userEvent.click(document.body);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });
});
