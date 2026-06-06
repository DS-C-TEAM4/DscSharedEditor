import { AppHeader } from "./components/layout/AppHeader";
import { InfoBar } from "./components/layout/InfoBar";
import { TextSessionList } from "./components/session/TextSessionList";
import { DocumentEditor } from "./components/editor/DocumentEditor";
import { StatusPanel } from "./components/status/StatusPanel";
import { mockEventLogs, mockLines, mockParticipants, mockSessions } from "./mockData";

export default function App() {
  return (
    <div className="app-root">
      <AppHeader documentNumber="38172946" saveStatus="저장되지 않음" lastEditor="user1" />
      <InfoBar username="user1" />
      <div className="workspace">
        <TextSessionList sessions={mockSessions} currentDocumentNumber="38172946" />
        <DocumentEditor title="공유 문서 편집기" currentUser="user1" lines={mockLines} />
        <StatusPanel participants={mockParticipants} events={mockEventLogs} />
      </div>
    </div>
  );
}
