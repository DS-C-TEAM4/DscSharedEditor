import { Download, Home, LogOut, Save } from "lucide-react";
import { SaveStatus } from "../../types";

interface AppHeaderProps {
  documentNumber: string;
  documentTitle: string;
  saveStatus: SaveStatus;
  lastEditor: string;
  onHome: () => void;
  onSave: () => void;
  onDownloadJson: () => void;
  onLogout: () => void;
}

export function AppHeader({
  documentNumber,
  documentTitle,
  saveStatus,
  lastEditor,
  onHome,
  onSave,
  onDownloadJson,
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
            saveStatus === "서버 저장됨"
              ? "status-pill saved"
              : "status-pill unsaved"
          }
        >
          {saveStatus}
        </span>
        <span className="muted">마지막 편집: {lastEditor}</span>
        <button className="secondary-button" onClick={onDownloadJson}>
          <Download size={16} /> JSON 내보내기
        </button>
        <button className="primary-button" onClick={onSave}>
          <Save size={16} /> 세션 저장
        </button>
        <button className="secondary-button" onClick={onLogout}>
          <LogOut size={16} /> 로그아웃
        </button>
      </div>
    </header>
  );
}
