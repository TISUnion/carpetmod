package redstone.multimeter.common.meter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.nbt.NBTTagCompound;

import redstone.multimeter.common.DimPos;
import redstone.multimeter.common.meter.event.EventType;
import redstone.multimeter.util.ColorUtils;

public class MeterProperties {
	
	private DimPos pos;
	private String name;
	private Integer color;
	private Boolean movable;
	private Integer eventTypes;
	
	public MeterProperties() {
		
	}
	
	public MeterProperties(DimPos pos, String name, int color, boolean movable, int eventTypes) {
		this.pos = pos;
		this.name = name;
		this.color = color;
		this.movable = movable;
		this.eventTypes = eventTypes;
	}
	
	@Override
	public String toString() {
		return String.format("MeterProperties[pos: %s, name: %s, color: %s, movable: %s, event types: %s]", pos, name, color, movable, eventTypes);
	}
	
	public DimPos getPos() {
		return pos;
	}
	
	public boolean setPos(DimPos pos) {
		DimPos prevPos = this.pos;
		this.pos = pos;
		
		return prevPos == null || !prevPos.equals(pos);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean setName(String name) {
		String prevName = this.name;
		this.name = name;
		
		return prevName == null || !prevName.equals(name);
	}
	
	public Integer getColor() {
		return color;
	}
	
	public boolean setColor(int color) {
		Integer prevColor = this.color;
		this.color = color;
		
		return prevColor == null || !prevColor.equals(color);
	}
	
	public Boolean getMovable() {
		return movable;
	}
	
	public boolean setMovable(boolean movable) {
		Boolean prevMovable = this.movable;
		this.movable = movable;
		
		return prevMovable == null || !prevMovable.equals(movable);
	}
	
	public Integer getEventTypes() {
		return eventTypes;
	}
	
	public boolean hasEventType(EventType type) {
		return eventTypes != null && (eventTypes & type.flag()) != 0;
	}
	
	public boolean setEventTypes(int eventTypes) {
		Integer prevEventTypes = this.eventTypes;
		this.eventTypes = eventTypes;
		
		return prevEventTypes == null || !prevEventTypes.equals(eventTypes);
	}
	
	public boolean toggleEventType(EventType type) {
		if (eventTypes == null) {
			eventTypes = 0;
		}
		
		return setEventTypes(eventTypes ^ type.flag());
	}
	
	public MeterProperties copy() {
		return new MeterProperties().fill(this);
	}
	
	/**
	 * If a property does not yet have a value, copy the value
	 * from the given properties.
	 */
	public MeterProperties fill(MeterProperties properties) {
		if (pos == null) {
			pos = properties.pos;
		}
		if (name == null) {
			name = properties.name;
		}
		if (color == null) {
			color = properties.color;
		}
		if (movable == null) {
			movable = properties.movable;
		}
		if (eventTypes == null) {
			eventTypes = properties.eventTypes;
		}
		
		return this;
	}
	
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		if (name != null) {
			json.addProperty("name", name);
		}
		if (color != null) {
			json.addProperty("color", ColorUtils.toRGBString(color));
		}
		if (movable != null) {
			json.addProperty("movable", movable);
		}
		if (eventTypes != null) {
			JsonArray types = new JsonArray();
			
			for (EventType type : EventType.ALL) {
				if (hasEventType(type)) {
					types.add(type.getName());
				}
			}
			
			json.add("event_types", types);
		}
		
		return json;
	}
	
	public static MeterProperties fromJson(JsonObject json) {
		MeterProperties properties = new MeterProperties();
		
		if (json.has("name")) {
			JsonElement nameJson = json.get("name");
			
			if (nameJson.isJsonPrimitive()) {
				properties.name = nameJson.getAsString();
			}
		}
		if (json.has("color")) {
			JsonElement colorJson = json.get("color");
			
			if (colorJson.isJsonPrimitive()) {
				try {
					properties.color = ColorUtils.fromRGBString(colorJson.getAsString());
				} catch (NumberFormatException e) {
					
				}
			}
		}
		if (json.has("movable")) {
			JsonElement movableJson = json.get("movable");
			
			if (movableJson.isJsonPrimitive()) {
				properties.movable = movableJson.getAsBoolean();
			}
		}
		if (json.has("event_types")) {
			JsonElement typesJson = json.get("event_types");
			
			if (typesJson.isJsonArray()) {
				properties.eventTypes = 0;
				JsonArray types = typesJson.getAsJsonArray();
				
				for (int index = 0; index < types.size(); index++) {
					JsonElement typeJson = types.get(index);
					
					if (typeJson.isJsonPrimitive()) {
						String typeName = typeJson.getAsString();
						EventType type = EventType.fromName(typeName);
						
						if (type != null) {
							properties.eventTypes |= type.flag();
						}
					}
				}
			}
		}
		
		return properties;
	}
	
	public NBTTagCompound toNbt() {
	    NBTTagCompound nbt = new NBTTagCompound();
		
		if (pos != null) {
			nbt.put("pos", pos.toNbt());
		}
		if (name != null) {
			nbt.putString("name", name);
		}
		if (color != null) {
			nbt.putInt("color", color);
		}
		if (movable != null) {
			nbt.putBoolean("movable", movable);
		}
		if (eventTypes != null) {
			nbt.putInt("event types", eventTypes);
		}
		
		return nbt;
	}
	
	public static MeterProperties fromNbt(NBTTagCompound nbt) {
		MeterProperties properties = new MeterProperties();
		
		if (nbt.contains("pos")) {
			properties.pos = DimPos.fromNbt(nbt.getCompound("pos"));
		}
		if (nbt.contains("name")) {
			properties.name = nbt.getString("name");
		}
		if (nbt.contains("color")) {
			properties.color = nbt.getInt("color");
		}
		if (nbt.contains("movable")) {
			properties.movable = nbt.getBoolean("movable");
		}
		if (nbt.contains("event types")) {
			properties.eventTypes = nbt.getInt("event types");
		}
		
		return properties;
	}
}
