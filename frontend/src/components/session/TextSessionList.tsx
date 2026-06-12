import { TextSessionItem } from "../../types";

interface TextSessionListProps {
  sessions: TextSessionItem[];
  currentDocumentId: number;
  onNewSession: () => void;
  onSessionSelect: (documentId: number) => void;
}

export function TextSessionList({
  sessions,
  currentDocumentId,
  onNewSession,
  onSessionSelect,
}: TextSessionListProps) {
  return (
    <aside className="left-sidebar">
      <div className="panel-header horizontal">
        <h2>텍스트 세션</h2>
        <button className="small-button" onClick={onNewSession}>
          + 새 세션
        </button>
      </div>
      <div className="session-list">
        {sessions.map((session) => {
          const active = session.documentId === currentDocumentId;
          return (
            <button
              className={active ? "session-item active" : "session-item"}
              key={session.sessionId}
              onClick={() => onSessionSelect(session.documentId)}
            >
              <span className="doc-number">#{session.documentId}</span>
              <strong>{session.title}</strong>
              <span className="muted">참여자 {session.participantCount}명</span>
            </button>
          );
        })}
      </div>
    </aside>
  );
}
