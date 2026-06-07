import { DocumentLine, EventLogMessage, Participant, TextSessionItem } from "./types";

export const mockSessions: TextSessionItem[] = [
  { sessionId: "session-1", documentNumber: "38172946", title: "분산 시스템 최종 보고서 초안", participantCount: 3, status: "active" },
  { sessionId: "session-2", documentNumber: "49281730", title: "시연 영상 대본", participantCount: 1, status: "saved" },
  { sessionId: "session-3", documentNumber: "73492815", title: "회의 메모", participantCount: 2, status: "saved" }
];

export const mockLines: DocumentLine[] = [
  { lineId: "line-1", lineNumber: 1, text: "공유 텍스트 편집 시스템은 여러 클라이언트가 동일한 텍스트 세션에 참여해 문서를 함께 수정하는 시스템이다.", editor: null },
  { lineId: "line-2", lineNumber: 2, text: "Late-comer 클라이언트는 세션에 늦게 참여해도 서버로부터 현재 문서 상태를 전달받아 동일한 화면을 확인할 수 있다.", editor: "user2" },
  { lineId: "line-3", lineNumber: 3, text: "동시성 제어는 줄 단위 critical section을 기준으로 하며, 같은 줄은 동시에 한 사용자만 편집할 수 있도록 표시한다.", editor: "user1" },
  { lineId: "line-4", lineNumber: 4, text: "편집 과정 공유를 위해 편집 시작, 편집 종료, 추가, 수정, 삭제, 세션 저장 이벤트를 우측 로그 패널에 표시한다.", editor: null }
];

export const mockParticipants: Participant[] = [
  { username: "user1", status: "editing", description: "line-3 편집 중" },
  { username: "user2", status: "editing", description: "line-2 편집 중" },
  { username: "user3", status: "online", description: "접속 중" }
];

export const mockEventLogs: EventLogMessage[] = [
  { message: "user1님이 문서 번호 38172946 세션에 참여했습니다.", timestamp: "23:18", type: "success" },
  { message: "서버로부터 현재 문서 상태를 수신했습니다. (Late-comer 동기화)", timestamp: "23:19", type: "success" },
  { message: "user2님이 line-2 편집을 시작했습니다.", timestamp: "23:20", type: "info" },
  { message: "user1님이 line-3 편집 권한을 획득했습니다.", timestamp: "23:21", type: "info" }
];
