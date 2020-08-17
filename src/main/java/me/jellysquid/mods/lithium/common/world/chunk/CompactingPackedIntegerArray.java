package me.jellysquid.mods.lithium.common.world.chunk;

import net.minecraft.world.chunk.IBlockStatePalette;

public interface CompactingPackedIntegerArray {
    /**
     * Copies the data out of this array into a new non-packed array. The returned array contains a copy of this array
     * re-mapped using {@param destPalette}.
     */
    <T> short[] compact(IBlockStatePalette<T> srcPalette, IBlockStatePalette<T> destPalette, T def);
}
