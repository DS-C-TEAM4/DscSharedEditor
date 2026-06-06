import { DocumentLine } from "../../types";

interface DocumentEditorProps {
  title: string;
  currentUser: string;
  lines: DocumentLine[];
}

export function DocumentEditor({ title, currentUser, lines }: DocumentEditorProps) {
  return (
    <main className="editor-shell">
      <section className="document-card">
        <div className="document-header">
          <div>
            <p className="eyebrow">lineId 기반 줄 단위 편집기</p>
            <h2>{title}</h2>
          </div>
          <div className="document-actions">
            <button className="secondary-button">줄 추가</button>
            <button className="secondary-button">선택 줄 삭제</button>
          </div>
        </div>

        <div className="line-list">
          {lines.map((line) => {
            const lockedByOther = line.editor && line.editor !== currentUser;
            const editingByMe = line.editor === currentUser;
            return (
              <div className={editingByMe ? "line-row selected" : lockedByOther ? "line-row locked" : "line-row"} key={line.lineId}>
                <span className="line-number">{line.lineNumber}</span>
                <textarea readOnly={!!lockedByOther} value={line.text} aria-label={`line ${line.lineNumber}`} />
                {editingByMe && <span className="badge mine">내가 편집 중</span>}
                {lockedByOther && <span className="badge locked">{line.editor} 편집 중</span>}
              </div>
            );
          })}
        </div>

        <button className="add-line-button">+ 줄 추가</button>
      </section>
    </main>
  );
}
