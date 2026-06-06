import { DocumentLine, SaveStatus, TextSessionItem } from "../types";
import { postJson, requestJson } from "./http";

export interface CreateSessionRequest {
  username: string;
  title: string;
  initialLines: DocumentLine[];
}

export interface SessionResponse extends TextSessionItem {
  lines: DocumentLine[];
  saveStatus: SaveStatus;
  lastEditor: string;
}

export interface JoinSessionRequest {
  username: string;
  documentNumber: string;
}

export interface SaveSessionRequest {
  sessionId: string;
  documentNumber: string;
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
  documentNumber: string;
  title: string;
  updatedAt: string;
}

export const sessionApi = {
  getActiveSessions() {
    // TODO(server): 여러 텍스트 세션 목록 조회 API 필요
    return requestJson<TextSessionItem[]>("/api/sessions/active");
  },

  createSession(request: CreateSessionRequest) {
    // TODO(server): documentNumber 생성과 초기 줄 상태 생성 담당
    return postJson<CreateSessionRequest, SessionResponse>(
      "/api/sessions/create",
      request,
    );
  },

  joinSession(request: JoinSessionRequest) {
    // TODO(server): Late-comer가 현재 문서 상태 전체를 받도록 응답 구조 조정 필요
    return postJson<JoinSessionRequest, SessionResponse>(
      "/api/sessions/join",
      request,
    );
  },

  saveSession(request: SaveSessionRequest) {
    // TODO(server): 서버가 현재 세션을 JSON 파일로 저장하고 파일명 반환
    return postJson<SaveSessionRequest, SaveSessionResponse>(
      "/api/sessions/save",
      request,
    );
  },

  getSavedFiles() {
    // TODO(server): 서버 저장 JSON 파일 목록 조회 API 필요
    return requestJson<SavedFileInfo[]>("/api/sessions/saved");
  },
};
