- # TISCM 特性列表

------

[English](https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/docs/Features.md)

**注意**：此处的功能列表并不完整，可能有原版 carpet 或者正在开发中的功能没有增加

------

# 功能

> `/carpet <CommandName> <Value>`

## commandPing

启用`/ping`指令来获取你的ping值

默认值： `true`

选项：`false`, `true`

分类：creative

## dragonCrashFix

修复了末影龙 AI 中导致崩服的无限循环

默认值： `false`

选项：`false`, `true`

分类：bugfix

## optimizeVoxelCode

优化了 voxel 部分的代码

默认值： `false`

选项：`false`, `true`

分类：optimization

## chunkCache

由 PhiPro 写的区块缓存代码

默认值： `false`

选项：`false`, `true`

分类：optimization

## entityMomentumLoss

关闭/启用加载区块时速度超过 10m/gt 实体的速度丢失

默认值： `true`

选项：`false`, `true`

分类：experimental

## stainedGlassNoMapRendering

关闭地图上渲染染色玻璃

默认值： `false`

选项：`false`, `true`

分类：experimental

## cacheExplosions

爆炸缓存，对珍珠炮等情况优化巨大

默认值： `false`

选项：`false`, `true`

分类：optimization

## missingLightFix

将光照变化过的子区块视作非空子区块，以修复浮空建筑下的黑影等bug

不再有烦人的黑影！

默认值： `false`

选项：`false`, `true`

分类：experimental, BUGFIX

## newLight

由 PhiPro 写的更好的光照代码，即 NewLight mod

由 Salandora 移植到 1.13！

默认值： `false`

选项：`false`, `true`

分类：experimental, optimization

## portalSuperCache

极大提升了地狱门搜索的效率，by LucunJi

目前最高效的地狱门优化

**对 fillUpdate=false 时创建的地狱门方块无效**

默认值： `false`

选项：`false`, `true`

分类：experimental, optimization

## microTiming

启用 [MicroTiming logger](#microTiming-1) 的功能

使用羊毛块来输出红石元件的动作、方块更新与堆栈跟踪

使用 `/log microTiming` 来开始监视

开启时服务端性能将受到一定影响

末地烛会检测方块更新，红石元件会输出它们的动作

| 方块类型                     | 如何记录动作        |
| ---------------------------- | ------------------- |
| 侦测器、活塞、末地烛         | 指向羊毛块          |
| 中继器、比较器、铁轨、按钮等 | 放置/依附在羊毛块上 |

除此之外，羊毛块上的末地烛指向方块的动作也会被记录

默认值： `false`

选项：`false`, `true`

分类：creative

## microTimingTarget

设置指定微时序记录器记录目标的方法

`labelled`: 记录被羊毛块标记的事件

`in_range`: 记录离任意玩家 32m 内的事件

`all`: 记录所有事件。**谨慎使用**

默认值： `labelled`

选项：`labelled`, `in_range`, `all`

分类：creative

## structureBlockLimit

覆写结构方块的大小限制

当相对位置的值大于 32 时客户端里结构的位置可能会错误地显示

默认值： `32`

选项：`32`, `64`, `96`, `128`

分类：creative

## optimizedInventories

优化漏斗与投掷器跟箱子的互动

感谢: skyrising (Quickcarpet)

默认值: `false`

选项: `false`, `true`

分类: experimental, optimization

## xpTrackingDistance

修改经验球检测并追踪玩家的距离

将其调至 0 以禁用追踪

默认值: `8`

选项: `0`, `1`, `8`, `32`

分类: creative

## elytraDeploymentFix

优化鞘翅的展开

代码来自 1.15，这是 `MC-111444` 的修复

默认值: `false`

选项: `false`, `true`

分类: experimental, bugfix

## explosionRandomRatio

将 `doExplosionA` 中爆炸射线的随机比率设为一个固定值

准确地来讲，这将替换用于随机化爆炸射线强度时的 `nextFloat()` 返回值，从而使得具有随机性的爆炸射线强度变为固定值

可用的范围为 `0.0` 至 `1.0`，将其设为 `-1.0` 以禁用随机比率覆写

默认值: `-1.0`

选项: `-1.0, `0.0`, `0.5`, `1.0`

分类: creative

## totallyNoBlockUpdate

禁止方块更新

默认值: `false`

选项: `false`, `true`

分类: creative

## visualizeProjectileLoggerEnabled

启用投掷物可视化记录器

默认值: `false`

选项: `false`, `true`

分类: survival

## commandEPSTest

启用用于性能测试的 `/epsTest`

默认值: `false`

选项: `false`, `true`

分类: command

## blockEventPacketRange

设置会在方块事件成功执行后收到数据包的玩家范围

对于活塞而言，这一个数据包是用于显示活塞移动的话。把这个值调小以减小客户端卡顿

默认值: `64`

选项: `0`, `16`, `64`, `128`

分类: optimization

## tntFuseDuration

覆盖 TNT 的默认引信时长

这也会影响被爆炸点燃的 TNT 的引信时长

默认值: `80`

选项: `0`, `80`, `32767`

分类: creative

## opPlayerNoCheat

禁用部分指令以避免op玩家意外地作弊

影响的指令列表：`/gamemode`, `/tp`, `/teleport`, `/give`, `/setblock`, `/summon`

默认值: `false`

选项: `false`, `true`

分类: survival

## hopperCountersUnlimitedSpeed

当漏斗指向羊毛方块时，漏斗将拥有无限的物品吸取以及传输速度

仅当hopperCounters开启时有效

默认值: `false`

选项: `false`, `true`

分类: creative

## instantCommandBlock

令位于红石矿上的命令方块瞬间执行命令，而不是添加一个1gt的计划刻事件用于执行

仅影响普通命令方块

默认值: `false`

选项: `false`, `true`

分类: creative

## antiSpamDisabled

禁用玩家身上的刷屏检测，包括：聊天信息发送冷却、创造模式扔物品冷却

默认值: `false`

选项: `false`, `true`

分类: creative, survival

## blockPlacementIgnoreEntity

方块可放置时无视实体碰撞检测，也就是你可以将方块放在实体内

仅对创造模式玩家有效

默认值: `false`

选项: `false`, `true`

分类: creative

## chunkTickSpeed

修改每游戏刻每区块的区块刻运算的频率

默认值为 `1`。将其设为 `0` 以禁用区块刻

受影响的游戏阶段：
- 雷电
- 结冰与积雪
- 随机刻
  
在值为 `n` 时，每游戏刻每区块，气候相关的阶段会发生 `n` 次，而随机刻会在每区段中发生 `n` * `randomTickSpeed` 次

默认值: `1`

选项: `0`, `1`, `10`, `100`, `1000`

分类: creative

## tileTickLimit

修改每游戏刻中计划刻事件的执行次数上限

默认值: `65536`

选项: `1024`, `65536`, `2147483647`

分类: creative

## repeaterHalfDelay

当红石中继器位于红石矿上方时，红石中继器的延迟将减半

延迟将会由 2, 4, 6, 8 游戏刻变为 1, 2,3 ,4 游戏刻

默认值: `false`

选项: `false`, `true`

分类: creative


## commandLifeTime

启用 `/lifetime` 命令用于追踪生物存活时间等信息

可助于调试刷怪塔等

默认值： `true`

选项：`false`, `true`

分类：creative


## optimizedHardHitBoxEntityCollision

优化实体与硬碰撞箱实体的碰撞

使用了独立的列表在区块中储存带有硬碰撞箱的实体，包括船和潜影贝

它能在实体移动并搜索路径上的带有硬碰撞箱的实体时减少大量无用的运算，因为世界里船和潜影贝的数量总是少数

在加载区块前开启它以使其工作

默认值: `false`

选项: `false`, `true`

分类: optimization, experimental


## YEET

**警告**：所有的yeet选项都会改变原版的特性，它们的行为不会表现得跟原版一致！

### yeetFishAI

去掉实体鱼造成巨大卡顿的followGroupLeaderAI

默认值： `false`

选项：`false`, `true`

分类：yeet

### yeetGolemSpawn

跳过铁傀儡生成使小墨的14k刷铁塔堆叠更快

默认值： `false`

选项：`false`, `true`

分类：yeet

### yeetVillagerAi

去掉部分村民ai使14k刷铁塔堆叠更快

默认值： `false`

选项：`false`, `true`

分类：yeet

### yeetUpdateSuppressionCrash

去掉由于更新抑制等导致的服务器崩溃

**警告**: 更新抑制后可能出现未知的游戏现象，出于安全考虑最好重启服务器或者恢复备份

默认值： `false`

选项：`false`, `true`

分类：yeet

------

# 记录器

- 保存记录器状态信息，以便在重启服务器后自动加载未关闭的记录器

> `/log <LoggerName> [<Option>]`

## projectiles

增加撞击点显示

选项：`brief`, `full`, `visualize`

### visualize

投掷物可视化记录器

## chunkdebug

区块加载/卸载记录器

选项：无

##  villagecount

于 tab 列表中显示村庄数量

选项：无

## microTiming

`/log microTiming <类型>`

记录元件的微时序，元件所在区块的加载票等级需至少为弱加载 (加载票等级 32)

见规则 [microTiming](#microTiming) 以获得详细信息，记得使用 `/carpet microTiming true` 启用监视器功能

可用的类型选项: 
- `all`: 默认值，输出所有事件
- `unique`: 默认值，输出所有每游戏刻中第一次出现的事件

选项：`all`, `unique`

### all

输出所有信息，默认状态

### unique

仅输出不同的信息，如果你不想被红石粉刷屏的话可以开

## autosave

在自动保存触发时告知玩家

选项：无


## commandBlock

`/log commandBlock <option>`

记录命令方块或命令方块矿车的指令执行

有助于找到烦人的不知所踪的命令方块在何处

当使用默认的 `throttled` 选项，每个命令方块最高以每 3 秒一次的频率记录其执行

- 默认选项: `throttled`
- 参考选项: `throttled`, `all`

------

# 指令

## epsTest

`/epsTest [<duration>]`

触发一个长为 2 分钟的基于爆炸的性能测试。完成时将会输出服务器每秒可处理的爆炸数，也就是 Explosion per Second (EPS)

使用 `/carpet commandEPSTest` 来启用 / 禁用此命令


## lifetime

一个追踪所有新生成生物的存活时间及生成/移除原因的记录器

该记录器主要用于测试各种刷怪塔，用于追踪从生物开始影响怪物容量上限，至移出怪物容量上限的这个过程。该记录器的生成追踪并未覆盖所有的生物生成原因

对于一个生物，除了其因各种原因被移出世界，当其**首次**变为不计入怪物容量上限时，例如被命名、捡起物品，也会被标记移除。如果一个生物生成时已不在怪物容量上限中，那它将不会被追踪

作为一项附加功能，该跟踪器还跟踪由方块或生物掉落的物品和经验球的存活时间。注意该追踪器并未追踪所有可能的掉落物或经验球生成，推荐先做好相关测试

给指令添加 `realtime` 后缀可将速率结果从基于游戏时间转换为基于现实时间

### tracking

`/raid tracking [<start|stop|restart>]`

控制存活时间追踪器

追踪的实体类型：
- 所有种类的生物 (MobEntity)
- 掉落物实体
- 经验球实体

追踪的实体生成原因
- 自然刷新
- 地狱门僵尸猪人生成
- 因传送门跨维度到来
- 被物品生成（怪物蛋等）
- 史莱姆分裂 (对于史莱姆以及岩浆怪)
- 僵尸增援
- `/summon` 指令
- 生物掉落（仅掉落物及经验球）
- 方块掉落（仅掉落物）

注意，只有被追踪了生成的实体会被统计

追踪的实体移除原因
- 消失，包括立即消失、随机消失、因游戏难度的消失以及超时消失
- 受伤致死
- 获取持久（persistent）标签。注意此时实体并未移除出世界
- 因传送门跨维度离开
- 实体合并（仅掉落物及经验球）
- 被玩家捡起（仅掉落物及经验球）
- 被漏斗或漏斗矿车收集（仅掉落物）
- 其他（其余未被统计的原因）

存活时间的定义为：**实体生成时刻与移除时刻间的经过的自然生物刷新阶段的数量**，也就是刷怪时被该实体影响了怪物容量上限的游戏刻数。技术上来讲，刷怪阶段数量计数器的调用位置是方法 `WorldEntitySpawner#findChunksForSpawning` 的开始

统计信息以数量所占比例排序 

### <实体类型>

`/lifetime <实体类型> [<life_time|removal|spawning>]`

显示指定实体类型的详细统计信息。你可以指定输出哪一部分统计信息

比如 `/lifetime creeper` 将详细地显示爬行者的统计信息，`/lifetime creeper removal` 则只详细显示爬行者的移除统计信息 

------

# 特性

## 破基岩统计项

在一个基岩被活塞或粘性活塞破除时触发，为十米内离被破坏的基岩最近的玩家的该统计项 +1

统计项名为 `custom` 分类下的 `break_bedrock`

------

# 修复

对原版 CarpetMod 的修复

- 修复了使用 `/player` 指令时没有限制名字长度的问题（过长的名字会使所有人都不能进入服务器）
- 移除仙人掌扳手修改拉杆的功能

-----

# 优化

TISCM 中包含着一些对游戏的优化。这些优化都不太适合做成可开关式的，因此在 TISCM 中这些优化将会被常开起着

## Lithium mod 移植

移植并启用了部分的 [Lithium mod](https://github.com/jellysquid3/lithium-fabric) 的实现：

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

如果需要，部分 Lithium 移植的实现可在 `LithiumConfig` 类中手动开关

## 定制的优化

TISCM 中也有一些在 lithium mod 不包含的优化：

- 像 Lithium alloc.enum_values 一样缓存 `EnumFacing.values()`，不过作用在了所有调用之处
- 在 `TileEntityHopper` 以及 `TileEntityPiston` 中缓存 BoundingBoxList 的创建结果
- 在 `TileEntityList` 中给 hashset/hashmap 预先分配 256 大小的空间以防止在方块实体数量较小时频繁重建容器
- 在 `TileEntityFurnace` 中永久性地储存物品的燃烧时间以避免每次调用都重复创建时间表
