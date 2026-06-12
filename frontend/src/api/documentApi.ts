import { stompClient } from "./stompClient";

export interface TextInsertRequest {
  documentId: number;
  lineNumber: number;
  text: string;
}

export interface TextUpdateRequest {
  documentId: number;
  lineNumber: number;
  text: string;
  logEdit?: boolean;
}

export interface TextDeleteRequest {
  documentId: number;
  lineNumber: number;
}

export interface DocumentUpdateNotification {
  type: "DOCUMENT_EDIT";
  documentId: number;
  operation: "INSERT" | "UPDATE" | "DELETE";
  username: string;
  content: string;
  lines: string[];
  lineNumber: number;
  logEntry?: {
    sequence: number;
    timestamp: string;
    username: string;
    operation: string;
    lineNumber: number;
    beforeText: string | null;
    afterText: string | null;
  } | null;
}

export interface DocumentPresenceNotification {
  type: "DOCUMENT_PRESENCE";
  documentId: number;
  username: string;
  status: "CREATED" | "JOINED" | "LEFT";
  message: string;
  activeParticipants: string[];
}

export interface DocumentSaveNotification {
  type: "DOCUMENT_SAVE";
  documentId: number;
  username: string;
  message: string;
  timestamp: string;
}

export type DocumentTopicNotification =
  | DocumentUpdateNotification
  | DocumentPresenceNotification
  | DocumentSaveNotification
  | {
      type: "DOCUMENT_LOCK";
      documentId: number;
      mode: "REQUEST" | "OK";
      lineNumber: number;
      clientId: string;
      username: string;
      targetClientId: string | null;
      timestamp: number;
    };

export const documentApi = {
  insert(request: TextInsertRequest) {
    stompClient.publish(
      `/app/documents/${request.documentId}/lines/insert`,
      {
        lineNumber: request.lineNumber,
        text: request.text,
      },
    );
  },

  update(request: TextUpdateRequest) {
    stompClient.publish(
      `/app/documents/${request.documentId}/lines/update`,
      {
        lineNumber: request.lineNumber,
        text: request.text,
        logEdit: request.logEdit,
      },
    );
  },

  delete(request: TextDeleteRequest) {
    stompClient.publish(
      `/app/documents/${request.documentId}/lines/delete`,
      {
        lineNumber: request.lineNumber,
      },
    );
  },

  subscribeDocumentUpdates(
    documentId: number,
    handler: (message: DocumentTopicNotification) => void,
  ) {
    return stompClient.subscribe<DocumentTopicNotification>(
      `/topic/documents/${documentId}`,
      handler,
    );
  },
};
