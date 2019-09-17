# TISCM Features/TISCM功能

------

###### 注：此处的功能列表并不完整，可能有原版carpet或者正在开发中的功能没有增加

warn: Features here is incomplete, some features in original carpet or developing features may not appear here

------

## Features/功能:

* dragonCrashFix: fix infinite loop in dragon AI causing server crash / 修复了末影龙AI中导致崩服的无限循环
* optimizeVoxelCode: optimizes the voxel code which is used by e.g. the entity movement / 优化了voxel部分的代码
* chunkCache: improved chunk caching by PhiPro / 由PhiPro写的区块缓存代码
* entityMomentumLoss: disable the entity momentum cancellation / 关闭加载区块时实体会丢失速度
* stainedGlassNoMapRendering: disable stained glass rendering on maps / 关闭地图上渲染染色玻璃
* cacheExplosions: Caching explosions, useful for situations eg pearl cannon / 爆炸缓存，对珍珠炮等情况优化巨大
* missingLightFix: Treat any subchunk with light changes as a not-empty subchunk to solve the missing sky/block light in empty subchunk after reloading the chunk / 将光照变化过的子区块视作非空子区块，以修复浮空建筑下的黑影等bug
* * YEET：
  * yeetFishAI: yeet fish followGroupLeaderAI for less lag / 去掉实体鱼造成巨大卡顿的followGroupLeaderAI
  * yeetGolemSpawn: yeet Golems spawning at village for faster stacking at 14k iron farm test / 跳过铁傀儡生成使小墨的14k刷铁塔堆叠更快
  * yeetVillagerAi : yeet villager ai for faster stacking at 14k iron farm test / 去掉部分村民ai使14k刷铁塔堆叠更快

------

## Loggers/记录器:

* `/log projectiles`: added hit point / 增加撞击点显示
* `/log projectiles visualize`: visual logger for projectiles / 投掷物可视化记录器
* `/log chunkdebug`: chunk loading/unloading / 区块加载/卸载记录器
* `/log villagecount`: log village count / 村庄数量记录器

------

## Fixes for original CarpetMod/对原版CarpetMod的修复:

* fix no username length limit with `/player` command (long name will make everyone cannt enter the server)/ 修复了使用`/player`指令时没有限制名字长度的问题（过长的名字会使所有人都不能进入服务器）

