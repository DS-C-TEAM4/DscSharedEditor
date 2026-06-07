package com.dsc.sharededitor.client;

import com.dsc.sharededitor.dto.request.DocumentJoinRequest;
import com.dsc.sharededitor.dto.request.DocumentLineDeleteRequest;
import com.dsc.sharededitor.dto.request.DocumentLineInsertRequest;
import com.dsc.sharededitor.dto.request.DocumentLineUpdateRequest;
import com.dsc.sharededitor.dto.request.DocumentLockRequest;
import com.dsc.sharededitor.dto.request.DocumentLockResponse;
import com.dsc.sharededitor.dto.request.LoginRequest;
import com.dsc.sharededitor.dto.response.DocumentEditLogResponse;
import com.dsc.sharededitor.dto.response.DocumentEditNotification;
import com.dsc.sharededitor.dto.response.DocumentLockNotification;
import com.dsc.sharededitor.dto.response.DocumentPresenceNotification;
import com.dsc.sharededitor.dto.response.DocumentSnapshotResponse;
import com.dsc.sharededitor.dto.response.DocumentSummaryResponse;
import com.dsc.sharededitor.dto.response.LoginResponse;
import com.dsc.sharededitor.dto.response.UserStatusNotification;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StompConsoleClient {

    private static final String API_BASE = "http://localhost:8080/api/documents";
    private static final long LOCK_TIMEOUT_MS = 10000L;

    private StompSession session;
    private StompSession.Subscription documentSubscription;
    private String username;
    private String pendingUsername;
    private String activeDocumentTopic;
    private Long activeDocumentId;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String clientId = UUID.randomUUID().toString();
    private final Object stateLock = new Object();

    private final List<String> currentLines = new ArrayList<>();
    private final List<DocumentEditLogResponse> currentLogs = new ArrayList<>();
    private final Set<String> activeParticipants = ConcurrentHashMap.newKeySet();
    private final Map<Integer, LineLockState> lineLocks = new ConcurrentHashMap<>();

    private long lamportClock;

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
        session.subscribe("/topic/client/" + clientId, new RawFrameHandler());

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
                    case "3" -> createDocument(scanner);
                    case "4" -> loadExistingDocument(scanner);
                    case "5" -> joinDocument(scanner);
                    case "6" -> insertLine(scanner);
                    case "7" -> updateLine(scanner);
                    case "8" -> deleteLine(scanner);
                    case "9" -> printCurrentDocument();
                    case "0" -> {
                        clearActiveDocument();
                        if (session != null && session.isConnected()) {
                            session.disconnect();
                        }
                        username = null;
                        pendingUsername = null;
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
        System.out.println("3. 새 문서 생성");
        System.out.println("4. 기존 문서 불러오기");
        System.out.println("5. 문서 ID로 참여");
        System.out.println("6. 줄 추가");
        System.out.println("7. 줄 수정");
        System.out.println("8. 줄 삭제");
        System.out.println("9. 현재 문서 보기");
        System.out.println("0. 종료");
        System.out.print("선택: ");
    }

    private void login(Scanner scanner) {
        if (username != null) {
            System.out.println("[Client] 이미 로그인 중입니다. 먼저 로그아웃하세요.");
            return;
        }

        if (pendingUsername != null) {
            System.out.println("[Client] 로그인 요청 처리 중입니다.");
            return;
        }

        System.out.print("아이디: ");
        String inputUsername = scanner.nextLine().trim();

        System.out.print("비밀번호: ");
        String password = scanner.nextLine().trim();

        pendingUsername = inputUsername;
        send("/app/auth/login", new LoginRequest(clientId, inputUsername, password));
    }

    private void logout() {
        if (username == null) {
            System.out.println("[Client] 로그인 상태가 아닙니다.");
            return;
        }

        send("/app/auth/logout", Map.of());
        clearActiveDocument();
        username = null;
        pendingUsername = null;
    }

    private void createDocument(Scanner scanner) throws Exception {
        if (!requireLogin()) {
            return;
        }

        System.out.print("문서 제목(비우면 Untitled): ");
        String title = scanner.nextLine().trim();

        DocumentSnapshotResponse snapshot = postJson(
                API_BASE,
                Map.of("title", title, "username", username),
                DocumentSnapshotResponse.class
        );

        attachDocument(snapshot);
    }

    private void joinDocument(Scanner scanner) throws Exception {
        if (!requireLogin()) {
            return;
        }

        System.out.print("문서 ID: ");
        Long documentId;
        try {
            documentId = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[Client] 올바른 숫자를 입력하세요.");
            return;
        }

        DocumentSnapshotResponse snapshot = postJson(
                API_BASE + "/" + documentId + "/join",
                new DocumentJoinRequest(username),
                DocumentSnapshotResponse.class
        );

        attachDocument(snapshot);
    }

    private void loadExistingDocument(Scanner scanner) throws Exception {
        if (!requireLogin()) {
            return;
        }

        List<DocumentSummaryResponse> documents = fetchMyDocuments();
        if (documents.isEmpty()) {
            System.out.println("[Client] 불러올 문서가 없습니다.");
            return;
        }

        System.out.println("--- 내 문서 목록 ---");
        documents.forEach(doc -> System.out.println(
                "#" + doc.getDocumentId() +
                " | " + doc.getTitle() +
                " | owner=" + doc.getOwnerUsername() +
                " | lines=" + doc.getLineCount()
        ));

        System.out.print("불러올 문서 ID: ");
        Long documentId;
        try {
            documentId = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[Client] 올바른 숫자를 입력하세요.");
            return;
        }

        DocumentSnapshotResponse snapshot = postJson(
                API_BASE + "/" + documentId + "/join",
                new DocumentJoinRequest(username),
                DocumentSnapshotResponse.class
        );

        attachDocument(snapshot);
    }

    private void insertLine(Scanner scanner) throws Exception {
        if (!requireActiveDocument()) {
            return;
        }

        System.out.print("줄 번호(0부터 시작, 끝에 추가하려면 현재 줄 수 입력): ");
        int lineNumber = parseInt(scanner);
        if (lineNumber < 0) {
            return;
        }

        System.out.print("추가할 텍스트: ");
        String text = scanner.nextLine();

        if (!acquireLineLock(lineNumber)) {
            System.out.println("[Client] 줄 락 획득에 실패했습니다.");
            return;
        }

        try {
            send(topicPath("lines/insert"), new DocumentLineInsertRequest(lineNumber, text));
        } finally {
            releaseLineLock(lineNumber);
        }
    }

    private void updateLine(Scanner scanner) throws Exception {
        if (!requireActiveDocument()) {
            return;
        }

        System.out.print("수정할 줄 번호: ");
        int lineNumber = parseInt(scanner);
        if (lineNumber < 0) {
            return;
        }

        System.out.print("새 텍스트: ");
        String text = scanner.nextLine();

        if (!acquireLineLock(lineNumber)) {
            System.out.println("[Client] 줄 락 획득에 실패했습니다.");
            return;
        }

        try {
            send(topicPath("lines/update"), new DocumentLineUpdateRequest(lineNumber, text));
        } finally {
            releaseLineLock(lineNumber);
        }
    }

    private void deleteLine(Scanner scanner) throws Exception {
        if (!requireActiveDocument()) {
            return;
        }

        System.out.print("삭제할 줄 번호: ");
        int lineNumber = parseInt(scanner);
        if (lineNumber < 0) {
            return;
        }

        if (!acquireLineLock(lineNumber)) {
            System.out.println("[Client] 줄 락 획득에 실패했습니다.");
            return;
        }

        try {
            send(topicPath("lines/delete"), new DocumentLineDeleteRequest(lineNumber));
        } finally {
            releaseLineLock(lineNumber);
        }
    }

    private void printCurrentDocument() {
        synchronized (stateLock) {
            if (activeDocumentId == null) {
                System.out.println("[Client] 활성 문서가 없습니다.");
                return;
            }

            System.out.println("--- 문서 #" + activeDocumentId + " ---");
            if (currentLines.isEmpty()) {
                System.out.println("(비어 있음)");
            } else {
                for (int i = 0; i < currentLines.size(); i++) {
                    System.out.println((i) + ": " + currentLines.get(i));
                }
            }

            System.out.println("--- 참여자 ---");
            if (activeParticipants.isEmpty()) {
                System.out.println("(없음)");
            } else {
                activeParticipants.stream()
                        .sorted()
                        .forEach(user -> System.out.println("- " + user));
            }

            System.out.println("--- 최근 로그 ---");
            currentLogs.stream()
                    .sorted(Comparator.comparingLong(DocumentEditLogResponse::getSequence))
                    .skip(Math.max(0, currentLogs.size() - 10L))
                    .forEach(log -> System.out.println(
                            "[" + log.getSequence() + "] " +
                            log.getUsername() + " " +
                            log.getOperation() + " line=" + log.getLineNumber()
                    ));
        }
    }

    private void handleServerMessage(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String type = root.path("type").asText("");

            System.out.println();

            switch (type) {
                case "LOGIN_RESPONSE" -> handleLoginResponse(json);
                case "USER_STATUS" -> handleUserStatus(json);
                case "DOCUMENT_SNAPSHOT" -> handleDocumentSnapshot(json);
                case "DOCUMENT_PRESENCE" -> handleDocumentPresence(json);
                case "DOCUMENT_LOCK" -> handleDocumentLock(json);
                case "DOCUMENT_EDIT" -> handleDocumentEdit(json);
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
            username = response.getUsername();
            pendingUsername = null;
            System.out.println("[로그인] 성공: " + response.getUsername());
        } else {
            pendingUsername = null;
            System.out.println("[로그인] 실패: " + response.getMessage());
        }
    }

    private void handleUserStatus(String json) throws Exception {
        UserStatusNotification notification = objectMapper.readValue(json, UserStatusNotification.class);
        System.out.println("[알림] " + notification.getMessage());
    }

    private void handleDocumentSnapshot(String json) throws Exception {
        DocumentSnapshotResponse snapshot = objectMapper.readValue(json, DocumentSnapshotResponse.class);
        attachDocument(snapshot);
    }

    private void handleDocumentPresence(String json) throws Exception {
        DocumentPresenceNotification notification = objectMapper.readValue(json, DocumentPresenceNotification.class);
        if (!isActiveDocument(notification.getDocumentId())) {
            return;
        }

        synchronized (stateLock) {
            activeParticipants.clear();
            if (notification.getActiveParticipants() != null) {
                activeParticipants.addAll(notification.getActiveParticipants());
            }
        }

        System.out.println("[문서] " + notification.getMessage());
    }

    private void handleDocumentLock(String json) throws Exception {
        DocumentLockNotification notification = objectMapper.readValue(json, DocumentLockNotification.class);
        if (!isActiveDocument(notification.getDocumentId())) {
            return;
        }

        if ("REQUEST".equals(notification.getMode())) {
            handleIncomingLockRequest(notification);
        } else if ("OK".equals(notification.getMode())) {
            handleIncomingLockOk(notification);
        }
    }

    private void handleDocumentEdit(String json) throws Exception {
        DocumentEditNotification notification = objectMapper.readValue(json, DocumentEditNotification.class);
        if (!isActiveDocument(notification.getDocumentId())) {
            return;
        }

        synchronized (stateLock) {
            currentLines.clear();
            if (notification.getLines() != null) {
                currentLines.addAll(notification.getLines());
            }

            if (notification.getLogEntry() != null) {
                currentLogs.add(notification.getLogEntry());
            }
        }

        System.out.println("[" + notification.getUsername() + "님이 " + notification.getOperation() + "했습니다.]");
        printCurrentDocument();
    }

    private void handleIncomingLockRequest(DocumentLockNotification notification) {
        if (notification.getClientId() != null && notification.getClientId().equals(clientId)) {
            return;
        }

        LineLockState state = lineLocks.computeIfAbsent(notification.getLineNumber(), key -> new LineLockState());
        boolean sendOkImmediately;

        synchronized (state) {
            if (state.holding) {
                state.deferredRequests.add(new DeferredLockRequest(notification));
                return;
            }

            if (!state.requesting) {
                sendOkImmediately = true;
            } else if (hasPriorityOver(notification, state)) {
                state.deferredRequests.add(new DeferredLockRequest(notification));
                return;
            } else {
                sendOkImmediately = true;
            }
        }

        if (sendOkImmediately) {
            sendLockOk(notification);
        }
    }

    private void handleIncomingLockOk(DocumentLockNotification notification) {
        if (notification.getTargetClientId() != null && !clientId.equals(notification.getTargetClientId())) {
            return;
        }

        LineLockState state = lineLocks.get(notification.getLineNumber());
        if (state == null) {
            return;
        }

        synchronized (state) {
            state.pendingAcks.remove(notification.getClientId());
            state.notifyAll();
        }
    }

    private boolean acquireLineLock(int lineNumber) throws InterruptedException {
        if (activeDocumentId == null) {
            return false;
        }

        LineLockState state = lineLocks.computeIfAbsent(lineNumber, key -> new LineLockState());
        List<String> peers;
        synchronized (stateLock) {
            peers = activeParticipants.stream()
                    .filter(participant -> !participant.equals(username))
                    .toList();
        }

        if (peers.isEmpty()) {
            return true;
        }

        long timestamp;
        synchronized (state) {
            state.requesting = true;
            state.holding = false;
            state.pendingAcks.clear();
            state.pendingAcks.addAll(peers);
            state.deferredRequests.clear();
            timestamp = ++lamportClock;
            state.timestamp = timestamp;
        }

        send(topicPath("locks/request"), new DocumentLockRequest(lineNumber, clientId, username, timestamp));

        long deadline = System.currentTimeMillis() + LOCK_TIMEOUT_MS;
        synchronized (state) {
            while (!state.pendingAcks.isEmpty()) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    state.requesting = false;
                    state.pendingAcks.clear();
                    return false;
                }
                state.wait(Math.min(remaining, 200L));
            }

            state.requesting = false;
            state.holding = true;
            return true;
        }
    }

    private void releaseLineLock(int lineNumber) {
        LineLockState state = lineLocks.get(lineNumber);
        if (state == null) {
            return;
        }

        List<DeferredLockRequest> deferredRequests;
        synchronized (state) {
            state.holding = false;
            deferredRequests = List.copyOf(state.deferredRequests);
            state.deferredRequests.clear();
        }

        for (DeferredLockRequest deferred : deferredRequests) {
            sendLockOk(deferred.notification);
        }
    }

    private boolean hasPriorityOver(DocumentLockNotification incoming, LineLockState state) {
        if (state.timestamp != incoming.getTimestamp()) {
            return state.timestamp < incoming.getTimestamp();
        }
        return clientId.compareTo(incoming.getClientId()) < 0;
    }

    private void sendLockOk(DocumentLockNotification request) {
        send(topicPath("locks/ok"), new DocumentLockResponse(
                request.getLineNumber(),
                clientId,
                username,
                request.getClientId(),
                ++lamportClock
        ));
    }

    private void attachDocument(DocumentSnapshotResponse snapshot) {
        if (snapshot == null) {
            return;
        }

        clearDocumentSubscription();

        synchronized (stateLock) {
            activeDocumentId = snapshot.getDocumentId();
            activeDocumentTopic = snapshot.getTopic();
            currentLines.clear();
            if (snapshot.getLines() != null) {
                currentLines.addAll(snapshot.getLines());
            }
            currentLogs.clear();
            if (snapshot.getEditLogs() != null) {
                currentLogs.addAll(snapshot.getEditLogs());
            }
            activeParticipants.clear();
            if (snapshot.getActiveParticipants() != null) {
                activeParticipants.addAll(snapshot.getActiveParticipants());
            }
        }

        documentSubscription = session.subscribe(activeDocumentTopic, new RawFrameHandler());
        System.out.println("[문서] #" + snapshot.getDocumentId() + " '" + snapshot.getTitle() + "' 에 참여했습니다.");
        printCurrentDocument();
    }

    private void clearActiveDocument() {
        clearDocumentSubscription();
        synchronized (stateLock) {
            activeDocumentId = null;
            activeDocumentTopic = null;
            currentLines.clear();
            currentLogs.clear();
            activeParticipants.clear();
            lineLocks.clear();
        }
    }

    private void clearDocumentSubscription() {
        if (documentSubscription != null) {
            documentSubscription.unsubscribe();
            documentSubscription = null;
        }
    }

    private boolean requireLogin() {
        if (username == null) {
            System.out.println("[Client] 로그인 후 사용하세요.");
            return false;
        }
        return true;
    }

    private boolean requireActiveDocument() {
        if (!requireLogin()) {
            return false;
        }

        if (activeDocumentId == null) {
            System.out.println("[Client] 먼저 문서를 생성하거나 참여하세요.");
            return false;
        }
        return true;
    }

    private boolean isActiveDocument(Long documentId) {
        return documentId != null && documentId.equals(activeDocumentId);
    }

    private int parseInt(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[Client] 올바른 숫자를 입력하세요.");
            return -1;
        }
    }

    private String topicPath(String suffix) {
        if (activeDocumentId == null) {
            throw new IllegalStateException("활성 문서가 없습니다.");
        }
        return "/app/documents/" + activeDocumentId + "/" + suffix;
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

    private <T> T postJson(String url, Object payload, Class<T> responseType) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseType);
        }

        throw new IllegalStateException(extractErrorMessage(response.body()));
    }

    private List<DocumentSummaryResponse> fetchMyDocuments() throws Exception {
        String query = username == null ? "" : "?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_BASE + query))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(
                    response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, DocumentSummaryResponse.class)
            );
        }

        throw new IllegalStateException(extractErrorMessage(response.body()));
    }

    private String extractErrorMessage(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.hasNonNull("message")) {
                return root.get("message").asText();
            }
        } catch (Exception ignored) {
        }
        return body;
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
                return new String(bytes, StandardCharsets.UTF_8);
            }

            return payload.toString();
        }

        @Override
        protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
            return payload.toString().getBytes(StandardCharsets.UTF_8);
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

    private static class LineLockState {
        private boolean requesting;
        private boolean holding;
        private long timestamp;
        private final Set<String> pendingAcks = new HashSet<>();
        private final List<DeferredLockRequest> deferredRequests = new ArrayList<>();
    }

    private static class DeferredLockRequest {
        private final DocumentLockNotification notification;

        private DeferredLockRequest(DocumentLockNotification notification) {
            this.notification = notification;
        }
    }
}
