package com.dsc.sharededitor.controller;

import com.dsc.sharededitor.component.SessionRegistry;
import com.dsc.sharededitor.dto.request.TextDeleteRequest;
import com.dsc.sharededitor.dto.request.TextInsertRequest;
import com.dsc.sharededitor.dto.request.TextUpdateRequest;
import com.dsc.sharededitor.dto.response.DocumentUpdateNotification;
import com.dsc.sharededitor.service.DocumentService;

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
        this.documentService   = documentService;
        this.sessionRegistry   = sessionRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/document/insert")
    public void insert(@Payload TextInsertRequest request,
                       SimpMessageHeaderAccessor headerAccessor) {
        String username = sessionRegistry.getUsername(headerAccessor.getSessionId());
        if (username == null) return;

        documentService.insert(username, request.getPosition(), request.getText())
                .ifPresent(updatedContent ->
                        messagingTemplate.convertAndSend(
                                "/topic/document",
                                new DocumentUpdateNotification(
                                        DocumentService.DEFAULT_DOCUMENT_ID, "INSERT", username, updatedContent)
                        )
                );
    }

    @MessageMapping("/document/delete")
    public void delete(@Payload TextDeleteRequest request,
                       SimpMessageHeaderAccessor headerAccessor) {
        String username = sessionRegistry.getUsername(headerAccessor.getSessionId());
        if (username == null) return;

        documentService.delete(username, request.getPosition(), request.getLength())
                .ifPresent(updatedContent ->
                        messagingTemplate.convertAndSend(
                                "/topic/document",
                                new DocumentUpdateNotification(
                                        DocumentService.DEFAULT_DOCUMENT_ID, "DELETE", username, updatedContent)
                        )
                );
    }

    @MessageMapping("/document/update")
    public void update(@Payload TextUpdateRequest request,
                       SimpMessageHeaderAccessor headerAccessor) {
        String username = sessionRegistry.getUsername(headerAccessor.getSessionId());
        if (username == null) return;

        documentService.update(username, request.getPosition(), request.getLength(), request.getText())
                .ifPresent(updatedContent ->
                        messagingTemplate.convertAndSend(
                                "/topic/document",
                                new DocumentUpdateNotification(
                                        DocumentService.DEFAULT_DOCUMENT_ID, "UPDATE", username, updatedContent)
                        )
                );
    }
}
