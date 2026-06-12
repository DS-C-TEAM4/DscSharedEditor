package com.dsc.sharededitor.controller;

import com.dsc.sharededitor.dto.request.DocumentCreateRequest;
import com.dsc.sharededitor.dto.request.DocumentJoinRequest;
import com.dsc.sharededitor.dto.response.DocumentPresenceNotification;
import com.dsc.sharededitor.dto.response.DocumentSnapshotResponse;
import com.dsc.sharededitor.dto.response.DocumentSummaryResponse;
import com.dsc.sharededitor.exception.DocumentNotFoundException;
import com.dsc.sharededitor.service.DocumentService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentRestController {

    private final DocumentService documentService;
    private final SimpMessagingTemplate messagingTemplate;

    public DocumentRestController(DocumentService documentService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.documentService = documentService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public List<DocumentSummaryResponse> list(@RequestParam(required = false) String username) {
        return documentService.listDocuments(username).stream()
                .map(snapshot -> new DocumentSummaryResponse(
                        snapshot.getDocumentId(),
                        snapshot.getTitle(),
                        snapshot.getOwnerUsername(),
                        snapshot.getLines().size(),
                        snapshot.getMembers().size(),
                        snapshot.getActiveParticipants().size(),
                        snapshot.getTopic()
                ))
                .toList();
    }

    @GetMapping("/{documentId}")
    public DocumentSnapshotResponse get(@PathVariable Long documentId) {
        return documentService.getSnapshot(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    @PostMapping
    public DocumentSnapshotResponse create(@RequestBody DocumentCreateRequest request) {
        DocumentSnapshotResponse snapshot = documentService.createDocument(request.getTitle(), request.getUsername());
        broadcastPresence(snapshot, request.getUsername(), "CREATED", request.getUsername() + "님이 새 문서를 생성했습니다.");
        return snapshot;
    }

    @PostMapping("/{documentId}/join")
    public DocumentSnapshotResponse join(@PathVariable Long documentId,
                                         @RequestBody DocumentJoinRequest request) {
        DocumentSnapshotResponse snapshot = documentService.joinDocument(documentId, request.getUsername());
        broadcastPresence(snapshot, request.getUsername(), "JOINED", request.getUsername() + "님이 문서에 참여했습니다.");
        return snapshot;
    }

    @PostMapping("/{documentId}/leave")
    public DocumentSnapshotResponse leave(@PathVariable Long documentId,
                                          @RequestBody DocumentJoinRequest request) {
        DocumentSnapshotResponse snapshot = documentService.leaveDocument(documentId, request.getUsername());
        broadcastPresence(snapshot, request.getUsername(), "LEFT", request.getUsername() + "님이 문서에서 나갔습니다.");
        return snapshot;
    }

    private void broadcastPresence(DocumentSnapshotResponse snapshot, String username, String status, String message) {
        messagingTemplate.convertAndSend(
                snapshot.getTopic(),
                new DocumentPresenceNotification(
                        snapshot.getDocumentId(),
                        username,
                        status,
                        message,
                        snapshot.getActiveParticipants()
                )
        );
    }
}
