interface InfoBarProps {
  username: string;
}

export function InfoBar({ username }: InfoBarProps) {
  return (
    <div className="info-bar">
      <span>로그인 사용자: <strong>{username}</strong></span>
      <span className="connection-dot" />
      <span>서버 연결됨</span>
      <span className="muted">최종 구현에서는 서버에서 내려온 세션/락/이벤트 상태를 표시합니다.</span>
    </div>
  );
}
