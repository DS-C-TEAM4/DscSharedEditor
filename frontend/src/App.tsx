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
import { DocumentLine } from "./types";

type Screen = "login" | "sessionEntry" | "editor";

function renumberLines(lines: DocumentLine[]) {
  return lines.map((line, index) => ({
    ...line,
    lineNumber: index + 1,
  }));
}

export default function App() {
  const [screen, setScreen] = useState<Screen>("login");
  const [username, setUsername] = useState("user1");
  const [lines, setLines] = useState<DocumentLine[]>(mockLines);
  const [selectedLineId, setSelectedLineId] = useState<string | null>(null);

  const handleLogin = (nextUsername: string) => {
    setUsername(nextUsername);
    setScreen("sessionEntry");
  };

  const openEditor = () => {
    setScreen("editor");
  };

  const handleLineSelect = (lineId: string) => {
    setSelectedLineId(lineId);
    setLines((prev) =>
      prev.map((line) => {
        if (line.editor && line.editor !== username) return line;
        return line.lineId === lineId
          ? { ...line, editor: username }
          : line.editor === username
            ? { ...line, editor: null }
            : line;
      }),
    );
  };

  const handleLineTextChange = (lineId: string, nextText: string) => {
    setLines((prev) =>
      prev.map((line) =>
        line.lineId === lineId
          ? { ...line, text: nextText, editor: username }
          : line,
      ),
    );
  };

  const handleAddLineBelow = () => {
    if (!selectedLineId) return;

    setLines((prev) => {
      const selectedIndex = prev.findIndex(
        (line) => line.lineId === selectedLineId,
      );
      if (selectedIndex < 0) return prev;

      const newLine: DocumentLine = {
        lineId: `line-${Date.now()}`,
        lineNumber: selectedIndex + 2,
        text: "",
        editor: username,
      };

      const nextLines = [
        ...prev.slice(0, selectedIndex + 1),
        newLine,
        ...prev.slice(selectedIndex + 1),
      ];

      setSelectedLineId(newLine.lineId);
      return renumberLines(nextLines);
    });
  };

  const handleAddLineAtEnd = () => {
    const newLine: DocumentLine = {
      lineId: `line-${Date.now()}`,
      lineNumber: lines.length + 1,
      text: "",
      editor: username,
    };

    setLines((prev) => renumberLines([...prev, newLine]));
    setSelectedLineId(newLine.lineId);
  };

  const handleDeleteSelectedLine = () => {
    if (!selectedLineId || lines.length <= 1) return;

    setLines((prev) => {
      const selectedLine = prev.find((line) => line.lineId === selectedLineId);
      if (selectedLine?.editor && selectedLine.editor !== username) return prev;

      const nextLines = prev.filter((line) => line.lineId !== selectedLineId);
      setSelectedLineId(null);
      return renumberLines(nextLines);
    });
  };

  if (screen === "login") {
    return <LoginPage onLogin={handleLogin} />;
  }

  if (screen === "sessionEntry") {
    return (
      <SessionEntryPage
        username={username}
        sessions={mockSessions}
        onCreateBlank={openEditor}
        onJoinSession={openEditor}
        onOpenEditor={openEditor}
      />
    );
  }

  return (
    <div className="app-root">
      <AppHeader
        documentNumber="38172946"
        saveStatus="저장되지 않음"
        lastEditor={username}
      />
      <InfoBar username={username} />
      <div className="workspace">
        <TextSessionList
          sessions={mockSessions}
          currentDocumentNumber="38172946"
        />
        <DocumentEditor
          title="공유 문서 편집기"
          currentUser={username}
          lines={lines}
          selectedLineId={selectedLineId}
          onLineSelect={handleLineSelect}
          onLineTextChange={handleLineTextChange}
          onAddLineBelow={handleAddLineBelow}
          onAddLineAtEnd={handleAddLineAtEnd}
          onDeleteSelectedLine={handleDeleteSelectedLine}
        />
        <StatusPanel participants={mockParticipants} events={mockEventLogs} />
      </div>
    </div>
  );
}
