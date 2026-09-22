import { apiClient } from "./client";
import type { MonthlySummaryResponse } from "../types/api";

export async function getMonthlySummary(month?: string): Promise<MonthlySummaryResponse> {
  const { data } = await apiClient.get<MonthlySummaryResponse>("/api/v1/summaries/monthly", {
    params: month ? { month } : undefined,
  });
  return data;
}
