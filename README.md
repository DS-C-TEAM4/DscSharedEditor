# DscSharedEditor

실시간 공유 텍스트 편집기를 구현하는 분산 시스템 프로젝트입니다.

## 프로젝트 목표

- 다중 클라이언트가 서버에 접속
- 사용자 접속 상태 공유
- 이후 실시간 공동 편집 기능 확장 예정

## 중간 구현 기능

### 클라이언트-서버 구조 기본 기능

- 사용자 로그인
- 클라이언트 종료 감지
- 접속 / 해제 상태 브로드캐스트

## 기술 스택

- Java 21
- Spring Boot
- WebSocket
- STOMP

## 실행 방법

```bash
./gradlew bootRun
```

## STOMP 프로토콜

| 구분 | 경로 | 설명 |
|---|---|---|
| Endpoint | `/ws` | WebSocket 연결 |
| Send | `/app/auth/login` | 로그인 요청 |
| Send | `/app/auth/logout` | 로그아웃 요청 |
| Subscribe | `/topic/user/{username}` | 로그인 결과 개인 응답 |
| Subscribe | `/topic/global` | 접속/해제 알림 브로드캐스트 |

## 테스트 계정

| 아이디 | 비밀번호 |
|---|---|
| user1 | 1234 |
| user2 | 2345 |
| user3 | 3456 |
| user4 | 4567 |