# Status

## 완료

- 최종 요구사항 기준 구현 상태 정리
- 문서 목록 조회
- 새 문서 생성
- 문서 참여 및 late-comer snapshot 로드
- 실시간 편집 반영
- presence 실시간 반영
- 저장 버튼을 눌렀을 때만 파일 저장
- documentId 기반 식별자 통일
- activeParticipants 기준의 분산 락 요청/응답 흐름
- 락 timeout 처리
- 락 승인 진행 표시
- 원격 INSERT/DELETE 시 selection 및 락 정리
- 서버 저장 파일 목록 조회
- 저장된 파일 선택 후 로드

## 검증

- 백엔드 Gradle 테스트 통과
- 프론트엔드 TypeScript / Vite 빌드 통과

## 참고

- 지금 구조는 테스트 유저 로그인만 사용한다.
- 실제 인증/비밀번호 흐름은 우선순위에서 제외한다.
- 락 승인 대상은 `members`가 아니라 `activeParticipants`다.
