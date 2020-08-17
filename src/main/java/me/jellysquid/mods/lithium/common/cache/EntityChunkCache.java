package me.jellysquid.mods.lithium.common.cache;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Fluids;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Arrays;


/**
 * Maintains a cached collection of chunks around an entity. This allows for much faster access to nearby chunks for
 * many entity related functions.
 */
public class EntityChunkCache {
    private static final int RADIUS = 1;

    private static final int LENGTH = (RADIUS * 2) + 1;

    private final Entity entity;

    private Chunk[] cache = new Chunk[LENGTH * LENGTH];

    private IChunkProvider chunkManager;

    private int startX, startZ;

    private boolean isCacheEmpty = true;

    public EntityChunkCache(Entity entity) {
        this.entity = entity;
        this.chunkManager = entity.getEntityWorld().getChunkProvider();
    }

    public World getWorld() {
        return this.entity.getEntityWorld();
    }

    public void updateChunks(AxisAlignedBB box) {
        int startX = toChunkCoord(MathHelper.floor(box.minX)) - RADIUS;
        int startZ = toChunkCoord(MathHelper.floor(box.minZ)) - RADIUS;

        IChunkProvider chunkManager = this.getWorld().getChunkProvider();

        // If the world/chunk manager has changed, we need to reset
        if (chunkManager != this.chunkManager) {
            Arrays.fill(this.cache, null);

            this.isCacheEmpty = true;
        } else {
            // If we're not watching any new chunks, we have no need to update anything
            if (startX == this.startX && startZ == this.startZ) {
                return;
            }
        }

        if (!this.isCacheEmpty) {
            Chunk[] cache = new Chunk[LENGTH * LENGTH];

            for (int x = 0; x < LENGTH; x++) {
                for (int z = 0; z < LENGTH; z++) {
                    cache[(x * LENGTH) + z] = this.getCachedChunk(startX + x, startZ + z);
                }
            }

            this.cache = cache;
        }

        this.startX = startX;
        this.startZ = startZ;
        this.chunkManager = chunkManager;

        this.isCacheEmpty = false;
    }

    private ChunkSection getChunkSection(int x, int y, int z) {
        if (y < 0 || y >= 16) {
            return null;
        }

        Chunk chunk = this.getChunk(x, z);

        if (chunk != null) {
            return chunk.getSections()[y];
        }

        return null;
    }

    public IBlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public IBlockState getBlockState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(toChunkCoord(x), toChunkCoord(y), toChunkCoord(z));

        if (section != null) {
            return section.get(toLocalCoord(x), toLocalCoord(y), toLocalCoord(z));
        }

        return Blocks.AIR.getDefaultState();
    }

    public IFluidState getFluidState(BlockPos pos) {
        return this.getFluidState(pos.getX(), pos.getY(), pos.getZ());
    }

    public IFluidState getFluidState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(toChunkCoord(x), toChunkCoord(y), toChunkCoord(z));

        if (section != null) {
            return section.getFluidState(toLocalCoord(x), toLocalCoord(y), toLocalCoord(z));
        }

        return Fluids.EMPTY.getDefaultState();
    }

    public Chunk getChunk(int x, int z) {
        int iX = x - this.startX;
        int iZ = z - this.startZ;

        if (iX >= 0 && iX < LENGTH && iZ >= 0 && iZ < LENGTH) {
            int i = (iX * LENGTH) + iZ;

            Chunk chunk = this.cache[i];

            if (chunk == null) {
                this.cache[i] = chunk = this.chunkManager.getChunk(x, z, false, false);
            }

            return chunk;
        }

        return this.chunkManager.getChunk(x, z, true, true);
    }

    public Chunk getCachedChunk(int x, int z) {
        int iX = x - this.startX;
        int iZ = z - this.startZ;

        if (iX >= 0 && iX < LENGTH && iZ >= 0 && iZ < LENGTH) {
            return this.cache[(iX * LENGTH) + iZ];
        }

        return null;
    }

    private static int toChunkCoord(int coord) {
        return coord >> 4;
    }

    private static int toLocalCoord(int coord) {
        return coord & 15;
    }

}
