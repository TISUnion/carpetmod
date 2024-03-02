package carpet.network;

import carpet.CarpetServer;
import carpet.helpers.TickSpeed;
import carpet.settings.CarpetSettings;
import carpet.settings.ParsedRule;
import carpet.settings.SettingsManager;
import carpet.utils.NetworkUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;

import java.util.*;
import java.util.function.BiConsumer;

public class CarpetServerNetworkHandler
{
    private static Map<EntityPlayerMP, String> remoteCarpetPlayers = new HashMap<>();
    private static Set<EntityPlayerMP> validCarpetPlayers = new HashSet<>();

    private static Map<String, BiConsumer<EntityPlayerMP, INBTBase>> dataHandlers = new HashMap<String, BiConsumer<EntityPlayerMP, INBTBase>>(){{
        put("clientCommand", (p, t) -> {
            handleClientCommand(p, (NBTTagCompound)t);
        });
    }};

    public static void handleData(PacketBuffer data, EntityPlayerMP player)
    {
        if (data != null)
        {
            data = ProtocolFixer.fixCarpetPacket(data);

            int id = data.readVarInt();
            if (id == CarpetClient.HELLO)
                onHello(player, data);
            if (id == CarpetClient.DATA)
                onClientData(player, data);
        }
    }

    public static void onPlayerJoin(EntityPlayerMP playerEntity)
    {
        if (!playerEntity.connection.netManager.isLocalChannel())
        {
            playerEntity.connection.sendPacket(new SPacketCustomPayload(
                    CarpetClient.CARPET_CHANNEL,
                    (new PacketBuffer(Unpooled.buffer())).writeVarInt(CarpetClient.HI).writeString(CarpetSettings.carpetVersion)
            ));
        }
        else
        {
            validCarpetPlayers.add(playerEntity);
        }
    }

    public static void onHello(EntityPlayerMP playerEntity, PacketBuffer packetData)
    {
        validCarpetPlayers.add(playerEntity);
        String clientVersion = packetData.readString(64);
        remoteCarpetPlayers.put(playerEntity, clientVersion);
        if (clientVersion.equals(CarpetSettings.carpetVersion))
            CarpetSettings.LOG.info("Player "+playerEntity.getName().getString()+" joined with a matching carpet client");
        else
            CarpetSettings.LOG.warn("Player "+playerEntity.getName().getString()+" joined with another carpet version: "+clientVersion);
        DataBuilder data = DataBuilder.create().withTickRate();
        SettingsManager.getRules().forEach(data::withRule);
        playerEntity.connection.sendPacket(new SPacketCustomPayload(CarpetClient.CARPET_CHANNEL, data.build()));

        CarpetServer.onCarpetClientHello(playerEntity);
    }

    private static void handleClientCommand(EntityPlayerMP player, NBTTagCompound commandData)
    {
        String command = commandData.getString("command");
        String id = commandData.getString("id");
        List<ITextComponent> output = new ArrayList<>();
        String[] error = {null};
        int resultCode = -1;
        if (player.getServer() == null)
        {
            error[0] = "No Server";
        }
        else
        {
            resultCode = player.getServer().getCommandManager().handleCommand(
                    new CommandSource(player, player.getPositionVector(), player.getPitchYaw(),
                    player.world instanceof WorldServer ? (WorldServer) player.world : null,
                    player.server.getPermissionLevel(player.getGameProfile()), player.getName().getString(), player.getDisplayName(),
                    player.world.getServer(), player)
                    {
                        @Override
                        public void sendErrorMessage(ITextComponent message)
                        {
                            error[0] = message.getString();
                        }
                        @Override
                        public void sendFeedback(ITextComponent message, boolean broadcastToOps)
                        {
                            output.add(message);
                        }
                    },
                    command
            );
        };
        NBTTagCompound result = new NBTTagCompound();
        result.putString("id", id);
        result.putInt("code", resultCode);
        if (error[0] != null) result.putString("error", error[0]);
        NBTTagList outputResult = new NBTTagList();
        for (ITextComponent line: output) outputResult.add(new NBTTagString(ITextComponent.Serializer.toJson(line)));
        if (!output.isEmpty()) result.put("output", outputResult);
        player.connection.sendPacket(new SPacketCustomPayload(
                CarpetClient.CARPET_CHANNEL,
                DataBuilder.create().withCustomNbt("clientCommand", result).build()
        ));
        // run command plug to command output,
    }


    private static void onClientData(EntityPlayerMP player, PacketBuffer data)
    {
        NBTTagCompound compound = NetworkUtil.readNbt(data);
        if (compound == null) return;
        for (String key: compound.keySet())
        {
            if (dataHandlers.containsKey(key))
                dataHandlers.get(key).accept(player, compound.get(key));
            else
                CarpetSettings.LOG.warn("Unknown carpet client data: "+key);
        }
    }
    
    public static void updateRuleWithConnectedClients(ParsedRule<?> rule)
    {
        if (CarpetSettings.superSecretSetting) return;
        for (EntityPlayerMP player : remoteCarpetPlayers.keySet())
        {
            player.connection.sendPacket(new SPacketCustomPayload(
                    CarpetClient.CARPET_CHANNEL,
                    DataBuilder.create().withRule(rule).build()
            ));
        }
    }
    
    public static void updateTickSpeedToConnectedPlayers()
    {
        if (CarpetSettings.superSecretSetting) return;
        for (EntityPlayerMP player : remoteCarpetPlayers.keySet())
        {
            player.connection.sendPacket(new SPacketCustomPayload(
                    CarpetClient.CARPET_CHANNEL,
                    DataBuilder.create().withTickRate().build()
            ));
        }
    }

    public static void broadcastCustomCommand(String command, INBTBase data)
    {
        if (CarpetSettings.superSecretSetting) return;
        for (EntityPlayerMP player : validCarpetPlayers)
        {
            player.connection.sendPacket(new SPacketCustomPayload(
                    CarpetClient.CARPET_CHANNEL,
                    DataBuilder.create().withCustomNbt(command, data).build()
            ));
        }
    }

    public static void sendCustomCommand(EntityPlayerMP player, String command, INBTBase data)
    {
        if (isValidCarpetPlayer(player))
        {
            player.connection.sendPacket(new SPacketCustomPayload(
                    CarpetClient.CARPET_CHANNEL,
                    DataBuilder.create().withCustomNbt(command, data).build()
            ));
        }
    }


    public static void onPlayerLoggedOut(EntityPlayerMP player)
    {
        validCarpetPlayers.remove(player);
        if (!player.connection.netManager.isLocalChannel())
            remoteCarpetPlayers.remove(player);
    }

    public static void close()
    {
        remoteCarpetPlayers.clear();
        validCarpetPlayers.clear();
    }

    public static boolean isValidCarpetPlayer(EntityPlayerMP player)
    {
        if (CarpetSettings.superSecretSetting) return false;
        return validCarpetPlayers.contains(player);

    }

    private static class DataBuilder
    {
        private NBTTagCompound tag;
        private static DataBuilder create()
        {
            return new DataBuilder();
        }
        private DataBuilder()
        {
            tag = new NBTTagCompound();
        }
        private DataBuilder withTickRate()
        {
            tag.putFloat("TickRate", TickSpeed.tickrate);
            return this;
        }
        private DataBuilder withRule(ParsedRule<?> rule)
        {
            NBTTagCompound rules = (NBTTagCompound) tag.get("Rules");
            if (rules == null)
            {
                rules = new NBTTagCompound();
                tag.put("Rules", rules);
            }
            NBTTagCompound ruleNBT = new NBTTagCompound();
            ruleNBT.putString("Value", rule.getAsString());
            rules.put(rule.name, ruleNBT);
            return this;
        }

        public DataBuilder withCustomNbt(String key, INBTBase value)
        {
            tag.put(key, value);
            return this;
        }

        private PacketBuffer build()
        {
            PacketBuffer packetBuf = new PacketBuffer(Unpooled.buffer());
            packetBuf.writeVarInt(CarpetClient.DATA);
            packetBuf.writeCompoundTag(tag);
            return packetBuf;
        }
    }
}
