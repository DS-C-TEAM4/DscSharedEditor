package com.dsc.sharededitor.domain.connection;

import com.dsc.sharededitor.domain.message.MessageParser;
import com.dsc.sharededitor.dto.message.*;
import com.dsc.sharededitor.domain.broadcast.MessageBroadcaster;

import java.util.List;

public class ConnectionGateway {

    private final MessageParser messageParser;
    private final ConnectionHandler connectionHandler;
    private final MessageBroadcaster messageBroadcaster;

    public ConnectionGateway(MessageParser messageParser,
                             ConnectionHandler connectionHandler,
                             MessageBroadcaster messageBroadcaster) {
        this.messageParser = messageParser;
        this.connectionHandler = connectionHandler;
        this.messageBroadcaster = messageBroadcaster;
    }

    public MessageType resolveMessageType(String rawMessage) {
        BaseMessage baseMessage = messageParser.parseBaseMessage(rawMessage);
        return baseMessage.getType();
    }

    public Object handleMessage(String sessionId, String rawMessage) {
        MessageType messageType = resolveMessageType(rawMessage);

        if (messageType == MessageType.LOGIN_REQUEST) {
            LoginRequestMessage request = messageParser.parseMessage(rawMessage, LoginRequestMessage.class);
            LoginResponseMessage response = connectionHandler.handleLogin(sessionId, request);

            if (response.isSuccess()) {
                ServerNotificationMessage joinMessage =
                        messageBroadcaster.createUserJoinedMessage(request.getUsername());

                List<String> targets =
                        messageBroadcaster.getBroadcastTargetSessionIds(sessionId, true);

                System.out.println("접속 브로드캐스트 대상: " + targets);
                System.out.println("메시지: " + joinMessage.getMessage());
            }

            return response;
        }

        if (messageType == MessageType.LOGOUT_REQUEST) {
            LogoutRequestMessage request = messageParser.parseMessage(rawMessage, LogoutRequestMessage.class);

            connectionHandler.handleLogout(request);

            ServerNotificationMessage leftMessage =
                    messageBroadcaster.createUserLeftMessage(request.getUsername());

            List<String> targets =
                    messageBroadcaster.getBroadcastTargetSessionIds(sessionId, true);

            System.out.println("해제 브로드캐스트 대상: " + targets);
            System.out.println("메시지: " + leftMessage.getMessage());

            return null;
        }

        throw new IllegalArgumentException("지원하지 않는 메시지 타입입니다: " + messageType);
    }
}