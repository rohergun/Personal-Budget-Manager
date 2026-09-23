import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AxiosError } from "axios";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { useAuth } from "../context/AuthContext";
import { LoginPage } from "./LoginPage";

vi.mock("../context/AuthContext");

const login = vi.fn();

function renderLoginPage() {
  vi.mocked(useAuth).mockReturnValue({
    user: null,
    isAuthenticated: false,
    isLoading: false,
    login,
    register: vi.fn(),
    logout: vi.fn(),
  });
  return render(
    <MemoryRouter initialEntries={["/login"]}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/app/dashboard" element={<p>Dashboard</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

async function fillAndSubmit() {
  const user = userEvent.setup();
  await user.type(screen.getByLabelText("Email"), "jane@example.com");
  await user.type(screen.getByLabelText("Password"), "password123");
  await user.click(screen.getByRole("button", { name: "Log in" }));
}

describe("LoginPage", () => {
  it("logs in and navigates to the dashboard", async () => {
    login.mockResolvedValue(undefined);
    renderLoginPage();

    await fillAndSubmit();

    expect(login).toHaveBeenCalledWith({ email: "jane@example.com", password: "password123" });
    expect(await screen.findByText("Dashboard")).toBeInTheDocument();
  });

  it("shows an error message when login fails", async () => {
    login.mockRejectedValue(new AxiosError("Network Error", "ERR_NETWORK"));
    renderLoginPage();

    await fillAndSubmit();

    expect(
      await screen.findByText("Could not reach the server. Is the backend running?"),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Log in" })).toBeEnabled();
  });
});
