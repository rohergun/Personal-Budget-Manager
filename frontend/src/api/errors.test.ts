import { AxiosError, AxiosHeaders, type AxiosResponse } from "axios";
import { describe, expect, it } from "vitest";
import type { ApiError } from "../types/api";
import { extractErrorMessage } from "./errors";

function axiosErrorWithBody(body: Partial<ApiError>): AxiosError<ApiError> {
  const response = {
    data: body,
    status: body.status ?? 400,
    statusText: "",
    headers: {},
    config: { headers: new AxiosHeaders() },
  } as AxiosResponse<ApiError>;
  return new AxiosError("Request failed", "ERR_BAD_REQUEST", undefined, undefined, response);
}

describe("extractErrorMessage", () => {
  it("joins validation details when present", () => {
    const error = axiosErrorWithBody({
      message: "Validation failed",
      details: ["email: must not be blank", "password: too short"],
    });

    expect(extractErrorMessage(error)).toBe("email: must not be blank, password: too short");
  });

  it("falls back to the API message when there are no details", () => {
    const error = axiosErrorWithBody({ message: "Invalid credentials", details: [] });

    expect(extractErrorMessage(error)).toBe("Invalid credentials");
  });

  it("reports an unreachable server when there is no response", () => {
    const error = new AxiosError("Network Error", "ERR_NETWORK");

    expect(extractErrorMessage(error)).toBe("Could not reach the server. Is the backend running?");
  });

  it("returns a generic message for non-Axios errors", () => {
    expect(extractErrorMessage(new Error("boom"))).toBe("Something went wrong. Please try again.");
  });
});
