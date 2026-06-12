import { API_BASE_URL } from "./config";

export async function requestJson<TResponse>(
  path: string,
  options: RequestInit = {},
): Promise<TResponse> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${path}`);
  }

  return response.json() as Promise<TResponse>;
}

export function postJson<TRequest, TResponse>(
  path: string,
  body: TRequest,
): Promise<TResponse> {
  return requestJson<TResponse>(path, {
    method: "POST",
    body: JSON.stringify(body),
  });
}
