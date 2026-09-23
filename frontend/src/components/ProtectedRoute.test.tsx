import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { useAuth } from "../context/AuthContext";
import { ProtectedRoute } from "./ProtectedRoute";

vi.mock("../context/AuthContext");

function mockAuth(state: { isAuthenticated: boolean; isLoading: boolean }) {
  vi.mocked(useAuth).mockReturnValue({
    user: null,
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    ...state,
  });
}

function renderAtProtectedRoute() {
  return render(
    <MemoryRouter initialEntries={["/app"]}>
      <Routes>
        <Route path="/login" element={<p>Login page</p>} />
        <Route
          path="/app"
          element={
            <ProtectedRoute>
              <p>Secret content</p>
            </ProtectedRoute>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
}

describe("ProtectedRoute", () => {
  it("shows a loading state while auth is resolving", () => {
    mockAuth({ isAuthenticated: false, isLoading: true });

    renderAtProtectedRoute();

    expect(screen.getByText("Loading…")).toBeInTheDocument();
    expect(screen.queryByText("Secret content")).not.toBeInTheDocument();
  });

  it("redirects to login when signed out", () => {
    mockAuth({ isAuthenticated: false, isLoading: false });

    renderAtProtectedRoute();

    expect(screen.getByText("Login page")).toBeInTheDocument();
  });

  it("renders children when signed in", () => {
    mockAuth({ isAuthenticated: true, isLoading: false });

    renderAtProtectedRoute();

    expect(screen.getByText("Secret content")).toBeInTheDocument();
  });
});
