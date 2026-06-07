package com.dsc.sharededitor.controller;

import com.dsc.sharededitor.dto.request.LoginRequest;
import com.dsc.sharededitor.dto.response.DocumentPresenceNotification;
import com.dsc.sharededitor.dto.response.LoginResponse;
import com.dsc.sharededitor.service.AuthService;
import com.dsc.sharededitor.dto.response.UserStatusNotification;
import com.dsc.sharededitor.service.DocumentService;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Controller
public class AuthController {

    private final AuthService authService;
    private final DocumentService documentService;
    private final SimpMessagingTemplate messagingTemplate;

    public AuthController(AuthService authService,
                          DocumentService documentService,
                          SimpMessagingTemplate messagingTemplate) {
        this.authService = authService;
        this.documentService = documentService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/auth/login")
    public void login(@Payload LoginRequest request,
                      SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        LoginResponse response = authService.login(sessionId, request);

        messagingTemplate.convertAndSend(
                "/topic/client/" + request.getClientId(),
                response
        );

        if (response.isSuccess()) {
            messagingTemplate.convertAndSend(
                    "/topic/global",
                    new UserStatusNotification(
                            request.getUsername(),
                            "JOINED",
                            request.getUsername() + "님이 접속했습니다."
                    )
            );
        }
    }

    @MessageMapping("/auth/logout")
    public void logout(SimpMessageHeaderAccessor headerAccessor) {
        handleLeave(headerAccessor.getSessionId(), "로그아웃");
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        handleLeave(event.getSessionId(), "연결 종료");
    }

    private void handleLeave(String sessionId, String reason) {
        String username = authService.logout(sessionId);

        if (username != null) {
            documentService.leaveDocuments(username).forEach(documentId ->
                    documentService.getSnapshot(documentId).ifPresent(snapshot ->
                            messagingTemplate.convertAndSend(
                                    snapshot.getTopic(),
                                    new DocumentPresenceNotification(
                                            snapshot.getDocumentId(),
                                            username,
                                            "LEFT",
                                            username + "님이 문서에서 나갔습니다. (" + reason + ")",
                                            snapshot.getActiveParticipants()
                                    )
                            )
                    )
            );

            messagingTemplate.convertAndSend(
                    "/topic/global",
                    new UserStatusNotification(
                            username,
                            "LEFT",
                            username + "님이 접속 해제했습니다. (" + reason + ")"
                    )
            );
        }
    }
}
