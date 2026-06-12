import { useState } from "react";
import { SavedFileInfo } from "../../api/sessionApi";
import { TextSessionItem } from "../../types";

interface SessionEntryPageProps {
  username: string;
  sessions: TextSessionItem[];
  savedFiles: SavedFileInfo[];
  onCreateBlank: () => void;
  onJoinSession: (documentId: number) => void;
  onOpenEditor: (documentId: number) => void;
  onLoadSavedSession: (documentId: number) => void;
}

export function SessionEntryPage({
  username,
  sessions,
  savedFiles,
  onCreateBlank,
  onJoinSession,
  onOpenEditor,
  onLoadSavedSession,
}: SessionEntryPageProps) {
  const [documentIdInput, setDocumentIdInput] = useState("38172946");

  return (
    <main className="session-page">
      <header className="session-page-header">
        <div>
          <p className="eyebrow">텍스트 세션 선택</p>
          <h1>참여할 문서를 선택하세요</h1>
          <p>새 세션을 만들거나 문서 ID를 기준으로 기존 세션에 참여합니다.</p>
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
          <h2>문서 ID로 참여</h2>
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
          <p>서버에 저장된 JSON 파일에서 세션을 불러오는 흐름입니다.</p>
          <div className="saved-file-list">
            {savedFiles.length === 0 ? (
              <p className="muted">저장된 파일이 없습니다.</p>
            ) : (
              savedFiles.map((file) => (
                <button
                  className="saved-file-item"
                  key={file.fileName}
                  onClick={() => onLoadSavedSession(file.documentId)}
                >
                  <strong>{file.title}</strong>
                  <span className="doc-number">#{file.documentId}</span>
                  <span className="muted">{file.updatedAt}</span>
                </button>
              ))
            )}
          </div>
        </article>
      </section>

      <section className="active-session-section">
        <div className="panel-header horizontal">
          <div>
            <h2>서버 문서 목록</h2>
            <p>서버에서 불러온 문서 목록입니다.</p>
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
