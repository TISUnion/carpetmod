package carpet.settings;

import carpet.CarpetServer;
import carpet.logging.microtiming.enums.MicroTimingTarget;
import carpet.logging.microtiming.marker.MicroTimingMarkerManager;
import carpet.utils.Messenger;
import carpet.utils.TISCMConfig;
import carpet.utils.Translations;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.math.shapes.VoxelShapes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static carpet.settings.RuleCategory.*;

public class CarpetSettings
{
    public static final String carpetVersion = "TISCarpet_build_undefined";
    public static final Logger LOG = LogManager.getLogger();
    public static boolean skipGenerationChecks = false;
    public static boolean impendingFillSkipUpdates = false;
    public static final int SHULKERBOX_MAX_STACK_AMOUNT = 64;
    public static boolean isEpsActive = false;

	private static class ValidateVoxelOpt extends Validator<Boolean>
    {
        @Override
        public Boolean validate(CommandSource source, ParsedRule<Boolean> currentRule, Boolean newValue, String string)
        {
            if (!newValue)
            {
                VoxelShapes.FULL_CUBE = VoxelShapes.FULL_CUBE_OLD;
            }
            else
            {
                VoxelShapes.FULL_CUBE = VoxelShapes.FULL_CUBE_NEW;
            }
            return newValue;
        }
    }

    // /$$$$$$$$ /$$$$$$  /$$$$$$   /$$$$$$  /$$      /$$
    //|__  $$__/|_  $$_/ /$$__  $$ /$$__  $$| $$$    /$$$
    //   | $$     | $$  | $$  \__/| $$  \__/| $$$$  /$$$$
    //   | $$     | $$  |  $$$$$$ | $$      | $$ $$/$$ $$
    //   | $$     | $$   \____  $$| $$      | $$  $$$| $$
    //   | $$     | $$   /$$  \ $$| $$    $$| $$\  $ | $$
    //   | $$    /$$$$$$|  $$$$$$/|  $$$$$$/| $$ \/  | $$
    //   |__/   |______/ \______/  \______/ |__/     |__/
    //
    //New features added at TISCarpet goes here for easier reading please

    @Rule(
            desc = "Enables /chunkRegen for regenerating chunks",
            category = COMMAND
    )
    public static String commandChunkRegen = "false";

    @Rule(
            desc = "Enables /epsTest for performance tests",
            category = COMMAND
    )
    public static String commandEPSTest = "false";
    
    @Rule(
            desc = "Enables /ping command to see your ping",
            category = COMMAND
    )
    public static String commandPing = "true";

    @Rule(
            desc = "Enables /refresh command to refresh client desync",
            category = COMMAND
    )
    public static String commandRefresh = "true";

    @Rule(
            desc = "Enables /village command to debug village stuffs",
            category = COMMAND
    )
    public static String commandVillage = "false";

    @Rule(
            desc = "Enables /raycount to calculate probabilities of a block being broken",
            extra = "Takes a block position as an argument",
            category = COMMAND
    )
    public static String commandRaycount = "false";

    @Rule(
            desc = "Enables /sleep command for creating lag",
            category = COMMAND
    )
    public static String commandSleep = "false";

    @Rule(
            desc = "Enables /threadstone or /ts command for some threadstone tools",
            category = COMMAND
    )
    public static String commandThreadstone = "false";

    @Rule(
            desc = "Enables /cluster for cluster chunk calculator & tools",
            category = COMMAND
    )
    public static String commandCluster = "false";

    @Rule(
            desc = "enable visualize projectile logger",
            category = SURVIVAL
    )
    public static boolean visualizeProjectileLoggerEnabled = false;

    @Rule(
    		desc = "No block updates",
    		category = CREATIVE
    )
    public static boolean totallyNoBlockUpdate = false;
    
    
    @Rule(
            desc = "fix Dragon crashes server when no endstone is on the end island",
            category = BUGFIX
    )
    public static boolean dragonCrashFix = false;

    @Rule(
            desc = "optimizes the voxel code which is used by e.g. the entity movement",
            extra = {
                    "This will disable voxel shape optimization by lithium shapes.specialized_shapes",
                    "There's no conclusion about which implementation works best, I'll suggest u to test between setting the rule on or off"
            },
            category = OPTIMIZATION,
            validate = ValidateVoxelOpt.class
    )
    public static boolean optimizeVoxelCode = false;
    
    @Rule(
            desc = "Improved chunk caching by PhiPro",
            category = OPTIMIZATION
    )
    public static boolean chunkCache = false;
    
    @Rule(
            desc = "Disable/Enable the entity momentum cancellation if its above 10 blocks per gametick when reading the data out of disk",
            category = EXPERIMENTAL
    )
    public static boolean entityMomentumLoss = true;
    
    @Rule(
            desc = "Disable the rendering of stained glass on maps",
            category = EXPERIMENTAL
    )
    public static boolean stainedGlassNoMapRendering = false;

    @Rule(
            desc = "Caching explosions, useful for situations eg pearl cannon",
            category = OPTIMIZATION
    )
    public static boolean cacheExplosions = false;

    @Rule(
            desc = "Treat any subchunk with light changes as a not-empty subchunk to solve the missing sky/block light in empty subchunk after reloading the chunk",
            extra = "No more ghost shadows below giant floating buildings",
            category = {EXPERIMENTAL, BUGFIX}
            )
    public static boolean missingLightFix = false;
    
    @Rule(
            desc = "Uses alternative lighting engine by PhiPros. AKA NewLight mod",
            extra = "Now Ported to 1.13 by Salandora!",
            category = {EXPERIMENTAL, OPTIMIZATION}
    )
    public static boolean newLight = false;
    
    @Rule(
            desc = "Greatly improve the efficiency of nether portal by LucunJi",
            extra = {
                    "Most powerful portal optimization ever, 10000 times faster!",
                    "DOES NOT WORK with portals created/destroyed when fillUpdate is false"
            },
            category = {EXPERIMENTAL, OPTIMIZATION}
    )
    public static boolean portalSuperCache = false;

    @Rule(
            desc = "Enable the function of MicroTiming logger",
            extra = {
                    "Display redstone components actions, blockupdates and stacktrace with a wool block",
                    "Use /log microTiming to start logging",
                    "Might impact the server performance when it's on",
                    "EndRods will detect block updates and redstone components will show their actions",
                    "- Observer, Piston, EndRod: pointing towards wool",
                    "- Repeater, Comparator, Rail, Button, etc.: placed on wool",
                    "Beside that, blocks pointed by EndRod on wool block will also show their actions"
            },
            category = {CREATIVE}
    )
    public static boolean microTiming = false;

    @Rule(
            desc = "Allow player to right click with dye item to mark a block to be logged by microTiming logger",
            extra = {
                    "You need to subscribe to microTiming logger for marking or displaying blocks",
                    "Right click with the same dye to switch the marker to end rod mode with which block update information will be logged additionally. Right click again to remove the marker",
                    "Right click a marker with slime ball item to make it movable. It will move to the corresponding new position when the attaching block is moved by a piston",
                    "Use `/carpet microTimingDyeMarker clear` to remove all markers",
                    "You can create a named marker by using a renamed dye item. Marker name will be shown in logging message as well",
                    "[TODO] You can see boxes at marked blocks with fabric-carpet installed on your client. " +
                            "With carpet-tis-addition installed the marker name could also be seen through blocks",
            },
            options = {"false", "true", "clear"},
            validate = ValidateMicroTimingDyeMarker.class,
            category = {CREATIVE}
    )
    public static String microTimingDyeMarker = "true";

    private static class ValidateMicroTimingDyeMarker extends Validator<String>
    {
        @Override
        public String validate(CommandSource source, ParsedRule<String> currentRule, String newValue, String string)
        {
            if ("clear".equals(newValue))
            {
                MicroTimingMarkerManager.getInstance().clear();
                if (source != null)
                {
                    Messenger.m(source, "w " + MicroTimingMarkerManager.getInstance().tr("cleared", "Marker cleared"));
                }
                return currentRule.get();
            }
            return newValue;
        }
    }

    @Rule(
            desc = "Modify the way to specify events to be logged in microTiming logger",
            extra = {
                    "labelled: Logs events labelled with wool",
                    "in_range: Logs events within 32m of any player",
                    "all: Logs every event. Use with caution"
            },
            category = {CREATIVE}
    )
    public static MicroTimingTarget microTimingTarget = MicroTimingTarget.LABELLED;

    @Rule(
            desc = "Overwrite the size limit of structure block",
            extra = {
                    "Relative position might display wrongly on client side if it's larger than 32"
            },
            options = {"32", "64", "96", "128"},
            validate = ValidateStructureBlockLimit.class,
            category = CREATIVE
    )
    public static int structureBlockLimit = 32;

    private static class ValidateStructureBlockLimit extends Validator<Integer>
    {
        @Override
        public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string)
        {
            return (newValue > 0 && newValue <= 65536) ? newValue : null;
        }
        public String description()
        {
            return "You must choose a value from 1 to 1000";
        }
    }
    
    @Rule(
            desc = "Optimizes hoppers and droppers interacting with chests",
            extra = "Credits: skyrising (Quickcarpet)",
            category = {EXPERIMENTAL, OPTIMIZATION}
    )
    public static boolean optimizedInventories = false;

    @Rule(
            desc = "Overwrite the tracking distance of xp orb",
            extra = {
                    "Change it to 0 to disable tracking"
            },
            options = {"0", "1", "8", "32"},
            validate = ValidateXPTrackingDistance.class,
            category = CREATIVE
    )
    public static double xpTrackingDistance = 8;

    private static class ValidateXPTrackingDistance extends Validator<Double>
    {
        @Override
        public Double validate(CommandSource source, ParsedRule<Double> currentRule, Double newValue, String string)
        {
            return (newValue >= 0 && newValue <= 128) ? newValue : null;
        }
        public String description()
        {
            return "You must choose a value from 0 to 128";
        }
    }

    @Rule(
            desc = "Optimized Elytra deployment",
            extra = "Code from 1.15. Fixes MC-111444",
            category = {EXPERIMENTAL, BUGFIX}
    )
    public static boolean elytraDeploymentFix = false;

    @Rule(
            desc = "Set the random size ratio in doExplosionA to a fixed value",
            extra = {
                    "More exactly, it replaces the return value of nextFloat() for blast ray strength randomization, " +
                            "so the random blast strength of the explosion becomes predictable",
                    "The value should be between 0.0 and 1.0, where 0.0 is for minimum possible strength and 1.0 is for maxmium possible strength. Set it to -1.0 to disable overriding",
            },
            validate = ExplosionRandomRatioValidator.class,
            options = {"-1.0", "0.0", "0.5", "1.0"},
            category = CREATIVE
    )
    public static float explosionRandomRatio = -1.0F;

    private static class ExplosionRandomRatioValidator extends Validator<Float>
    {
        @Override
        public Float validate(CommandSource source, ParsedRule<Float> currentRule, Float newValue, String string) {
            return newValue == -1.0F || (0.0F <= newValue && newValue <= 1.0F) ? newValue : null;
        }

        @Override
        public String description() {
            return "You must choose a value from 0.7 to 1.3 or -1";
        }
    }

    @Rule(
            desc = "Set the range where player will receive a block event packet after a block event fires successfully",
            extra = "For piston the packet is used to render the piston movement animation. Decrease it to reduce client's lag",
            validate = BlockEventPacketRangeValidator.class,
            options = {"0", "16", "64", "128"},
            category = OPTIMIZATION
    )
    public static double blockEventPacketRange = 64;

    private static class BlockEventPacketRangeValidator extends Validator<Double>
    {
        @Override
        public Double validate(CommandSource source, ParsedRule<Double> currentRule, Double newValue, String string) {
            return 0 <= newValue && newValue <= 256 ? newValue : null;
        }

        @Override
        public String description() {
            return "You must choose a value from 0 to 256";
        }
    }

    @Rule(
            desc = "Overwrite the default fuse duration of TNT",
            extra = "This might also affects the fuse duration of TNT ignited in explosion",
            options = {"0", "80", "32767"},
            validate = ValidateTNTFuseDuration.class,
            category = CREATIVE
    )
    public static int tntFuseDuration = 80;

    private static class ValidateTNTFuseDuration extends Validator<Integer>
    {
        @Override
        public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string)
        {
            return 0 <= newValue && newValue <= 32767 ? newValue : null;
        }
        public String description()
        {
            return "You must choose a integer from 0 to 32767";
        }
    }

    @Rule(
            desc = "HUD Logger update interval",
            options = {"2", "5", "20", "100", "1200"},
            validate = ValidateHUDLoggerUpdateInterval.class,
            category = FEATURE
    )
    public static int HUDLoggerUpdateInterval = 20;

    private static class ValidateHUDLoggerUpdateInterval extends Validator<Integer>
    {
        @Override
        public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string)
        {
            return 1 <= newValue && newValue <= 1200 ? newValue : null;
        }
        public String description()
        {
            return "You must choose a integer from 1 to 1200";
        }
    }

    @Rule(
            desc = "Disable some command to prevent accidentally cheating",
            extra = "Affects command list: /gamemode, /tp, /teleport, /give, /setblock, /summon",
            category = SURVIVAL
    )
    public static boolean opPlayerNoCheat = false;

    @Rule(
            desc = "Make hopper pointing towards wool has infinity speed to suck in or transfer items",
            extra = {
                    "Only works when hopperCounters option in Carpet Mod is on"
            },
            category = CREATIVE
    )
    public static boolean hopperCountersUnlimitedSpeed = false;

    @Rule(
            desc = "Make command blocks on redstone ores execute command instantly instead of scheduling a 1gt delay TileTick event for execution",
            extra = "Only affects normal command blocks",
            category = CREATIVE
    )
    public static boolean instantCommandBlock = false;

    @Rule(
            desc = "Disable spamming checks on players, including: chat message cooldown, creative item drop cooldown",
            category = {CREATIVE, SURVIVAL}
    )
    public static boolean antiSpamDisabled = false;

    @Rule(
            desc = "Disable entity collision check before block placement, aka you can place blocks inside entities",
            extra = {
                    "Works with creative mode players only"
            },
            category = {CREATIVE}
    )
    public static boolean blockPlacementIgnoreEntity = false;
    @Rule(
            desc = "Modify how often the chunk tick occurs per chunk per game tick",
            extra = {
                    "The default value is 1. Set it to 0 to disables chunk ticks",
                    "Affected game phases: thunder, ice and snow, randomtick",
                    "With a value of n, in every chunk every game tick, climate things will tick n times, and randomtick will tick n * randomTickSpeed times per chunk section"
            },
            options = {"0", "1", "10", "100", "1000"},
            validate = Validator.NONNEGATIVE_NUMBER.class,
            category = {CREATIVE}
    )
    public static int chunkTickSpeed = 1;

    @Rule(
            desc = "Modify the limit of executed tile tick events per game tick",
            options = {"1024", "65536", "2147483647"},
            validate = ValidatePositive.class,
            category = {CREATIVE}
    )
    public static int tileTickLimit = 65536;

    private static class ValidatePositive extends Validator<Number>
    {
        @Override
        public Number validate(CommandSource source, ParsedRule<Number> currentRule, Number newValue, String string)
        {
            return newValue.doubleValue() > 0.0D ? newValue : null;
        }
        public String description()
        {
            return "You must choose a positive value";
        }
    }

    @Rule(
            desc = "Halve the delay of redstone repeaters upon a redstone ore",
            extra = {
                    "The delay will change from 2, 4, 6 or 8 game tick instead of 1, 2, 3 or 4 game tick"
            },
            category = {CREATIVE}
    )
    public static boolean repeaterHalfDelay = false;

    @Rule(
            desc = "Enables /lifetime command to track entity lifetime and so on",
            extra = {
                    "Useful for mob farm debugging etc."
            },
            category = COMMAND
    )
    public static String commandLifeTime = "true";

    @Rule(
            desc = "Optimize entity colliding with entities with hard hit box",
            extra = {
                    "It uses an extra separate list to store entities, that have a hard hit box including boat and shulker, in a chunk",
                    "It reduces quite a lot of unnecessary iterating when an entity is moving and trying to search entities with hard hit box on the way," +
                            "since the world is always not filled with boats and shulkers",
                    "Enable it before loading the chunk to make it work",
                    "See also: optimizedPushableEntityCollision"
            },
            category = {OPTIMIZATION, EXPERIMENTAL}
    )
    public static boolean optimizedHardHitBoxEntityCollision = false;

    @Rule(
            desc = "Optimize fast entity movement by only checking block collisions on current moving axis",
            extra = {
                    "Inspired by the fastMovingEntityOptimization rule in carpetmod112",
                    "Works with optimizeVoxelCode off. Rule optimizeVoxelCode itself already has relative optimization",
            },
            category = {OPTIMIZATION, EXPERIMENTAL}
    )
    public static boolean optimizedFastEntityMovement = false;

    @Rule(
            desc = "Optimize entity fetching when entities try to collide with nearby pushable entities",
            extra = {
                    "It uses an extra separate list to store entities, that have the possibility of being pushed, in a chunk",
                    "It reduces unnecessary iterating on the entity list if there are tons of non-pushable entities nearby e.g. items",
                    "Enable it before loading the chunk to make it work",
                    "See also: optimizedHardHitBoxEntityCollision"
            },
            category = {OPTIMIZATION, EXPERIMENTAL}
    )
    public static boolean optimizedPushableEntityCollision = false;

    @Rule(
            desc = "Fixed memory leak caused by drowned path finding AI",
            extra = "Check MC-202246 for details",
            category = {BUGFIX}
    )
    public static boolean drownedNavigatorMemoryLeakFix = false;

    @Rule(
            desc = "Allow creative players to open a container even if the container is blocked. e.g. for shulker box",
            category = {CREATIVE}
    )
    public static boolean creativeOpenContainerForcibly = false;

    public static final int VANILLA_CHUNK_UPDATE_PACKET_THRESHOLD = 64;
    public static final int MAXIMUM_CHUNK_UPDATE_PACKET_THRESHOLD = 65536;
    @Rule(
            desc = "The threshold which the game will just send an chunk data packet if the amount of block change is more than",
            extra = {
                    "Increasing this value might reduce network bandwidth usage",
                    "Set it to really high to simulate 1.16+ behavior, which is no chunk packet but only multiple block change packet"
            },
            validate = ValidateChunkUpdatePacketThreshold.class,
            options = {"64", "512", "4096", "65536"},
            strict = false,
            category = {EXPERIMENTAL}
    )
    public static int chunkUpdatePacketThreshold = VANILLA_CHUNK_UPDATE_PACKET_THRESHOLD;
    private static class ValidateChunkUpdatePacketThreshold extends Validator<Integer>
    {
        @Override
        public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string)
        {
            return (newValue >= 2 && newValue <= MAXIMUM_CHUNK_UPDATE_PACKET_THRESHOLD) ? newValue : null;
        }

        public String description()
        {
            return "You must choose a value from 2 to " + MAXIMUM_CHUNK_UPDATE_PACKET_THRESHOLD;
        }
    }

    @Rule(
            desc = "Hopper with wool block on top outputs item infinitely without having its item decreased",
            category = {CREATIVE}
    )
    public static boolean hopperNoItemCost = false;

    @Rule(
            desc = "Disable block destruction by fluid flowing",
            extra = {
                    "Fluid will just simple stopped at the state before destroying the block",
                    "It's useful to prevent liquid from accidentally flooding your redstone wiring in creative"
            },
            category = {CREATIVE}
    )
    public static boolean fluidDestructionDisabled = false;

    @Rule(
            desc = "Remove all enchantment restriction checks inside /enchant command",
            category = {CREATIVE}
    )
    public static boolean enchantCommandNoRestriction = false;

    @Rule(
            desc = "Fixed player might become invisible after switching between dimensions",
            extra = {
                    "Minecraft will make a `tickEntity` call in the previous world when changing the dimension of a player",
                    "which causes the player entity to be added in a chunk in the new player position in the previous world",
                    "If the player returns to the previous dimension before that chunk gets unloaded, when that chunk gets unloaded,",
                    "the player will be removed from the world, which causes the player becomes invisible and more weird stuffs"
            },
            category = {BUGFIX}
    )
    public static boolean transDimensionInvisibleFix = false;

    @Rule(
            desc = "Ignore invalid property keys/values in block state arguments used in e.g. /setblock command",
            extra = {
                    "In vanilla invalid property keys/values cause command failure when parsing, this rule suppresses that",
                    "Useful during cross-version litematica schematic pasting etc."
            },
            category = {CREATIVE}
    )
    public static boolean failSoftBlockStateParsing = false;

    @Rule(
            desc = "Prevent TNT blocks from being ignited from redstone",
            extra = "You can still use explosion etc. to ignite a tnt",
            category = {CREATIVE}
    )
    public static boolean tntIgnoreRedstoneSignal = false;

    @Rule(
            desc = "Strategy for lifetime tracker to deal with mob that doesn't count towards mobcap",
            extra = {
                    "true: Don't track mobs that don't count towards mobcap, and treat mobs as removal as soon as they don't affect mobcap e.g. right when they pick up some items. Good for mob farm designing",
                    "false: Tracks everything it can track and mark mobs as removal when they actually get removed. Good for raid testing Good for raid testing or non-mobfarms"
            },
            category = {CREATIVE}
    )
    public static boolean lifeTimeTrackerConsidersMobcap = true;

    @Rule(
            desc = "Disable block and entity collision check during entity placement with items",
            extra = {
                    "Affected items: armorstand, end crystal, all kinds of boat",
                    "Spawn egg items are not affected"
            },
            category = {CREATIVE}
    )
    public static boolean entityPlacementIgnoreCollision = false;

    @Rule(
            desc = "When placing / summoning entity with item, place the entity at the exact position of player's cursor target position",
            extra = "Affected items: Spawn eggs, armorstand, ender crystal",
            category = {CREATIVE}
    )
    public static boolean preciseEntityPlacement = false;

    @Rule(
            desc = "Make player be able to place block against cauldron block with any filled level",
            extra = "Affected Minecraft <= 1.16.x. This annoying behavior is already been fixed in 1.17+",
            category = {BUGFIX}
    )
    public static boolean cauldronBlockItemInteractFix = false;

    public static final int VANILLA_SNOW_MELT_MIN_LIGHT_LEVEL = 12;
    @Rule(
            desc = "The minimum light level allowed for snow to melt",
            extra = {
                    "In vanilla the value is 12, which means snow will melt when the light level >=12 when random ticked",
                    "Set it to 0 to melt all annoying snow on your builds",
                    "Set it to the same level as the minimum light level for snow to not fall on block (light level 10) to easily test if your build is snow-proof with light or not",
                    "You can modify gamerule randomTickSpeed to speed up the melting progress, or modify carpet rule chunkTickSpeed to speed up the snowfall progress"
            },
            options = {"0", "10", "12"},
            strict = false,
            validate = Validator.NONNEGATIVE_NUMBER.class,
            category = {CREATIVE}
    )
    public static int snowMeltMinLightLevel = VANILLA_SNOW_MELT_MIN_LIGHT_LEVEL;

    @Rule(
            desc = "The maximum horizontal chebyshev distance (in chunks) for the server to sync entities information to the client",
            extra = {
                    "Basically this works as a \"entity view distance\", but will still be limited to the server view distance",
                    "Set it to a value not less than the server view distance to make the server sync all entities within the view distance to the client",
                    "Set it to a non-positive value to use vanilla logic",
                    "Requires chunk reloading to set the new rule value to entities"
            },
            options = {"-1", "16", "64"},
            strict = false,
            category = {CREATIVE}
    )
    public static int entityTrackerDistance = -1;

    @Rule(
            desc = "The time interval (in gametick) for the server to sync entities information to the client",
            extra = {
                    "With a small number e.g. 1, entity information will be synced to the client every 1 gametick, resulting in less-likely client-side entity desync",
                    "Set it to a non-positive value to use vanilla logic",
                    "Requires chunk reloading to set the new rule value to entities"
            },
            options = {"-1", "1"},
            strict = false,
            category = {CREATIVE}
    )
    public static int entityTrackerInterval = -1;

    @Rule(
            desc = "Disable the wither spawned sound emitted when a wither fully reset its health after summoned",
            category = {CREATIVE}
    )
    public static boolean witherSpawnedSoundDisabled = false;

    public static final double VANILLA_MINECART_TAKE_PASSENGER_MIN_VELOCITY = 0.1D;  // sqrt(0.01)
    @Rule(
            desc = "Determine the minimum required horizontal velocity for a minecart to pick up nearby entity as its passenger",
            extra = {
                    "Set it to 0 to let minecart always take passenger no matter how fast it is, just like a boat",
                    "Set it to NaN to let minecart never takes passenger",
            },
            options = {"0", "0.1", "NaN"},
            strict = false,
            category = {CREATIVE}
    )
    public static double minecartTakePassengerMinVelocity = VANILLA_MINECART_TAKE_PASSENGER_MIN_VELOCITY;

    @Rule(
            desc = "Fixed stored client settings are not migrated from the old player entity during player respawn or entering end portal in the end",
            extra = {
                    "So mods relies on client settings are always able to work correctly, e.g. serverside translation of this mod and worldedit mod"
            },
            category = {BUGFIX}
    )
    public static boolean clientSettingsLostOnRespawnFix = false;

    @Rule(
            desc = "Modify the related altitude between the bottom of the world and the void where entities will receive void damages",
            options = {"-64", "-512", "-4096"},
            validate = Validator.NEGATIVE.class,
            strict = false,
            category = CREATIVE
    )
    public static double voidRelatedAltitude = -64.0D;

    @Rule(
            desc = "Deobfuscate stack traces in crash report",
            category = {FEATURE}
    )
    public static boolean deobfuscateCrashReportStackTrace = false;

    @Rule(
            desc = "The switch of the TISCM network protocol",
            category = {TISCM_PROTOCOL}
    )
    public static boolean tiscmNetworkProtocol = false;

    @Rule(
            desc = "Sync server's mspt metrics data to the client, so players can see that in the debug screen with F3 + ALT",
            extra = {
                    "Carpet TIS Addition is required to be installed on the client"
            },
            category = {CLIENT}
    )
    public static boolean syncServerMsptMetricsData = false;

    @Rule(
            desc = "Allow creative players place water via water bucket in nether",
            extra = {
                    "Technically this rule applies to all ultrawarm dimensions"
            },
            category = {CREATIVE}
    )
    public static boolean creativeNetherWaterPlacement = false;

    @Rule(
            desc = "Enables /removeentity command for directly erase target entities from the world",
            category = {COMMAND, CREATIVE}
    )
    public static String commandRemoveEntity = "ops";

    @Rule(
            desc = "Add a double confirmation for /stop command to prevent stopping server accidentally",
            extra = {
                    "You need to enter /stop twice within 1 minute to stop the server",
                    "This mechanics only works for players"
            },
            category = {COMMAND}
    )
    public static boolean stopCommandDoubleConfirmation = false;

    @Rule(
            desc = "Observers updated on an async thread will behave like an ITT observer in 1.12. ",
            category = {CREATIVE}
    )
    public static boolean asyncUpdatesAmplifier = false;

    @Rule(
            desc = "A note block does a specific thing on activation, based on the block underneath. \n " +
                    "Purpur block - Sends out a PP update on an async thread. \n" +
                    "Comparator - Lag the current thread for 10x milliseconds, where x is the power level " +
                    "of the comparator. \n" +
                    "Activator rails - Throws a StackOverflowError to imitate update suppression. \n",
            category = {CREATIVE}
    )
    public static boolean debugNoteBlocks = false;

    /*
    @Rule(
            desc = "If a beacon receives an NC update when a purpur block is adjacent, " +
                    "it will start a new thread and send an NC update there. ",
            category = {CREATIVE}
    )
    public static boolean asyncBeaconUpdates = false;
    */

    @Rule(
            desc = "Enables /palette command for block state palette debug",
            category = COMMAND
    )
    public static String commandPalette = "false";

    @Rule(desc = "Fixes the async packet bugs related to asynch observer updates.", category = BUGFIX)
    public static boolean asyncPacketUpdatesFix = false;



    // /$$$$$$$$ /$$$$$$  /$$$$$$   /$$$$$$  /$$      /$$
    //|__  $$__/|_  $$_/ /$$__  $$ /$$__  $$| $$$    /$$$
    //   | $$     | $$  | $$  \__/| $$  \__/| $$$$  /$$$$
    //   | $$     | $$  |  $$$$$$ | $$      | $$ $$/$$ $$
    //   | $$     | $$   \____  $$| $$      | $$  $$$| $$
    //   | $$     | $$   /$$  \ $$| $$    $$| $$\  $ | $$
    //   | $$    /$$$$$$|  $$$$$$/|  $$$$$$/| $$ \/  | $$
    //   |__/   |______/ \______/  \______/ |__/     |__/    END OF TISCM
    
    @Rule(
            desc = "Fixes server crashing supposedly on falling behind 60s in ONE tick, yeah bs.",
            extra = "Fixed 1.12 watchdog crash in 1.13 pre-releases, reintroduced with 1.13, GG.",
            category = BUGFIX
    )
    public static boolean watchdogCrashFix = false;

    @Rule(
            desc = "Nether portals correctly place entities going through",
            extra = "Entities shouldn't suffocate in obsidian",
            category = BUGFIX
    )
    public static boolean portalSuffocationFix = false;

    @Rule(desc = "Gbhs sgnf sadsgras fhskdpri!", category = EXPERIMENTAL)
    public static boolean superSecretSetting = false;


    @Rule(desc = "Guardians honor players invisibility effect", category = BUGFIX)
    public static boolean invisibilityFix = false;

    @Rule(
            desc = "Amount of delay ticks to use a nether portal in creative",
            options = {"1", "40", "80", "72000"},
            category = CREATIVE,
            validate = OneHourMaxDelayLimit.class
    )
    public static int portalCreativeDelay = 1;

    private static class OneHourMaxDelayLimit extends Validator<Integer> {
        @Override
        public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return (newValue > 0 && newValue <= 72000) ? newValue : null;
        }
        @Override
        public String description() { return "You must choose a value from 1 to 72000";}
    }

    @Rule(desc = "Dropping entire stacks works also from on the crafting UI result slot", category = {BUGFIX, SURVIVAL})
    public static boolean ctrlQCraftingFix = false;

    @Rule(
            desc = "Parrots don't get of your shoulders until you receive damage",
            category = {SURVIVAL, FEATURE}
    )
    public static boolean persistentParrots = false;

    /*@Rule(
            desc = "Mobs growing up won't glitch into walls or go through fences",
            category = BUGFIX,
            validate = Validator.WIP.class
    )
    public static boolean growingUpWallJump = false;

    @Rule(
            desc = "Won't let mobs glitch into blocks when reloaded.",
            extra = "Can cause slight differences in mobs behaviour",
            category = {BUGFIX, EXPERIMENTAL},
            validate = Validator.WIP.class
    )
    public static boolean reloadSuffocationFix = false;
    */

    @Rule( desc = "Players absorb XP instantly, without delay", category = CREATIVE )
    public static boolean xpNoCooldown = false;

    @Rule( desc = "XP orbs combine with other into bigger orbs", category = FEATURE )
    public static boolean combineXPOrbs = false;

    @Rule(
            desc = "Empty shulker boxes can stack to 64 when dropped on the ground",
            extra = "To move them around between inventories, use shift click to move entire stacks",
            category = {SURVIVAL, FEATURE}
    )
    public static boolean stackableShulkerBoxes = false;

    @Rule( desc = "Explosions won't destroy blocks", category = CREATIVE )
    public static boolean explosionNoBlockDamage = false;

    @Rule( desc = "Removes random TNT momentum when primed", category = CREATIVE )
    public static boolean tntPrimerMomentumRemoved = false;

    @Rule(
            desc = "Lag optimizations for redstone dust",
            extra = "by Theosib",
            category = {EXPERIMENTAL, OPTIMIZATION}
    )
    public static boolean fastRedstoneDust = false;

    @Rule(desc = "Only husks spawn in desert temples", category = FEATURE)
    public static boolean huskSpawningInTemples = false;

    @Rule( desc = "Shulkers will respawn in end cities", category = FEATURE )
    public static boolean shulkerSpawningInEndCities = false;

    @Rule(desc = "Entities pushed or moved into unloaded chunks no longer disappear", category = {EXPERIMENTAL, BUGFIX})
    public static boolean unloadedEntityFix = false;

    @Rule( desc = "TNT doesn't update when placed against a power source", category = CREATIVE )
    public static boolean tntDoNotUpdate = false;

    @Rule(
            desc = "Prevents players from rubberbanding when moving too fast",
            extra = "Puts more trust in clients positioning",
            category = {CREATIVE, SURVIVAL}
    )
    public static boolean antiCheatDisabled = false;

    @Rule(desc = "Pistons, droppers and dispensers react if block above them is powered", category = CREATIVE)
    public static boolean quasiConnectivity = true;

    @Rule(
            desc = "Players can flip and rotate blocks when holding cactus",
            extra = {
                    "Doesn't cause block updates when rotated/flipped",
                    "Applies to pistons, observers, droppers, repeaters, stairs, glazed terracotta etc..."
            },
            category = {CREATIVE, SURVIVAL, FEATURE}
    )
    public static boolean flippinCactus = false;

    @Rule(
            desc = "hoppers pointing to wool will count items passing through them",
            extra = {
                    "Enables /counter command, and actions while placing red and green carpets on wool blocks",
                    "Use /counter <color?> reset to reset the counter, and /counter <color?> to query",
                    "In survival, place green carpet on same color wool to query, red to reset the counters",
                    "Counters are global and shared between players, 16 channels available",
                    "Items counted are destroyed, count up to one stack per tick per hopper"
            },
            category = {COMMAND, CREATIVE, FEATURE}
    )
    public static boolean hopperCounters = false;

    @Rule( desc = "Guardians turn into Elder Guardian when struck by lightning", category = FEATURE )
    public static boolean renewableSponges = false;

    @Rule( desc = "Pistons can push tile entities, like hoppers, chests etc.", category = {EXPERIMENTAL, FEATURE} )
    public static boolean movableTileEntities = false;

    @Rule( desc = "Saplings turn into dead shrubs in hot climates and no water access", category = FEATURE )
    public static boolean desertShrubs = false;

    @Rule( desc = "Silverfish drop a gravel item when breaking out of a block", category = FEATURE )
    public static boolean silverFishDropGravel = false;

    @Rule( desc = "summoning a lightning bolt has all the side effects of natural lightning", category = CREATIVE )
    public static boolean summonNaturalLightning = false;

    @Rule(desc = "Enables /spawn command for spawn tracking", category = COMMAND)
    public static String commandSpawn = "true";

    @Rule(
            desc = "Enables /profile command to monitor game performance",
            extra = "subset of /tick command capabilities",
            category = COMMAND
    )
    public static String commandTick = "true";

    @Rule(desc = "Enables /tick command to control game clocks", category = COMMAND)
    public static String commandProfile = "true";

    @Rule(desc = "Enables /log command to monitor events in the game via chat and overlays", category = COMMAND)
    public static String commandLog = "true";

    @Rule(
            desc = "Enables /distance command to measure in game distance between points",
            extra = "Also enables brown carpet placement action if 'carpets' rule is turned on as well",
            category = COMMAND
    )
    public static String commandDistance = "true";

    @Rule(
            desc = "Enables /info command for blocks",
            extra = {
                    "Also enables gray carpet placement action",
                    "if 'carpets' rule is turned on as well"
            },
            category = COMMAND
    )
    public static String commandInfo = "true";

    @Rule(
            desc = "Enables /c and /s commands to quickly switch between camera and survival modes",
            extra = "/c and /s commands are available to all players regardless of their permission levels",
            category = COMMAND
    )
    public static String commandCameramode = "true";

    @Rule(
            desc = "Enables /perimeterinfo command",
            extra = "... that scans the area around the block for potential spawnable spots",
            category = COMMAND
    )
    public static String commandPerimeterInfo = "true";

    @Rule(desc = "Enables /draw commands", extra = "... allows for drawing simple shapes", category = COMMAND)
    public static String commandDraw = "true";

    @Rule(desc = "Enables /script command", extra = "An in-game scripting API for Scarpet programming language", category = COMMAND)
    public static String commandScript = "true";

    @Rule(desc = "Enables /player command to control/spawn players", category = COMMAND)
    public static String commandPlayer = "true";

    @Rule(desc = "Placing carpets may issue carpet commands for non-op players", category = SURVIVAL)
    public static boolean carpets = false;

    @Rule(
            desc = "Pistons, Glass and Sponge can be broken faster with their appropriate tools",
            category = SURVIVAL
    )
    public static boolean missingTools = false;

    @Rule(desc = "Alternative, persistent caching strategy for nether portals", category = {SURVIVAL, CREATIVE})
    public static boolean portalCaching = false;

    @Rule(desc = "fill/clone/setblock, structure blocks and worldedit cause block updates", category = CREATIVE)
    public static boolean fillUpdates = true;

    private static class PushLimitLimits extends Validator<Integer>
    {
        @Override public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return (newValue>0 && newValue <= 1024) ? newValue : null;
        }
        @Override
        public String description() { return "You must choose a value from 1 to 1024";}
    }
    
    @Rule(
            desc = "Customizable piston push limit",
            options = {"10", "12", "14", "100"},
            category = CREATIVE,
            validate = PushLimitLimits.class
    )
    public static int pushLimit = 12;

    @Rule(
            desc = "Customizable powered rail power range",
            options = {"9", "15", "30"},
            category = CREATIVE,
            validate = PushLimitLimits.class
    )
    public static int railPowerLimit = 9;

    private static class FillLimitLimits extends Validator<Integer>
    {
        @Override public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return (newValue>0 && newValue < 20000000) ? newValue : null;
        }
        @Override
        public String description() { return "You must choose a value from 1 to 20M";}
    }
    @Rule(
            desc = "Customizable fill/clone volume limit",
            options = {"32768", "250000", "1000000"},
            category = CREATIVE,
            validate = FillLimitLimits.class
    )
    public static int fillLimit = 32768;
    @Rule(
            desc = "Customizable maximal entity collision limits, 0 for no limits",
            options = {"0", "1", "20"},
            category = OPTIMIZATION,
            validate = Validator.NONNEGATIVE_NUMBER.class
    )

    public static int maxEntityCollisions = 0;

    /*
    @Rule(
            desc = "Fix for piston ghost blocks",
            category = BUGFIX,
            validate = Validator.WIP.class
    )
    public static boolean pistonGhostBlocksFix = true;

    @Rule(
            desc = "fixes water performance issues",
            category = OPTIMIZATION,
            validate = Validator.WIP.class
    )
    public static boolean waterFlow = true;
    */

    @Rule(desc = "One player is required on the server to cause night to pass", category = SURVIVAL)
    public static boolean onePlayerSleeping = false;

    @Rule(
            desc = "Sets a different motd message on client trying to connect to the server",
            extra = "use '_' to use the startup setting from server.properties",
            options = "_",
            category = CREATIVE
    )
    public static String customMOTD = "_";
    
    @Rule(desc = "Cactus in dispensers rotates blocks.", extra = "Rotates block anti-clockwise if possible", category = FEATURE)
    public static boolean rotatorBlock = false;

    private static class ViewDistanceValidator extends Validator<Integer>
    {
        @Override public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string)
        {
            if (currentRule.get().equals(newValue) || source == null)
            {
                return newValue;
            }
            if (newValue < 0 || newValue > 32)
            {
                Messenger.m(source, "r view distance has to be between 0 and 32");
                return null;
            }
            MinecraftServer server = source.getServer();

            if (server.isDedicatedServer())
            {
                int vd = (newValue >= 2)?newValue:((DedicatedServer) server).getIntProperty("view-distance", 10);
                if (vd != CarpetServer.minecraft_server.getPlayerList().getViewDistance())
                    CarpetServer.minecraft_server.getPlayerList().setViewDistance(vd);
                return newValue;
            }
            else
            {
                Messenger.m(source, "r view distance can only be changed on a server");
                return 0;
            }
        }
        @Override
        public String description() { return "You must choose a value from 0 (use server settings) to 32";}
    }
    @Rule(
            desc = "Changes the view distance of the server.",
            extra = "Set to 0 to not override the value in server settings.",
            options = {"0", "12", "16", "32"},
            category = CREATIVE,
            validate = ViewDistanceValidator.class
    )
    public static int viewDistance = 0;

    private static class DisableSpawnChunksValidator extends Validator<Boolean>
    {
        @Override public Boolean validate(CommandSource source, ParsedRule<Boolean> currentRule, Boolean newValue, String string) {
            if (source == null) return newValue;
            if (!newValue)
                Messenger.m(source, "w Spawn chunks re-enabled. Visit spawn to load them?");
            return newValue;
        }
    }
    @Rule(
            desc = "Allows spawn chunks to unload",
            category = CREATIVE,
            validate = DisableSpawnChunksValidator.class
    )
    public static boolean disableSpawnChunks = false;

    private static class KelpLimit extends Validator<Integer>
    {
        @Override public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return (newValue>=0 && newValue <=25)?newValue:null;
        }
        @Override
        public String description() { return "You must choose a value from 0 to 25. 25 and all natural kelp can grow 25 blocks, choose 0 to make all generated kelp not to grow";}
    }
    @Rule(
            desc = "limits growth limit of newly naturally generated kelp to this amount of blocks",
            options = {"0", "2", "25"},
            category = FEATURE,
            validate = KelpLimit.class
    )
    public static int kelpGenerationGrowthLimit = 25;

    @Rule(desc = "Coral structures will grow with bonemeal from coral plants", category = FEATURE)
    public static boolean renewableCoral = false;

    @Rule(desc = "fixes block placement rotation issue when player rotates quickly while placing blocks", category = BUGFIX)
    public static boolean placementRotationFix = false;

    @Rule(
            desc = "Fixes leads breaking/becoming invisible in unloaded chunks",
            extra = "You may still get visibly broken leash links on the client side, but server side the link is still there.",
            category = BUGFIX
    )
    public static boolean leadFix = false;

    @Rule(
            desc = "smooth client animations with low tps settings",
            extra = "works only in SP, and will slow down players",
            category = {CREATIVE, SURVIVAL}
    )
    public static boolean smoothClientAnimations = false;

    @Rule(
            desc = "Creative No Clip",
            extra = {
                    "On servers it needs to be set on both ",
                    "client and server to function properly.",
                    "Has no effect when set on the server only",
                    "Can allow to phase through walls",
                    "if only set on the carpet client side",
                    "but requires some trapdoor magic to",
                    "allow the player to enter blocks"
            },
            category = {CREATIVE}
    )
    public static boolean creativeNoClip = false;

    @Rule(desc = "Spawning requires much less CPU and Memory", category = OPTIMIZATION)
    public static boolean lagFreeSpawning = false;

    @Rule(
            desc = "Client can provide alternative block placement.",
            category = {CREATIVE}
    )
    public static boolean accurateBlockPlacement = false;

    @Rule(
            desc = "Sets the horizontal random angle on TNT for debugging of TNT contraptions",
            extra = "Set to -1 for default behavior",
            category = CREATIVE,
            options = "-1.0",
            validate = TNTAngleValidator.class
    )
    public static double hardcodeTNTangle = -1.0D;

    private static class TNTAngleValidator extends Validator<Double> {
        @Override
        public Double validate(CommandSource source, ParsedRule<Double> currentRule, Double newValue, String string) {
            return (newValue >= 0 && newValue < Math.PI * 2) || newValue == -1 ? newValue : null;
        }

        @Override
        public String description() {
            return "Must be between 0 and 2pi, or -1";
        }
    }

    private static class LanguageValidator extends Validator<String>
    {
        @Override public String validate(CommandSource source, ParsedRule<String> currentRule, String newValue, String string)
        {
            if (currentRule.get().equals(newValue) || source == null)
            {
                return newValue;
            }
            if (!Translations.isValidLanguage(newValue))
            {
                Messenger.m(source, "r "+newValue+" is not a valid language");
                return null;
            }
            CarpetSettings.language = newValue;
            Translations.updateLanguage(source);
            return newValue;
        }
    }
    @Rule(
            desc = "sets the language for carpet",
            category = FEATURE,
            options = {"none", "zh_cn"},
            validate = LanguageValidator.class
    )
    public static String language = "none";

    // /$$     /$$/$$$$$$$$ /$$$$$$$$/$$$$$$$$
    //|  $$   /$$/ $$_____/| $$_____/__  $$__/
    // \  $$ /$$/| $$      | $$        | $$
    //  \  $$$$/ | $$$$$   | $$$$$     | $$
    //   \  $$/  | $$__/   | $$__/     | $$
    //    | $$   | $$      | $$        | $$
    //    | $$   | $$$$$$$$| $$$$$$$$  | $$
    //    |__/   |________/|________/  |__/  power yeet those things!

    @Rule(
            desc = "yeet fish followGroupLeaderAI for less lag",
            extra = "Warn: all yeet options will change vanilla behaviour, they WILL NOT behave like vanilla",
            category = YEET
    )
    public static boolean yeetFishAI = false;

    @Rule(
            desc = "yeet Golems spawing at village for faster stacking at iron farm stacking tests",
            extra = "Warn: all yeet options will change vanilla behaviour, they WILL NOT behave like vanilla",
            category = YEET
    )
    public static boolean yeetGolemSpawn = false;

    @Rule(
            desc = "yeet Villager AI for faster stacking at iron farm stacking tests",
            extra = "Warn: all yeet options will change vanilla behaviour, they WILL NOT behave like vanilla",
            category = YEET
    )
    public static boolean yeetVillagerAi = false;

    @Rule(
            desc = "yeet updates suppression causing server crashes",
            extra = "WARNING: Unknown game behaviors might occur after updates suppression, better restart the server or restore a backup for safety",
            category = YEET
    )
    public static boolean yeetUpdateSuppressionCrash;

    //////////////////////////////////
    //  TISCM bundled mod switches  //
    //////////////////////////////////

    // Litematica Server Paster
    // https://github.com/Fallen-Breath/litematica-server-paster
    @Rule(
            desc = "Enable server-side Litematica Server Paster protocol support.",
            extra = "https://github.com/Fallen-Breath/litematica-server-paster",
            category = CREATIVE
    )
    public static boolean modLitematicaServerPaster = false;

    // RSMM
    @Rule(
            desc = "Enable server-side Redstone Multimeter support.",
            category = {
                    CREATIVE,
                    COMMAND
            }
    )
    public static boolean modRedstoneMultimeter = false;

    @Rule(
            desc = "Enables spark command",
            category = COMMAND,
            validate = ValidateSpark.class
    )
    public static String modSpark = "false";

    private static class ValidateSpark extends Validator<String>
    {
        @Override
        public String validate(CommandSource source, ParsedRule<String> currentRule, String newValue, String string)
        {
            if (!newValue.equals("false") && !TISCMConfig.MOD_SPARK)
            {
                return null;
            }
            return newValue;
        }
        public String description()
        {
            return "You must set `TISCMConfig.MOD_SPARK` to true during mod compiling and have spark classes included in .jar to enable spark";
        }
    }

    @Rule(
            desc = "Enables world edit operations",
            category = COMMAND,
            validate = ValidateWorldEdit.class
    )
    public static String modWorldEdit = "false";

    private static class ValidateWorldEdit extends Validator<String>
    {
        @Override
        public String validate(CommandSource source, ParsedRule<String> currentRule, String newValue, String string)
        {
            if (!newValue.equals("false") && !TISCMConfig.MOD_WORLDEDIT)
            {
                return null;
            }
            return newValue;
        }
        public String description()
        {
            return "You must set `TISCMConfig.MOD_WORLDEDIT` to true during mod compiling and have worldedit classes included in .jar to enable world edit";
        }
    }
}
