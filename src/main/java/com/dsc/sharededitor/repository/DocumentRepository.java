package com.dsc.sharededitor.repository;

import com.dsc.sharededitor.model.Document;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class DocumentRepository {

    private final AtomicLong idSequence = new AtomicLong(1);
    private final Map<Long, Document> documents = new ConcurrentHashMap<>();

    public DocumentRepository() {
        create("공유 문서", "system");
    }

    public Document create(String title, String ownerUsername) {
        Long documentId = idSequence.getAndIncrement();
        Document document = new Document(documentId, title, ownerUsername);
        documents.put(documentId, document);
        return document;
    }

    public Optional<Document> findById(Long documentId) {
        return Optional.ofNullable(documents.get(documentId));
    }

    public boolean exists(Long documentId) {
        return documents.containsKey(documentId);
    }
}
