import { useState } from "react";

interface LoginPageProps {
  socketReady: boolean;
  pending: boolean;
  error: string | null;
  onLogin: (username: string, password: string) => void;
}

const testUsers = [
  { username: "user1", password: "1234" },
  { username: "user2", password: "2345" },
  { username: "user3", password: "3456" },
  { username: "user4", password: "4567" },
];

export function LoginPage({
  socketReady,
  pending,
  error,
  onLogin,
}: LoginPageProps) {
  const [username, setUsername] = useState("user1");
  const [password, setPassword] = useState("1234");

  return (
    <main className="auth-page">
      <section className="auth-card">
        <p className="eyebrow">Distributed Shared Editor</p>
        <h1>공유 문서 편집기</h1>
        <p className="auth-description">
          테스트 계정으로 접속해 텍스트 세션 목록과 줄 단위 편집 화면을
          확인합니다.
        </p>

        <div className="login-form">
          <label>
            Username
            <input
              value={username}
              placeholder="user1"
              onChange={(event) => setUsername(event.target.value)}
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              placeholder="1234"
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          <button
            className="primary-button full-width"
            disabled={!socketReady || pending}
            onClick={() => onLogin(username, password)}
          >
            {pending ? "로그인 중..." : socketReady ? "로그인" : "연결 대기 중"}
          </button>
          {error && <p className="auth-error">{error}</p>}
        </div>

        <div className="quick-login">
          <p>테스트 계정</p>
          <div className="quick-grid">
            {testUsers.map((user) => (
              <button
                className="secondary-button"
                key={user.username}
                disabled={!socketReady || pending}
                onClick={() => onLogin(user.username, user.password)}
              >
                {user.username} / {user.password}
              </button>
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}
