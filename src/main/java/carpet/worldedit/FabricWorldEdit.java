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

import com.mojang.brigadier.CommandDispatcher;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlatformUnreadyEvent;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.Command;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static carpet.worldedit.FabricAdapter.adaptPlayer;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;
import static java.util.stream.Collectors.toList;

/**
 * The Fabric implementation of WorldEdit.
 */
public class FabricWorldEdit {

    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final String MOD_ID = "worldedit";
    public static final String VERSION = "7.2.6-SNAPSHOT";
    public static final String CUI_PLUGIN_CHANNEL = "cui";

    public static final SimpleLifecycled<MinecraftServer> LIFECYCLED_SERVER = SimpleLifecycled.invalid();

    private FabricPermissionsProvider provider;

    public static final FabricWorldEdit inst = new FabricWorldEdit();

    private FabricPlatform platform;
    private FabricConfiguration config;
    private Path workingDir;

    public FabricWorldEdit() {
    }

    /*
     * ---------------
     *   Event Hooks
     * ---------------
     */

    public void onInitialize() {
        // Setup working directory
        workingDir = Paths.get("config/worldedit");
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectories(workingDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        this.platform = new FabricPlatform(this);

        WorldEdit.getInstance().getPlatformManager().register(platform);

        config = new FabricConfiguration(this);
        this.provider = getInitialPermissionsProvider();

        LOGGER.info("WorldEdit for Fabric (version " + getInternalVersion() + ") is loaded");
    }

    // fabric-api: ServerLifecycleEvents.SERVER_STARTING
    public void onStartingServer(MinecraftServer minecraftServer) {
        final Path delChunks = workingDir.resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }
    }

    // fabric-api: ServerLifecycleEvents.SERVER_STARTED
    public void onStartServer(MinecraftServer minecraftServer) {
        LIFECYCLED_SERVER.newValue(minecraftServer);

        setupRegistries(minecraftServer);

        config.load();
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent(platform));
    }

    // fabric-api: ServerLifecycleEvents.SERVER_STOPPING
    public void onStopServer(MinecraftServer minecraftServer) {
        LIFECYCLED_SERVER.invalidate();

        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        WorldEdit.getInstance().getEventBus().post(new PlatformUnreadyEvent(platform));
    }

    // fabric-api: ServerLifecycleEvents.END_SERVER_TICK
    public void onEndServerTick(MinecraftServer minecraftServer)
    {
        ThreadSafeCache.getInstance().onEndTick(minecraftServer);
    }

    // TODO Pass empty left click to server
    // fabric-api: ServerPlayConnectionEvents.DISCONNECT
    public void onPlayerDisconnect(EntityPlayerMP player) {
        WorldEdit.getInstance().getEventBus().post(new SessionIdleEvent(new FabricPlayer.SessionKeyImpl(player)));
    }

    // fabric-api: AttackBlockCallback.EVENT
    public EnumActionResult onLeftClickBlock(EntityPlayer playerEntity, World world, EnumHand hand, BlockPos blockPos, EnumFacing direction) {
        if (shouldSkip() || hand == EnumHand.OFF_HAND || world.isRemote) {
            return EnumActionResult.PASS;
        }

        WorldEdit we = WorldEdit.getInstance();
        FabricPlayer player = adaptPlayer((EntityPlayerMP) playerEntity);
        FabricWorld localWorld = getWorld(world);
        Location pos = new Location(localWorld,
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ()
        );
        com.sk89q.worldedit.util.Direction weDirection = FabricAdapter.adaptEnumFacing(direction);

        if (we.handleBlockLeftClick(player, pos, weDirection)) {
            return EnumActionResult.SUCCESS;
        }

        if (we.handleArmSwing(player)) {
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    // fabric-api: UseBlockCallback.EVENT
    public EnumActionResult onRightClickBlock(EntityPlayer playerEntity, World world, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (shouldSkip() || hand == EnumHand.OFF_HAND || world.isRemote) {
            return EnumActionResult.PASS;
        }

        WorldEdit we = WorldEdit.getInstance();
        FabricPlayer player = adaptPlayer((EntityPlayerMP) playerEntity);
        FabricWorld localWorld = getWorld(world);
        Location pos = new Location(localWorld, hitX, hitY, hitZ);
        com.sk89q.worldedit.util.Direction direction = FabricAdapter.adaptEnumFacing(facing);

        if (we.handleBlockRightClick(player, pos, direction)) {
            return EnumActionResult.SUCCESS;
        }

        if (we.handleRightClick(player)) {
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    // fabric-api: UseItemCallback.EVENT
    public ActionResult<ItemStack> onRightClickAir(EntityPlayer playerEntity, World world, EnumHand hand) {
        ItemStack stackInHand = playerEntity.getHeldItem(hand);
        if (shouldSkip() || hand == EnumHand.OFF_HAND || world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, stackInHand);
        }

        WorldEdit we = WorldEdit.getInstance();
        FabricPlayer player = adaptPlayer((EntityPlayerMP) playerEntity);

        if (we.handleRightClick(player)) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stackInHand);
        }

        return new ActionResult<>(EnumActionResult.PASS, stackInHand);
    }

    // fabric-api: CommandRegistrationCallback.EVENT
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
        WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());
        PlatformManager manager = WorldEdit.getInstance().getPlatformManager();
        Platform commandsPlatform = manager.queryCapability(Capability.USER_COMMANDS);
        if (commandsPlatform != platform || !platform.isHookingEvents()) {
            // We're not in control of commands/events -- do not register.
            return;
        }

        List<Command> commands = manager.getPlatformCommandManager().getCommandManager()
            .getAllCommands().collect(toList());
        for (Command command : commands) {
            CommandWrapper.register(dispatcher, command);
            Set<String> perms = command.getCondition().as(PermissionCondition.class)
                .map(PermissionCondition::getPermissions)
                .orElseGet(Collections::emptySet);
            if (!perms.isEmpty()) {
                perms.forEach(getPermissionsProvider()::registerPermission);
            }
        }
    }

    /*
     * --------------------
     *   Event Hooks ends
     * --------------------
     */

    private FabricPermissionsProvider getInitialPermissionsProvider() {
        return new FabricPermissionsProvider.CarpetPermissionsProvider(platform);
    }

    private void setupRegistries(MinecraftServer server) {
        // Blocks
        for (ResourceLocation name : IRegistry.BLOCK.keySet()) {
            if (BlockType.REGISTRY.get(name.toString()) == null) {
                BlockType.REGISTRY.register(name.toString(), new BlockType(name.toString(),
                    input -> FabricAdapter.adapt(FabricAdapter.adapt(input.getBlockType()).getDefaultState())));
            }
        }
        // Items
        for (ResourceLocation name : IRegistry.ITEM.keySet()) {
            if (ItemType.REGISTRY.get(name.toString()) == null) {
                ItemType.REGISTRY.register(name.toString(), new ItemType(name.toString()));
            }
        }
        // Entities
        for (ResourceLocation name : IRegistry.ENTITY_TYPE.keySet()) {
            if (EntityType.REGISTRY.get(name.toString()) == null) {
                EntityType.REGISTRY.register(name.toString(), new EntityType(name.toString()));
            }
        }
        // Biomes
        for (ResourceLocation name : IRegistry.BIOME.keySet()) {
            if (BiomeType.REGISTRY.get(name.toString()) == null) {
                BiomeType.REGISTRY.register(name.toString(), new BiomeType(name.toString()));
            }
        }
        // Tags
        for (ResourceLocation name : BlockTags.getCollection().getRegisteredTags()) {
            if (BlockCategory.REGISTRY.get(name.toString()) == null) {
                BlockCategory.REGISTRY.register(name.toString(), new BlockCategory(name.toString()));
            }
        }
        for (ResourceLocation name : ItemTags.getCollection().getRegisteredTags()) {
            if (ItemCategory.REGISTRY.get(name.toString()) == null) {
                ItemCategory.REGISTRY.register(name.toString(), new ItemCategory(name.toString()));
            }
        }
    }

    private boolean shouldSkip() {
        if (platform == null) {
            return true;
        }

        return !platform.isHookingEvents(); // We have to be told to catch these events
    }

    /**
     * Get the configuration.
     *
     * @return the Fabric configuration
     */
    FabricConfiguration getConfig() {
        return this.config;
    }

    /**
     * Get the session for a player.
     *
     * @param player the player
     * @return the session
     */
    public LocalSession getSession(EntityPlayerMP player) {
        checkNotNull(player);
        return WorldEdit.getInstance().getSessionManager().get(adaptPlayer(player));
    }

    /**
     * Get the WorldEdit proxy for the given world.
     *
     * @param world the world
     * @return the WorldEdit world
     */
    public FabricWorld getWorld(World world) {
        checkNotNull(world);
        return new FabricWorld(world);
    }

    /**
     * Get the WorldEdit proxy for the platform.
     *
     * @return the WorldEdit platform
     */
    public Platform getPlatform() {
        return this.platform;
    }

    /**
     * Get the working directory where WorldEdit's files are stored.
     *
     * @return the working directory
     */
    public Path getWorkingDir() {
        return this.workingDir;
    }

    /**
     * Get the version of the WorldEdit-Fabric implementation.
     *
     * @return a version string
     */
    String getInternalVersion() {
        return VERSION;
    }

    public void setPermissionsProvider(FabricPermissionsProvider provider) {
        this.provider = provider;
    }

    public FabricPermissionsProvider getPermissionsProvider() {
        return provider;
    }
}
