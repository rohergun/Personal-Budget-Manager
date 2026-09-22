import { apiClient } from "./client";
import type { BudgetResponse, BudgetUpdateRequest, CreateBudgetRequest, PageResponse } from "../types/api";

export async function listBudgets(page = 0): Promise<PageResponse<BudgetResponse>> {
  const { data } = await apiClient.get<PageResponse<BudgetResponse>>("/api/v1/budgets", {
    params: { page },
  });
  return data;
}

export async function createBudget(request: CreateBudgetRequest): Promise<BudgetResponse> {
  const { data } = await apiClient.post<BudgetResponse>("/api/v1/budgets", request);
  return data;
}

export async function updateBudget(
  id: string,
  request: BudgetUpdateRequest,
): Promise<BudgetResponse> {
  const { data } = await apiClient.put<BudgetResponse>(`/api/v1/budgets/${id}`, request);
  return data;
}

export async function deleteBudget(id: string): Promise<void> {
  await apiClient.delete(`/api/v1/budgets/${id}`);
}
