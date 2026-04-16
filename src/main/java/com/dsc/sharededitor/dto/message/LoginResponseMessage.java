package com.dsc.sharededitor.dto.message;

public class LoginResponseMessage extends BaseMessage {

    private boolean success;
    private String message;

    public LoginResponseMessage() {
        super();
        setType(MessageType.LOGIN_RESPONSE);
    }

    public LoginResponseMessage(String username, boolean success, String message) {
        super(MessageType.LOGIN_RESPONSE, username);
        this.success = success;
        this.message = message;
    }

    public static LoginResponseMessage success(String username) {
        return new LoginResponseMessage(username, true, "로그인 성공");
    }

    public static LoginResponseMessage fail(String username) {
        return new LoginResponseMessage(username, false, "아이디 또는 비밀번호 오류 또는 중복 로그인");
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}