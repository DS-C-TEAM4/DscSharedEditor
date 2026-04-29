package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private final String type = "LOGIN_RESPONSE";
    private boolean success;
    private String username;
    private String message;

    public LoginResponse() {}
    private LoginResponse(boolean success, String username, String message) {
        this.success = success;
        this.username = username;
        this.message = message;
    }

    public static LoginResponse success(String username) {
        return new LoginResponse(true, username, "로그인 성공");
    }

    public static LoginResponse fail(String message) {
        return new LoginResponse(false, null, message);
    }
}
