import { Home, LogOut, Save } from "lucide-react";
import { SaveStatus } from "../../types";

interface AppHeaderProps {
  documentNumber: string;
  documentTitle: string;
  saveStatus: SaveStatus;
  lastEditor: string;
  onHome: () => void;
  onSave: () => void;
  onLogout: () => void;
}

export function AppHeader({
  documentNumber,
  documentTitle,
  saveStatus,
  lastEditor,
  onHome,
  onSave,
  onLogout,
}: AppHeaderProps) {
  return (
    <header className="app-header">
      <div className="header-left">
        <button className="icon-button" aria-label="home" onClick={onHome}>
          <Home size={18} />
        </button>
        <div>
          <p className="eyebrow">문서 번호 {documentNumber}</p>
          <h1>{documentTitle}</h1>
        </div>
      </div>
      <div className="header-right">
        <span
          className={
            saveStatus === "저장됨"
              ? "status-pill saved"
              : "status-pill unsaved"
          }
        >
          {saveStatus}
        </span>
        <span className="muted">마지막 편집: {lastEditor}</span>
        <button className="primary-button" onClick={onSave}>
          <Save size={16} /> 저장
        </button>
        <button className="secondary-button" onClick={onLogout}>
          <LogOut size={16} /> 로그아웃
        </button>
      </div>
    </header>
  );
}
