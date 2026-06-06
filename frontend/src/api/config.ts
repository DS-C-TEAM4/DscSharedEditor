export const API_BASE_URL =
  import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export const SOCKET_URL =
  import.meta.env.VITE_SOCKET_URL ?? "http://localhost:8080/ws";

export const MOCK_MODE = import.meta.env.VITE_MOCK_MODE === "true";
