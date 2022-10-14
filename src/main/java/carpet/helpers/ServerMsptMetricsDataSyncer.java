package carpet.helpers;

import carpet.network.tiscm.TISCMProtocol;
import carpet.network.tiscm.TISCMServerPacketHandler;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Serverside data syncer
 */
public class ServerMsptMetricsDataSyncer
{
	private static final ServerMsptMetricsDataSyncer INSTANCE = new ServerMsptMetricsDataSyncer();

	private ServerMsptMetricsDataSyncer() {}

	public static ServerMsptMetricsDataSyncer getInstance()
	{
		return INSTANCE;
	}

	public void broadcastSample(long timeStamp, long msThisTick)
	{
		TISCMServerPacketHandler.getInstance().broadcast(TISCMProtocol.S2C.MSPT_METRICS_SAMPLE, nbt -> {
			nbt.putLong("time_stamp", timeStamp);
			nbt.putLong("millisecond", msThisTick);
		});
	}

	public void receiveMetricData(NBTTagCompound payload)
	{
		// mc 1.13 clientside doesn't have the mspt monitor
	}
}
