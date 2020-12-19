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

	public static final boolean CACHE_BOUNDING_BOX_LIST_CREATION  = TISCM_OPTIMIZATION_ENABLE && true;
	public static final boolean LARGER_TILE_ENTITY_LIST           = TISCM_OPTIMIZATION_ENABLE && true;
	public static final boolean CACHE_ITEM_BURN_TIMES             = TISCM_OPTIMIZATION_ENABLE && true;
}
