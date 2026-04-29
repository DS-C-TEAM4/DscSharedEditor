package com.dsc.sharededitor.repository;

import com.dsc.sharededitor.model.Document;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DocumentRepository {

    private final Map<String, Document> documents = new ConcurrentHashMap<>();

    public Document getOrCreate(String documentId) {
        return documents.computeIfAbsent(documentId, Document::new);
    }

    public Optional<Document> findById(String documentId) {
        return Optional.ofNullable(documents.get(documentId));
    }

    public boolean exists(String documentId) {
        return documents.containsKey(documentId);
    }
}
