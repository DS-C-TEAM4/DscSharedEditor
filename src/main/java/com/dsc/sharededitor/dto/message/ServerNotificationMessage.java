package com.dsc.sharededitor.dto.message;

public class ServerNotificationMessage extends BaseMessage {

    private String message;

    public ServerNotificationMessage() {
        super();
    }

    public ServerNotificationMessage(MessageType type, String username, String message) {
        super(type, username);
        this.message = message;
    }

    public static ServerNotificationMessage joined(String username) {
        return new ServerNotificationMessage(
                MessageType.USER_JOINED,
                username,
                username + "가 접속했습니다."
        );
    }

    public static ServerNotificationMessage left(String username) {
        return new ServerNotificationMessage(
                MessageType.USER_LEFT,
                username,
                username + "가 접속 해제했습니다."
        );
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "{type=" + getType()
                + ", message='" + message + "'}";
    }
}