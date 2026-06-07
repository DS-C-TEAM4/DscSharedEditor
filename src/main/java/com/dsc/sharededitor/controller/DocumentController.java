package com.dsc.sharededitor.controller;

import com.dsc.sharededitor.component.SessionRegistry;
import com.dsc.sharededitor.dto.request.DocumentLineDeleteRequest;
import com.dsc.sharededitor.dto.request.DocumentLineInsertRequest;
import com.dsc.sharededitor.dto.request.DocumentLineUpdateRequest;
import com.dsc.sharededitor.dto.request.DocumentLockRequest;
import com.dsc.sharededitor.dto.request.DocumentLockResponse;
import com.dsc.sharededitor.dto.response.DocumentEditNotification;
import com.dsc.sharededitor.dto.response.DocumentLockNotification;
import com.dsc.sharededitor.dto.response.DocumentSnapshotResponse;
import com.dsc.sharededitor.service.DocumentService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class DocumentController {

    private final DocumentService documentService;
    private final SessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public DocumentController(DocumentService documentService,
                              SessionRegistry sessionRegistry,
                              SimpMessagingTemplate messagingTemplate) {
        this.documentService = documentService;
        this.sessionRegistry = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/documents/{documentId}/locks/request")
    public void requestLock(@DestinationVariable Long documentId,
                            @Payload DocumentLockRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        String username = sessionRegistry.getUsername(headerAccessor.getSessionId());
        if (username == null) {
            return;
        }

        messagingTemplate.convertAndSend(
                topic(documentId),
                new DocumentLockNotification(
                        documentId,
                        "REQUEST",
                        request.getLineNumber(),
                        request.getClientId(),
                        username,
                        null,
                        request.getTimestamp()
                )
        );
    }

    @MessageMapping("/documents/{documentId}/locks/ok")
    public void replyLock(@DestinationVariable Long documentId,
                          @Payload DocumentLockResponse request,
                          SimpMessageHeaderAccessor headerAccessor) {
        String username = sessionRegistry.getUsername(headerAccessor.getSessionId());
        if (username == null) {
            return;
        }

        messagingTemplate.convertAndSend(
                topic(documentId),
                new DocumentLockNotification(
                        documentId,
                        "OK",
                        request.getLineNumber(),
                        request.getClientId(),
                        username,
                        request.getTargetClientId(),
                        request.getTimestamp()
                )
        );
    }

    @MessageMapping("/documents/{documentId}/lines/insert")
    public void insert(@DestinationVariable Long documentId,
                       @Payload DocumentLineInsertRequest request,
                       SimpMessageHeaderAccessor headerAccessor) {
        String username = getUsername(headerAccessor);
        if (username == null) {
            return;
        }

        handleEdit(documentId, request.getLineNumber(), "INSERT", username,
                () -> documentService.insertLine(documentId, username, request.getLineNumber(), request.getText()));
    }

    @MessageMapping("/documents/{documentId}/lines/update")
    public void update(@DestinationVariable Long documentId,
                       @Payload DocumentLineUpdateRequest request,
                       SimpMessageHeaderAccessor headerAccessor) {
        String username = getUsername(headerAccessor);
        if (username == null) {
            return;
        }

        handleEdit(documentId, request.getLineNumber(), "UPDATE", username,
                () -> documentService.updateLine(documentId, username, request.getLineNumber(), request.getText()));
    }

    @MessageMapping("/documents/{documentId}/lines/delete")
    public void delete(@DestinationVariable Long documentId,
                       @Payload DocumentLineDeleteRequest request,
                       SimpMessageHeaderAccessor headerAccessor) {
        String username = getUsername(headerAccessor);
        if (username == null) {
            return;
        }

        handleEdit(documentId, request.getLineNumber(), "DELETE", username,
                () -> documentService.deleteLine(documentId, username, request.getLineNumber()));
    }

    private void handleEdit(Long documentId,
                            int lineNumber,
                            String operation,
                            String username,
                            SnapshotSupplier snapshotSupplier) {
        DocumentSnapshotResponse snapshot = snapshotSupplier.get();
        DocumentEditNotification notification = documentService.toEditNotification(
                documentId,
                snapshot,
                username,
                operation,
                lineNumber
        );

        messagingTemplate.convertAndSend(topic(documentId), notification);
    }

    private String getUsername(SimpMessageHeaderAccessor headerAccessor) {
        return sessionRegistry.getUsername(headerAccessor.getSessionId());
    }

    private String topic(Long documentId) {
        return "/topic/documents/" + documentId;
    }

    @FunctionalInterface
    private interface SnapshotSupplier {
        DocumentSnapshotResponse get();
    }
}
