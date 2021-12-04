package redstone.multimeter.common.meter.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;

import redstone.multimeter.util.NbtUtils;
import redstone.multimeter.util.TextUtils;

public enum EventType {
	
	UNKNOWN(-1, "unknown"),
	POWERED(0, "powered") {
		
		@Override
		public void addTextForTooltip(List<ITextComponent> lines, int metadata) {
			TextUtils.addFancyText(lines, "became powered", metadata == 1);
		}
	},
	ACTIVE(1, "active") {
		
		@Override
		public void addTextForTooltip(List<ITextComponent> lines, int metadata) {
			TextUtils.addFancyText(lines, "became active", metadata == 1);
		}
	},
	MOVED(2, "moved") {
		
		@Override
		public void addTextForTooltip(List<ITextComponent> lines, int metadata) {
			TextUtils.addFancyText(lines, "EnumFacing", EnumFacing.byIndex(metadata).getName());
		}
	},
	POWER_CHANGE(3, "power_change") {
		
		@Override
		public void addTextForTooltip(List<ITextComponent> lines, int metadata) {
			int oldPower = (metadata >> 8) & 0xFF;
			int newPower =  metadata       & 0xFF;
			
			TextUtils.addFancyText(lines, "old power", oldPower);
			TextUtils.addFancyText(lines, "new power", newPower);
		}
	},
	RANDOM_TICK(4, "random_tick"),
	SCHEDULED_TICK(5, "scheduled_tick") {
		
		@Override
		public void addTextForTooltip(List<ITextComponent> lines, int metadata) {
			TextUtils.addFancyText(lines, "priority", metadata);
		}
	},
	BLOCK_EVENT(6, "block_event") {
		
		@Override
		public void addTextForTooltip(List<ITextComponent> lines, int metadata) {
			TextUtils.addFancyText(lines, "type", metadata);
		}
	},
	ENTITY_TICK(7, "entity_tick"),
	BLOCK_ENTITY_TICK(8, "block_entity_tick"),
	BLOCK_UPDATE(9, "block_update"),
	COMPARATOR_UPDATE(10, "comparator_update"),
	SHAPE_UPDATE(11, "shape_update") {
		
		@Override
		public void addTextForTooltip(List<ITextComponent> lines, int metadata) {
			TextUtils.addFancyText(lines, "EnumFacing", EnumFacing.byIndex(metadata).getName());
		}
	},
	OBSERVER_UPDATE(12, "observer_update"),
	INTERACT_BLOCK(13, "interact_block");
	
	public static final EventType[] ALL;
	private static final Map<String, EventType> BY_NAME;
	
	static {
		EventType[] types = values();
		
		ALL = new EventType[types.length - 1];
		BY_NAME = new HashMap<>();
		
		for (int index = 1; index < types.length; index++) {
			EventType type = types[index];
			
			ALL[type.index] = type;
			BY_NAME.put(type.name, type);
		}
	}
	
	private final int index;
	private final String name;
	
	private EventType(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public static EventType fromIndex(int index) {
		if (index >= 0 && index < ALL.length) {
			return ALL[index];
		}
		
		return UNKNOWN;
	}
	
	public String getName() {
		return name;
	}
	
	public static EventType fromName(String name) {
		return BY_NAME.getOrDefault(name, UNKNOWN);
	}
	
	public int flag() {
		return 1 << index;
	}
	
	public void addTextForTooltip(List<ITextComponent> lines, int metadata) {
		
	}
	
	public INBTBase toNbt() {
		return new NBTTagByte((byte)index);
	}
	
	public static EventType fromNbt(INBTBase nbt) {
		if (nbt.getId() != NbtUtils.TYPE_BYTE) {
			return UNKNOWN;
		}
		
		NBTTagByte byteTag = (NBTTagByte)nbt;
		int index = byteTag.getByte();
		
		return fromIndex(index);
	}
}
