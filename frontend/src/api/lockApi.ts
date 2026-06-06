export interface LockRequest {
  username: string;
  sessionId: string;
  lineId: string;
  timestamp: number;
}

export interface LockResponse {
  type: "LOCK_GRANTED" | "LOCK_DENIED";
  lineId: string;
  lockedBy: string;
}

export interface UnlockRequest {
  username: string;
  sessionId: string;
  lineId: string;
  finalText?: string;
}

export interface LineUpdateRequest {
  username: string;
  sessionId: string;
  lineId: string;
  text: string;
}

export interface AddLineRequest {
  username: string;
  sessionId: string;
  afterLineId?: string;
  position?: "end";
  newLineText: string;
}

export interface DeleteLineRequest {
  username: string;
  sessionId: string;
  lineId: string;
}

export const lockApi = {
  requestLock(_request: LockRequest): Promise<LockResponse> {
    // TODO(server): lineId 기반 lock 요청/응답
    return Promise.reject(new Error("lock API is not connected yet"));
  },

  releaseLock(_request: UnlockRequest) {
    // TODO(server): 편집 종료 시 lineId 기반 lock release 메시지 전송
  },

  updateLine(_request: LineUpdateRequest) {
    // TODO(server): lineId 기반 줄 수정 broadcast 경로와 연결
  },

  addLine(_request: AddLineRequest) {
    // TODO(server): lineId 기반 줄 추가 broadcast 경로와 연결
  },

  deleteLine(_request: DeleteLineRequest) {
    // TODO(server): lineId 기반 줄 삭제 검증/응답 경로와 연결
  },
};
