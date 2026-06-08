import { useEffect, useRef, useState } from "react";
import { AppHeader } from "./components/layout/AppHeader";
import { InfoBar } from "./components/layout/InfoBar";
import { TextSessionList } from "./components/session/TextSessionList";
import { DocumentEditor } from "./components/editor/DocumentEditor";
import { StatusPanel } from "./components/status/StatusPanel";
import { LoginPage } from "./components/pages/LoginPage";
import { SessionEntryPage } from "./components/pages/SessionEntryPage";
import { documentApi, lockApi, sessionApi, stompClient } from "./api";
import type {
  DocumentPresenceNotification,
  DocumentTopicNotification,
  DocumentUpdateNotification,
} from "./api/documentApi";
import type {
  DocumentSnapshotResponse,
  DocumentSummaryResponse,
  SavedFileInfo,
} from "./api/sessionApi";
import {
  mockEventLogs,
  mockLines,
  mockParticipants,
  mockSessions,
} from "./mockData";
import { DocumentLine, TextSessionState } from "./types";

type Screen = "login" | "sessionEntry" | "editor";
type DocumentLockNotification = Extract<
  DocumentTopicNotification,
  { type: "DOCUMENT_LOCK" }
>;

interface LocalLineLockState {
  status: "requesting" | "locked";
  timestamp: number;
  lineId: string | null;
  peerCount: number;
  pendingAcks: Set<string>;
  deferredRequests: DocumentLockNotification[];
  timeoutId: number | null;
}

const LOCK_TIMEOUT_MS = 10000;

function renumberLines(lines: DocumentLine[]) {
  return lines.map((line, index) => ({
    ...line,
    lineNumber: index + 1,
  }));
}

function createClientId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return `client-${crypto.randomUUID()}`;
  }

  return `client-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function createSessionNumber() {
  return Math.floor(10000000 + Math.random() * 90000000);
}

function formatTimestamp(value: string) {
  return new Date(value).toLocaleTimeString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function mapSummaryToSession(summary: DocumentSummaryResponse): TextSessionState {
  return {
    sessionId: `document-${summary.documentId}`,
    documentId: summary.documentId,
    title: summary.title,
    participantCount: summary.memberCount,
    status: summary.activeParticipantCount > 0 ? "active" : "saved",
    lines: [],
    participants: [],
    eventLogs: [],
    saveStatus: "서버 저장됨",
    lastEditor: summary.ownerUsername,
  };
}

function mapSnapshotToSession(snapshot: DocumentSnapshotResponse): TextSessionState {
  const uniqueUsers = Array.from(
    new Set([...snapshot.members, ...snapshot.activeParticipants]),
  );
  const activeParticipantSet = new Set(snapshot.activeParticipants);

  return {
    sessionId: `document-${snapshot.documentId}`,
    documentId: snapshot.documentId,
    title: snapshot.title,
    participantCount: snapshot.members.length,
    status: snapshot.activeParticipants.length > 0 ? "active" : "saved",
    lines: snapshot.lines.map((text, index) => ({
      lineId: `document-${snapshot.documentId}-line-${index + 1}`,
      lineNumber: index + 1,
      text,
      editor: null,
    })),
    participants: uniqueUsers.map((username) => ({
      username,
      status: activeParticipantSet.has(username) ? "editing" : "online",
      description: activeParticipantSet.has(username) ? "편집 중" : "접속 중",
    })),
    eventLogs: snapshot.editLogs.map((log) => ({
      message: `${log.username}님이 ${log.lineNumber + 1}행 ${log.operation === "INSERT" ? "추가" : log.operation === "UPDATE" ? "수정" : "삭제"}했습니다.`,
      timestamp: formatTimestamp(log.timestamp),
      type: log.operation === "DELETE" ? "warning" : "info",
    })),
    saveStatus: "서버 저장됨",
    lastEditor:
      snapshot.editLogs.length > 0
        ? snapshot.editLogs[snapshot.editLogs.length - 1].username
        : snapshot.ownerUsername,
  };
}

function mapDocumentUpdateToSession(
  session: TextSessionState,
  notification: DocumentUpdateNotification,
): TextSessionState {
  const nextLines = notification.lines.map((text, index) => ({
    lineId:
      session.lines[index]?.lineId ??
      `document-${notification.documentId}-line-${index + 1}`,
    lineNumber: index + 1,
    text,
    editor: null,
  }));

  const logEntry = notification.logEntry;
  const nextLog =
    logEntry == null
      ? {
          message: `${notification.username}님이 문서를 편집했습니다.`,
          timestamp: new Date().toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit",
          }),
          type: notification.operation === "DELETE" ? "warning" : "info",
        }
      : {
          message: `${logEntry.username}님이 ${logEntry.lineNumber + 1}행 ${logEntry.operation === "INSERT" ? "추가" : logEntry.operation === "UPDATE" ? "수정" : "삭제"}했습니다.`,
          timestamp: formatTimestamp(logEntry.timestamp),
          type: logEntry.operation === "DELETE" ? "warning" : "info",
        };

  return {
    ...session,
    lines: nextLines,
    eventLogs: [...session.eventLogs, nextLog],
    lastEditor: notification.username,
    saveStatus: "저장 필요",
  };
}

function mapDocumentPresenceToSession(
  session: TextSessionState,
  notification: DocumentPresenceNotification,
): TextSessionState {
  const activeParticipantSet = new Set(notification.activeParticipants);
  const knownUsers = Array.from(
    new Set([
      ...session.participants.map((participant) => participant.username),
      ...notification.activeParticipants,
      notification.username,
    ]),
  );

  const participants = knownUsers.map((username) => ({
    username,
    status: activeParticipantSet.has(username) ? "editing" : "online",
    description:
      username === notification.username
        ? notification.message
        : activeParticipantSet.has(username)
          ? "편집 중"
          : "접속 중",
  }));

  return {
    ...session,
    participants,
    participantCount: notification.activeParticipants.length,
    status: notification.activeParticipants.length > 0 ? "active" : "saved",
    eventLogs: [
      ...session.eventLogs,
      {
        message: notification.message,
        timestamp: new Date().toLocaleTimeString("ko-KR", {
          hour: "2-digit",
          minute: "2-digit",
        }),
        type: notification.status === "LEFT" ? "warning" : "success",
      },
    ],
  };
}

function createInitialSessions(): TextSessionState[] {
  return mockSessions.map((session, index) => ({
    ...session,
    lines:
      index === 0
        ? mockLines
        : [
            {
              lineId: `${session.sessionId}-line-1`,
              lineNumber: 1,
              text: `${session.title} 문서의 목업 내용입니다.`,
              editor: null,
            },
          ],
    saveStatus: index === 0 ? "저장 필요" : "서버 저장됨",
    lastEditor: "user1",
    participants:
      index === 0
        ? mockParticipants
        : [
            {
              username: "user1",
              status: "online",
              description: "접속 중",
            },
          ],
    eventLogs:
      index === 0
        ? mockEventLogs
        : [
            {
              message: `${session.title} 세션 상태를 불러왔습니다.`,
              timestamp: "23:22",
              type: "info",
            },
          ],
  }));
}

export default function App() {
  const [screen, setScreen] = useState<Screen>("login");
  const [username, setUsername] = useState("user1");
  const [sessions, setSessions] = useState<TextSessionState[]>(
    createInitialSessions,
  );
  const [currentDocumentId, setCurrentDocumentId] = useState(38172946);
  const [selectedLineId, setSelectedLineId] = useState<string | null>(null);
  const [editableLineId, setEditableLineId] = useState<string | null>(null);
  const [lockNotice, setLockNotice] = useState<string>("편집할 줄을 선택하세요.");
  const [savedFiles, setSavedFiles] = useState<SavedFileInfo[]>([]);
  const [socketReady, setSocketReady] = useState(false);
  const clientIdRef = useRef(createClientId());
  const lamportClockRef = useRef(0);
  const activeParticipantsRef = useRef<string[]>([]);
  const lineLocksRef = useRef(new Map<number, LocalLineLockState>());

  const currentSession =
    sessions.find((session) => session.documentId === currentDocumentId) ??
    sessions[0];

  const pushEventLog = (
    message: string,
    type: "info" | "success" | "warning" = "info",
  ) => {
    updateCurrentSession((session) => ({
      ...session,
      eventLogs: [
        ...session.eventLogs,
        {
          message,
          timestamp: new Date().toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit",
          }),
          type,
        },
      ],
    }));
  };

  const updateCurrentSession = (
    updater: (session: TextSessionState) => TextSessionState,
  ) => {
    setSessions((prev) =>
      prev.map((session) =>
        session.documentId === currentSession.documentId
          ? updater(session)
          : session,
      ),
    );
  };

  const syncActiveParticipants = (participants: string[]) => {
    activeParticipantsRef.current = participants;
  };

  const getLineIdByIndex = (lineNumber: number) =>
    currentSession.lines[lineNumber]?.lineId ?? null;

  const nextLamportTimestamp = () => {
    lamportClockRef.current += 1;
    return lamportClockRef.current;
  };

  const observeLamportTimestamp = (remoteTimestamp: number) => {
    lamportClockRef.current = Math.max(lamportClockRef.current, remoteTimestamp) + 1;
    return lamportClockRef.current;
  };

  const getLineLockState = (lineNumber: number) => {
    let state = lineLocksRef.current.get(lineNumber);
    if (!state) {
      state = {
        status: "requesting",
        timestamp: 0,
        lineId: null,
        peerCount: 0,
        pendingAcks: new Set(),
        deferredRequests: [],
        timeoutId: null,
      };
      lineLocksRef.current.set(lineNumber, state);
    }
    return state;
  };

  const clearLockTimeout = (state: LocalLineLockState) => {
    if (state.timeoutId != null) {
      window.clearTimeout(state.timeoutId);
      state.timeoutId = null;
    }
  };

  const releaseAllLineLocks = () => {
    const lineNumbers = Array.from(lineLocksRef.current.keys());
    lineNumbers.forEach((lineNumber) => {
      const state = lineLocksRef.current.get(lineNumber);
      if (!state) return;
      clearLockTimeout(state);
      sendDeferredReplies(lineNumber);
    });
    lineLocksRef.current.clear();
    setSelectedLineId(null);
    setEditableLineId(null);
    setLockNotice("편집할 줄을 선택하세요.");
  };

  const comparePriority = (
    localTimestamp: number,
    localClientId: string,
    incoming: DocumentLockNotification,
  ) => {
    if (localTimestamp !== incoming.timestamp) {
      return localTimestamp < incoming.timestamp;
    }

    return localClientId < incoming.clientId;
  };

  const sendDeferredReplies = (lineNumber: number) => {
    const state = lineLocksRef.current.get(lineNumber);
    if (!state || state.deferredRequests.length === 0) {
      return;
    }

    const deferredRequests = [...state.deferredRequests];
    state.deferredRequests = [];

    deferredRequests.forEach((request) => {
      lockApi.replyLock(currentSession.documentId, {
        lineNumber: request.lineNumber,
        clientId: clientIdRef.current,
        username,
        targetClientId: request.clientId,
        timestamp: nextLamportTimestamp(),
      });
    });
  };

  const releaseLineLock = (lineId: string | null) => {
    if (!lineId) {
      return;
    }

    const lineNumber = currentSession.lines.findIndex((line) => line.lineId === lineId);
    if (lineNumber < 0) {
      if (editableLineId === lineId) {
        setEditableLineId(null);
      }
      return;
    }

    const state = lineLocksRef.current.get(lineNumber);
    if (!state) {
      if (editableLineId === lineId) {
        setEditableLineId(null);
      }
      return;
    }

    clearLockTimeout(state);
    sendDeferredReplies(lineNumber);
    lineLocksRef.current.delete(lineNumber);

    if (editableLineId === lineId) {
      setEditableLineId(null);
    }
  };

  const requestLineLock = (lineId: string, lineNumberOverride?: number) => {
    if (screen !== "editor") {
      return;
    }

    const lineNumber =
      lineNumberOverride ?? currentSession.lines.findIndex((line) => line.lineId === lineId);
    if (lineNumber < 0) {
      return;
    }

    const peers = activeParticipantsRef.current.filter((participant) => participant !== username);
    const state = getLineLockState(lineNumber);
    const timestamp = nextLamportTimestamp();

    clearLockTimeout(state);
    state.status = peers.length === 0 ? "locked" : "requesting";
    state.timestamp = timestamp;
    state.lineId = lineId;
    state.peerCount = peers.length;
    state.pendingAcks = new Set(peers);
    state.deferredRequests = [];

    if (peers.length === 0) {
      setEditableLineId(lineId);
      setLockNotice(`${lineNumber + 1}행 잠금을 즉시 획득했습니다.`);
      pushEventLog(`${lineNumber + 1}행 잠금을 즉시 획득했습니다.`, "success");
      return;
    }

    setEditableLineId(null);
    setLockNotice(`${lineNumber + 1}행 잠금 요청 중 0/${peers.length} 승인`);
    pushEventLog(`${lineNumber + 1}행 잠금을 요청했습니다.`, "info");

    state.timeoutId = window.setTimeout(() => {
      const currentState = lineLocksRef.current.get(lineNumber);
      if (!currentState || currentState.timestamp !== timestamp || currentState.status !== "requesting") {
        return;
      }

      clearLockTimeout(currentState);
      lineLocksRef.current.delete(lineNumber);
      setSelectedLineId(null);
      setEditableLineId(null);
      setLockNotice(`${lineNumber + 1}행 잠금 요청이 시간 초과되었습니다.`);
      pushEventLog(`${lineNumber + 1}행 잠금 요청이 시간 초과되었습니다.`, "warning");
    }, LOCK_TIMEOUT_MS);

    lockApi.requestLock(currentSession.documentId, {
      lineNumber,
      clientId: clientIdRef.current,
      username,
      timestamp,
    });
  };

  const handleIncomingLockRequest = (notification: DocumentLockNotification) => {
    observeLamportTimestamp(notification.timestamp);

    if (notification.clientId === clientIdRef.current) {
      return;
    }

    const state = lineLocksRef.current.get(notification.lineNumber);
    if (!state) {
      lockApi.replyLock(currentSession.documentId, {
        lineNumber: notification.lineNumber,
        clientId: clientIdRef.current,
        username,
        targetClientId: notification.clientId,
        timestamp: nextLamportTimestamp(),
      });
      return;
    }

    if (state.status === "locked") {
      state.deferredRequests.push(notification);
      return;
    }

    if (state.status === "requesting") {
      if (comparePriority(state.timestamp, clientIdRef.current, notification)) {
        state.deferredRequests.push(notification);
        return;
      }
    }

    lockApi.replyLock(currentSession.documentId, {
      lineNumber: notification.lineNumber,
      clientId: clientIdRef.current,
      username,
      targetClientId: notification.clientId,
      timestamp: nextLamportTimestamp(),
    });
  };

  const handleIncomingLockOk = (notification: DocumentLockNotification) => {
    observeLamportTimestamp(notification.timestamp);

    if (notification.targetClientId !== clientIdRef.current) {
      return;
    }

    const state = lineLocksRef.current.get(notification.lineNumber);
    if (!state || state.status !== "requesting") {
      return;
    }

    state.pendingAcks.delete(notification.username);

    if (state.pendingAcks.size > 0) {
      const approved = state.peerCount - state.pendingAcks.size;
      setLockNotice(`${notification.lineNumber + 1}행 잠금 요청 중 ${approved}/${state.peerCount} 승인`);
      return;
    }

    clearLockTimeout(state);
    state.status = "locked";
    const lineId = state.lineId ?? getLineIdByIndex(notification.lineNumber);
    if (!lineId) {
      return;
    }

    setEditableLineId(lineId);
    setLockNotice(`${notification.lineNumber + 1}행 잠금을 획득했습니다.`);
    pushEventLog(`${notification.lineNumber + 1}행 잠금을 획득했습니다.`, "success");
  };

  const syncPendingLocksWithPresence = (participants: string[]) => {
    const activePeers = new Set(participants.filter((participant) => participant !== username));

    lineLocksRef.current.forEach((state, lineNumber) => {
      if (state.status !== "requesting") {
        return;
      }

      let changed = false;
      Array.from(state.pendingAcks).forEach((peer) => {
        if (!activePeers.has(peer)) {
          state.pendingAcks.delete(peer);
          changed = true;
        }
      });

      if (!changed) {
        return;
      }

      state.peerCount = activePeers.size;
      if (state.pendingAcks.size === 0) {
        clearLockTimeout(state);
        state.status = "locked";
        const lineId = state.lineId ?? getLineIdByIndex(lineNumber);
        if (lineId && selectedLineId === lineId) {
          setEditableLineId(lineId);
        }
        setLockNotice(`${lineNumber + 1}행 잠금을 획득했습니다.`);
        pushEventLog(`${lineNumber + 1}행 잠금을 획득했습니다.`, "success");
        return;
      }

      const approved = state.peerCount - state.pendingAcks.size;
      setLockNotice(`${lineNumber + 1}행 잠금 요청 중 ${approved}/${state.peerCount} 승인`);
    });
  };

  const mergeSession = (nextSession: TextSessionState) => {
    setSessions((prev) => {
      const existingIndex = prev.findIndex(
        (session) => session.documentId === nextSession.documentId,
      );

      if (existingIndex < 0) {
        return [...prev, nextSession];
      }

      const nextSessions = [...prev];
      nextSessions[existingIndex] = nextSession;
      return nextSessions;
    });
  };

  const markUnsaved = (nextLines: DocumentLine[]) => {
    updateCurrentSession((session) => ({
      ...session,
      lines: nextLines,
      saveStatus: "저장 필요",
      lastEditor: username,
    }));
  };

  useEffect(() => {
    stompClient.connect(
      () => setSocketReady(true),
      (error) => console.error("STOMP 연결 오류", error),
    );

    return () => {
      stompClient.disconnect();
      setSocketReady(false);
    };
  }, []);

  useEffect(() => {
    if (!socketReady || screen !== "editor") {
      return;
    }

    const subscriptionId = documentApi.subscribeDocumentUpdates(
      currentSession.documentId,
      (notification: DocumentTopicNotification) => {
        if (notification.documentId !== currentSession.documentId) {
          return;
        }

        if (notification.type === "DOCUMENT_LOCK") {
          if (notification.mode === "REQUEST") {
            handleIncomingLockRequest(notification);
          } else {
            handleIncomingLockOk(notification);
          }
          return;
        }

        if (notification.type === "DOCUMENT_PRESENCE") {
          syncActiveParticipants(notification.activeParticipants);
          syncPendingLocksWithPresence(notification.activeParticipants);
          setSessions((prev) =>
            prev.map((session) =>
              session.documentId === notification.documentId
                ? mapDocumentPresenceToSession(session, notification)
                : session,
            ),
          );
          return;
        }

        if (
          notification.type === "DOCUMENT_EDIT" &&
          notification.username !== username &&
          notification.operation !== "UPDATE"
        ) {
          releaseAllLineLocks();
          setSelectedLineId(null);
          setEditableLineId(null);
        }

        setSessions((prev) =>
          prev.map((session) =>
            session.documentId === notification.documentId
              ? mapDocumentUpdateToSession(session, notification)
              : session,
          ),
        );
      },
    );

    return () => {
      stompClient.unsubscribe(subscriptionId);
    };
  }, [socketReady, screen, currentSession.documentId]);

  useEffect(() => {
    return () => {
      releaseAllLineLocks();
    };
  }, []);

  const loadDocumentList = async () => {
    try {
      const summaries = await sessionApi.listDocuments(username);
      setSessions((prev) => {
        const nextMap = new Map<number, TextSessionState>();
        prev.forEach((session) => nextMap.set(session.documentId, session));

        summaries.forEach((summary) => {
          const existing = nextMap.get(summary.documentId);
          if (!existing || existing.lines.length === 0) {
            nextMap.set(summary.documentId, mapSummaryToSession(summary));
          } else {
            nextMap.set(summary.documentId, {
              ...existing,
              title: summary.title,
              participantCount: summary.memberCount,
              status:
                summary.activeParticipantCount > 0 ? "active" : existing.status,
            });
          }
        });

        return Array.from(nextMap.values()).sort(
          (left, right) => left.documentId - right.documentId,
        );
      });
    } catch (error) {
      console.error("문서 목록을 불러오지 못했습니다.", error);
    }
  };

  const openDocument = async (documentId: number, join = true) => {
    try {
      releaseAllLineLocks();
      const snapshot = join
        ? await sessionApi.joinDocument(documentId, { username })
        : await sessionApi.getDocument(documentId);
      const nextSession = mapSnapshotToSession(snapshot);
      syncActiveParticipants(snapshot.activeParticipants);
      mergeSession(nextSession);
      setCurrentDocumentId(documentId);
      setSelectedLineId(null);
      setEditableLineId(null);
      setScreen("editor");
    } catch (error) {
      console.error("문서를 불러오지 못했습니다.", error);
    }
  };

  useEffect(() => {
    if (screen === "sessionEntry" || screen === "editor") {
      void loadDocumentList();
    }
  }, [screen, username]);

  const loadSavedFiles = async () => {
    try {
      const files = await sessionApi.getSavedFiles();
      setSavedFiles(files);
    } catch (error) {
      console.error("저장된 파일 목록을 불러오지 못했습니다.", error);
    }
  };

  useEffect(() => {
    if (screen === "sessionEntry") {
      void loadSavedFiles();
    }
  }, [screen]);

  const handleLogin = (nextUsername: string) => {
    releaseAllLineLocks();
    setSelectedLineId(null);
    setEditableLineId(null);
    setUsername(nextUsername);
    setScreen("sessionEntry");
  };

  const openEditor = (documentId = currentDocumentId) => {
    void openDocument(documentId);
  };

  const handleCreateBlankSession = async () => {
    try {
      releaseAllLineLocks();
      const snapshot = await sessionApi.createDocument({
        username,
        title: "새 공유 문서",
        initialLines: [],
      });
      const nextSession = mapSnapshotToSession(snapshot);
      syncActiveParticipants(snapshot.activeParticipants);
      mergeSession(nextSession);
      setCurrentDocumentId(nextSession.documentId);
      setSelectedLineId(null);
      setEditableLineId(null);
      setScreen("editor");
    } catch (error) {
      console.error("새 문서를 생성하지 못했습니다.", error);
    }
  };

  const handleJoinSession = async (documentId: number) => {
    try {
      releaseAllLineLocks();
      const snapshot = await sessionApi.joinDocument(documentId, { username });
      const nextSession = mapSnapshotToSession(snapshot);
      syncActiveParticipants(snapshot.activeParticipants);
      mergeSession(nextSession);
      setCurrentDocumentId(nextSession.documentId);
      setSelectedLineId(null);
      setEditableLineId(null);
      setScreen("editor");
    } catch (error) {
      console.error("문서 참여에 실패했습니다.", error);
    }
  };

  const handleSave = async () => {
    try {
      await sessionApi.saveSession({
        sessionId: currentSession.sessionId,
        documentId: currentSession.documentId,
        title: currentSession.title,
        lines: currentSession.lines,
        username,
      });
    } catch (error) {
      console.error("세션 저장 요청에 실패했습니다.", error);
    } finally {
      updateCurrentSession((session) => ({
        ...session,
        saveStatus: "서버 저장됨",
        lastEditor: username,
        status: "saved",
      }));
    }
  };

  const handleDownloadJson = () => {
    // TODO(server): 서버 JSON export API 응답으로 다운로드를 처리한다.
    const payload = {
      documentId: currentSession.documentId,
      title: currentSession.title,
      lines: currentSession.lines,
      savedBy: username,
      exportedAt: new Date().toISOString(),
    };

    const blob = new Blob([JSON.stringify(payload, null, 2)], {
      type: "application/json",
    });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");

    anchor.href = url;
    anchor.download = `${currentSession.documentId}.json`;
    anchor.click();

    URL.revokeObjectURL(url);
  };

  const handleLoadSavedSession = async (documentId: number) => {
    try {
      releaseAllLineLocks();
      await openDocument(documentId, false);
    } catch (error) {
      console.error("저장된 세션을 불러오지 못했습니다.", error);
    }
  };

  const handleImportJson = async (file: File) => {
    // TODO(server): 로컬 JSON을 서버 import API에 전달한 뒤 생성된 세션으로 진입한다.
    const text = await file.text();
    const parsed = JSON.parse(text) as {
      title?: string;
      lines?: Array<{ text: string }>;
    };

    const documentId = createSessionNumber();
    const importedLines =
      parsed.lines && parsed.lines.length > 0
        ? parsed.lines.map((line, index) => ({
            lineId: `line-${Date.now()}-${index}`,
            lineNumber: index + 1,
            text: line.text,
            editor: null,
          }))
        : [
            {
              lineId: `line-${Date.now()}`,
              lineNumber: 1,
              text: text,
              editor: null,
            },
          ];

    const importedSession: TextSessionState = {
      sessionId: `session-${Date.now()}`,
      documentId,
      title: parsed.title ?? file.name.replace(/\.json$/i, ""),
      participantCount: 1,
      status: "active",
      lines: importedLines,
      saveStatus: "저장 필요",
      lastEditor: username,
      participants: [
        {
          username,
          status: "online",
          description: "로컬 JSON 불러오기",
        },
      ],
      eventLogs: [
        {
          message: `${file.name} 파일을 가져와 새 세션을 만들었습니다.`,
          timestamp: new Date().toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit",
          }),
          type: "success",
        },
      ],
    };

    setSessions((prev) => [...prev, importedSession]);
    setCurrentDocumentId(documentId);
    setSelectedLineId(null);
    setEditableLineId(null);
    setScreen("editor");
  };

  const handleLineSelect = (lineId: string) => {
    if (selectedLineId === lineId) {
      return;
    }

    if (selectedLineId && selectedLineId !== lineId) {
      releaseLineLock(selectedLineId);
    }

    setSelectedLineId(lineId);
    setEditableLineId(null);
    requestLineLock(lineId);
  };

  const handleLineTextChange = (lineId: string, nextText: string) => {
    if (editableLineId !== lineId) {
      return;
    }

    const targetLine = currentSession.lines.find((line) => line.lineId === lineId);
    if (!targetLine) return;

    documentApi.update({
      documentId: currentSession.documentId,
      lineNumber: targetLine.lineNumber - 1,
      text: nextText,
    });

    const nextLines = currentSession.lines.map((line) =>
      line.lineId === lineId
        ? { ...line, text: nextText, editor: username }
        : line,
    );
    markUnsaved(nextLines);
  };

  const handleAddLineBelow = () => {
    if (!selectedLineId) return;

    if (editableLineId !== selectedLineId) {
      return;
    }

    const selectedIndex = currentSession.lines.findIndex(
      (line) => line.lineId === selectedLineId,
    );
    if (selectedIndex < 0) return;

    const newLine: DocumentLine = {
      lineId: `line-${Date.now()}`,
      lineNumber: selectedIndex + 2,
      text: "",
      editor: username,
    };

    documentApi.insert({
      documentId: currentSession.documentId,
      lineNumber: selectedIndex + 1,
      text: "",
    });

    const nextLines = renumberLines([
      ...currentSession.lines.slice(0, selectedIndex + 1),
      newLine,
      ...currentSession.lines.slice(selectedIndex + 1),
    ]);

    releaseAllLineLocks();
    setSelectedLineId(newLine.lineId);
    markUnsaved(nextLines);
    window.setTimeout(() => {
      requestLineLock(newLine.lineId, newLine.lineNumber - 1);
    }, 0);
  };

  const handleAddLineAtEnd = () => {
    if (selectedLineId && editableLineId !== selectedLineId) {
      return;
    }

    const newLine: DocumentLine = {
      lineId: `line-${Date.now()}`,
      lineNumber: currentSession.lines.length + 1,
      text: "",
      editor: username,
    };

    documentApi.insert({
      documentId: currentSession.documentId,
      lineNumber: currentSession.lines.length,
      text: "",
    });

    releaseAllLineLocks();
    setSelectedLineId(newLine.lineId);
    markUnsaved(renumberLines([...currentSession.lines, newLine]));
    window.setTimeout(() => {
      requestLineLock(newLine.lineId, currentSession.lines.length);
    }, 0);
  };

  const handleDeleteSelectedLine = () => {
    if (!selectedLineId || currentSession.lines.length <= 1) return;

    if (editableLineId !== selectedLineId) {
      return;
    }

    const selectedIndex = currentSession.lines.findIndex(
      (line) => line.lineId === selectedLineId,
    );
    if (selectedIndex < 0) return;

    documentApi.delete({
      documentId: currentSession.documentId,
      lineNumber: selectedIndex,
    });

    releaseAllLineLocks();
    setSelectedLineId(null);
    setEditableLineId(null);
    markUnsaved(
      renumberLines(
        currentSession.lines.filter((line) => line.lineId !== selectedLineId),
      ),
    );
  };

  if (screen === "login") {
    return <LoginPage onLogin={handleLogin} />;
  }

  if (screen === "sessionEntry") {
    return (
      <SessionEntryPage
        username={username}
        sessions={sessions}
        savedFiles={savedFiles}
        onCreateBlank={handleCreateBlankSession}
        onJoinSession={handleJoinSession}
        onOpenEditor={openEditor}
        onLoadSavedSession={handleLoadSavedSession}
        onImportJson={handleImportJson}
      />
    );
  }

  return (
    <div className="app-root">
      <AppHeader
        documentId={currentSession.documentId}
        documentTitle={currentSession.title}
        saveStatus={currentSession.saveStatus}
        lastEditor={currentSession.lastEditor}
        onSave={handleSave}
        onDownloadJson={handleDownloadJson}
        onHome={() => {
          releaseAllLineLocks();
          setSelectedLineId(null);
          setScreen("sessionEntry");
        }}
        onLogout={() => {
          releaseAllLineLocks();
          setSelectedLineId(null);
          setScreen("login");
        }}
      />
      <InfoBar username={username} />
      <div className="workspace">
        <TextSessionList
          sessions={sessions}
          currentDocumentId={currentSession.documentId}
          onNewSession={handleCreateBlankSession}
          onSessionSelect={openEditor}
        />
        <DocumentEditor
          title={currentSession.title}
          documentId={currentSession.documentId}
          currentUser={username}
          lockNotice={lockNotice}
          lines={currentSession.lines}
          selectedLineId={selectedLineId}
          editableLineId={editableLineId}
          onLineSelect={handleLineSelect}
          onLineTextChange={handleLineTextChange}
          onAddLineBelow={handleAddLineBelow}
          onAddLineAtEnd={handleAddLineAtEnd}
          onDeleteSelectedLine={handleDeleteSelectedLine}
        />
        <StatusPanel
          participants={currentSession.participants}
          events={currentSession.eventLogs}
        />
      </div>
    </div>
  );
}
