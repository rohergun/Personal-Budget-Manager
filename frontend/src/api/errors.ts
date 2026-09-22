import { isAxiosError } from "axios";
import type { ApiError } from "../types/api";

export function extractErrorMessage(error: unknown): string {
  if (isAxiosError<ApiError>(error) && error.response?.data) {
    const { message, details } = error.response.data;
    if (details?.length) {
      return details.join(", ");
    }
    if (message) {
      return message;
    }
  }
  if (isAxiosError(error) && !error.response) {
    return "Could not reach the server. Is the backend running?";
  }
  return "Something went wrong. Please try again.";
}
