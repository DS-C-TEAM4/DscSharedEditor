package com.dsc.sharededitor.dto.message;

public class LoginRequestMessage extends BaseMessage {

    private String password;

    public LoginRequestMessage() {
        super();
        setType(MessageType.LOGIN_REQUEST);
    }

    public LoginRequestMessage(String username, String password) {
        super(MessageType.LOGIN_REQUEST, username);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}