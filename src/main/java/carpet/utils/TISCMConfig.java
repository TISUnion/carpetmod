package carpet.utils;

/**
 * Custom optimizations which are inconvenient to make to be hot-switchable
 * But still, here's a code level configurable optimization switches
 *
 * It's also the switch panel of other bundled mods
 *
 * See also: {@link me.jellysquid.mods.lithium.LithiumConfig}
 */
@SuppressWarnings("PointlessBooleanExpression")
public class TISCMConfig
{
	private static final boolean TISCM_OPTIMIZATION_ENABLE = true;

	// ========== Optimizations ==========

	// TISCM Cache BoundingBoxList creation
	public static final boolean CACHE_BOUNDING_BOX_LIST_CREATION  = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM hashset load factor
	// Smaller load factor might boost up the performance since memory isn't an issue here
	public static final boolean HASH_SET_LOAD_FACTOR              = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM cache item burn times
	public static final boolean CACHE_ITEM_BURN_TIMES             = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM cache TileEntity Serialization
	public static final boolean CACHE_TILE_ENTITY_SERIALIZATION   = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM cache block state light values
	public static final boolean CACHE_BLOCK_STATE_LIGHT_VALUES    = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM multi-threading chunk saving
	// Use multi-threading to serialize chunks to be saved into NBT tag, effectively reducing the lag spike at autosave
	public static final boolean MULTI_THREAD_CHUNK_SAVING         = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM merged session lock check
	// When saving all chunks in a world, the session lock file is checked on processing each chunk and that's redundant
	// This optimization performs the session lock check at the beginning of chunks saving and skip the following checks if it passes
	public static final boolean MERGED_SESSION_LOCK_CHECK         = TISCM_OPTIMIZATION_ENABLE && true;
	// Lazy DFU Switch (https://github.com/astei/lazydfu)
	// Not necessary to be under Mods since the code change is tiny
	public static final boolean LAZY_DFU                          = TISCM_OPTIMIZATION_ENABLE && true;

	// ============== Mods ==============

	// https://github.com/EngineHub/WorldEdit
	public static final boolean MOD_WORLDEDIT    = true && classExists("com.sk89q.worldedit.WorldEdit");

	// https://github.com/lucko/spark
	public static final boolean MOD_SPARK        = true && classExists("me.lucko.spark.common.SparkPlugin");

	private static boolean classExists(String className)
	{
		try
		{
			Class.forName(className);
			return true;
		}
		catch (ClassNotFoundException e)
		{
			return false;
		}
	}

	// ============= Debug =============

	private static final boolean DEBUG_SWITCH                     = false;

	public static final boolean AUTO_SAVE_TIME_COST_DISPLAY       = DEBUG_SWITCH && true;
}
