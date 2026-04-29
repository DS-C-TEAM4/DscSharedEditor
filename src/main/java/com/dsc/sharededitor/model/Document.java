package com.dsc.sharededitor.model;

import lombok.Getter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Document {

    private final Long documentId;
    private final String title;
    private final String ownerUsername;
    private final Set<String> members = ConcurrentHashMap.newKeySet();

    @Getter(lombok.AccessLevel.NONE)
    private final StringBuilder content = new StringBuilder();

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

    public synchronized void insert(int position, String text) {
        int clamped = Math.max(0, Math.min(position, content.length()));
        content.insert(clamped, text);
    }

    public synchronized void update(int position, int length, String text) {
        int start = Math.max(0, Math.min(position, content.length()));
        int end   = Math.max(start, Math.min(position + length, content.length()));
        content.replace(start, end, text);
    }

    public synchronized String getContent() {
        return content.toString();
    }
}
