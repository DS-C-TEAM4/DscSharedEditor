import { useRef, useState } from "react";
import { TextSessionItem } from "../../types";

interface SessionEntryPageProps {
  username: string;
  sessions: TextSessionItem[];
  onCreateBlank: () => void;
  onJoinSession: (documentId: number) => void;
  onOpenEditor: (documentId: number) => void;
  onLoadSavedSession: () => void;
  onImportJson: (file: File) => void;
}

export function SessionEntryPage({
  username,
  sessions,
  onCreateBlank,
  onJoinSession,
  onOpenEditor,
  onLoadSavedSession,
  onImportJson,
}: SessionEntryPageProps) {
  const [documentIdInput, setDocumentIdInput] = useState("38172946");
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    onImportJson(file);
    event.target.value = "";
  };

  return (
    <main className="session-page">
      <header className="session-page-header">
        <div>
          <p className="eyebrow">텍스트 세션 선택</p>
          <h1>참여할 문서를 선택하세요</h1>
          <p>새 세션을 만들거나 문서 번호를 기준으로 기존 세션에 참여합니다.</p>
        </div>
        <div className="profile-chip">{username}</div>
      </header>

      <section className="session-card-grid">
        <article className="entry-card">
          <h2>새 텍스트 세션</h2>
          <p>빈 문서로 시작해 줄 단위 편집 상태를 확인합니다.</p>
          <button className="primary-button" onClick={onCreateBlank}>
            빈 문서로 시작
          </button>
        </article>

        <article className="entry-card">
          <h2>문서 번호로 참여</h2>
          <p>
            Late-comer 클라이언트가 현재 문서 상태를 받는 흐름을 확인합니다.
          </p>
          <div className="join-row">
            <input
              value={documentIdInput}
              aria-label="문서 ID"
              onChange={(event) => setDocumentIdInput(event.target.value)}
            />
            <button
              className="secondary-button"
              onClick={() => onJoinSession(Number(documentIdInput))}
            >
              참여
            </button>
          </div>
        </article>

        <article className="entry-card">
          <h2>세션 저장 파일</h2>
          <p>
            서버 저장 JSON 또는 로컬 JSON 파일에서 세션을 불러오는 흐름입니다.
          </p>
          <div className="entry-actions">
            <button className="secondary-button" onClick={onLoadSavedSession}>
              서버 저장 파일 불러오기
            </button>
            <button
              className="secondary-button"
              onClick={() => fileInputRef.current?.click()}
            >
              JSON 불러오기
            </button>
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept=".json,application/json"
            className="hidden-file-input"
            onChange={handleFileChange}
          />
        </article>
      </section>

      <section className="active-session-section">
        <div className="panel-header horizontal">
          <div>
            <h2>활성 텍스트 세션</h2>
            <p>현재 목업 데이터 기준 목록입니다.</p>
          </div>
        </div>

        <div className="active-session-list">
          {sessions.map((session) => (
            <button
              className="active-session-item"
              key={session.sessionId}
              onClick={() => onOpenEditor(session.documentId)}
            >
              <span className="doc-number">#{session.documentId}</span>
              <strong>{session.title}</strong>
              <span className="muted">참여자 {session.participantCount}명</span>
            </button>
          ))}
        </div>
      </section>
    </main>
  );
}
