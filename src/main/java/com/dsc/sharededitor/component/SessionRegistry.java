package com.dsc.sharededitor.component;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();
    private final Map<String, String> usernameToSession = new ConcurrentHashMap<>();
    private final Map<String, String> usernameToClientId = new ConcurrentHashMap<>();

    public void register(String sessionId, String username, String clientId) {
        sessionToUsername.put(sessionId, username);
        usernameToSession.put(username, sessionId);
        usernameToClientId.put(username, clientId);
    }

    public String removeBySessionId(String sessionId) {
        String username = sessionToUsername.remove(sessionId);
        if (username != null) {
            usernameToSession.remove(username);
            usernameToClientId.remove(username);
        }
        return username;
    }

    public String getUsername(String sessionId) {
        return sessionToUsername.get(sessionId);
    }

    public String getClientId(String username) {
        return usernameToClientId.get(username);
    }

    public boolean isOnline(String username) {
        return usernameToSession.containsKey(username);
    }
}
