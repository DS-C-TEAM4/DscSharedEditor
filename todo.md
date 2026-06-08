# TODO

## 현재 동작하는 것

- 문서 목록 조회
- 새 문서 생성
- 문서 참여 및 late-comer snapshot 로드
- 실시간 편집 반영
- presence 실시간 반영
- 저장 버튼을 눌렀을 때만 파일 저장
- documentId 기반 식별자 통일
- activeParticipants 기준의 분산 락 요청/응답 흐름

## 남은 작업

### 1. 락 안정화

- 락 승인 timeout 처리
- `N명 중 M명 승인` 같은 진행 표시
- presence가 stale할 때의 예외 처리
- 락 해제/재획득 로그 정리

### 2. 편집 정합성 보강

- 다른 클라이언트 편집 중 selection 유지 규칙 정리
- insert/delete 후 현재 선택 줄 보정
- 원격 업데이트와 로컬 입력 충돌 처리

### 3. 저장/로드 UX 정리

- 저장 파일 목록 조회 화면 연결
- 저장된 파일 선택 후 로드 흐름 정리
- 저장 결과와 로드 결과를 UI에서 더 명확히 구분

### 4. presence/UI 정리

- 접속 사용자와 편집 중 사용자 상태를 더 명확하게 표시
- members와 activeParticipants를 화면에서 어떻게 보여줄지 확정

### 5. 프론트 빌드 검증

- `frontend/node_modules` 설치
- `npm run build` 또는 `tsc` 실행
- 타입 오류와 런타임 경계 조건 수정

## 참고

- 지금 구조는 테스트 유저 로그인만 사용한다.
- 실제 인증/비밀번호 흐름은 우선순위에서 제외한다.
- 락 승인 대상은 `members`가 아니라 `activeParticipants`다.
- 현재 구현은 기능적으로는 동작하지만, 프론트 빌드 검증은 아직 이 환경에서 못 돌렸다.
