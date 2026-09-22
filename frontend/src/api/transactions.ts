import { apiClient } from "./client";
import type {
  CreateTransactionRequest,
  PageResponse,
  TransactionResponse,
  TransactionUpdateRequest,
} from "../types/api";

export async function listTransactions(page = 0): Promise<PageResponse<TransactionResponse>> {
  const { data } = await apiClient.get<PageResponse<TransactionResponse>>("/api/v1/transactions", {
    params: { page },
  });
  return data;
}

export async function createTransaction(
  request: CreateTransactionRequest,
): Promise<TransactionResponse> {
  const { data } = await apiClient.post<TransactionResponse>("/api/v1/transactions", request);
  return data;
}

export async function updateTransaction(
  id: string,
  request: TransactionUpdateRequest,
): Promise<TransactionResponse> {
  const { data } = await apiClient.put<TransactionResponse>(`/api/v1/transactions/${id}`, request);
  return data;
}

export async function deleteTransaction(id: string): Promise<void> {
  await apiClient.delete(`/api/v1/transactions/${id}`);
}
