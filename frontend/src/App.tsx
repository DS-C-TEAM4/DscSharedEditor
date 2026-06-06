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

type Screen = "login" | "sessionEntry" | "editor";

export default function App() {
  const [screen, setScreen] = useState<Screen>("login");
  const [username, setUsername] = useState("user1");

  const handleLogin = (nextUsername: string) => {
    setUsername(nextUsername);
    setScreen("sessionEntry");
  };

  const openEditor = () => {
    setScreen("editor");
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
          lines={mockLines}
        />
        <StatusPanel participants={mockParticipants} events={mockEventLogs} />
      </div>
    </div>
  );
}
