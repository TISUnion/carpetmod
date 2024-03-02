package carpet.network;

import carpet.CarpetServer;
import carpet.helpers.TickSpeed;
import carpet.settings.CarpetSettings;
import carpet.settings.ParsedRule;
import carpet.settings.SettingsManager;
import carpet.utils.NetworkUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CarpetClientNetworkHandler
{
    private static Map<String, BiConsumer<EntityPlayerSP, INBTBase>> dataHandlers = new HashMap<String, BiConsumer<EntityPlayerSP, INBTBase>>(){{
        put("Rules", (p, t) -> {
            NBTTagCompound ruleset = (NBTTagCompound)t;
            for (String ruleName: ruleset.keySet())
            {
                ParsedRule<?> rule = SettingsManager.getRule(ruleName);
                if (rule == null)
                    CarpetSettings.LOG.error("Received unknown rule: "+ruleName);
                else
                {
                    NBTTagCompound ruleNBT = (NBTTagCompound) ruleset.get(ruleName);
                    String value = ruleNBT.getString("Value");
                    rule.set(null, value);
                }
            }
        });
        put("TickRate", (p, t) -> TickSpeed.tickrate(((NBTPrimitive)t).getFloat(), false));
        put("scShape", (p, t) -> { // deprecated
//            if (CarpetClient.shapes != null)
//                CarpetClient.shapes.addShape((NBTTagCompound)t);
        });
        put("scShapes", (p, t) -> {
//            if (CarpetClient.shapes != null)
//                CarpetClient.shapes.addShapes((ListTag) t);
        });
        put("clientCommand", (p, t) -> {
            CarpetClient.onClientCommand(t);
        });
    }};


    public static void handleData(PacketBuffer data, EntityPlayerSP player)
    {
        if (data != null)
        {
            data = ProtocolFixer.fixCarpetPacket(data);

            int id = data.readVarInt();
            if (id == CarpetClient.HI)
                onHi(data);
            if (id == CarpetClient.DATA)
                onSyncData(data, player);
        }
    }

    private static void onHi(PacketBuffer data)
    {
        synchronized (CarpetClient.sync)
        {
            CarpetClient.setCarpet();
            CarpetClient.serverCarpetVersion = data.readString(64);
            if (CarpetSettings.carpetVersion.equals(CarpetClient.serverCarpetVersion))
            {
                CarpetSettings.LOG.info("Joined carpet server with matching carpet version");
            }
            else
            {
                CarpetSettings.LOG.warn("Joined carpet server with another carpet version: "+CarpetClient.serverCarpetVersion);
            }
            if (CarpetClient.getPlayer() != null)
                respondHello();

        }
    }

    public static void respondHello()
    {
        CarpetClient.getPlayer().connection.sendPacket(new CPacketCustomPayload(
                CarpetClient.CARPET_CHANNEL,
                (new PacketBuffer(Unpooled.buffer())).writeVarInt(CarpetClient.HELLO).writeString(CarpetSettings.carpetVersion)
        ));
    }

    private static void onSyncData(PacketBuffer data, EntityPlayerSP player)
    {
        NBTTagCompound compound = NetworkUtil.readNbt(data);
        if (compound == null) return;
        for (String key: compound.keySet())
        {
            if (dataHandlers.containsKey(key))
                dataHandlers.get(key).accept(player, compound.get(key));
            else
                CarpetSettings.LOG.error("Unknown carpet data: "+key);
        }
    }

    public static void clientCommand(String command)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.putString("id", command);
        tag.putString("command", command);
        NBTTagCompound outer = new NBTTagCompound();
        outer.put("clientCommand", tag);
        CarpetClient.getPlayer().connection.sendPacket(new CPacketCustomPayload(
                CarpetClient.CARPET_CHANNEL,
                (new PacketBuffer(Unpooled.buffer())).writeVarInt(CarpetClient.DATA).writeCompoundTag(outer)
        ));
    }
}
