package me.jellysquid.mods.lithium;

/**
 * Code level configurable lithium config
 * Great thanks to lithium mod team for providing such OP optimizations
 * Lithium mod repos: https://github.com/jellysquid3/lithium-fabric
 *
 * See also: {@link carpet.utils.TISCMOptimizationConfig}
 */
@SuppressWarnings("PointlessBooleanExpression")
public class LithiumConfig
{
	private static final boolean LITHIUM_ENABLE = true;

	public static final boolean ALLOC_ENTITY_TRACKER            = LITHIUM_ENABLE && true;
	public static final boolean ALLOC_ENUM_VALUES               = LITHIUM_ENABLE && true;
	public static final boolean BLOCK_MOVING_BLOCK_SHAPES       = LITHIUM_ENABLE && true;
	public static final boolean BLOCK_PISTON_SHAPES             = LITHIUM_ENABLE && true;
	public static final boolean CACHED_HASHCODE                 = LITHIUM_ENABLE && true;
	public static final boolean ENTITY_DATA_TRACKER_USE_ARRAYS  = LITHIUM_ENABLE && true;
	public static final boolean MATH_FAST_UTIL                  = LITHIUM_ENABLE && true;
	public static final boolean SHAPES_PRECOMPUTE_SHAPE_ARRAYS  = LITHIUM_ENABLE && true;
	public static final boolean SHAPES_SHAPE_MERGING            = LITHIUM_ENABLE && true;
	public static final boolean SHAPES_SPECIALIZED_SHAPES       = LITHIUM_ENABLE && true;
	public static final boolean TAG                             = LITHIUM_ENABLE && true;
	public static final boolean WORLD_BLOCK_ENTITY_TICKING      = LITHIUM_ENABLE && true;
	public static final boolean WORLD_EXPLOSIONS                = LITHIUM_ENABLE && true;
	public static final boolean WORLD_TICK_SCHEDULER            = LITHIUM_ENABLE && true;
}
