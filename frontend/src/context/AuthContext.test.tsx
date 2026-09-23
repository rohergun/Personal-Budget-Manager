import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import * as authApi from "../api/auth";
import { getStoredToken, setStoredToken } from "../api/client";
import type { UserResponse } from "../types/api";
import { AuthProvider, useAuth } from "./AuthContext";

vi.mock("../api/auth");

const user: UserResponse = { id: "1", email: "jane@example.com", name: "Jane", surname: "Doe" };

function Consumer() {
  const auth = useAuth();
  return (
    <>
      <p>
        {auth.isLoading ? "loading" : auth.isAuthenticated ? `signed in as ${auth.user?.name}` : "signed out"}
      </p>
      <button onClick={() => auth.login({ email: user.email, password: "password123" })}>
        Log in
      </button>
      <button
        onClick={() =>
          auth.register({ email: user.email, name: "Jane", surname: "Doe", password: "password123" })
        }
      >
        Register
      </button>
      <button onClick={auth.logout}>Log out</button>
    </>
  );
}

function renderWithProvider() {
  return render(
    <AuthProvider>
      <Consumer />
    </AuthProvider>,
  );
}

describe("AuthProvider", () => {
  it("is signed out when no token is stored", async () => {
    renderWithProvider();

    expect(await screen.findByText("signed out")).toBeInTheDocument();
    expect(authApi.getCurrentUser).not.toHaveBeenCalled();
  });

  it("restores the user from a stored token", async () => {
    setStoredToken("stored-token");
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(user);

    renderWithProvider();

    expect(await screen.findByText("signed in as Jane")).toBeInTheDocument();
  });

  it("clears an invalid stored token", async () => {
    setStoredToken("expired-token");
    vi.mocked(authApi.getCurrentUser).mockRejectedValue(new Error("401"));

    renderWithProvider();

    expect(await screen.findByText("signed out")).toBeInTheDocument();
    expect(getStoredToken()).toBeNull();
  });

  it("stores the token and loads the user on login", async () => {
    vi.mocked(authApi.login).mockResolvedValue({ token: "new-token" });
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(user);
    renderWithProvider();
    await screen.findByText("signed out");

    await userEvent.click(screen.getByRole("button", { name: "Log in" }));

    expect(await screen.findByText("signed in as Jane")).toBeInTheDocument();
    expect(getStoredToken()).toBe("new-token");
  });

  it("stores the token and loads the user on register", async () => {
    vi.mocked(authApi.register).mockResolvedValue({ token: "new-token" });
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(user);
    renderWithProvider();
    await screen.findByText("signed out");

    await userEvent.click(screen.getByRole("button", { name: "Register" }));

    expect(await screen.findByText("signed in as Jane")).toBeInTheDocument();
    expect(getStoredToken()).toBe("new-token");
  });

  it("clears the token and user on logout", async () => {
    setStoredToken("stored-token");
    vi.mocked(authApi.getCurrentUser).mockResolvedValue(user);
    renderWithProvider();
    await screen.findByText("signed in as Jane");

    await userEvent.click(screen.getByRole("button", { name: "Log out" }));

    expect(await screen.findByText("signed out")).toBeInTheDocument();
    expect(getStoredToken()).toBeNull();
  });
});

describe("useAuth", () => {
  it("throws when used outside an AuthProvider", () => {
    vi.spyOn(console, "error").mockImplementation(() => {});

    expect(() => render(<Consumer />)).toThrow("useAuth must be used within an AuthProvider");
  });
});
