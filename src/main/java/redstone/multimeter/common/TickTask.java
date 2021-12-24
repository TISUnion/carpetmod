package redstone.multimeter.common;

public enum TickTask {
	
	UNKNOWN              ( 0, "unknown"),
	WORLD_BORDER         ( 1, "world border"),
	WEATHER              ( 2, "weather"),
	WAKE_SLEEPING_PLAYERS( 3, "wake sleeping players"),
	CHUNK_SOURCE         ( 4, "chunk source"),
	CHUNK_MAP            ( 5, "chunk map"),
	MOB_SPAWNING         ( 6, "mob spawning"),
	CUSTOM_MOB_SPAWNING  ( 7, "custom mob spawning"),
	TICK_CHUNK           ( 8, "tick chunk"),
	TICK_CHUNKS          ( 9, "tick chunks"),
	THUNDER              (10, "thunder"),
	PRECIPITATION        (11, "precipitation"),
	RANDOM_TICKS         (12, "random ticks"),
	SCHEDULED_TICKS      (13, "scheduled ticks"),
	BLOCK_TICKS          (14, "block ticks"),
	FLUID_TICKS          (15, "fluid ticks"),
	VILLAGES             (16, "villages"),
	RAIDS                (17, "raids"),
	PORTALS              (18, "portals"),
	BLOCK_EVENTS         (19, "block events"),
	ENTITIES             (20, "entities"),
	REGULAR_ENTITIES     (21, "regular entities"),
	GLOBAL_ENTITIES      (22, "global entities"),
	PLAYERS              (23, "players"),
	DRAGON_FIGHT         (24, "dragon fight"),
	BLOCK_ENTITIES       (25, "block entities"),
	PACKETS              (26, "packets"),
	COMMAND_FUNCTIONS    (27, "command functions");
	
	public static final TickTask[] ALL;
	
	static {
		ALL = new TickTask[values().length];
		
		for (TickTask phase : values()) {
			ALL[phase.index] = phase;
		}
	}
	
	private final int index;
	private final String name;
	
	private TickTask(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public static TickTask fromIndex(int index) {
		if (index > 0 && index < ALL.length) {
			return ALL[index];
		}
		
		return UNKNOWN;
	}
	
	public String getName() {
		return name;
	}
}
