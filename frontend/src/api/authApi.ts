import { postJson } from "./http";

export interface LoginRequest {
  clientId: string;
  username: string;
  password: string;
}

export interface LoginResponse {
  type: "LOGIN_RESPONSE";
  success: boolean;
  username: string | null;
  message: string;
}

export interface UserStatusNotification {
  type: "USER_STATUS";
  username: string;
  status: "JOINED" | "LEFT";
  message: string;
}

export const authApi = {
  login(request: LoginRequest) {
    return postJson<LoginRequest, LoginResponse>("/api/auth/login", request);
  },

  logout() {
    return postJson<Record<string, never>, void>("/api/auth/logout", {});
  },
};
