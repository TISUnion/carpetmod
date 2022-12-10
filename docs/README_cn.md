# TISCarpet for Minecraft 1.13.2

------

#### [English Document](https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/README.md)

[gnembon 的 1.13.2 CarpetMod](https://github.com/gnembon/carpetmod) 的分支，简称 TISCM。感谢 gnembon 写出了这么优秀的工具让我们能应付 1.13+ 的菜鸡性能

感谢 [Lithium mod](https://github.com/jellysquid3/lithium-fabric) 提供了一些超棒的优化

感谢 [WorldEdit mod](https://github.com/EngineHub/WorldEdit) 以及 [Spark mod](https://github.com/lucko/spark) 提供了非常强大的功能扩展

内置的模组列表:

- [LazyDFU](https://github.com/astei/lazydfu), v0.1.2
- [Litematica Server Paster](https://github.com/Fallen-Breath/litematica-server-paster), v1.1.0
- [Lithium](https://github.com/jellysquid3/lithium-fabric), 部分特性
- [newLight](https://github.com/Salandora/newLight)
- [Redstone Multimeter](https://github.com/SpaceWalkerRS/redstone-multimeter-fabric), v1.6
- [Spark](https://github.com/lucko/spark), v1.10.13
- [voxel optimization](https://github.com/OverengineeredCodingDuo/mcoptimizations)
- [WorldEdit](https://github.com/EngineHub/WorldEdit), v7.2.7

## 开始使用

- 下载最新的 [release](https://github.com/TISUnion/TISCarpet113/releases)
- 或者在 [GitHub Action](https://github.com/TISUnion/TISCarpet113/actions) 下载最新的构建 (不一定稳定，但是可以用到最新的功能。记得选择 `TIS-Server` 分支的构建，除非你知道你下载的是什么)
- 按照 zip 文件内的 `README.txt` 说明把 carpetmod 补丁装进官方服务端
- 在 [这里](https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/docs/Features_cn.md) 可以看到功能文档，或者在游戏内执行 `/carpet list` 即可看到所有的功能，使用 `/carpet <功能名字>` 可以看到此功能的详细介绍

## TISCM 开发：

Java 8

git 仓库中的源码相关位置:

- `./patches`: 对 Minecraft 源代码的 patch 文件。所有修改后的 **Minecraft** 源代码在此处以 patch 文件的形式储存，因为直接将 Minecraft 的源代码公开出来的不合法的
- `./src`: 所有非 Minecraft 源代码的代码文件，如 carpet 的源代码

一些有用的 gradle 指令:

- `gradle setup`: 根据 patch 文件，生成修改后的 Minecraft 源代码。这也是初始化开发环境的第一步
- `gradle genPatches`: 从修改后的 Minecraft 源代码生成 patch 文件
- `gradle createRelease`: 编译，生成发布的文件至 `./build/distributions`
- `gradle runserver`: 编译，运行服务端。当然，如果你想的话，你可以直接运行主类 `net.minecraft.server.dedicated.DedicatedServer`
- `gradle runclient`: 编译，运行客户端。当然，如果你想的话，你也可以直接运行主类

Gradle 项目:

- `./`: 主项目，在 `./src` 文件夹中储存了所有非 Minecraft 的源代码，如 carpet 的源代码。在此处编写你的代码
- `./projects/carpetmod`: 在 `setup` 后自动生成。于 `./projects/carpetmod/src` 中储存了修改后的 Minecraft 源代码。在这里编写对 Minecraft 的修改
- `./projects/clean`: 在 `setup` 后自动生成。储存未被修改的 Minecraft 原版代码。看看就好，别去改它

注意：

- 在 commit 你的代码前，记得 `genPatches`
- 在切换 git 分支 / 从远端仓库拉取代码前，记得 `setup`
- 除此之外，你需要从 [here](https://files.catbox.moe/xqp3xy.zip) 下载 TISCM_libs 并解压至工程文件夹中，因为有些依赖库提供者已经挂掉了

参照: https://github.com/gnembon/carpetmod/blob/master/README.md
