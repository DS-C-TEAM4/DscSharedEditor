interface LoginPageProps {
  onLogin: (username: string) => void;
}

const testUsers = [
  { username: "user1", password: "1234" },
  { username: "user2", password: "2345" },
  { username: "user3", password: "3456" },
  { username: "user4", password: "4567" },
];

export function LoginPage({ onLogin }: LoginPageProps) {
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
            <input placeholder="user1" />
          </label>
          <label>
            Password
            <input type="password" placeholder="1234" />
          </label>
          <button
            className="primary-button full-width"
            onClick={() => onLogin("user1")}
          >
            로그인
          </button>
        </div>

        <div className="quick-login">
          <p>테스트 계정</p>
          <div className="quick-grid">
            {testUsers.map((user) => (
              <button
                className="secondary-button"
                key={user.username}
                onClick={() => onLogin(user.username)}
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
