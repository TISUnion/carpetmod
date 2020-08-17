package me.jellysquid.mods.lithium.common.cache;

import me.jellysquid.mods.lithium.common.entity.EntityWithChunkCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;

/**
 * Makes use a flattened array and direct access to chunk sections for improved performance when
 * compared to direct uses in {@link net.minecraft.pathfinding.PathNavigate}.
 */
public class NavigationChunkCache implements IBlockReader {
    private static final ChunkSection EMPTY_SECTION = new ChunkSection(0, false);

    private final int startX, startZ;
    private final int width, height;

    private final Chunk[] chunks;
    private final ChunkSection[] sections;

    private final World world;

    public NavigationChunkCache(Entity entity, World world, BlockPos min, BlockPos max) {
        EntityChunkCache cache = entity instanceof EntityWithChunkCache ? ((EntityWithChunkCache) entity).getEntityChunkCache() : null;

        this.world = world;

        this.startX = min.getX() >> 4;
        this.startZ = min.getZ() >> 4;

        int maxX = max.getX() >> 4;
        int maxZ = max.getZ() >> 4;

        this.width = maxX - this.startX;
        this.height = maxZ - this.startZ;

        this.chunks = new Chunk[(this.width + 1) * (this.height + 1)];
        this.sections = new ChunkSection[this.chunks.length * 16];

        for (int x = this.startX; x <= maxX; ++x) {
            for (int z = this.startZ; z <= maxZ; ++z) {
                Chunk chunk = cache != null ? cache.getChunk(x, z) : world.getChunkProvider().getChunk(x, z, false, false);

                if (chunk == null) {
                    chunk = new EmptyChunk(this.world, x, z);
                }

                int i = indexChunk(x - this.startX, z - this.startZ);

                this.chunks[i] = chunk;

                for (int j = 0; j < 16; j++) {
                    this.sections[(i * 16) + j] = chunk.getSections()[j] == null ? EMPTY_SECTION : chunk.getSections()[j];
                }
            }
        }
    }

    public int getLightLevel(BlockPos pos, int int_1) {
        return this.world.getLightSubtracted(pos, int_1);
    }

    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status, boolean flag) {
        int i = indexChunk(chunkX - this.startX, chunkZ - this.startZ);

        if (i >= 0 && i < this.chunks.length) {
            return this.chunks[i];
        } else {
            return new EmptyChunk(this.world, chunkX, chunkZ);
        }
    }

    public ChunkSection getChunkSection(int chunkX, int chunkY, int chunkZ) {
        if (chunkY < 0 || chunkY >= 16) {
            return EMPTY_SECTION;
        }

        int offsetX = chunkX - this.startX;
        int offsetZ = chunkZ - this.startZ;

        if (offsetX < 0 || offsetX >= this.width || offsetZ < 0 || offsetZ >= this.height) {
            return EMPTY_SECTION;
        }

        return this.sections[this.indexChunk(offsetX, chunkY, offsetZ)];
    }

    private int indexChunk(int x, int y, int z) {
        return (((x * this.width) + z) * 16) + y;
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        int x = chunkX - this.startX;
        int z = chunkZ - this.startZ;

        return x >= 0 && x < this.width && z >= 0 && z < this.height;
    }

    public BlockPos getTopPosition(Heightmap.Type type, BlockPos pos) {
        return this.world.getHeight(type, pos);
    }

    public int getTop(Heightmap.Type type, int x, int z) {
        return this.world.getHeight(type, x, z);
    }

    public int getAmbientDarkness() {
        return this.world.getSkylightSubtracted();
    }

    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    public boolean intersectsEntities(Entity entity, VoxelShape shape) {
        return true;
    }

    public boolean isClient() {
        return false;
    }

    public int getSeaLevel() {
        return this.world.getSeaLevel();
    }

    public Dimension getDimension() {
        return this.world.getDimension();
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return this.getChunk(pos).getTileEntity(pos);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)
                .get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        if (World.isOutsideBuildHeight(pos)) {
            return Fluids.EMPTY.getDefaultState();
        }

        return this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)
                .getFluidState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    public Biome getBiome(BlockPos pos) {
        return this.getChunk(pos).getBiome(pos);
    }

    public int getLightLevel(EnumLightType type, BlockPos pos) {
        return this.world.getLightFor(type, pos);
    }

    private int indexChunk(int x, int z) {
        return (x * this.width) + z;
    }

    private Chunk getChunk(BlockPos pos) {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULLCHUNK, false);
    }
}
