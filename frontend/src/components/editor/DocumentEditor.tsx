import { DocumentLine } from "../../types";

interface DocumentEditorProps {
  title: string;
  currentUser: string;
  lines: DocumentLine[];
  selectedLineId: string | null;
  onLineSelect: (lineId: string) => void;
  onLineTextChange: (lineId: string, nextText: string) => void;
  onAddLineBelow: () => void;
  onAddLineAtEnd: () => void;
  onDeleteSelectedLine: () => void;
}

export function DocumentEditor({
  title,
  currentUser,
  lines,
  selectedLineId,
  onLineSelect,
  onLineTextChange,
  onAddLineBelow,
  onAddLineAtEnd,
  onDeleteSelectedLine,
}: DocumentEditorProps) {
  return (
    <main className="editor-shell">
      <section className="document-card">
        <div className="document-header">
          <div>
            <p className="eyebrow">lineId 기반 줄 단위 편집기</p>
            <h2>{title}</h2>
          </div>
          <div className="document-actions">
            <button
              className="secondary-button"
              disabled={!selectedLineId}
              onClick={onAddLineBelow}
            >
              줄 추가
            </button>
            <button
              className="secondary-button danger"
              disabled={!selectedLineId || lines.length <= 1}
              onClick={onDeleteSelectedLine}
            >
              선택 줄 삭제
            </button>
          </div>
        </div>

        <div className="line-list">
          {lines.map((line) => {
            const lockedByOther = line.editor && line.editor !== currentUser;
            const editingByMe = line.editor === currentUser;
            const selected = selectedLineId === line.lineId;
            const className = selected
              ? "line-row selected"
              : lockedByOther
                ? "line-row locked"
                : "line-row";

            return (
              <div
                className={className}
                key={line.lineId}
                onClick={() => {
                  if (!lockedByOther) onLineSelect(line.lineId);
                }}
              >
                <span className="line-number">{line.lineNumber}</span>
                <textarea
                  readOnly={!!lockedByOther || !selected}
                  value={line.text}
                  aria-label={`line ${line.lineNumber}`}
                  placeholder="내용을 입력하세요."
                  onChange={(event) =>
                    onLineTextChange(line.lineId, event.target.value)
                  }
                />
                {editingByMe && (
                  <span className="badge mine">내가 편집 중</span>
                )}
                {lockedByOther && (
                  <span className="badge locked">{line.editor} 편집 중</span>
                )}
              </div>
            );
          })}
        </div>

        <button className="add-line-button" onClick={onAddLineAtEnd}>
          + 줄 추가
        </button>
      </section>
    </main>
  );
}
