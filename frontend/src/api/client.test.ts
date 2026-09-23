import { AxiosHeaders, type InternalAxiosRequestConfig } from "axios";
import { describe, expect, it } from "vitest";
import { apiClient, getStoredToken, setStoredToken } from "./client";

async function runRequestInterceptors(): Promise<InternalAxiosRequestConfig> {
  let config: InternalAxiosRequestConfig = { headers: new AxiosHeaders() };
  // Axios doesn't expose its interceptor list publicly, so read the registered handlers.
  const handlers = (
    apiClient.interceptors.request as unknown as {
      handlers: { fulfilled: (c: InternalAxiosRequestConfig) => InternalAxiosRequestConfig }[];
    }
  ).handlers;
  for (const handler of handlers) {
    config = await handler.fulfilled(config);
  }
  return config;
}

describe("token storage", () => {
  it("stores and reads the token", () => {
    setStoredToken("abc123");

    expect(getStoredToken()).toBe("abc123");
  });

  it("removes the token when set to null", () => {
    setStoredToken("abc123");
    setStoredToken(null);

    expect(getStoredToken()).toBeNull();
  });
});

describe("apiClient request interceptor", () => {
  it("adds a bearer token when one is stored", async () => {
    setStoredToken("abc123");

    const config = await runRequestInterceptors();

    expect(config.headers.Authorization).toBe("Bearer abc123");
  });

  it("leaves the Authorization header unset without a token", async () => {
    const config = await runRequestInterceptors();

    expect(config.headers.Authorization).toBeUndefined();
  });
});
