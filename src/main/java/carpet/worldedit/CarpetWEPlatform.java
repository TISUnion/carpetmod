/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package carpet.worldedit;

import carpet.worldedit.internal.ExtendedChunk;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.*;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.lifecycle.Lifecycled;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.enginehub.piston.CommandManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

class CarpetWEPlatform extends AbstractPlatform implements MultiUserPlatform {

    private final CarpetWEWorldEdit mod;
    private final CarpetWEDataFixer dataFixer;
    private final Lifecycled<Optional<Watchdog>> watchdog;
    private boolean hookingEvents = false;

    CarpetWEPlatform(CarpetWEWorldEdit mod) {
        this.mod = mod;
        this.dataFixer = new CarpetWEDataFixer(getDataVersion());

        this.watchdog = CarpetWEWorldEdit.LIFECYCLED_SERVER.map(
            server -> server instanceof DedicatedServer
                ? Optional.of((Watchdog) server)
                : Optional.empty()
        );
    }

    boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public Registries getRegistries() {
        return CarpetWERegistries.getInstance();
    }

    @Override
    public int getDataVersion() {
        return 1631;
    }

    @Override
    public DataFixer getDataFixer() {
        return dataFixer;
    }

    @Override
    public boolean isValidMobType(String type) {
        return IRegistry.ENTITY_TYPE.containsKey(new ResourceLocation(type));
    }

    @Override
    public void reload() {
        getConfiguration().load();
        super.reload();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        return -1;
    }

    @Override
    @Nullable
    public Watchdog getWatchdog() {
        return watchdog.value().flatMap(Function.identity()).orElse(null);
    }

    @Override
    public List<? extends World> getWorlds() {
        Iterable<WorldServer> worlds = CarpetWEWorldEdit.LIFECYCLED_SERVER.valueOrThrow().getWorlds();
        List<World> ret = new ArrayList<>();
        for (WorldServer world : worlds) {
            ret.add(new CarpetWEWorld(world));
        }
        return ret;
    }

    @Nullable
    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof CarpetWEPlayer) {
            return player;
        } else {
            EntityPlayerMP entity = CarpetWEWorldEdit.LIFECYCLED_SERVER.valueOrThrow()
                .getPlayerList().getPlayerByUsername(player.getName());
            return entity != null ? new CarpetWEPlayer(entity) : null;
        }
    }

    @Nullable
    @Override
    public World matchWorld(World world) {
        if (world instanceof CarpetWEWorld) {
            return world;
        } else {
            for (WorldServer ws : CarpetWEWorldEdit.LIFECYCLED_SERVER.valueOrThrow().getWorlds()) {
                if (ws.getWorldInfo().getWorldName().equals(world.getName())) {
                    return new CarpetWEWorld(ws);
                }
            }

            return null;
        }
    }

    @Override
    public void registerCommands(CommandManager manager) {
    }

    @Override
    public void setGameHooksEnabled(boolean enabled) {
        this.hookingEvents = enabled;
    }

    @Override
    public CarpetWEConfiguration getConfiguration() {
        return mod.getConfig();
    }

    @Override
    public String getVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public String getPlatformName() {
        return "TIS Carpet";
    }

    @Override
    public String getPlatformVersion() {
        return mod.getInternalVersion();
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.PREFER_OTHERS);
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
        capabilities.put(Capability.GAME_HOOKS, Preference.NORMAL);
        capabilities.put(Capability.PERMISSIONS, Preference.NORMAL);
        capabilities.put(Capability.USER_COMMANDS, Preference.NORMAL);
        capabilities.put(Capability.WORLD_EDITING, Preference.PREFERRED);
        return capabilities;
    }

    private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS_NO_MIXIN = Sets.immutableEnumSet(
        SideEffect.VALIDATION,
        SideEffect.ENTITY_AI,
        SideEffect.LIGHTING,
        SideEffect.NEIGHBORS
    );

    private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS = Sets.immutableEnumSet(
        Iterables.concat(SUPPORTED_SIDE_EFFECTS_NO_MIXIN, Collections.singleton(SideEffect.UPDATE))
    );

    @Override
    public Set<SideEffect> getSupportedSideEffects() {
        return ExtendedChunk.class.isAssignableFrom(Chunk.class)
            ? SUPPORTED_SIDE_EFFECTS
            : SUPPORTED_SIDE_EFFECTS_NO_MIXIN;
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        PlayerList scm = CarpetWEWorldEdit.LIFECYCLED_SERVER.valueOrThrow().getPlayerList();
        for (EntityPlayerMP entity : scm.getPlayers()) {
            if (entity != null) {
                users.add(new CarpetWEPlayer(entity));
            }
        }
        return users;
    }
}
