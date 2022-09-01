package gca.tools;

import carpet.patches.EntityPlayerMPFake;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;

import java.util.Map;

public class FakePlayerResident {

    public static JsonObject save(EntityPlayer player) {
        double pos_x = player.posX;
        double pos_y = player.posY;
        double pos_z = player.posZ;
        double yaw = player.cameraYaw;
        double pitch = player.cameraPitch;
        String dimension = player.world.dimension.getType().toString();
        String gamemode = ((EntityPlayerMP) player).interactionManager.getGameType().getName();
        JsonObject fakePlayer = new JsonObject();
        fakePlayer.addProperty("pos_x", pos_x);
        fakePlayer.addProperty("pos_y", pos_y);
        fakePlayer.addProperty("pos_z", pos_z);
        fakePlayer.addProperty("yaw", yaw);
        fakePlayer.addProperty("pitch", pitch);
        fakePlayer.addProperty("dimension", dimension);
        fakePlayer.addProperty("gamemode", gamemode);
        return fakePlayer;
    }

    public static void load(Map.Entry<String, JsonElement> entry, MinecraftServer server) {
        String username = entry.getKey();
        JsonObject fakePlayer = entry.getValue().getAsJsonObject();
        double pos_x = fakePlayer.get("pos_x").getAsDouble();
        double pos_y = fakePlayer.get("pos_y").getAsDouble();
        double pos_z = fakePlayer.get("pos_z").getAsDouble();
        double yaw = fakePlayer.get("yaw").getAsDouble();
        double pitch = fakePlayer.get("pitch").getAsDouble();
        String dimension = fakePlayer.get("dimension").getAsString();
        String gamemode = fakePlayer.get("gamemode").getAsString();
        EntityPlayerMPFake.createFake(username, server, pos_x, pos_y, pos_z, yaw, pitch,
                DimensionType.byName(new ResourceLocation(dimension)),
                GameType.getByName(gamemode));
    }
}
