package com.dsc.sharededitor.domain.connection;

import com.dsc.sharededitor.domain.auth.InMemoryUserStore;
import com.dsc.sharededitor.domain.session.SessionManager;
import com.dsc.sharededitor.dto.message.LoginRequestMessage;
import com.dsc.sharededitor.dto.message.LoginResponseMessage;
import com.dsc.sharededitor.dto.message.MessageType;

public class ConnectionHandler {

    private final InMemoryUserStore userStore;
    private final SessionManager sessionManager;

    public ConnectionHandler(InMemoryUserStore userStore, SessionManager sessionManager) {
        this.userStore = userStore;
        this.sessionManager = sessionManager;
    }

    public LoginResponseMessage handleLogin(String sessionId, LoginRequestMessage request) {

        String username = request.getUsername();
        String password = request.getPassword();

        // 계정 검증
        if (!userStore.matches(username, password)) {
            return LoginResponseMessage.fail(username);
        }

        // 중복 로그인 체크
        if (sessionManager.isUserOnline(username)) {
            return LoginResponseMessage.fail(username);
        }

        // 세션 등록
        sessionManager.addUser(username, sessionId);

        return LoginResponseMessage.success(username);
    }
}