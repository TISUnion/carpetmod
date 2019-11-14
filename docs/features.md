# TISCM Features/TISCM功能

------

###### 注：此处的功能列表并不完整，可能有原版carpet或者正在开发中的功能没有增加

warn: Features here maybe incomplete, some features in original carpet or developing features may not appear here

------

## Features/功能:

* dragonCrashFix: fix infinite loop in dragon AI causing server crash / 修复了末影龙AI中导致崩服的无限循环
* optimizeVoxelCode: optimizes the voxel code which is used by e.g. the entity movement / 优化了voxel部分的代码
* chunkCache: improved chunk caching by PhiPro / 由PhiPro写的区块缓存代码
* entityMomentumLoss: disable the entity momentum cancellation / 关闭加载区块时实体会丢失速度
* stainedGlassNoMapRendering: disable stained glass rendering on maps / 关闭地图上渲染染色玻璃
* cacheExplosions: Caching explosions, useful for situations eg pearl cannon / 爆炸缓存，对珍珠炮等情况优化巨大
* missingLightFix: Treat any subchunk with light changes as a not-empty subchunk to solve the missing sky/block light in empty subchunk after reloading the chunk / 将光照变化过的子区块视作非空子区块，以修复浮空建筑下的黑影等bug
* newLight: improved light code by PhiPro / 由PhiPro写的更好的光照代码
* betterPortalSearcher: powerful portal searching and caching methods by LucunJi / 由LucunJi写的强劲的地狱门搜索与缓存代码
  * vanilla: highly vanilla mode / 高度原汁原味原版的代码
  * box: earch on the edges of a square bounding box expanding from the center / 在一个从中心往外扩张的矩形边上搜索
  * spiral: search from the center in a spiral, slightly (very slightly) slower than the box mode / 从中心往外螺旋形搜索，比box略微慢一点（难以察觉）
  * super_cache: super_cache: cache the portals chunk-by-chunk on teleport, and newly-created/destroyed portals in cached chunks will be cached/uncached automatically (restart the server or switch to other options for a while can reset the cache, **DOES NOT WORK with portals created/destroyed when fillUpdate is false**) / 在传送时以区块为单位缓存地狱门，并且在被缓存的区块中被创建/删除的地狱门会自动被缓存/取消缓存（重新加载存档或切换到其它选项等一会可以将缓存重置，**对fillUpdate=false时创建的地狱门方块无效**）
* YEET：
  * yeetFishAI: yeet fish followGroupLeaderAI for less lag / 去掉实体鱼造成巨大卡顿的followGroupLeaderAI
  * yeetGolemSpawn: yeet Golems spawning at village for faster stacking at 14k iron farm test / 跳过铁傀儡生成使小墨的14k刷铁塔堆叠更快
  * yeetVillagerAi : yeet villager ai for faster stacking at 14k iron farm test / 去掉部分村民ai使14k刷铁塔堆叠更快

------

## Loggers/记录器:

* `/log projectiles`: added hit point / 增加撞击点显示
* `/log projectiles visualize`: visual logger for projectiles / 投掷物可视化记录器
* `/log chunkdebug`: chunk loading/unloading / 区块加载/卸载记录器
* `/log villagecount`: log village count / 村庄数量记录器
* `/log tileticklist`: log tile tick list for making tiletick EMP / NTE列表记录器，有助于制作NTE EMP
* `/log blockupdates`: log NeighborChanged and PostPlacement of observers pointing to a wool block, and powered state changes of activator rails / 记录指着羊毛方块上的侦测器所发生的的NeighborChanged与PostPlacement，与羊毛块上的激活铁轨的充能状态改变
  * `/log blockupdates all`: log all updates, default stats / 记录所有更新，默认状态
  * `/log blockupdates unique`: log unique updates. Turn it on if you don't want to be spammed by redstone dusts / 只记录不同的更新，如果你不想被红石粉刷屏的话可以开
* Store logger status to automatically load unclosed loggers after reboot / 保存记录器状态信息，以便在重启服务器后自动加载未关闭的记录器

------

## Fixes for original CarpetMod/对原版CarpetMod的修复:

* fix no username length limit with `/player` command (long name will make everyone cannt enter the server)/ 修复了使用`/player`指令时没有限制名字长度的问题（过长的名字会使所有人都不能进入服务器）
* remove lever modification ability from flippinCactus / 移除仙人掌扳手修改拉杆的功能

