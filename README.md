# DscSharedEditor

실시간 공유 텍스트 편집기를 구현하는 분산 시스템 프로젝트입니다.

## 프로젝트 목표

- 다중 클라이언트가 서버에 접속
- 사용자 접속 상태 공유
- 여러 텍스트 세션의 생성, 저장, 로드
- Late-comer 클라이언트의 현재 문서 상태 동기화
- 줄 단위 mutual exclusion을 통한 동시 편집 제어
- 편집 결과와 편집 과정의 실시간 공유

## 최종 구현 기능

### 클라이언트-서버 구조 기본 기능

- 사용자 로그인
- 클라이언트 종료 감지
- 접속 / 해제 상태 브로드캐스트

### 공유 텍스트 편집 기능

- 여러 개의 텍스트 세션 생성 및 목록 조회
- 텍스트 세션 저장 및 저장된 세션 로드
- Late-comer 클라이언트에게 현재 문서 내용, 참여자, 편집 로그 전달
- 줄 단위 텍스트 추가 / 수정 / 삭제
- 편집 결과와 편집 로그를 모든 참여 클라이언트에 실시간 공유
- Lamport timestamp 기반 줄 단위 mutual exclusion 요청 / 승인

## 기술 스택

- Java 21
- Spring Boot
- WebSocket/STOMP
- React
- TypeScript
- Vite
- 파일 기반 문서 저장

## 실행 방법

서버와 프론트엔드를 별도 터미널에서 실행합니다.

**터미널 1 - 서버**
```bash
./gradlew bootRun
```

**터미널 2 - 프론트엔드**
```bash
cd frontend
npm install
npm run dev
```

**콘솔 클라이언트 실행이 필요한 경우**
```bash
./gradlew runClient --console=plain
```

## 주요 화면 기능

| 기능 | 설명 |
|---|---|
| 로그인 | 테스트 계정으로 서버 접속 |
| 문서 목록 조회 | 서버에 저장된 텍스트 세션 목록 확인 |
| 새 문서 생성 | 새로운 공유 텍스트 세션 생성 |
| 문서 참여 | 문서 ID를 기준으로 기존 세션 참여 |
| 문서 편집 | 줄 단위 추가 / 수정 / 삭제 |
| 세션 저장 | 현재 문서 내용과 편집 로그를 서버 파일로 저장 |
| 저장 파일 로드 | 서버에 저장된 세션 파일 목록에서 문서 로드 |

## REST / STOMP 프로토콜

| 구분 | 경로 | 설명 |
|---|---|---|
| Endpoint | `/ws` | WebSocket 연결 |
| Send | `/app/auth/login` | 로그인 요청 |
| Send | `/app/auth/logout` | 로그아웃 요청 |
| REST | `GET /api/documents` | 문서 목록 조회 |
| REST | `GET /api/documents/{documentId}` | 문서 snapshot 조회 |
| REST | `POST /api/documents` | 새 문서 생성 |
| REST | `POST /api/documents/{documentId}/join` | 문서 참여 및 late-comer snapshot 로드 |
| REST | `POST /api/sessions/save` | 세션 저장 |
| REST | `GET /api/sessions/saved` | 저장된 파일 목록 조회 |
| Send | `/app/documents/{documentId}/lines/insert` | 줄 추가 |
| Send | `/app/documents/{documentId}/lines/update` | 줄 수정 |
| Send | `/app/documents/{documentId}/lines/delete` | 줄 삭제 |
| Send | `/app/documents/{documentId}/locks/request` | 줄 잠금 요청 |
| Send | `/app/documents/{documentId}/locks/ok` | 줄 잠금 승인 |
| Subscribe | `/topic/client/{clientId}` | 로그인 결과 개인 응답 |
| Subscribe | `/topic/global` | 접속/해제 알림 브로드캐스트 |
| Subscribe | `/topic/documents/{documentId}` | 문서 편집 / 참여자 / 잠금 이벤트 공유 |

## 테스트 계정

| 아이디 | 비밀번호 |
|---|---|
| user1 | 1234 |
| user2 | 2345 |
| user3 | 3456 |
| user4 | 4567 |
