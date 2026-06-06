export type SaveStatus = "저장됨" | "저장되지 않음";
export type SessionStatus = "active" | "saved";
export type EventLogType = "info" | "success" | "warning";

export interface TextSessionItem {
  sessionId: string;
  documentNumber: string;
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
  saveStatus: SaveStatus;
  lastEditor: string;
}
