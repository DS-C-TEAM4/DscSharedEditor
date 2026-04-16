package com.dsc.sharededitor.domain.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<String, String> sessionIdToUsername = new ConcurrentHashMap<>();
    private final Map<String, String> usernameToSessionId = new ConcurrentHashMap<>();

    public void addUser(String username, String sessionId) {
        sessionIdToUsername.put(sessionId, username);
        usernameToSessionId.put(username, sessionId);
    }

    public void removeUserBySessionId(String sessionId) {
        String username = sessionIdToUsername.get(sessionId);
        if (username != null) {
            sessionIdToUsername.remove(sessionId);
            usernameToSessionId.remove(username);
        }
    }

    public boolean isUserOnline(String username) {
        return usernameToSessionId.containsKey(username);
    }

    public String getUsernameBySessionId(String sessionId) {
        return sessionIdToUsername.get(sessionId);
    }

    public int getOnlineUserCount() {
        return usernameToSessionId.size();
    }
}