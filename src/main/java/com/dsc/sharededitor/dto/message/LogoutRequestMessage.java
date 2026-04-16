package com.dsc.sharededitor.dto.message;

public class LogoutRequestMessage extends BaseMessage {

    public LogoutRequestMessage() {
        super();
        setType(MessageType.LOGOUT_REQUEST);
    }

    public LogoutRequestMessage(String username) {
        super(MessageType.LOGOUT_REQUEST, username);
    }
}