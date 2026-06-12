import { DocumentLine, SaveStatus, TextSessionItem } from "../types";
import { postJson, requestJson } from "./http";

export interface CreateSessionRequest {
  username: string;
  title: string;
  initialLines: DocumentLine[];
}

export interface DocumentSummaryResponse {
  documentId: number;
  title: string;
  ownerUsername: string;
  lineCount: number;
  memberCount: number;
  activeParticipantCount: number;
  topic: string;
}

export interface SessionResponse extends TextSessionItem {
  lines: DocumentLine[];
  saveStatus: SaveStatus;
  lastEditor: string;
}

export interface DocumentSnapshotResponse {
  type: "DOCUMENT_SNAPSHOT";
  documentId: number;
  title: string;
  ownerUsername: string;
  lines: string[];
  content: string;
  members: string[];
  activeParticipants: string[];
  editLogs: Array<{
    sequence: number;
    timestamp: string;
    username: string;
    operation: string;
    lineNumber: number;
    beforeText: string | null;
    afterText: string | null;
  }>;
  topic: string;
}

export interface JoinSessionRequest {
  username: string;
  documentId: number;
}

export interface SaveSessionRequest {
  sessionId: string;
  documentId: number;
  title: string;
  lines: DocumentLine[];
  username: string;
}

export interface SaveSessionResponse {
  success: boolean;
  fileName: string;
  message: string;
}

export interface SavedFileInfo {
  fileName: string;
  documentId: number;
  title: string;
  updatedAt: string;
}

export const sessionApi = {
  listDocuments(username?: string) {
    const query = username ? `?username=${encodeURIComponent(username)}` : "";
    return requestJson<DocumentSummaryResponse[]>(`/api/documents${query}`);
  },

  getDocument(documentId: number) {
    return requestJson<DocumentSnapshotResponse>(`/api/documents/${documentId}`);
  },

  createDocument(request: CreateSessionRequest) {
    return postJson<CreateSessionRequest, DocumentSnapshotResponse>(
      "/api/documents",
      request,
    );
  },

  joinDocument(documentId: number, request: { username: string }) {
    return postJson<{ username: string }, DocumentSnapshotResponse>(
      `/api/documents/${documentId}/join`,
      request,
    );
  },

  leaveDocument(documentId: number, request: { username: string }) {
    return postJson<{ username: string }, DocumentSnapshotResponse>(
      `/api/documents/${documentId}/leave`,
      request,
    );
  },

  getActiveSessions() {
    return requestJson<TextSessionItem[]>("/api/sessions/active");
  },

  createSession(request: CreateSessionRequest) {
    return postJson<CreateSessionRequest, SessionResponse>(
      "/api/sessions/create",
      request,
    );
  },

  joinSession(request: JoinSessionRequest) {
    return postJson<JoinSessionRequest, SessionResponse>(
      "/api/sessions/join",
      request,
    );
  },

  saveSession(request: SaveSessionRequest) {
    return postJson<SaveSessionRequest, SaveSessionResponse>(
      "/api/sessions/save",
      request,
    );
  },

  getSavedFiles() {
    return requestJson<SavedFileInfo[]>("/api/sessions/saved");
  },
};
