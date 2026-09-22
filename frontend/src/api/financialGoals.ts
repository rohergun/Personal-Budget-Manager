import { apiClient } from "./client";
import type {
  ContributeToGoalRequest,
  CreateFinancialGoalRequest,
  FinancialGoalResponse,
  FinancialGoalUpdateRequest,
  PageResponse,
} from "../types/api";

export async function listFinancialGoals(page = 0): Promise<PageResponse<FinancialGoalResponse>> {
  const { data } = await apiClient.get<PageResponse<FinancialGoalResponse>>(
    "/api/v1/financial-goals",
    { params: { page } },
  );
  return data;
}

export async function createFinancialGoal(
  request: CreateFinancialGoalRequest,
): Promise<FinancialGoalResponse> {
  const { data } = await apiClient.post<FinancialGoalResponse>("/api/v1/financial-goals", request);
  return data;
}

export async function updateFinancialGoal(
  id: string,
  request: FinancialGoalUpdateRequest,
): Promise<FinancialGoalResponse> {
  const { data } = await apiClient.put<FinancialGoalResponse>(
    `/api/v1/financial-goals/${id}`,
    request,
  );
  return data;
}

export async function contributeToFinancialGoal(
  id: string,
  request: ContributeToGoalRequest,
): Promise<FinancialGoalResponse> {
  const { data } = await apiClient.patch<FinancialGoalResponse>(
    `/api/v1/financial-goals/${id}/contributions`,
    request,
  );
  return data;
}

export async function deleteFinancialGoal(id: string): Promise<void> {
  await apiClient.delete(`/api/v1/financial-goals/${id}`);
}
