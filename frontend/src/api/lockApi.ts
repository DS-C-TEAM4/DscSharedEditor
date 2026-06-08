import { stompClient } from "./stompClient";

export interface LockRequest {
  lineNumber: number;
  clientId: string;
  username: string;
  timestamp: number;
}

export interface LockReply {
  lineNumber: number;
  clientId: string;
  username: string;
  targetClientId: string;
  timestamp: number;
}

export const lockApi = {
  requestLock(documentId: number, request: LockRequest) {
    stompClient.publish(`/app/documents/${documentId}/locks/request`, request);
  },

  replyLock(documentId: number, request: LockReply) {
    stompClient.publish(`/app/documents/${documentId}/locks/ok`, request);
  },
};
