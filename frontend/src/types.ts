export type SaveStatus = "서버 저장됨" | "저장 필요";
export type SessionStatus = "active" | "saved";
export type EventLogType = "info" | "success" | "warning";

export interface TextSessionItem {
  sessionId: string;
  documentId: number;
  title: string;
  participantCount: number;
  status: SessionStatus;
}

export interface DocumentLine {
  lineId: string;
  lineNumber: number;
  text: string;
  editor: string | null;
}

export interface Participant {
  username: string;
  status: "online" | "editing";
  description: string;
}

export interface EventLogMessage {
  message: string;
  timestamp: string;
  type: EventLogType;
}
export interface TextSessionState extends TextSessionItem {
  lines: DocumentLine[];
  participants: Participant[];
  eventLogs: EventLogMessage[];
  saveStatus: SaveStatus;
  lastEditor: string;
}
