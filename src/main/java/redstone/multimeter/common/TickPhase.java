package redstone.multimeter.common;

import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import redstone.multimeter.util.NbtUtils;
import redstone.multimeter.util.TextUtils;

public class TickPhase {
	
	public static final TickPhase UNKNOWN = new TickPhase(new TickTask[] { TickTask.UNKNOWN });
	
	private final TickTask[] tasks;
	
	public TickPhase(TickTask[] tasks) {
		this.tasks = tasks;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof TickPhase)) {
			return false;
		}
		
		return Arrays.equals(tasks, ((TickPhase)obj).tasks);
	}
	
	@Override
	public String toString() {
		String string = tasks[0].getName();
		
		for (int index = 1; index < tasks.length; index++) {
			string += " > " + tasks[index].getName();
		}
		
		return string;
	}
	
	public void addTextForTooltip(List<ITextComponent> lines) {
		TextUtils.addFancyText(lines, "tick phase", tasks[0].getName());
		
		// used to indent subsequent lines
		String whitespace = "              ";
		
		for (int index = 1; index < tasks.length; index++) {
			String text = whitespace + "> " + tasks[index].getName();
			lines.add(new TextComponentString(text));
			
			whitespace += "  ";
		}
	}
	
	public TickPhase startTask(TickTask task) {
		if (this == UNKNOWN) {
			return new TickPhase(new TickTask[] { task });
		}
		
		TickTask[] array = new TickTask[tasks.length + 1];
		
		for (int index = 0; index < tasks.length; index++) {
			array[index] = tasks[index];
		}
		array[tasks.length] = task;
		
		return new TickPhase(array);
	}
	
	public TickPhase endTask() {
		if (this == UNKNOWN || tasks.length == 1) {
			return UNKNOWN;
		}
		
		TickTask[] array = new TickTask[tasks.length - 1];
		
		for (int index = 0; index < array.length; index++) {
			array[index] = tasks[index];
		}
		
		return new TickPhase(array);
	}
	
	public TickPhase swapTask(TickTask task) {
		if (this == UNKNOWN || tasks.length == 1) {
			return new TickPhase(new TickTask[] { task });
		}
		
		TickTask[] array = new TickTask[tasks.length];
		
		for (int index = 0; index < tasks.length; index++) {
			array[index] = tasks[index];
		}
		array[array.length - 1] = task;
		
		return new TickPhase(array);
	}
	
	public INBTBase toNbt() {
		if (this == UNKNOWN) {
			return NbtUtils.NULL;
		}
		
		byte[] array = new byte[tasks.length];
		
		for (int index = 0; index < array.length; index++) {
			array[index] = (byte)tasks[index].getIndex();
		}
		
		return new NBTTagByteArray(array);
	}
	
	public static TickPhase fromNbt(INBTBase nbt) {
		if (nbt.getId() != NbtUtils.TYPE_BYTE_ARRAY) {
			return UNKNOWN;
		}
		
		NBTTagByteArray array = (NBTTagByteArray)nbt;
		TickTask[] tasks = new TickTask[array.size()];
		
		for (int index = 0; index < tasks.length; index++) {
			int taskIndex = array.get(index).getByte();
			tasks[index] = TickTask.fromIndex(taskIndex);
		}
		
		return new TickPhase(tasks);
	}
}
