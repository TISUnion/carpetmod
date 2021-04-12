# TISCM Features

------

[中文](https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/docs/Features_cn.md)

**Notice**: Features here maybe incomplete, some features in original carpet or developing features may not appear here

------

# Features

> `/carpet <CommandName> <Value>`

## commandPing

Enables `/ping` command to see your ping

Default: `true`

Options: `false`, `true`

Categories: creative

## dragonCrashFix

fix infinite loop in dragon AI causing server crash

Default: `false`

Options: `false`, `true`

Categories: bugfix

## optimizeVoxelCode

optimizes the voxel code which is used by e.g. the entity movement

Default: `false`

Options: `false`, `true`

Categories: optimization

## chunkCache

Improved chunk caching by PhiPro

Default: `false`

Options: `false`, `true`

Categories: optimization

## entityMomentumLoss

Disable/Enable the entity momentum cancellation if its above 10 blocks per gametick when reading the data out of disk

Default: `true`

Options: `false`, `true`

Categories: experimental

## stainedGlassNoMapRendering

Disable the rendering of stained glass on maps

Default: `false`

Options: `false`, `true`

Categories: experimental

## cacheExplosions

Caching explosions, useful for situations eg pearl cannon

Default: `false`

Options: `false`, `true`

Categories: optimization

## missingLightFix

Treat any subchunk with light changes as a not-empty subchunk to solve the missing sky / block light in empty subchunk after reloading the chunk

no more ghost shadows below giant floating buildings

Default: `false`

Options: `false`, `true`

Categories: experimental, BUGFIX

## newLight

Uses alternative lighting engine by PhiPros. AKA NewLight mod

Now Ported to 1.13 by Salandora!

Default: `false`

Options: `false`, `true`

Categories: experimental, optimization

## portalSuperCache

Greatly improve the efficiency of nether portal by LucunJi

Most powerful portal optimization ever, 10000 times faster!

**DOES NOT WORK with portals created/destroyed when fillUpdate is false**

Default: `false`

Options: `false`, `true`

Categories: experimental, optimization

## microTiming

Enable the function of [MicroTiming logger](#microTiming-1)

Display redstone components actions, blockupdates and stacktrace with a wool block

Use `/log microTiming` to start logging

Might impact the server performance when it's on

EndRods will detect block updates and redstone components will show their actions

| Block Type                               | How to log actions    |
| ---------------------------------------- | --------------------- |
| Observer, Piston, EndRod                 | pointing towards wool |
| Repeater, Comparator, Rail, Button, etc. | placed on wool        |

Beside that, blocks pointed by EndRod on wool block will also show their actions

Default: `false`

Options: `false`, `true`

Categories: creative

## microTimingTarget

Modify the way to specify events to be logged in microTiming logger

`labelled`: Logs events labelled with wool

`in_range`: Logs events within 32m of any player

`all`: Logs every event. **Use with caution**

Default: `labelled`

Options: `labelled`, `in_range`, `all`

Categories: creative

## structureBlockLimit

Overwrite the size limit of structure block

Relative position might display wrongly on client side if it's larger than 32

Default: `32`

Options: `32`, `64`, `96`, `128`

Categories: creative

## optimizedInventories

Optimizes hoppers and droppers interacting with chests

Credits: skyrising (Quickcarpet)

Default: `false`

Options: `false`, `true`

Categories: experimental, optimization

## xpTrackingDistance

Overwrite the tracking distance of xp orb

Change it to 0 to disable tracking

Default: `8`

Options: `0`, `1`, `8`, `32`

Categories: creative

## elytraDeploymentFix

Optimized Elytra deployment

Code from 1.15. Fixes MC-111444

Default: `false`

Options: `false`, `true`

Categories: experimental, bugfix

## explosionRandomRatio

Set the random size ratio in `collectBlocksAndDamageEntities` to a fixed value

More exactly, it replaces the return value of `nextFloat()` for blast ray strength randomization, so the random blast strength of the explosion becomes predictable

The value should be between `0.0` and `1.0`, where `0.0` is for minimum possible strength and `1.0` is for maxmium possible strength. Set it to `-1.0` to disable overriding

Default: `-1.0`

Options: `-1.0`, `0.0`, `0.5`, `1.0`

Categories: creative

## totallyNoBlockUpdate

Ban block updates

Default: `false`

Options: `false`, `true`

Categories: creative

## visualizeProjectileLoggerEnabled

Enable visualize projectile logger

Default: `false`

Options: `false`, `true`

Categories: survival

## commandEPSTest

Enables `/epsTest` for performance tests

Default: `false`

Options: `false`, `true`

Categories: command

## blockEventPacketRange

Set the range where player will receive a block event packet after a block event fires successfully
            
For piston the packet is used to render the piston movement animation. Decrease it to reduce client's lag

Default: `64`

Options: `0`, `16`, `64`, `128`

Categories: optimization

## tntFuseDuration

Overwrite the default fuse duration of TNT
            
This might also affects the fuse duration of TNT ignited in explosion

Default: `80`

Options: `0`, `80`, `32767`

Categories: creative

## opPlayerNoCheat

Disable some command to prevent accidentally cheating

Affects command list: `/gamemode`, `/tp`, `/teleport`, `/give`, `/setblock`, `/summon`

Default: `false`

Options: `false`, `true`

Categories: survival

## hopperCountersUnlimitedSpeed

Make hopper pointing towards wool has infinity speed to suck in or transfer items

Only works when hopperCounters option in Carpet Mod is on

Default: `false`

Options: `false`, `true`

Categories: creative

## instantCommandBlock

Make command blocks on redstone ores execute command instantly instead of scheduling a 1gt delay TileTick event for execution

Only affects normal command blocks

Default: `false`

Options: `false`, `true`

Categories: creative

## antiSpamDisabled

Disable spamming checks on players, including: chat message cooldown, creative item drop cooldown

Default: `false`

Options: `false`, `true`

Categories: creative, survival

## blockPlacementIgnoreEntity

Disable entity collision check before block placement, aka you can place blocks inside entities

Works with creative mode players only

Default: `false`

Options: `false`, `true`

Categories: creative

## chunkTickSpeed

Modify how often the chunk tick occurs per chunk per game tick

The default value is `1`. Set it to `0` to disables chunk ticks

Affected game phases: 
- thunder
- ice and snow
- randomtick

With a value of `n`, in every chunk every game tick, climate things will tick `n` times, and randomtick will tick `n` * `randomTickSpeed` times per chunk section

Default: `1`

Options: `0`, `1`, `10`, `100`, `1000`

Categories: creative

## tileTickLimit

Modify the limit of executed tile tick events per game tick

Default: `65536`

Options: `1024`, `65536`, `2147483647`

Categories: creative

## repeaterHalfDelay

Halve the delay of redstone repeaters upon a redstone ore

The delay will change from 2, 4, 6 or 8 game tick instead of 1, 2, 3 or 4 game tick

Default: `false`

Options: `false`, `true`

Categories: creative


## commandLifeTime

Enables `/lifetime` command to track entity lifetime and so on

Useful for mob farm debugging etc.

Default: `true`

Options: `false`, `true`

Categories: creative


## optimizedHardHitBoxEntityCollision

Optimize entity colliding with entities with hard hit box

It uses an extra separate list to store entities, that have a hard hit box including boat and shulker, in a chunk

It reduces quite a lot of unnecessary iterating when an entity is moving and trying to search entities with hard hit box on the way, since the world is always not filled with boats and shulkers

Enable it before loading the chunk to make it work

Default: `false`

Options: `false`, `true`

Categories: optimization, experimental


## optimizedFastEntityMovement

Optimize fast entity movement by only checking block collisions on current moving axis

Inspired by the `fastMovingEntityOptimization` rule in [carpetmod112](https://github.com/gnembon/carpetmod112)

Works with `optimizeVoxelCode` off. Rule `optimizeVoxelCode` itself already has relative optimization

Default: `false`

Options: `false`, `true`

Categories: optimization, experimental


## drownedNavigatorMemoryLeakFix

Fixed memory leak caused by drowned path finding AI

Check [MC-202246](https://bugs.mojang.com/browse/MC-202246) for details

Default: `false`

Options: `false`, `true`

Categories: bugfix


## creativeOpenShulkerBoxForcibly

Allow creative players to open a shulker block even if the shulker box is blocked

Default: `false`

Options: `false`, `true`

Categories: creative


## YEET

**Warn**: all yeet options will change vanilla behaviour, they WILL NOT behave like vanilla

### yeetFishAI

yeet fish followGroupLeaderAI for less lag

Default: `false`

Options: `false`, `true`

Categories: yeet

### yeetGolemSpawn

yeet Golems spawning at village for faster stacking at 14k iron farm test

Default: `false`

Options: `false`, `true`

Categories: yeet

### yeetVillagerAi

yeet villager ai for faster stacking at 14k iron farm test

Default: `false`

Options: `false`, `true`

Categories: yeet

### yeetUpdateSuppressionCrash

yeet updates suppression causing server crashes

**WARNING**: Unknown game behaviors might occur after updates suppression, better restart the server or restore a backup for safety

Default: `false`

Options: `false`, `true`

Categories: yeet

------

# Loggers

- Store logger status to automatically load unclosed loggers after reboot

> `/log <LoggerName> [<Option>]`

## projectiles

added hit point

Options: `brief`, `full`, `visualize`

### visualize

visual logger for projectiles

## chunkdebug

Log chunk loading / unloading

Options: none

##  villagecount

log village count on tab list

Options: none

## microTiming

`/log microTiming <type>`

Log micro timings of redstone components. The ticket of the chunk the component is in needs to be at least lazy-processing (ticket level 32)

Check rule [microTiming](#microTiming) for detail. Remember to use `/carpet microTiming true` to enable logger functionality

Available options: 
- `all`: Log all events
- `unique`: Log the first unique event in every gametick

Options: `all`, `unique`

### all

Output all messages, default stats

### unique

Only output unique messages. Turn it on if you don't want to be spammed by redstone dusts

## autosave

Inform the player when the auto save trigger

Options: none


## commandBlock

`/log commandBlock <option>`

Info when a command block or command block minecart executes command

It's useful to find out where the annoying hidden running command block is

With default `throttled` option every command block will log at the highest frequency once every 3 seconds

- Default option: `throttled`
- Suggested options: `throttled`, `all`

------

# Commands

## epsTest

`/epsTest [<duration>]`

Trigger a `<duration>` (default: `30`) seconds long explosion performance test. When it finishes it will output the number of explosions which the server can process every second, aka Explosion per Second (EPS)

Use `/carpet commandEPSTest` to enable / disable this command


## lifetime

A tracker to track lifetime and spawn / removal reasons from all newly spawned and dead entities

This tracker is mostly used to debug mobfarms. It aims to track the process from mob starting affecting the mobcap to mob being removed from the mobcap. The spawning tracking part of it doesn't cover every kind of mob spawning reasons

Other than being removed from the world, if a mob becomes persistent for the first time like nametagged or item pickup, it will be marked as removal too. If a mob doesn't count towards the mobcap when it spawns, it will not be tracked

This tracker also tracks lifetime of items and xp orbs from mob and block drops as an additional functionality. Note that it doesn't track all item / xp orb spawning, so you'd better have a test before actually using it

Adding a `realtime` suffix to the command will turn the rate result from in-game time based to realtime based

### tracking

`/raid tracking [<start|stop|restart>]`

Control the lifetime tracker

Tracked entity types:
- All kinds of mob (MobEntity)
- Item Entity
- Experience Orb Entity

Tracked entity spawning reasons
- Natural spawning
- Portal pigman spawning
- Trans-dimension from portal
- Spawned by item (spawn eggs etc.)
- Slime division (for slime and magma cube)
- Zombie Reinforce
- `/summon` command
- Mob drop (item and xp orb only)
- Block drop (item only)

Note that only entities that have been tracked spawning will be counted to the statistic 

Tracked entity removal reasons
- Despawn, including immediately despawn, random despawn, difficulty despawn and timeout despawn
- Damaged to death
- Becomes persistent. Note that the entity is still not removed from the world
- Trans-dimension through portal
- Entity merged (item and xp orb only)
- Picked up by player (item and xp orb only)
- Collected up by hopper or hopper minecart (item only)
- Other (anything else not in the list)

The definition of lifetime is: **The amount of spawning stage passing between entity spawning and entity removal**, in other words, how many gameticks does the entity counts towards mobcap. Technically the invoking point for the passing spawning stage counter increment is at the beginning of the method `WorldEntitySpawner#findChunksForSpawning`

Statistics are sorted by the proportion of the amount 

### <entity_type>

`/lifetime <entity_type> [<life_time|removal|spawning>]`

Show the detail statistic of specific entity type. You can specify which part of the statistic will be output

For example, `/lifetime creeper` shows all statistic of creeper in detail, and `/lifetime creeper removal` only shows removal statistic of creeper in detail 

------

# Features

## Stat for breaking bedrock

When a bedrock is deleted by piston or sticky piston, the nearest player to the bedrock with 10m will +1 stat

The stat name is `break_bedrock` in `custom` catalogory

------

# Fixes

Fixes for original CarpetMod

- Fixed no username length limit with `/player` command (long name will make everyone cannot enter the server) 
- Removed lever modification ability from flippinCactus
- Added an OP permission check for cheaty command `/player mount`
- Fix block placement being notvanilla in EntityPlayerActionPack

-----

# Optimizations

There are some optimizations in TISCM. It's not suitable to make this optimizations switchable, so they're all hardcoded to be enabled

## Lithium mod porting

Ports and enable part of implementations of [Lithium mod](https://github.com/jellysquid3/lithium-fabric):

- alloc.entity_tracker
- alloc.enum_values
- [block.moving_block_shapes](https://github.com/jellysquid3/lithium-fabric/pull/145)
- block.piston_shapes
- cached_hashcode
- entity.data_tracker.use_arrays
- math.fast_util
- shapes.precompute_shape_arrays
- shapes.shape_merging
- shapes.specialized_shapes
- tag
- world.block_entity_ticking
- world.explosions
- world.tick_scheduler

If necessary, part of the lithium port implementation can be switched manually in the `LithiumConfig` class

## Custom Optimizations

There are also a few optimizations which is not from lithium mod:

- Cache `EnumFacing.values()` like Lithium alloc.enum_values but in everywhere
- Cache BoundingBoxList creation in TileEntityHopper and TileEntityPiston
- Use smaller load factor (`Hash.VERY_FAST_LOAD_FACTOR`) in TileEntityList
- Permanently store item burn times in `TileEntityFurnace` to avoid costly map generating each time
- Cache some expensive tile entity serialization data

If necessary, part of the optimization implementation can be switched manually in the `TISCMOptimizationConfig` class
