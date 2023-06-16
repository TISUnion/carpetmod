package carpet.helpers;

import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.logging.microtiming.MicroTimingAccess;
import carpet.logging.microtiming.tickphase.TickPhase;
import carpet.utils.Messenger;
import carpet.utils.Translator;
import com.google.common.base.Suppliers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Supplier;

public class UpdateSuppressionException extends RuntimeException
{
	private static final Translator translator = new Translator("rule.yeetUpdateSuppressionCrash");
	private final Supplier<ITextComponent> textHolder;

	public UpdateSuppressionException(Throwable cause, World world, BlockPos pos)
	{
		super(cause);
		TickPhase tickPhase = MicroTimingAccess.getTickPhase(world);
		this.textHolder = Suppliers.memoize(() -> Messenger.hover(
				translator.advTr("exception_detail", "Update Suppression in %1$s at %2$s",
						Messenger.coord(pos, world.getDimension().getType()),
						tickPhase.toText()
				),
				Messenger.s(cause.getClass().getSimpleName())
		));
	}

	public ITextComponent getMessageText()
	{
		return this.textHolder.get();
	}

	@Override
	public String getMessage()
	{
		return this.getMessageText().getString();
	}

	@Override
	public String toString()
	{
		return this.getMessage();
	}

	public static void noop()
	{
		// load this class in advanced
		// to prevent NoClassDefFoundError due to stack overflow again when loading this class
	}

	public void report()
	{
		ITextComponent message = Messenger.formatting(
				translator.advTr("report_message", "You just caused a server crash: %1$s", this.getMessageText()),
				TextFormatting.RED, TextFormatting.ITALIC
		);

		// fabric carpet 1.4.49 introduces rule updateSuppressionCrashFix with related logger
		// we reuse the logger for message subscribing
		Logger logger = LoggerRegistry.getLogger("updateSuppressedCrashes");
		if (logger != null)
		{
			logger.log(() -> new ITextComponent[]{message});
			Messenger.sendToConsole(message);
		}
		else
		{
			Messenger.broadcast(message);
		}
	}

	public static Optional<UpdateSuppressionException> extractInCauses(Throwable throwable)
	{
		for (; throwable != null; throwable = throwable.getCause())
		{
			if (throwable instanceof UpdateSuppressionException)
			{
				return Optional.of((UpdateSuppressionException)throwable);
			}
		}
		return Optional.empty();
	}
}