package com.dsc.sharededitor.service;

import com.dsc.sharededitor.model.Document;
import com.dsc.sharededitor.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public String insert(String documentId, int position, String text) {
        Document document = documentRepository.getOrCreate(documentId);
        document.insert(position, text);
        return document.getContent();
    }

    public String getContent(String documentId) {
        return documentRepository.findById(documentId)
                .map(Document::getContent)
                .orElse("");
    }
}
