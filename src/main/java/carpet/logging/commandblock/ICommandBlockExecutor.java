package carpet.logging.commandblock;

public interface ICommandBlockExecutor
{
	long getLastLoggedTime();

	void setLastLoggedTime(long time);
}
