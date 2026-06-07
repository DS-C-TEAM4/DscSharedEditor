package com.dsc.sharededitor.component;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DocumentParticipationRegistry {

    private final ConcurrentHashMap<Long, Set<String>> documentToUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Long>> userToDocuments = new ConcurrentHashMap<>();

    public void join(Long documentId, String username) {
        documentToUsers.computeIfAbsent(documentId, key -> ConcurrentHashMap.newKeySet()).add(username);
        userToDocuments.computeIfAbsent(username, key -> ConcurrentHashMap.newKeySet()).add(documentId);
    }

    public void leave(Long documentId, String username) {
        Set<String> users = documentToUsers.get(documentId);
        if (users != null) {
            users.remove(username);
            if (users.isEmpty()) {
                documentToUsers.remove(documentId);
            }
        }

        Set<Long> documents = userToDocuments.get(username);
        if (documents != null) {
            documents.remove(documentId);
            if (documents.isEmpty()) {
                userToDocuments.remove(username);
            }
        }
    }

    public Set<Long> leaveAll(String username) {
        Set<Long> documents = userToDocuments.remove(username);
        if (documents == null || documents.isEmpty()) {
            return Set.of();
        }

        for (Long documentId : documents) {
            Set<String> users = documentToUsers.get(documentId);
            if (users != null) {
                users.remove(username);
                if (users.isEmpty()) {
                    documentToUsers.remove(documentId);
                }
            }
        }

        return Collections.unmodifiableSet(documents);
    }

    public Set<String> getParticipants(Long documentId) {
        Set<String> users = documentToUsers.get(documentId);
        if (users == null) {
            return Set.of();
        }
        return Set.copyOf(users);
    }

    public Set<Long> getDocuments(String username) {
        Set<Long> documents = userToDocuments.get(username);
        if (documents == null) {
            return Set.of();
        }
        return Set.copyOf(documents);
    }
}
