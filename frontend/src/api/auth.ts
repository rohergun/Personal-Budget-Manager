import { apiClient } from "./client";
import type { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from "../types/api";

export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>("/api/v1/auth/login", request);
  return data;
}

export async function register(request: RegisterRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>("/api/v1/auth/register", request);
  return data;
}

export async function getCurrentUser(): Promise<UserResponse> {
  const { data } = await apiClient.get<UserResponse>("/api/v1/users/me");
  return data;
}
