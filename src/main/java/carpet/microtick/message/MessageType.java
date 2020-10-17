package carpet.microtick.message;

import carpet.microtick.enums.EventType;

public enum MessageType
{
	ATOM,
	PROCEDURE;

	public static MessageType fromEventType(EventType eventType)
	{
		switch (eventType)
		{
			case EVENT:
			case ACTION:
				return ATOM;
			case ACTION_START:
			case ACTION_END:
				return PROCEDURE;
			default:
				return null;
		}
	}
}
