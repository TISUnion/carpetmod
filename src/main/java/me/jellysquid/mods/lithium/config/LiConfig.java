package me.jellysquid.mods.lithium.config;

public class LiConfig {


    /**
     * Class changed:
     * {@link net.minecraft.world.WorldServer}
     *
     */
    public static class World {
        /**
         * {@link me.jellysquid.mods.lithium.common.world.scheduler.LithiumServerTickScheduler}
         */
        public static boolean world_useLithiumTickScheduler = true;

    }

    /**
     * Class modified:
     * {@link net.minecraft.entity.EntityLiving}
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
        public static boolean fastChunkPalette = true;
        public static boolean fastChunkPalette_tweakedThresholdScaleOf3 = true;
        public static boolean fastChunkSerialization = true;
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
