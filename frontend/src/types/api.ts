export interface AuthResponse {
  token: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  name: string;
  surname: string;
  password: string;
}

export interface CategorySpendingResponse {
  categoryId: string;
  categoryName: string;
  spent: number;
  budgetLimit: number | null;
}

export interface MonthlySummaryResponse {
  month: string;
  totalIncome: number;
  totalExpenses: number;
  net: number;
  byCategory: CategorySpendingResponse[];
}

export interface UserResponse {
  id: string;
  email: string;
  name: string;
  surname: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  details: string[];
}

export interface CategoryResponse {
  id: string;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
}

export interface CategoryUpdateRequest {
  name: string;
  description?: string;
}

export interface BudgetResponse {
  id: string;
  monthlyLimit: number;
  categoryId: string;
  categoryName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBudgetRequest {
  monthlyLimit: number;
  categoryId: string;
}

export interface BudgetUpdateRequest {
  monthlyLimit: number;
  categoryId: string;
}

export type TransactionType = "INCOME" | "EXPENSE";

export interface TransactionResponse {
  id: string;
  amount: number;
  type: TransactionType;
  categoryId: string | null;
  categoryName: string | null;
  transactionDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTransactionRequest {
  amount: number;
  type: TransactionType;
  categoryId: string;
  transactionDate: string;
}

export interface TransactionUpdateRequest {
  amount: number;
  type: TransactionType;
  categoryId: string;
  transactionDate: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
