package com.dsc.sharededitor.controller;

import com.dsc.sharededitor.dto.request.LoginRequest;
import com.dsc.sharededitor.dto.response.LoginResponse;
import com.dsc.sharededitor.service.AuthService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate;

    public AuthController(AuthService authService,
                          SimpMessagingTemplate messagingTemplate) {
        this.authService = authService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/auth/login")
    public void login(@Payload LoginRequest request,
                      SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        LoginResponse response = authService.login(sessionId, request);

        messagingTemplate.convertAndSend(
                "/topic/user/" + request.getUsername(),
                response
        );
    }
}