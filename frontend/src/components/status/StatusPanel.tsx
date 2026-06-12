import { EventLogMessage, Participant } from "../../types";

interface StatusPanelProps {
  participants: Participant[];
  events: EventLogMessage[];
}

export function StatusPanel({ participants, events }: StatusPanelProps) {
  const latestEvents = [...events].reverse();

  return (
    <aside className="right-panel">
      <section className="status-section">
        <div className="panel-header">
          <h2>접속 사용자</h2>
          <p>현재 세션 참여 상태</p>
        </div>
        <div className="user-list">
          {participants.map((user) => (
            <div className="user-item" key={user.username}>
              <div className="avatar">{user.username.slice(-1)}</div>
              <div>
                <strong>{user.username}</strong>
                <p>{user.description}</p>
              </div>
              <span className={user.status === "editing" ? "user-state editing" : "user-state"}>{user.status === "editing" ? "편집" : "접속"}</span>
            </div>
          ))}
        </div>
      </section>

      <section className="status-section grow">
        <div className="panel-header">
          <h2>이벤트 로그</h2>
          <p>편집 과정 공유</p>
        </div>
        <div className="event-list">
          {latestEvents.map((event, index) => (
            <div className={`event-item ${event.type}`} key={`${event.timestamp}-${index}`}>
              <span>{event.timestamp}</span>
              <p>{event.message}</p>
            </div>
          ))}
        </div>
      </section>
    </aside>
  );
}
