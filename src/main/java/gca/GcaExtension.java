package gca;

import carpet.CarpetServer;
import carpet.patches.EntityPlayerMPFake;
import carpet.settings.SettingsManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import gca.api.Function;
import gca.tools.FakePlayerInventoryContainer;
import gca.tools.FakePlayerResident;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GcaExtension {

    public static String MOD_ID = "gca";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static final HashMap<EntityPlayer, FakePlayerInventoryContainer> fakePlayerInventoryContainerMap = new HashMap<>();
    public static final List<Pair<Long, Function>> planFunction = new ArrayList<>();

    public static void onPlayerLoggedIn(EntityPlayerMP player) {
        GcaExtension.fakePlayerInventoryContainerMap.put(player, new FakePlayerInventoryContainer(player));
    }

    private static File getFile(MinecraftServer server) {
        return server.getActiveAnvilConverter().getFile(server.getFolderName(), "fake_player.gca.json");
    }

    public static void onPlayerLoggedOut(EntityPlayerMP player) {
        GcaExtension.fakePlayerInventoryContainerMap.remove(player);
    }

    public static void onGameStarted() {
        SettingsManager.parseSettingsClass(GcaSetting.class);
    }

    public static void onServerClose(MinecraftServer server) {
        JsonObject fakePlayerList = new JsonObject();
        fakePlayerInventoryContainerMap.forEach((player, fakePlayerInventoryContainer) -> {
            if (!(player instanceof EntityPlayerMPFake)) return;
            String username = player.getName().getString();
            fakePlayerList.add(username, FakePlayerResident.save(player));
        });
        File file = getFile(server);
        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedWriter bfw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            bfw.write(new Gson().toJson(fakePlayerList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onServerStart(MinecraftServer server) {
        if (GcaSetting.fakePlayerResident) {
            JsonObject fakePlayerList = new JsonObject();
            File file = getFile(server);
            if (!file.isFile()) {
                return;
            }
            try (BufferedReader bfr = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                fakePlayerList = new Gson().fromJson(bfr, JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String, JsonElement> entry : fakePlayerList.entrySet()) {
                FakePlayerResident.load(entry, server);
            }
            file.delete();
        }
    }
}
