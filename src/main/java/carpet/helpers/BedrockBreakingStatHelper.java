package carpet.helpers;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;

public class BedrockBreakingStatHelper
{
	// (world, chunk) -> (blockPos -> player)
	// use chunk in key for clean up at chunk unload
	private static final Map<Pair<World, Long>, Map<BlockPos, EntityPlayer>> pistonPlacer = Maps.newHashMap();

	public static void onChunkUnload(World world, Chunk chunk)
	{
		pistonPlacer.remove(Pair.of(world, chunk.getPos().asLong()));
	}

	private static Pair<World, Long> getKey(World world, BlockPos pos)
	{
		return Pair.of(world, (new ChunkPos(pos.getX() / 16, pos.getZ() / 16)).asLong());
	}

	public static void onPlayerPlacedBlock(Block block, World world, BlockPos pos, EntityPlayer player)
	{
		if (block instanceof BlockPistonBase)
		{
			Map<BlockPos, EntityPlayer> chunkMap = pistonPlacer.computeIfAbsent(getKey(world, pos), key -> Maps.newHashMap());
			chunkMap.put(pos, player);
		}
	}

	public static void onBedrockYeeted(World world, BlockPos pistonPos, BlockPos bedrockPos)
	{
		Map<BlockPos, EntityPlayer> chunkMap = pistonPlacer.get(getKey(world, pistonPos));
		if (chunkMap != null)
		{
			EntityPlayer player = chunkMap.remove(pistonPos);
			if (player != null && world.getServer() != null && world.getServer().getPlayerList().getPlayerByUUID(player.getUniqueID()) != null)  // the player is in the server
			{
				if (player.getEntityWorld().getDimension() == world.getDimension())
				{
					if (player.getDistanceSq(bedrockPos.getX() + 0.5D, bedrockPos.getY() + 0.5D, bedrockPos.getZ() + 0.5D) <= 10.0D * 10.0D)
					{
						{
							player.addStat(StatList.BREAK_BEDROCK, 1);
						}
					}
				}
			}
		}
	}
}
