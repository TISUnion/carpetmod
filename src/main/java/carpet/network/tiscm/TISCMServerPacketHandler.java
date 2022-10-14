package carpet.network.tiscm;

import carpet.CarpetServer;
import carpet.utils.NbtUtil;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TISCMServerPacketHandler
{
	private static final Logger LOGGER = CarpetServer.LOGGER;
	private static final TISCMServerPacketHandler INSTANCE = new TISCMServerPacketHandler();

	private final Map<TISCMProtocol.C2S, Consumer<HandlerContext.C2S>> handlers = new EnumMap<>(TISCMProtocol.C2S.class);
	private final Map<NetHandlerPlayServer, Set<TISCMProtocol.S2C>> clientSupportedPacketsMap = new WeakHashMap<>();

	private TISCMServerPacketHandler()
	{
		this.handlers.put(TISCMProtocol.C2S.HI, this::handleHi);
		this.handlers.put(TISCMProtocol.C2S.SUPPORTED_S2C_PACKETS, this::handleSupportPackets);
		if (this.handlers.size() < TISCMProtocol.C2S.ID_MAP.size())
		{
			throw new RuntimeException("TISCMServerPacketDispatcher doesn't handle all C2S packets");
		}
	}

	public static TISCMServerPacketHandler getInstance()
	{
		return INSTANCE;
	}

	/**
	 * Invoked on network thread
	 */
	public void dispatch(NetHandlerPlayServer networkHandler, PacketBuffer packetByteBuf)
	{
		String packetId = packetByteBuf.readString(Short.MAX_VALUE);
		NBTTagCompound payload = packetByteBuf.readCompoundTag();
		HandlerContext.C2S ctx = new HandlerContext.C2S(networkHandler, payload);
		ctx.runSynced(() -> TISCMProtocol.C2S.fromId(packetId).
				map(this.handlers::get).
				ifPresent( handler -> handler.accept(ctx)));
	}

	public boolean doesClientSupport(NetHandlerPlayServer networkHandler, TISCMProtocol.S2C packetId)
	{
		if (packetId.isHandshake)
		{
			return true;
		}
		Set<TISCMProtocol.S2C> packetIds = this.clientSupportedPacketsMap.get(networkHandler);
		return packetIds != null && packetIds.contains(packetId);
	}

	public void sendPacket(NetHandlerPlayServer networkHandler, TISCMProtocol.S2C packetId, Consumer<NBTTagCompound> payloadBuilder)
	{
		if (this.doesClientSupport(networkHandler, packetId))
		{
			networkHandler.sendPacket(packetId.packet(payloadBuilder));
		}
	}

	public void broadcast(TISCMProtocol.S2C packetId, Consumer<NBTTagCompound> payloadBuilder)
	{
		this.clientSupportedPacketsMap.forEach((serverPlayNetworkHandler, supportedPackets) -> {
			this.sendPacket(serverPlayNetworkHandler, packetId, payloadBuilder);
		});
	}

	/*
	 * -------------------------
	 *       Packet Senders
	 * -------------------------
	 */

	/*
	 * -------------------------
	 *       Packet Handlers
	 * -------------------------
	 */

	public void handleHi(HandlerContext.C2S ctx)
	{
		String platformName = ctx.payload.getString("platform_name");
		String platformVersion = ctx.payload.getString("platform_version");
		LOGGER.info("Player {} connected with TISCM protocol support ({} @ {})", ctx.playerName, platformName, platformVersion);

		ctx.send(TISCMProtocol.S2C.HELLO, nbt -> {
			nbt.putString("platform_name", TISCMProtocol.PLATFORM_NAME);
			nbt.putString("platform_version", TISCMProtocol.PLATFORM_VERSION);
		});

		List<String> ids = Lists.newArrayList(TISCMProtocol.C2S.ID_MAP.keySet());
		ctx.send(TISCMProtocol.S2C.SUPPORTED_C2S_PACKETS, nbt -> {
			nbt.put("supported_c2s_packets", NbtUtil.stringList2Nbt(ids));
		});
	}

	public void handleSupportPackets(HandlerContext.C2S ctx)
	{
		List<String> ids = NbtUtil.nbt2StringList(ctx.payload.getCompound("supported_s2c_packets"));
		LOGGER.debug("Player {} clientside supported TISCM S2C packet ids: {}", ctx.playerName, ids);
		Set<TISCMProtocol.S2C> packetIds = ids.stream().
				map(TISCMProtocol.S2C::fromId).
				filter(Optional::isPresent).
				map(Optional::get).
				collect(Collectors.toSet());
		this.clientSupportedPacketsMap.put(ctx.networkHandler, packetIds);
	}
}
