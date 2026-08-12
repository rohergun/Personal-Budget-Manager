const BASE_URL = "/api/v1";

export async function apiRequest(path, method = "GET", body, requiresAuth = true) {
    const headers = {
        "Content-Type": "application/json",
    };

    if (requiresAuth) {
        const token = localStorage.getItem("token");
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }
    }

    const response = await fetch(BASE_URL + path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
    });

    if (!response.ok) {
        const errorBody = await response.json().catch(() => null);
        throw new Error(errorBody?.message ?? `Request failed with status ${response.status}`);
    }

    if (response.status === 204) {
        return undefined;
    }

    return response.json();
}