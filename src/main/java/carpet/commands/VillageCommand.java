package carpet.commands;

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class VillageCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literalargumentbuilder = literal("village").
				requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandVillage)).
				then(literal("info").
						then(literal("all").executes(c -> sendVillageList(c.getSource(), null))).
						then(argument("id", integer()).executes(c -> sendVillageList(c.getSource(), getInteger(c, "id"))))
				);
		dispatcher.register(literalargumentbuilder);
	}

	private static int sendVillageList(CommandSource source, @Nullable Integer villageId)
	{
		List<ITextComponent> messages = Lists.newArrayList();
		// show all
		if (villageId == null)
		{
			for (WorldServer world : source.getServer().getWorlds())
			{
				List<Village> villageList = world.getVillageCollection().getVillageList();
				if (villageList.isEmpty())
				{
					continue;
				}
				messages.add(Messenger.c(
						"w ====== ",
						Messenger.dimension(world),
						String.format("w  %dx Villages ======", villageList.size())
				));
				for (int i = 0; i < villageList.size(); i++)
				{
					Village village = villageList.get(i);
					generateVillageInfo(messages, village, i, false);
				}
			}
			if (messages.isEmpty())
			{
				messages.add(Messenger.s("Seems like there's no village in all dimensions"));
			}
		}
		else
		{
			List<Village> villageList = source.getWorld().getVillageCollection().getVillageList();
			if (0 <= villageId && villageId < villageList.size())
			{
				generateVillageInfo(messages, villageList.get(villageId), villageId, true);
			}
			else
			{
				messages.add(Messenger.s(String.format("Village id %d is out of valid range [%d, %d]", villageId, 0, villageList.size() - 1), "r"));
			}
		}
		Messenger.send(source, messages);
		return 1;
	}

	private static void generateVillageInfo(List<ITextComponent> messages, Village village, int index, boolean showDoorDetail)
	{
		messages.add(Messenger.fancy(
				null,
				Messenger.s(String.format("Village #%d", index)),
				Messenger.s(String.format("Click to see full information of village #%d\nYou need to be in the same dimension of this village", index)),
				new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/village info %d", index))
		));
		messages.add(Messenger.c("w   Dimension: ", Messenger.dimension(village.getWorld())));
		BlockPos center = village.getCenter();
		messages.add(Messenger.format("  Center: %s", Messenger.coord(center)));
		messages.add(Messenger.s(String.format("  Radius: %d", village.getVillageRadius())));
		messages.add(Messenger.s(String.format("  VillagerCount: %d", village.getNumVillagers())));
		messages.add(Messenger.s(String.format("  DoorCount: %d", village.getVillageDoorInfoList().size())));
		List<ITextComponent> line = Lists.newArrayList();
		List<VillageDoorInfo> villageDoorInfoList = village.getVillageDoorInfoList();
		for (int i = 0; i < villageDoorInfoList.size(); i++)
		{
			VillageDoorInfo doorInfo = villageDoorInfoList.get(i);
			BlockPos pos = doorInfo.getDoorBlockPos();
			line.add(Messenger.fancy(
					"g",
					Messenger.s(" x"),
					Messenger.c(
							String.format("w Door #%s\n", i + 1),
							"w ------------\n",
							String.format("w X: %d\n", pos.getX()),
							String.format("w Y: %d\n", pos.getY()),
							String.format("w Z: %d", pos.getZ())
					),
					new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtil.tp(pos, village.getWorld().getDimension().getType()))
			));
			if (line.size() == 20 || i == villageDoorInfoList.size() - 1)
			{
				messages.add(Messenger.c("w    ", Messenger.c(line.toArray(new Object[0]))));
				line.clear();
			}
			if (!showDoorDetail && i == 2)
			{
				messages.add(Messenger.c("w    ", Messenger.c(line.toArray(new Object[0])), "g  ..."));
				break;
			}
		}
	}
}
