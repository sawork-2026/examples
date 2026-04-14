"""服务端 Session 管理器：在服务器端保存会话状态。"""
import threading
import uuid
from ioc.decorators import component


@component
class SessionManager:
    def __init__(self):
        self._sessions: dict[str, dict] = {}
        self._lock = threading.Lock()

    def get_or_create(self, session_id: str | None) -> tuple[str, dict, bool]:
        with self._lock:
            if session_id and session_id in self._sessions:
                return session_id, self._sessions[session_id], False
            new_id = str(uuid.uuid4())
            session = {}
            self._sessions[new_id] = session
            return new_id, session, True
