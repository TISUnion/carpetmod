# TISCarpet for Minecraft 1.13.2

------

#### [中文文档](https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/docs/README_cn.md)

TISCarpet13, a fork of [gnembon's original CarpetMod for 1.13.2](https://github.com/gnembon/carpetmod), or TISCM for short. Thanks for gnembon's great tool allowing us to deal with bad performance of 1.13+.

Thanks to [Lithium mod](https://github.com/jellysquid3/lithium-fabric) and [Lazy DFU](https://github.com/astei/lazydfu) for some epic optimizations

Thanks to [WorldEdit mod](https://github.com/EngineHub/WorldEdit) and [Spark mod](https://github.com/lucko/spark) for providing such powerful functionality extensions

Bundled mod list:

- [LazyDFU](https://github.com/astei/lazydfu), v0.1.2
- [Litematica Server Paster](https://github.com/Fallen-Breath/litematica-server-paster), v1.1.0
- [Lithium](https://github.com/jellysquid3/lithium-fabric), partial features
- [newLight](https://github.com/Salandora/newLight)
- [Redstone Multimeter](https://github.com/SpaceWalkerRS/redstone-multimeter-fabric), v1.6
- [Spark](https://github.com/lucko/spark), v1.10.13
- [voxel optimization](https://github.com/OverengineeredCodingDuo/mcoptimizations)
- [WorldEdit](https://github.com/EngineHub/WorldEdit), v7.2.7

## How to use

- go to download the latest [release](https://github.com/TISUnion/TISCarpet113/releases)
- or go download the latest build on [GitHub Action](https://github.com/TISUnion/TISCarpet113/actions) (maybe unstable, but with the latest features ofc. remember to choose builds in `TIS-Server` branch unless you know what you are choosing)
- follow instructions in `README.txt` in that zip file to patch vanilla server jar
- read command docs [here](https://github.com/TISUnion/TISCarpet113/blob/TIS-Server/docs/Features.md) or run `/carpet list` to see all options, run `/carpet <featureName>` for details.

## Developing TISCM:

Java 8

Sources related location in git repository:

- `./patches`: Minecraft source code patches. All modification to **Minecraft**'s source code are stored as patch file here since it's illegal to directly publish Minecraft's source codes
- `./src`: All non-Minecraft source files, e.g. carpet's source files

Useful gradle commands:

- `gradle setup`: Generate modded Minecraft source from patch file. It's also the way to initialize the environment
- `gradle genPatches`: Generate patch file from your modded Minecraft source
- `gradle createRelease`: Compile, and create a release distribution in `./build/distributions`
- `gradle runserver`: Compile and run the server. Of course, you can just launch the main class `net.minecraft.server.dedicated.DedicatedServer` if you want
- `gradle runclient`: Compile and run the client. Of course, you can launch from the main class too

Gradle projects:

- `./`: Main project, stores all non-Minecraft source files, e.g. carpet's source files, in `./src/` folder. Write your codes here
- `./projects/carpetmod`: Auto-generated after `setup`. Stores the modded Minecraft source in `./projects/carpetmod/src`. Apply modification to Minecraft codes here
- `./projects/clean`: Auto-generated after `setup`. Stores the untouched vanilla Minecraft source. Don't modify it

Notes:

- Remember to `genPatches` before committing your changes
- Remember to `setup` after switching git branch / pulling from remote repository
- You need to also pull subproject [TISCM_libs](https://github.com/TISUnion/TISCM_libs), since some library providers are down
  - e.g. `git submodule update --init`

See also: https://github.com/gnembon/carpetmod/blob/master/README.md
