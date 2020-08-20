package me.jellysquid.mods.lithium.config;

public class LiConfig {

    /**
     * Class modified:
     * {@link net.minecraft.entity.Entity}
     * {@link net.minecraft.entity.EntityLiving}
     * {@link net.minecraft.util.math.BlockPos}
     * {@link net.minecraft.world.gen.ChunkProviderServer}
     * {@link net.minecraft.world.WorldServer}, Line 446
     */
    public static class AvoidAlloc {

    }

    /**
     * Class modified:
     * {@link net.minecraft.block.Block.RenderSideCacheKey}
     */
    public static class CachedHashCode {
        public static boolean cachedBlockRenderSideCacheKey = true;
    }

    /**
     * Class modified:
     * {@link net.minecraft.world.chunk.BlockStateContainer}
     * {@link net.minecraft.util.BitArray}
     */
    public static class Chunk {
        public final static boolean fastChunkPalette = true;
        public final static boolean fastChunkPalette_tweakedThresholdScaleOf3 = true;
        public final static boolean fastChunkSerialization = true;
    }

    /**
     * Class modified:
     * {@link net.minecraft.entity.Entity}
     *
     * {@link net.minecraft.entity.ai.EntityAIBase}
     *
     * {@link net.minecraft.entity.ai.EntityAITasks}
     *
     * {@link net.minecraft.entity.boss.EntityWither}
     *
     * {@link net.minecraft.entity.item.EntityBoat
     *  @link net.minecraft.entity.item.EntityEnderCrystal
     *  @link net.minecraft.entity.item.EntityEnderPearl
     *  @link net.minecraft.entity.item.EntityFallingBlock
     *  @link net.minecraft.entity.item.EntityItem
     *  @link net.minecraft.entity.item.EntityItemFrame
     *  @link net.minecraft.entity.item.EntityMinecart
     *  @link net.minecraft.entity.item.EntityXPOrb}
     *
     *  {@link net.minecraft.entity.monster.EntityEnderman
     * @link net.minecraft.entity.monster.EntityEvoker
     * @link net.minecraft.entity.monster.EntityIronGolem
     * @link net.minecraft.entity.monster.EntityShulker
     * @link net.minecraft.entity.monster.EntitySilverfish
     * @link net.minecraft.entity.monster.EntitySnowman
     * @link net.minecraft.entity.monster.EntityZombie
     * @link net.minecraft.entity.monster.EntityZombieVillager}
     *
     *  {@link net.minecraft.entity.passive.AbstractHorse
     * @link net.minecraft.entity.passive.EntityBat
     * @link net.minecraft.entity.passive.EntityDolphin
     * @link net.minecraft.entity.passive.EntityLlama
     * @link net.minecraft.entity.passive.EntityParrot
     * @link net.minecraft.entity.passive.EntityRabbit
     * @link net.minecraft.entity.passive.EntitySquid
     * @link net.minecraft.entity.passive.EntityTurtle}
     *
     *  {@link net.minecraft.entity.player.EntityPlayer
     * @link net.minecraft.entity.player.EntityPlayerMP}
     *
     *  {@link net.minecraft.entity.projectile.EntityArrow
     * @link net.minecraft.entity.projectile.EntityFishHook
     * @link net.minecraft.entity.projectile.EntityPotion
     * @link net.minecraft.entity.projectile.EntityThrowable}
     *
     *  {@link net.minecraft.entity.EntityFlying
     * @link net.minecraft.entity.EntityHanging
     * @link net.minecraft.entity.EntityLeashKnot}
     *
     *  {@link net.minecraft.network.datasync.EntityDataManager}
     *
     *  {@link net.minecraft.network.play.server.SPacketSpawnMob
     * @link net.minecraft.network.play.server.SPacketSpawnPlayer}
     *
     *  {@link net.minecraft.pathfinding.PathNavigate}
     */
    public static class Entity {
        public final static boolean newAITaskEntryContainer = true;
        public final static boolean entityChunkCache = true;
        public final static boolean useNewDataManager = true;
        public final static boolean entityDataManagerNoLock = true;
        public final static boolean chunkCachedPathFind = true;
        public final static boolean skipMovementTickIfSpeedSquaredLessThan0point0001 = true;
    }

    /**
     * Class modified
     * {@link net.minecraft.world.WorldServer}
     * {@link net.minecraft.util.ClassInheritanceMultiMap}
     */
    public static class FastContainers {
        public final static boolean lithiumTickScheduler = true;
        public final static boolean fastClassInheritanceMultiMap = true;
    }

    public static class NBT {
        public final static boolean fastNBTSerialization = true;
    }

    /**
     * Class changed:
     * {@link net.minecraft.util.math.AxisAlignedBB}
     * {@link net.minecraft.util.EnumFacing}
     * {@link net.minecraft.util.AxisRotation}
     */
    public static class Maths {

    }
}
