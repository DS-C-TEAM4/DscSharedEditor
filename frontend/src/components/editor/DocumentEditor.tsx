import { useRef, useState } from "react";
import { DocumentLine } from "../../types";

interface DocumentEditorProps {
  title: string;
  documentId: number;
  currentUser: string;
  lockNotice: string;
  lines: DocumentLine[];
  selectedLineId: string | null;
  editableLineId: string | null;
  onLineSelect: (lineId: string) => void;
  onLineTextChange: (lineId: string, nextText: string) => void;
  onAddLineBelow: () => void;
  onAddLineAtEnd: () => void;
  onDeleteSelectedLine: () => void;
}

export function DocumentEditor({
  title,
  documentId,
  currentUser,
  lockNotice,
  lines,
  selectedLineId,
  editableLineId,
  onLineSelect,
  onLineTextChange,
  onAddLineBelow,
  onAddLineAtEnd,
  onDeleteSelectedLine,
}: DocumentEditorProps) {
  const composingRef = useRef(false);
  const [draftLineId, setDraftLineId] = useState<string | null>(null);
  const [draftText, setDraftText] = useState<string>("");

  return (
    <main className="editor-shell">
      <section className="document-card">
        <div className="document-header">
          <div>
            <p className="eyebrow">문서 ID {documentId}</p>
            <h2>{title}</h2>
            <p className="muted">{lockNotice}</p>
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
            const editingByMe = editableLineId === line.lineId;
            const selected = selectedLineId === line.lineId;
            const waitingForLock = selected && !editingByMe;
            const className = selected ? "line-row selected" : "line-row";

            return (
              <div
                className={className}
                key={line.lineId}
                onClick={() => onLineSelect(line.lineId)}
              >
                <span className="line-number">{line.lineNumber}</span>
                <textarea
                  readOnly={editableLineId !== line.lineId}
                  value={draftLineId === line.lineId ? draftText : line.text}
                  aria-label={`line ${line.lineNumber}`}
                  placeholder="내용을 입력하세요."
                  onCompositionStart={(event) => {
                    composingRef.current = true;
                    setDraftLineId(line.lineId);
                    setDraftText((event.target as HTMLTextAreaElement).value);
                  }}
                  onCompositionEnd={(event) => {
                    composingRef.current = false;
                    const value = (event.target as HTMLTextAreaElement).value;
                    setDraftLineId(null);
                    onLineTextChange(line.lineId, value);
                  }}
                  onChange={(event) => {
                    if (composingRef.current) {
                      setDraftText(event.target.value);
                    } else {
                      onLineTextChange(line.lineId, event.target.value);
                    }
                  }}
                />
                {editingByMe && (
                  <span className="badge mine">내가 편집 중</span>
                )}
                {waitingForLock && (
                  <span className="badge locked">잠금 대기</span>
                )}
                {line.editor && line.editor !== currentUser && !waitingForLock && (
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
