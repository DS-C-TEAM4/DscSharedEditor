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

    public void removeUserByUsername(String username) {
        String sessionId = usernameToSessionId.get(username);
        if (sessionId != null) {
            usernameToSessionId.remove(username);
            sessionIdToUsername.remove(sessionId);
        }
    }

    public String getSessionIdByUsername(String username) {
        return usernameToSessionId.get(username);
    }

    public Map<String, String> getAllUsernameToSessionId() {
        return new ConcurrentHashMap<>(usernameToSessionId);
    }
}