package com.dsc.sharededitor.domain.auth;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserStore {

    private final Map<String, String> userMap = new HashMap<>();

    public InMemoryUserStore() {
        userMap.put("user1", "1234");
        userMap.put("user2", "2345");
        userMap.put("user3", "3456");
        userMap.put("user4", "4567");
    }

    public boolean exists(String username) {
        return userMap.containsKey(username);
    }

    public boolean matches(String username, String password) {
        if (!userMap.containsKey(username)) {
            return false;
        }
        return userMap.get(username).equals(password);
    }
}