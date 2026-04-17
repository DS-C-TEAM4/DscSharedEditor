package com.dsc.sharededitor.domain.connection;

import com.dsc.sharededitor.domain.broadcast.MessageBroadcaster;
import com.dsc.sharededitor.domain.message.MessageParser;
import com.dsc.sharededitor.dto.message.BaseMessage;
import com.dsc.sharededitor.dto.message.LoginRequestMessage;
import com.dsc.sharededitor.dto.message.LoginResponseMessage;
import com.dsc.sharededitor.dto.message.LogoutRequestMessage;
import com.dsc.sharededitor.dto.message.MessageType;
import com.dsc.sharededitor.dto.message.ServerNotificationMessage;
import com.dsc.sharededitor.runtime.socket.ClientOutputRegistry;

import java.util.List;

public class ConnectionGateway {

    private final MessageParser messageParser;
    private final ConnectionHandler connectionHandler;
    private final MessageBroadcaster messageBroadcaster;
    private final ClientOutputRegistry clientOutputRegistry;

    public ConnectionGateway(MessageParser messageParser,
                             ConnectionHandler connectionHandler,
                             MessageBroadcaster messageBroadcaster,
                             ClientOutputRegistry clientOutputRegistry) {
        this.messageParser = messageParser;
        this.connectionHandler = connectionHandler;
        this.messageBroadcaster = messageBroadcaster;
        this.clientOutputRegistry = clientOutputRegistry;
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
                        messageBroadcaster.getBroadcastTargetSessionIds(sessionId, false);

                for (String targetSessionId : targets) {
                    clientOutputRegistry.sendToSession(
                            targetSessionId,
                            "[알림] " + joinMessage
                    );
                }
            }

            return response;
        }

        if (messageType == MessageType.LOGOUT_REQUEST) {
            LogoutRequestMessage request = messageParser.parseMessage(rawMessage, LogoutRequestMessage.class);

            ServerNotificationMessage leftMessage =
                    messageBroadcaster.createUserLeftMessage(request.getUsername());

            connectionHandler.handleLogout(request);

            List<String> targets =
                    messageBroadcaster.getBroadcastTargetSessionIds(sessionId, false);

            for (String targetSessionId : targets) {
                clientOutputRegistry.sendToSession(
                        targetSessionId,
                        "[알림] " + leftMessage
                );
            }

            return null;
        }

        throw new IllegalArgumentException("지원하지 않는 메시지 타입입니다: " + messageType);
    }

    public void handleDisconnect(String sessionId) {
        String username = connectionHandler.getUsernameBySessionId(sessionId);
        if (username == null) {
            return;
        }

        ServerNotificationMessage leftMessage =
                messageBroadcaster.createUserLeftMessage(username);

        connectionHandler.handleDisconnect(sessionId);

        List<String> targets =
                messageBroadcaster.getBroadcastTargetSessionIds(sessionId, false);

        for (String targetSessionId : targets) {
            clientOutputRegistry.sendToSession(
                    targetSessionId,
                    "[알림] " + leftMessage
            );
        }
    }
}