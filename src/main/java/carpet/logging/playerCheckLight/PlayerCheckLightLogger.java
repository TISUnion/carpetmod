package carpet.logging.playerCheckLight;

import carpet.logging.AbstractLogger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerCheckLightLogger extends AbstractLogger
{
	public static final String NAME = "playerCheckLight";
	private static final PlayerCheckLightLogger INSTANCE = new PlayerCheckLightLogger();

	private PlayerCheckLightLogger()
	{
		super(NAME);
	}

	public static PlayerCheckLightLogger getInstance()
	{
		return INSTANCE;
	}

	@Nullable
	@Override
	public String[] getSuggestedLoggingOption()
	{
		List<String> list = Arrays.stream(EnumLightType.values()).
				map(Enum::toString).
				map(String::toLowerCase).
				collect(Collectors.toList());
		list.add("all");
		return list.toArray(new String[0]);
	}

	@Nullable
	@Override
	public String getDefaultLoggingOption()
	{
		return "all";
	}

	public void onLightChanged(WorldServer world, BlockPos pos, EntityPlayer player, EnumLightType lightType, int oldLight, int newLight)
	{
		if (!LoggerRegistry.__playerCheckLight)
		{
			return;
		}
		this.log((option) -> {
			if (!option.equals("all") && !option.equals(lightType.name().toLowerCase()))
			{
				return null;
			}
			return new ITextComponent[]{Messenger.format("[PlayerCheckLight] %1$s light %2$s -> %3$s, at %4$s by %5$s",
					lightType.name().toLowerCase(),
					oldLight, newLight,
					Messenger.coord(pos, world.getDimension().getType()),
					player.getDisplayName()
			)};
		});
	}
}
