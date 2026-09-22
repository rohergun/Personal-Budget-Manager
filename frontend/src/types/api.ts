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
