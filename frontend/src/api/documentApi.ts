import { postJson } from "./http";

export interface TextInsertRequest {
  position: number;
  text: string;
}

export interface TextUpdateRequest {
  position: number;
  length: number;
  text: string;
}

export interface TextDeleteRequest {
  position: number;
  length: number;
}

export interface DocumentUpdateNotification {
  type: "DOCUMENT_UPDATE";
  documentId: number;
  operation: "INSERT" | "UPDATE" | "DELETE";
  username: string;
  content: string;
}

export const documentApi = {
  insert(request: TextInsertRequest) {
    // STOMP /app/document/insert 기준
    return postJson<TextInsertRequest, DocumentUpdateNotification>(
      "/api/document/insert",
      request,
    );
  },

  update(request: TextUpdateRequest) {
    // STOMP /app/document/update 기준
    return postJson<TextUpdateRequest, DocumentUpdateNotification>(
      "/api/document/update",
      request,
    );
  },

  delete(request: TextDeleteRequest) {
    // STOMP /app/document/delete 기준
    return postJson<TextDeleteRequest, DocumentUpdateNotification>(
      "/api/document/delete",
      request,
    );
  },
};
