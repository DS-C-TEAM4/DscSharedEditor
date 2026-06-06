import { Home, LogOut, Save } from "lucide-react";
import { SaveStatus } from "../../types";

interface AppHeaderProps {
  documentNumber: string;
  saveStatus: SaveStatus;
  lastEditor: string;
}

export function AppHeader({ documentNumber, saveStatus, lastEditor }: AppHeaderProps) {
  return (
    <header className="app-header">
      <div className="header-left">
        <button className="icon-button" aria-label="home"><Home size={18} /></button>
        <div>
          <p className="eyebrow">공유 텍스트 편집 시스템</p>
          <h1>문서 번호 {documentNumber}</h1>
        </div>
      </div>
      <div className="header-right">
        <span className={saveStatus === "저장됨" ? "status-pill saved" : "status-pill unsaved"}>{saveStatus}</span>
        <span className="muted">마지막 편집: {lastEditor}</span>
        <button className="primary-button"><Save size={16} /> 저장</button>
        <button className="secondary-button"><LogOut size={16} /> 로그아웃</button>
      </div>
    </header>
  );
}
