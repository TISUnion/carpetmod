package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import carpet.utils.deobfuscator.McpMapping;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.util.BitArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.*;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.command.Commands.literal;

public class PaletteCommand extends AbstractCommand
{
	private static final PaletteCommand INSTANCE = new PaletteCommand();
	private static final String NAME = "palette";

	private PaletteCommand()
	{
		super(NAME);
	}

	public static PaletteCommand getInstance()
	{
		return INSTANCE;
	}

	@Override
	public void registerCommand(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literalargumentbuilder = literal(NAME).
				requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandPalette)).
				then(literal("info").executes(c -> showInfo(c.getSource()))).
				then(literal("idInfo").executes(c -> showIdInfo(c.getSource()))).
				then(literal("posInfo").
						executes(c -> showPosInfo(c.getSource(), false)).
						then(literal("withNearby").executes(c -> showPosInfo(c.getSource(), true)))
				);
		dispatcher.register(literalargumentbuilder);
	}

	private interface BlockStateContainerAction
	{
		int execute(BlockStateContainer<IBlockState> container, IBlockStatePalette<IBlockState> palette);
	}

	private int doWithBlockStateContainer(CommandSource source, BlockStateContainerAction action)
	{
		BlockPos blockPos = new BlockPos(source.getPos());
		Chunk chunk = source.getWorld().getChunk(blockPos);
		ChunkSection[] sections = chunk.getSections();

		int sectionIndex = MathHelper.clamp(blockPos.getY() >> 4, 0, sections.length - 1);
		ChunkSection section = sections[sectionIndex];
		String coordStr = TextUtil.coord(new Vec3i(chunk.x, sectionIndex, chunk.z));
		if (section == null)
		{
			Messenger.tell(source, Messenger.format("Chunk section %s is null", coordStr));
			return 0;
		}
		else
		{
			BlockStateContainer<IBlockState> container = section.getData();
			IBlockStatePalette<IBlockState> palette = container.getPalette();
			Messenger.tell(source, Messenger.format("Chunk section %s", coordStr));
			return action.execute(container, palette);
		}
	}

	private static String remapClass(Class<?> clazz)
	{
		return McpMapping.remapClass(clazz.getName().replace('.', '/')).orElse(clazz.getSimpleName());
	}

	private int showInfo(CommandSource source)
	{
		return this.doWithBlockStateContainer(source, (container, palette) -> {
			int bits = container.getBits();
			Messenger.m(source, String.format("w Bits: %d (max=%d)", bits, 1 << bits));
			Messenger.m(source, "w Palette type: " + remapClass(palette.getClass()));

			int size = -1;
			if (palette instanceof BlockStatePaletteLinear)
			{
				size = ((BlockStatePaletteLinear<IBlockState>)palette).func_202137_b();
			}
			else if (palette instanceof BlockStatePaletteHashMap)
			{
				size = ((BlockStatePaletteHashMap<IBlockState>)palette).getPaletteSize();
			}
			else if (palette instanceof BlockStatePaletteRegistry)
			{
				size = Block.BLOCK_STATE_IDS.size();
			}
			else if (palette instanceof LithiumHashPalette)
			{
				size = ((LithiumHashPalette<IBlockState>)palette).getSize();
			}
			Messenger.m(source, "w Palette size: " + size);
			return size;
		});
	}

	private int showIdInfo(CommandSource source)
	{
		return this.doWithBlockStateContainer(source, (container, palette) -> {
			List<IBlockState> states = null;
			if (palette instanceof BlockStatePaletteLinear)
			{
				states = ((BlockStatePaletteLinear<IBlockState>)palette).getStates();
			}
			else if (palette instanceof BlockStatePaletteHashMap)
			{
				states = ((BlockStatePaletteHashMap<IBlockState>)palette).getStates();
			}
			else if (palette instanceof LithiumHashPalette)
			{
				states = ((LithiumHashPalette<IBlockState>)palette).getStates();
			}

			int bits = container.getBits();
			if (states != null)
			{
				Messenger.m(source, "w Palette ids:");
				int maxIdxLen = String.valueOf(states.size() - 1).length();
				for (int i = 0; i < states.size(); i++)
				{
					IBlockState state = states.get(i);
					if (state != null)
					{
						Messenger.m(source,
								String.format("w %" + maxIdxLen + "d. %s ", i, long2Bits(i, bits)),
								Messenger.block(state)
						);
					}
				}
			}
			else
			{
				Messenger.m(source, "w It's a Registry Palette");
			}
			return bits;
		});
	}

	// [low, high]
	private String long2Bits(long l, int len)
	{
		StringBuilder builder = new StringBuilder();
		for (int bitNum = 0; bitNum < len; bitNum++)
		{
			long c = l & 1;
			l = l >> 1;
			builder.append(c);
		}
		return builder.toString();
	}
	private String long2Bits(long l)
	{
		return long2Bits(l, 64);
	}

	private static int getIndex(BlockPos pos)
	{
		int x = pos.getX() & 15;
		int y = pos.getY() & 15;
		int z = pos.getZ() & 15;

		return y << 8 | z << 4 | x;
	}

	private static BlockPos getBlockIndex(int index, BlockPos base)
	{
		int x = (base.getX() & ~0xF) | (index & 0xF);
		int y = (base.getY() & ~0xF) | ((index >>> 8) & 0xF);
		int z = (base.getZ() & ~0xF) | ((index >>> 4) & 0xF);

		return new BlockPos(x, y, z);
	}

	private int showPosInfo(CommandSource source, boolean showNearby)
	{
		return this.doWithBlockStateContainer(source, (container, palette) -> {
			BlockPos pos = new BlockPos(source.getPos());
			BitArray storage = container.getStorage();
			long[] longArray = storage.getBackingLongArray();
			int bits = storage.bitsPerEntry();
			int index = getIndex(pos);
			int i = index * bits;
			int j = i / 64;
			int k = ((index + 1) * bits - 1) / 64;
			int l = i % 64;

			if (j == k)
			{
				displayJKBits(source, j, longArray[j], l, l + bits - 1, "");
			}
			else
			{
				displayJKBits(source, j, longArray[j], l, 64, "1");
				displayJKBits(source, k, longArray[k], 0, (l + bits - 1) % 64, "2");
			}
			if (showNearby)
			{
				for (BlockPos bp : getJKNearbyPos(j, k, bits, pos))
				{
					Messenger.m(source, Messenger.coord(null, bp, source.getWorld().getDimension().getType()));
				}
			}

			return j == k ? 1 : 2;
		});
	}

	private void displayJKBits(CommandSource source, int index, long longString, long start, long end, String append)
	{
		List<ITextComponent> line = Lists.newArrayList();
		for (int bitNum = 0; bitNum < 64; bitNum++)
		{
			long bit = longString & 1;
			longString = longString >> 1;
			line.add(Messenger.s(String.valueOf(bit), start <= bitNum && bitNum <= end ? "r" : "w"));
		}
		Messenger.m(
				source,
				Messenger.fancy(
						Messenger.s(String.format("L%s:", append), "f"),
						Messenger.s("Long array index: " + index),
						null
				),
				Messenger.c(line.toArray(new Object[0]))
		);
	}

	private static BlockPos[] getJKNearbyPos(int j, int k, int bits, BlockPos pos)
	{
		BlockPos basePos = new BlockPos(pos.getX() >>> 4 << 4, pos.getY() >>> 4 << 4, pos.getZ() >>> 4 << 4);
		ArrayList<BlockPos> list = new ArrayList<>();
		for (int index = 0; index < 4096; index++)
		{
			int i = index * bits;
			int jj = i / 64;
			int kk = ((index + 1) * bits - 1) / 64;
			if (jj == j || kk == k || jj == k || kk == j)
			{
				list.add(getBlockIndex(index, basePos));
			}
		}
		return list.toArray(new BlockPos[0]);
	}
}
