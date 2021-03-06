package carpet.utils;

/**
 * Custom optimizations which are inconvenient to make to be hot-switchable
 * But still, here's a code level configurable optimization switches
 *
 * See also: {@link me.jellysquid.mods.lithium.LithiumConfig}
 */
@SuppressWarnings("PointlessBooleanExpression")
public class TISCMOptimizationConfig
{
	private static final boolean TISCM_OPTIMIZATION_ENABLE = true;

	// TISCM Cache BoundingBoxList creation
	public static final boolean CACHE_BOUNDING_BOX_LIST_CREATION  = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM Larger tile entity list
	public static final boolean LARGER_TILE_ENTITY_LIST           = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM cache item burn times
	public static final boolean CACHE_ITEM_BURN_TIMES             = TISCM_OPTIMIZATION_ENABLE && true;
	// TISCM cache TileEntity Serialization
	public static final boolean CACHE_TILE_ENTITY_SERIALIZATION   = TISCM_OPTIMIZATION_ENABLE && true;
}
