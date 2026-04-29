package com.dsc.sharededitor.dto.request;

public class LoginRequest {

    private String clientId;
    private String username;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String clientId, String username, String password) {
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}