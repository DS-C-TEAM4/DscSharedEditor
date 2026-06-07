package com.dsc.sharededitor.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Document {

    private final Long documentId;
    private final String title;
    private final String ownerUsername;
    private final Set<String> members = ConcurrentHashMap.newKeySet();
    private final List<String> lines = new ArrayList<>();
    private final List<DocumentEditLog> editLogs = new ArrayList<>();
    private final AtomicLong logSequence = new AtomicLong(1L);

    public Document(Long documentId, String title, String ownerUsername) {
        this.documentId    = documentId;
        this.title         = title;
        this.ownerUsername = ownerUsername;
        this.members.add(ownerUsername);
    }

    public void addMember(String username) {
        members.add(username);
    }

    public boolean isMember(String username) {
        return members.contains(username);
    }

    public boolean isOwner(String username) {
        return ownerUsername.equals(username);
    }

    public synchronized int getLineCount() {
        return lines.size();
    }

    public synchronized List<String> getLines() {
        return List.copyOf(lines);
    }

    public synchronized List<DocumentEditLog> getEditLogs() {
        return List.copyOf(editLogs);
    }

    public synchronized String getContent() {
        return String.join("\n", lines);
    }

    public synchronized void insertLine(int lineNumber, String text, String username) {
        int index = clampInsertIndex(lineNumber);
        String newText = normalize(text);
        lines.add(index, newText);
        addLog(username, "INSERT", index, null, newText);
    }

    public synchronized void updateLine(int lineNumber, String text, String username) {
        int index = requireLineIndex(lineNumber);
        String before = lines.get(index);
        String after = normalize(text);
        lines.set(index, after);
        addLog(username, "UPDATE", index, before, after);
    }

    public synchronized void deleteLine(int lineNumber, String username) {
        int index = requireLineIndex(lineNumber);
        String before = lines.remove(index);
        addLog(username, "DELETE", index, before, null);
    }

    private int clampInsertIndex(int lineNumber) {
        if (lineNumber < 0) return lines.size();
        return Math.min(lineNumber, lines.size());
    }

    private int requireLineIndex(int lineNumber) {
        if (lineNumber < 0 || lineNumber >= lines.size()) {
            throw new IllegalArgumentException("존재하지 않는 줄 번호입니다.");
        }
        return lineNumber;
    }

    private String normalize(String text) {
        return text == null ? "" : text;
    }

    private void addLog(String username, String operation, int lineNumber, String beforeText, String afterText) {
        editLogs.add(new DocumentEditLog(
                logSequence.getAndIncrement(),
                username,
                operation,
                lineNumber,
                beforeText,
                afterText
        ));
    }
}
