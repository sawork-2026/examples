from dataclasses import dataclass, field
import uuid


@dataclass
class Contact:
    name: str
    phone: str
    email: str
    id: str = field(default_factory=lambda: str(uuid.uuid4())[:8])
