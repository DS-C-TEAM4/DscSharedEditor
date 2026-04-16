package com.dsc.sharededitor.domain.broadcast;

import com.dsc.sharededitor.domain.session.SessionManager;
import com.dsc.sharededitor.dto.message.ServerNotificationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageBroadcaster {

    private final SessionManager sessionManager;

    public MessageBroadcaster(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public List<String> getBroadcastTargetSessionIds(String senderSessionId, boolean includeSender) {
        Map<String, String> usernameToSessionId = sessionManager.getAllUsernameToSessionId();
        List<String> targetSessionIds = new ArrayList<>();

        for (String sessionId : usernameToSessionId.values()) {
            if (includeSender || !sessionId.equals(senderSessionId)) {
                targetSessionIds.add(sessionId);
            }
        }

        return targetSessionIds;
    }

    public ServerNotificationMessage createUserJoinedMessage(String username) {
        return ServerNotificationMessage.joined(username);
    }

    public ServerNotificationMessage createUserLeftMessage(String username) {
        return ServerNotificationMessage.left(username);
    }
}