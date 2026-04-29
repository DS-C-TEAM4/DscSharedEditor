package com.dsc.sharededitor.controller;

import com.dsc.sharededitor.component.SessionRegistry;
import com.dsc.sharededitor.dto.request.TextInsertRequest;
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
        String sessionId = headerAccessor.getSessionId();
        String username  = sessionRegistry.getUsername(sessionId);

        if (username == null) {
            return;
        }

        String updatedContent = documentService.insert(request.getDocumentId(), request.getPosition(), request.getText());

        messagingTemplate.convertAndSend(
                "/topic/document",
                new DocumentUpdateNotification(request.getDocumentId(), "INSERT", username, updatedContent)
        );
    }
}
