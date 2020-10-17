package carpet.settings;

import carpet.CarpetServer;
import carpet.utils.Messenger;
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
            desc = "Enables /epsTest for performance tests",
            category = COMMAND
    )
    public static boolean commandEPSTest = false;
    
    @Rule(
            desc = "Enables /ping command to see your ping",
            category = COMMAND
    )
    public static boolean commandPing = true;

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
            desc = "Enable the function of MicroTick logger",
            extra = {
                    "Display redstone components actions, blockupdates and stacktrace with a wool block",
                    "Use /log microtick to start logging",
                    "Might impact the server performance when it's on",
                    "EndRods will detect block updates and redstone components will show their actions",
                    "- Observer, Piston, EndRod: pointing towards wool",
                    "- Repeater, Comparator, Rail, Button, etc.: placed on wool",
                    "Beside that, blocks pointed by EndRod on wool block will also show their actions"
            },
            category = {COMMAND, CREATIVE}
    )
    public static boolean microTick = false;

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
            return (newValue > 0 && newValue <= 1000) ? newValue : null;
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
            extra = "The value should be between 0.7 and 1.3 as vanilla behavior. Set it to -1 to disable overriding",
            validate = ExplosionRandomSizeRatioValidator.class,
            options = {"-1", "0.7", "1", "1.3"},
            category = CREATIVE
    )
    public static double explosionRandomSizeRatio = -1;

    private static class ExplosionRandomSizeRatioValidator extends Validator<Double>
    {
        @Override
        public Double validate(CommandSource source, ParsedRule<Double> currentRule, Double newValue, String string) {
            return newValue == -1 || (0.7 <= newValue && newValue <= 1.3) ? newValue : null;
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
            desc = "Portals won't let a creative player go through instantly",
            extra = "Holding obsidian in either hand won't let you through at all",
            category = CREATIVE
    )
    public static boolean portalCreativeDelay = false;

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
    public static boolean commandSpawn = true;

    @Rule(desc = "Enables /tick command to control game clocks", category = COMMAND)
    public static boolean commandTick = true;

    @Rule(desc = "Enables /log command to monitor events in the game via chat and overlays", category = COMMAND)
    public static boolean commandLog = true;

    @Rule(
            desc = "Enables /distance command to measure in game distance between points",
            extra = "Also enables brown carpet placement action if 'carpets' rule is turned on as well",
            category = COMMAND
    )
    public static boolean commandDistance = true;

    @Rule(
            desc = "Enables /info command for blocks",
            extra = {
                    "Also enables gray carpet placement action",
                    "if 'carpets' rule is turned on as well"
            },
            category = COMMAND
    )
    public static boolean commandInfo = true;

    @Rule(
            desc = "Enables /c and /s commands to quickly switch between camera and survival modes",
            extra = "/c and /s commands are available to all players regardless of their permission levels",
            category = COMMAND
    )
    public static boolean commandCameramode = true;

    @Rule(
            desc = "Enables /perimeterinfo command",
            extra = "... that scans the area around the block for potential spawnable spots",
            category = COMMAND
    )
    public static boolean commandPerimeterInfo = true;

    @Rule(desc = "Enables /draw commands", extra = "... allows for drawing simple shapes", category = COMMAND)
    public static boolean commandDraw = true;

    @Rule(desc = "Enables /script command", extra = "An in-game scripting API for Scarpet programming language", category = COMMAND)
    public static boolean commandScript = true;

    @Rule(desc = "Enables /player command to control/spawn players", category = COMMAND)
    public static boolean commandPlayer = true;

    @Rule(desc = "Placing carpets may issue carpet commands for non-op players", category = SURVIVAL)
    public static boolean carpets = false;

    @Rule(
            desc = "Pistons, Glass and Sponge can be broken faster with their appropriate tools",
            category = SURVIVAL
    )
    public static boolean missingTools = false;

    @Rule(desc = "Alternative, persistent caching strategy for nether portals", category = {SURVIVAL, CREATIVE})
    public static boolean portalCaching = false;

    @Rule(desc = "fill/clone/setblock and structure blocks cause block updates", category = CREATIVE)
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

    private static class SetMotd extends Validator<String>
    {
        @Override public String validate(CommandSource source, ParsedRule<String> currentRule, String newValue, String string) {
            customMOTD = newValue; // accelerate a smidge
            CarpetServer.minecraft_server.checkMOTD();
            return newValue;
        }
    }
    @Rule(
            desc = "Sets a different motd message on client trying to connect to the server",
            extra = "use '_' to use the startup setting from server.properties",
            options = "_",
            category = CREATIVE,
            validate = SetMotd.class
    )
    public static String customMOTD = "_";
    
    @Rule(desc = "Cactus in dispensers rotates blocks.", extra = "Rotates block anti-clockwise if possible", category = FEATURE)
    public static boolean rotatorBlock = false;

    private static class ViewDistanceValidator extends Validator<Integer>
    {
        @Override public Integer validate(CommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string)
        {
            if (currentRule.get().equals(newValue))
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
            desc = "yeet kicking players from the server from spamming messages",
            category = YEET
    )
    public static boolean yeetKickedForSpam = false;
}
