import { apiRequest } from "./api.js";

export async function login(email, password) {
    const response = await apiRequest("/auth/login", "POST", { email, password }, false);
    localStorage.setItem("token", response.token);
}

export async function register(email, name, surname, password) {
    const response = await apiRequest(
        "/auth/register",
        "POST",
        { email, name, surname, password },
        false
    );
    localStorage.setItem("token", response.token);
}

export function logout() {
    localStorage.removeItem("token");
    window.location.href = "/login.html";
}

export function isLoggedIn() {
    return localStorage.getItem("token") !== null;
}

export function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = "/login.html";
    }
}