import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { useAuth } from "../context/AuthContext";
import { RegisterPage } from "./RegisterPage";

vi.mock("../context/AuthContext");

const register = vi.fn();

function renderRegisterPage() {
  vi.mocked(useAuth).mockReturnValue({
    user: null,
    isAuthenticated: false,
    isLoading: false,
    login: vi.fn(),
    register,
    logout: vi.fn(),
  });
  return render(
    <MemoryRouter initialEntries={["/register"]}>
      <Routes>
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/app/dashboard" element={<p>Dashboard</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

async function fillAndSubmit() {
  const user = userEvent.setup();
  await user.type(screen.getByLabelText("First name"), "Jane");
  await user.type(screen.getByLabelText("Last name"), "Doe");
  await user.type(screen.getByLabelText("Email"), "jane@example.com");
  await user.type(screen.getByLabelText("Password"), "password123");
  await user.click(screen.getByRole("button", { name: "Create account" }));
}

describe("RegisterPage", () => {
  it("registers and navigates to the dashboard", async () => {
    register.mockResolvedValue(undefined);
    renderRegisterPage();

    await fillAndSubmit();

    expect(register).toHaveBeenCalledWith({
      name: "Jane",
      surname: "Doe",
      email: "jane@example.com",
      password: "password123",
    });
    expect(await screen.findByText("Dashboard")).toBeInTheDocument();
  });

  it("shows an error message when registration fails", async () => {
    register.mockRejectedValue(new Error("boom"));
    renderRegisterPage();

    await fillAndSubmit();

    expect(await screen.findByText("Something went wrong. Please try again.")).toBeInTheDocument();
  });
});
