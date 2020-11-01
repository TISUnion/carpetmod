package carpet.network;

import carpet.CarpetServer;
import carpet.settings.CarpetSettings;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class CarpetClient
{
    public static final Object sync = new Object();
    public static final int HI = 69;
    public static final int HELLO = 420;
    public static final int DATA = 1;
    // public static ShapesRenderer shapes = null;

    private static EntityPlayerSP clientPlayer = null;
    private static boolean isServerCarpet = false;
    public static String serverCarpetVersion;
    public static final ResourceLocation CARPET_CHANNEL = new ResourceLocation("carpet:hello");

    public static void gameJoined(EntityPlayerSP player)
    {
        synchronized (sync)
        {
            clientPlayer = player;
            // client didn't say hi back yet
            if (isServerCarpet)
                CarpetClientNetworkHandler.respondHello();

        }
    }

    public static void disconnect()
    {
        isServerCarpet = false;
        clientPlayer = null;
        CarpetServer.disconnect();
    }

    public static void setCarpet()
    {
        isServerCarpet = true;
    }

    public static EntityPlayerSP getPlayer()
    {
        return clientPlayer;
    }

    public static boolean isCarpet()
    {
        return isServerCarpet;
    }

    public static boolean sendClientCommand(String command)
    {
        if (!isServerCarpet && CarpetServer.minecraft_server == null) return false;
        CarpetClientNetworkHandler.clientCommand(command);
        return true;
    }

    public static void onClientCommand(INBTBase t)
    {
        CarpetSettings.LOG.info("Server Response:");
        NBTTagCompound tag = (NBTTagCompound)t;
        CarpetSettings.LOG.info(" - id: "+tag.getString("id"));
        CarpetSettings.LOG.info(" - code: "+tag.getInt("code"));
        if (tag.contains("error")) CarpetSettings.LOG.warn(" - error: "+tag.getString("error"));
        if (tag.contains("output"))
        {
            NBTTagList outputTag = (NBTTagList) tag.get("output");
            for (int i = 0; i < outputTag.size(); i++)
                CarpetSettings.LOG.info(" - response: " + ITextComponent.Serializer.fromJson(outputTag.getString(i)).getString());
        }
    }
}
