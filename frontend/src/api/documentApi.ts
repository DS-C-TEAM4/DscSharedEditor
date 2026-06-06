import { stompClient } from "./stompClient";

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
    stompClient.publish("/app/document/insert", request);
  },

  update(request: TextUpdateRequest) {
    stompClient.publish("/app/document/update", request);
  },

  delete(request: TextDeleteRequest) {
    stompClient.publish("/app/document/delete", request);
  },

  subscribeDocumentUpdates(
    handler: (message: DocumentUpdateNotification) => void,
  ) {
    return stompClient.subscribe<DocumentUpdateNotification>(
      "/topic/document",
      handler,
    );
  },
};
