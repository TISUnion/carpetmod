package carpet.microtiming.utils.stacktrace;

import carpet.utils.Messenger;
import carpet.utils.TextUtil;
import com.google.common.base.Joiner;
import net.minecraft.util.text.ITextComponent;

import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.min;

public class StackTracePrinter
{
	private static final int DEFAULT_MAX_STACK_TRACE_SIZE = 64;

	private StackTraceElement[] stackTrace;
	private int maxStackTraceSize;
	private String ignorePackagePath;

	private StackTracePrinter()
	{
		this.stackTrace = Thread.currentThread().getStackTrace();
		this.maxStackTraceSize = DEFAULT_MAX_STACK_TRACE_SIZE;
	}

	public static StackTracePrinter create()
	{
		return new StackTracePrinter();
	}

	// limits the maximum display line
	public StackTracePrinter limit(int maxStackTraceSize)
	{
		this.maxStackTraceSize = maxStackTraceSize;
		return this;
	}

	// all StackTraceElements before any StackTraceElement with package starts with given path will be ignored
	public StackTracePrinter ignore(String ignorePackagePath)
	{
		this.ignorePackagePath = ignorePackagePath;
		return this;
	}

	public StackTracePrinter ignore(Class<?> ignoreClass)
	{
		return this.ignore(ignoreClass.getPackage().getName());
	}

	public StackTracePrinter deobfuscate()
	{
		this.stackTrace = StackTraceDeobfuscator.deobfuscateStackTrace(this.stackTrace, this.ignorePackagePath);
		return this;
	}

	public StackTraceElement[] toStackTraceElements()
	{
		return this.stackTrace;
	}

	@Deprecated
	public ITextComponent toBaseText()
	{
		List<StackTraceElement> list = Arrays.asList(this.stackTrace).subList(0, min(this.stackTrace.length, this.maxStackTraceSize));
		int restLineCount = this.stackTrace.length - this.maxStackTraceSize;
		String text = Joiner.on("\n").join(list);
		if (restLineCount > 0)
		{
			text += "\n... " + String.format( "%d more lines", restLineCount);
		}
		return Messenger.s(text);
	}

	// a $ symbol with hover text showing the stack trace
	public ITextComponent toSymbolText()
	{
		return TextUtil.getFancyText("f", Messenger.s("$"), this.toBaseText(), null);
	}
}
