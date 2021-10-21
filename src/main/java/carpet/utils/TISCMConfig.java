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

	// TISCM Cache BoundingBoxList creation
	public static final boolean CACHE_BOUNDING_BOX_LIST_CREATION  = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM Smaller hashset list container load factor
	// Some hashsets (e.g. TileEntityList, block event queue) works as a list but store elements with hash table
	// Smaller load factor might boost up the performance since memory isn't an issue here
	public static final boolean HASH_SET_LIST_LOAD_FACTOR         = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM cache item burn times
	public static final boolean CACHE_ITEM_BURN_TIMES             = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM cache TileEntity Serialization
	public static final boolean CACHE_TILE_ENTITY_SERIALIZATION   = TISCM_OPTIMIZATION_ENABLE && true;

	// ========== Mods ==========
	// https://github.com/EngineHub/WorldEdit
	public static final boolean MOD_WORLDEDIT = true && classExists("com.sk89q.worldedit.WorldEdit");

	// https://github.com/lucko/spark
	public static final boolean MOD_SPARK = true && classExists("me.lucko.spark.common.SparkPlugin");

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
}
