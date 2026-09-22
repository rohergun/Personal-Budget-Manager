import { apiClient } from "./client";
import type {
  CategoryResponse,
  CategoryUpdateRequest,
  CreateCategoryRequest,
  PageResponse,
} from "../types/api";

export async function listCategories(
  page = 0,
  size?: number,
): Promise<PageResponse<CategoryResponse>> {
  const { data } = await apiClient.get<PageResponse<CategoryResponse>>("/api/v1/categories", {
    params: { page, size },
  });
  return data;
}

export async function createCategory(request: CreateCategoryRequest): Promise<CategoryResponse> {
  const { data } = await apiClient.post<CategoryResponse>("/api/v1/categories", request);
  return data;
}

export async function updateCategory(
  id: string,
  request: CategoryUpdateRequest,
): Promise<CategoryResponse> {
  const { data } = await apiClient.put<CategoryResponse>(`/api/v1/categories/${id}`, request);
  return data;
}

export async function deleteCategory(id: string): Promise<void> {
  await apiClient.delete(`/api/v1/categories/${id}`);
}
