package com.dsc.sharededitor.service;

import com.dsc.sharededitor.model.Document;
import com.dsc.sharededitor.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentService {

    public static final Long DEFAULT_DOCUMENT_ID = 1L;

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Optional<String> insert(String username, int position, String text) {
        return documentRepository.findById(DEFAULT_DOCUMENT_ID)
                .map(doc -> {
                    doc.insert(position, text);
                    return doc.getContent();
                });
    }

    public Optional<String> update(String username, int position, int length, String text) {
        return documentRepository.findById(DEFAULT_DOCUMENT_ID)
                .map(doc -> {
                    doc.update(position, length, text);
                    return doc.getContent();
                });
    }

    public Optional<String> delete(String username, int position, int length) {
        return documentRepository.findById(DEFAULT_DOCUMENT_ID)
                .map(doc -> {
                    doc.delete(position, length);
                    return doc.getContent();
                });
    }

    public Optional<Document> findById(Long documentId) {
        return documentRepository.findById(documentId);
    }
}
