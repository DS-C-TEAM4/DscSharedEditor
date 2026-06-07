package com.dsc.sharededitor.service;

import com.dsc.sharededitor.component.DocumentParticipationRegistry;
import com.dsc.sharededitor.model.Document;
import com.dsc.sharededitor.dto.response.DocumentEditLogResponse;
import com.dsc.sharededitor.dto.response.DocumentEditNotification;
import com.dsc.sharededitor.dto.response.DocumentSnapshotResponse;
import com.dsc.sharededitor.exception.DocumentEditException;
import com.dsc.sharededitor.exception.DocumentNotFoundException;
import com.dsc.sharededitor.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentParticipationRegistry participationRegistry;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentParticipationRegistry participationRegistry) {
        this.documentRepository = documentRepository;
        this.participationRegistry = participationRegistry;
    }

    public DocumentSnapshotResponse createDocument(String title, String ownerUsername) {
        String resolvedOwner = requireUsername(ownerUsername);
        String resolvedTitle = (title == null || title.isBlank()) ? "Untitled" : title.trim();
        Document document = documentRepository.create(resolvedTitle, resolvedOwner);
        participationRegistry.join(document.getDocumentId(), resolvedOwner);
        return toSnapshot(document);
    }

    public DocumentSnapshotResponse joinDocument(Long documentId, String username) {
        String resolvedUsername = requireUsername(username);
        Document document = getRequiredDocument(documentId);
        document.addMember(resolvedUsername);
        participationRegistry.join(documentId, resolvedUsername);
        return toSnapshot(document);
    }

    public DocumentSnapshotResponse leaveDocument(Long documentId, String username) {
        String resolvedUsername = requireUsername(username);
        Document document = getRequiredDocument(documentId);
        participationRegistry.leave(documentId, resolvedUsername);
        return toSnapshot(document);
    }

    public Optional<DocumentSnapshotResponse> getSnapshot(Long documentId) {
        return documentRepository.findById(documentId).map(this::toSnapshot);
    }

    public DocumentSnapshotResponse insertLine(Long documentId, String username, int lineNumber, String text) {
        Document document = getRequiredDocument(documentId);
        ensureActiveParticipant(documentId, username);
        try {
            document.insertLine(lineNumber, text, username);
        } catch (IllegalArgumentException ex) {
            throw new DocumentEditException(ex.getMessage());
        }
        return toSnapshot(document);
    }

    public DocumentSnapshotResponse updateLine(Long documentId, String username, int lineNumber, String text) {
        Document document = getRequiredDocument(documentId);
        ensureActiveParticipant(documentId, username);
        try {
            document.updateLine(lineNumber, text, username);
        } catch (IllegalArgumentException ex) {
            throw new DocumentEditException(ex.getMessage());
        }
        return toSnapshot(document);
    }

    public DocumentSnapshotResponse deleteLine(Long documentId, String username, int lineNumber) {
        Document document = getRequiredDocument(documentId);
        ensureActiveParticipant(documentId, username);
        try {
            document.deleteLine(lineNumber, username);
        } catch (IllegalArgumentException ex) {
            throw new DocumentEditException(ex.getMessage());
        }
        return toSnapshot(document);
    }

    public DocumentEditNotification toEditNotification(Long documentId, DocumentSnapshotResponse snapshot, String username, String operation, int lineNumber) {
        DocumentEditLogResponse logEntry = snapshot.getEditLogs().isEmpty()
                ? null
                : snapshot.getEditLogs().get(snapshot.getEditLogs().size() - 1);
        return new DocumentEditNotification(
                documentId,
                operation,
                username,
                lineNumber,
                snapshot.getContent(),
                snapshot.getLines(),
                logEntry
        );
    }

    public Set<Long> leaveDocuments(String username) {
        return participationRegistry.leaveAll(username);
    }

    public List<String> getActiveParticipants(Long documentId) {
        return List.copyOf(participationRegistry.getParticipants(documentId));
    }

    public List<DocumentSnapshotResponse> listDocuments() {
        return documentRepository.findAll().stream()
                .sorted(Comparator.comparing(Document::getDocumentId))
                .map(this::toSnapshot)
                .collect(Collectors.toList());
    }

    public List<DocumentSnapshotResponse> listDocuments(String username) {
        if (username == null || username.isBlank()) {
            return listDocuments();
        }

        String resolvedUsername = username.trim();
        return documentRepository.findAll().stream()
                .filter(document -> document.isOwner(resolvedUsername) || document.isMember(resolvedUsername))
                .sorted(Comparator.comparing(Document::getDocumentId))
                .map(this::toSnapshot)
                .collect(Collectors.toList());
    }

    public Optional<Document> findById(Long documentId) {
        return documentRepository.findById(documentId);
    }

    private Document getRequiredDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    private String requireUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("사용자명이 필요합니다.");
        }
        return username.trim();
    }

    private void ensureActiveParticipant(Long documentId, String username) {
        if (!participationRegistry.getParticipants(documentId).contains(username)) {
            throw new DocumentEditException("해당 문서에 참여 중인 사용자가 아닙니다.");
        }
    }

    private DocumentSnapshotResponse toSnapshot(Document document) {
        List<DocumentEditLogResponse> logs = document.getEditLogs().stream()
                .map(DocumentEditLogResponse::new)
                .collect(Collectors.toList());
        return new DocumentSnapshotResponse(
                document.getDocumentId(),
                document.getTitle(),
                document.getOwnerUsername(),
                document.getLines(),
                document.getContent(),
                List.copyOf(document.getMembers()),
                List.copyOf(participationRegistry.getParticipants(document.getDocumentId())),
                logs,
                "/topic/documents/" + document.getDocumentId()
        );
    }
}
