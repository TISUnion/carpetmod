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

Categories: command, creative

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

## explosionRandomSizeRatio

Set the random size ratio in `doExplosionA` to a fixed value

The value should be between 0.7 and 1.3 as vanilla behavior. Set it to -1 to disable overriding

Default: `-1`

Options: `-1`, `0.7`, `1`, `1.3`

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

------

# Features

## Stat for breaking bedrock

When a bedrock is deleted by piston or sticky piston, the nearest player to the bedrock with 10m will +1 stat

The stat name is `break_bedrock` in `custom` catalogory

------

# Commands

## epsTest

`/epsTest [<duration>]`

Trigger a `<duration>` (default: `30`) seconds long explosion performance test. When it finishes it will output the number of explosions which the server can process every second, aka Explosion per Second (EPS)

Use `/carpet commandEPSTest` to enable / disable this command

------

# Fixes

Fixes for original CarpetMod

- fix no username length limit with `/player` command (long name will make everyone cannot enter the server) 
- remove lever modification ability from flippinCactus

-----

# Optimizations

There are some optimizations in TISCM. It's not suitable to make this optimizations switchable, so they're all hardcoded to be enabled

## Lithium mod porting

Ports and enable part of implementations of [Lithium mod](https://github.com/jellysquid3/lithium-fabric):

- alloc.entity_tracker
- alloc.enum_values
- block.piston_shapes
- cached_hashcode
- math.fast_util
- shapes.precompute_shape_arrays
- shapes.shape_merging
- tag
- world.block_entity_ticking
- world.explosions
- world.tick_scheduler

## Custom Optimizations

There are also a few optimizations which is not from lithium mod:

- Cache `EnumFacing.values()` like Lithium alloc.enum_values but in everywhere
- Cache BoundingBoxList creation in TileEntityHopper and TileEntityPiston
- Pre-allocate 256 size in hashsets/hashmaps to avoid constantly rehash when the amount of TileEntity is small
- Permanently store item burn times in `TileEntityFurnace` to avoid costly map generating each time
