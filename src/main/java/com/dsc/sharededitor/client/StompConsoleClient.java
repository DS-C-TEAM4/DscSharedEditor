package com.dsc.sharededitor.client;

import com.dsc.sharededitor.dto.request.LoginRequest;
import com.dsc.sharededitor.dto.response.LoginResponse;
import com.dsc.sharededitor.dto.response.UserStatusNotification;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class StompConsoleClient {

    private StompSession session;
    private String username;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void start() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JsonStringConverter());

        session = stompClient.connectAsync(
                "ws://localhost:8080/ws",
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        System.out.println("[Client] 서버에 연결되었습니다.");
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        System.out.println("[Client] 연결 오류: " + exception.getMessage());
                    }
                }
        ).get(10, TimeUnit.SECONDS);

        session.subscribe("/topic/global", new RawFrameHandler());

        Scanner scanner = new Scanner(System.in);
        runMenu(scanner);
    }

    private void runMenu(Scanner scanner) {
        while (true) {
            printMenu();

            String command = scanner.nextLine().trim();

            try {
                switch (command) {
                    case "1" -> login(scanner);
                    case "2" -> logout();
                    case "0" -> {
                        logout();
                        System.out.println("[Client] 종료합니다.");
                        return;
                    }
                    default -> System.out.println("[Client] 올바른 번호를 입력하세요.");
                }
            } catch (Exception e) {
                System.out.println("[Client] 오류: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== 메뉴 === " + (username != null ? "[" + username + "]" : ""));
        System.out.println("1. 로그인");
        System.out.println("2. 로그아웃");
        System.out.println("0. 종료");
        System.out.print("선택: ");
    }

    private void login(Scanner scanner) {
        System.out.print("아이디: ");
        String inputUsername = scanner.nextLine().trim();

        System.out.print("비밀번호: ");
        String password = scanner.nextLine().trim();

        session.subscribe("/topic/user/" + inputUsername, new RawFrameHandler());

        username = inputUsername;
        send("/app/auth/login", new LoginRequest(inputUsername, password));
    }

    private void logout() {
        if (username == null) {
            return;
        }

        send("/app/auth/logout", Map.of());
        username = null;
    }

    private void handleServerMessage(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String type = root.path("type").asText("");

            System.out.println();

            switch (type) {
                case "LOGIN_RESPONSE" -> handleLoginResponse(json);
                case "USER_STATUS" -> handleUserStatus(json);
                default -> System.out.println("[서버] " + json);
            }
        } catch (Exception e) {
            System.out.println("[서버] " + json);
        }

        System.out.print("선택: ");
    }

    private void handleLoginResponse(String json) throws Exception {
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        if (response.isSuccess()) {
            System.out.println("[로그인] 성공: " + response.getUsername());
        } else {
            System.out.println("[로그인] 실패: " + response.getMessage());
            username = null;
        }
    }

    private void handleUserStatus(String json) throws Exception {
        UserStatusNotification notification =
                objectMapper.readValue(json, UserStatusNotification.class);

        System.out.println("[알림] " + notification.getMessage());
    }

    private void send(String destination, Object payload) {
        try {
            StompHeaders headers = new StompHeaders();
            headers.setDestination(destination);
            headers.setContentType(MimeTypeUtils.APPLICATION_JSON);

            String json = objectMapper.writeValueAsString(payload);
            session.send(headers, json);
        } catch (JacksonException e) {
            System.out.println("[Client] 전송 오류: " + e.getMessage());
        }
    }

    private static class JsonStringConverter extends AbstractMessageConverter {

        JsonStringConverter() {
            super(MimeTypeUtils.APPLICATION_JSON, MimeTypeUtils.TEXT_PLAIN, MimeType.valueOf("*/*"));
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return String.class == clazz;
        }

        @Override
        protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
            Object payload = message.getPayload();

            if (payload instanceof byte[] bytes) {
                return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            }

            return payload.toString();
        }

        @Override
        protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
            return payload.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private class RawFrameHandler implements StompFrameHandler {

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            handleServerMessage((String) payload);
        }
    }
}