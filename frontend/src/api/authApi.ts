import { stompClient } from "./stompClient";

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
  sendLogin(request: LoginRequest) {
    stompClient.publish("/app/auth/login", request);
  },

  sendLogout() {
    stompClient.publish("/app/auth/logout", {});
  },

  subscribeLoginResponse(
    clientId: string,
    handler: (response: LoginResponse) => void,
  ) {
    return stompClient.subscribe<LoginResponse>(
      `/topic/client/${clientId}`,
      handler,
    );
  },

  subscribeUserStatus(handler: (message: UserStatusNotification) => void) {
    return stompClient.subscribe<UserStatusNotification>(
      "/topic/global",
      handler,
    );
  },
};
