package com.dsc.sharededitor.runtime.socket;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientOutputRegistry {

    private final Map<String, PrintWriter> sessionWriterMap = new ConcurrentHashMap<>();

    public void addWriter(String sessionId, PrintWriter writer) {
        sessionWriterMap.put(sessionId, writer);
    }

    public void removeWriter(String sessionId) {
        sessionWriterMap.remove(sessionId);
    }

    public void sendToSession(String sessionId, String message) {
        PrintWriter writer = sessionWriterMap.get(sessionId);
        if (writer != null) {
            writer.println(message);
        }
    }
}
