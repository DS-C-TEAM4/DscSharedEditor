import { useState } from "react";
import { AppHeader } from "./components/layout/AppHeader";
import { InfoBar } from "./components/layout/InfoBar";
import { TextSessionList } from "./components/session/TextSessionList";
import { DocumentEditor } from "./components/editor/DocumentEditor";
import { StatusPanel } from "./components/status/StatusPanel";
import { LoginPage } from "./components/pages/LoginPage";
import { SessionEntryPage } from "./components/pages/SessionEntryPage";
import {
  mockEventLogs,
  mockLines,
  mockParticipants,
  mockSessions,
} from "./mockData";
import { DocumentLine, TextSessionState } from "./types";

type Screen = "login" | "sessionEntry" | "editor";

function renumberLines(lines: DocumentLine[]) {
  return lines.map((line, index) => ({
    ...line,
    lineNumber: index + 1,
  }));
}

function createSessionNumber() {
  return String(Math.floor(10000000 + Math.random() * 90000000));
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
  const [currentDocumentNumber, setCurrentDocumentNumber] =
    useState("38172946");
  const [selectedLineId, setSelectedLineId] = useState<string | null>(null);

  const currentSession =
    sessions.find(
      (session) => session.documentNumber === currentDocumentNumber,
    ) ?? sessions[0];

  const updateCurrentSession = (
    updater: (session: TextSessionState) => TextSessionState,
  ) => {
    setSessions((prev) =>
      prev.map((session) =>
        session.documentNumber === currentSession.documentNumber
          ? updater(session)
          : session,
      ),
    );
  };

  const markUnsaved = (nextLines: DocumentLine[]) => {
    updateCurrentSession((session) => ({
      ...session,
      lines: nextLines,
      saveStatus: "저장 필요",
      lastEditor: username,
    }));
  };

  const handleLogin = (nextUsername: string) => {
    setUsername(nextUsername);
    setScreen("sessionEntry");
  };

  const openEditor = (documentNumber = currentDocumentNumber) => {
    setCurrentDocumentNumber(documentNumber);
    setSelectedLineId(null);
    setScreen("editor");
  };

  const handleCreateBlankSession = () => {
    const documentNumber = createSessionNumber();
    const newSession: TextSessionState = {
      sessionId: `session-${Date.now()}`,
      documentNumber,
      title: "새 공유 문서",
      participantCount: 1,
      status: "active",
      lines: [
        {
          lineId: `line-${Date.now()}`,
          lineNumber: 1,
          text: "",
          editor: username,
        },
      ],
      saveStatus: "저장 필요",
      lastEditor: username,
      participants: [
        {
          username,
          status: "editing",
          description: "line-1 편집 중",
        },
      ],
      eventLogs: [
        {
          message: `${username}님이 새 텍스트 세션을 생성했습니다.`,
          timestamp: new Date().toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit",
          }),
          type: "success",
        },
      ],
    };

    setSessions((prev) => [...prev, newSession]);
    setCurrentDocumentNumber(documentNumber);
    setSelectedLineId(newSession.lines[0].lineId);
    setScreen("editor");
  };

  const handleJoinSession = (documentNumber: string) => {
    const target = sessions.find(
      (session) => session.documentNumber === documentNumber,
    );

    if (target) {
      openEditor(target.documentNumber);
      return;
    }

    const joinedSession: TextSessionState = {
      sessionId: `session-${Date.now()}`,
      documentNumber,
      title: "참여한 공유 문서",
      participantCount: 2,
      status: "active",
      lines: [
        {
          lineId: `line-${Date.now()}`,
          lineNumber: 1,
          text: "Late-comer 클라이언트가 서버에서 받은 현재 문서 상태를 표시하는 목업 줄입니다.",
          editor: null,
        },
      ],
      saveStatus: "서버 저장됨",
      lastEditor: "server",
      participants: [
        {
          username,
          status: "online",
          description: "Late-comer로 참여",
        },
        {
          username: "user2",
          status: "online",
          description: "기존 참여자",
        },
      ],
      eventLogs: [
        {
          message: `${username}님이 문서 번호 ${documentNumber} 세션에 참여했습니다.`,
          timestamp: new Date().toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit",
          }),
          type: "success",
        },
        {
          message:
            "서버로부터 현재 문서 상태를 수신했습니다. (Late-comer 동기화)",
          timestamp: new Date().toLocaleTimeString("ko-KR", {
            hour: "2-digit",
            minute: "2-digit",
          }),
          type: "info",
        },
      ],
    };

    setSessions((prev) => [...prev, joinedSession]);
    setCurrentDocumentNumber(documentNumber);
    setSelectedLineId(null);
    setScreen("editor");
  };

  const handleSave = () => {
    updateCurrentSession((session) => ({
      ...session,
      saveStatus: "서버 저장됨",
      lastEditor: username,
      status: "saved",
    }));
  };

    const handleDownloadJson = () => {
      // TODO(server): 서버 JSON export API 응답으로 다운로드를 처리한다.
      const payload = {
        documentNumber: currentSession.documentNumber,
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
      anchor.download = `${currentSession.documentNumber}.json`;
      anchor.click();

      URL.revokeObjectURL(url);
    };

    const handleLoadSavedSession = () => {
      // TODO(server): /api/sessions/load 응답으로 세션 상태를 구성한다.
      const documentNumber = "52849163";
      const loadedSession: TextSessionState = {
        sessionId: `session-${Date.now()}`,
        documentNumber,
        title: "서버 저장 파일에서 불러온 문서",
        participantCount: 1,
        status: "saved",
        lines: [
          {
            lineId: `line-${Date.now()}`,
            lineNumber: 1,
            text: "서버에 저장된 JSON 파일을 불러온 상황을 표현하는 목업 문장입니다.",
            editor: null,
          },
        ],
        saveStatus: "서버 저장됨",
        lastEditor: "server",
        participants: [
          {
            username,
            status: "online",
            description: "서버 저장 파일 확인 중",
          },
        ],
        eventLogs: [
          {
            message: "서버 저장 JSON 파일에서 세션을 불러왔습니다.",
            timestamp: new Date().toLocaleTimeString("ko-KR", {
              hour: "2-digit",
              minute: "2-digit",
            }),
            type: "success",
          },
        ],
      };

      setSessions((prev) => [...prev, loadedSession]);
      setCurrentDocumentNumber(documentNumber);
      setSelectedLineId(null);
      setScreen("editor");
    };

    const handleImportJson = async (file: File) => {
      // TODO(server): 로컬 JSON을 서버 import API에 전달한 뒤 생성된 세션으로 진입한다.
      const text = await file.text();
      const parsed = JSON.parse(text) as {
        title?: string;
        lines?: Array<{ text: string }>;
      };

      const documentNumber = createSessionNumber();
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
        documentNumber,
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
      setCurrentDocumentNumber(documentNumber);
      setSelectedLineId(null);
      setScreen("editor");
    };

  const handleLineSelect = (lineId: string) => {
    setSelectedLineId(lineId);
    updateCurrentSession((session) => ({
      ...session,
      lines: session.lines.map((line) => {
        if (line.editor && line.editor !== username) return line;
        return line.lineId === lineId
          ? { ...line, editor: username }
          : line.editor === username
            ? { ...line, editor: null }
            : line;
      }),
    }));
  };

  const handleLineTextChange = (lineId: string, nextText: string) => {
    const nextLines = currentSession.lines.map((line) =>
      line.lineId === lineId
        ? { ...line, text: nextText, editor: username }
        : line,
    );
    markUnsaved(nextLines);
  };

  const handleAddLineBelow = () => {
    if (!selectedLineId) return;

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

    const nextLines = renumberLines([
      ...currentSession.lines.slice(0, selectedIndex + 1),
      newLine,
      ...currentSession.lines.slice(selectedIndex + 1),
    ]);

    setSelectedLineId(newLine.lineId);
    markUnsaved(nextLines);
  };

  const handleAddLineAtEnd = () => {
    const newLine: DocumentLine = {
      lineId: `line-${Date.now()}`,
      lineNumber: currentSession.lines.length + 1,
      text: "",
      editor: username,
    };

    setSelectedLineId(newLine.lineId);
    markUnsaved(renumberLines([...currentSession.lines, newLine]));
  };

  const handleDeleteSelectedLine = () => {
    if (!selectedLineId || currentSession.lines.length <= 1) return;

    const selectedLine = currentSession.lines.find(
      (line) => line.lineId === selectedLineId,
    );
    if (selectedLine?.editor && selectedLine.editor !== username) return;

    setSelectedLineId(null);
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
        documentNumber={currentSession.documentNumber}
        documentTitle={currentSession.title}
        saveStatus={currentSession.saveStatus}
        lastEditor={currentSession.lastEditor}
        onHome={() => setScreen("sessionEntry")}
        onSave={handleSave}
        onDownloadJson={handleDownloadJson}
        onLogout={() => setScreen("login")}
      />
      <InfoBar username={username} />
      <div className="workspace">
        <TextSessionList
          sessions={sessions}
          currentDocumentNumber={currentSession.documentNumber}
          onNewSession={handleCreateBlankSession}
          onSessionSelect={openEditor}
        />
        <DocumentEditor
          title={currentSession.title}
          documentNumber={currentSession.documentNumber}
          currentUser={username}
          lines={currentSession.lines}
          selectedLineId={selectedLineId}
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
