/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package carpet.spark;

import me.lucko.spark.common.platform.world.AbstractChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class CarpetWorldInfoProvider implements WorldInfoProvider {

    protected List<CarpetChunkInfo> getChunksFromCache(ChunkProviderServer chunkProvider) {
        Collection<Chunk> loadedChunks = chunkProvider.getLoadedChunks();
        List<CarpetChunkInfo> list = new ArrayList<>(loadedChunks.size());

        for (Chunk loadedChunk : loadedChunks) {
            list.add(new CarpetChunkInfo(loadedChunk));
        }

        return list;
    }

    public static final class Server extends CarpetWorldInfoProvider {
        private final MinecraftServer server;

        public Server(MinecraftServer server) {
            this.server = server;
        }

        @Override
        public CountsResult pollCounts() {
            int players = this.server.getCurrentPlayerCount();
            int entities = 0;
            int tileEntities = 0;
            int chunks = 0;

            for (WorldServer world : this.server.getWorlds()) {
                entities += world.loadedEntityList.size();
                tileEntities += world.loadedTileEntityList.size();
                chunks += world.getChunkProvider().getLoadedChunkCount();
            }

            return new CountsResult(players, entities, tileEntities, chunks);
        }

        @Override
        public ChunksResult<CarpetChunkInfo> pollChunks() {
            ChunksResult<CarpetChunkInfo> data = new ChunksResult<>();

            for (WorldServer world : this.server.getWorlds()) {
                List<CarpetChunkInfo> list = getChunksFromCache(world.getChunkProvider());
                data.put(world.getDimension().getType().toString(), list);
            }

            return data;
        }
    }

    static final class CarpetChunkInfo extends AbstractChunkInfo<EntityType<?>> {
        private final CountMap<EntityType<?>> entityCounts;

        CarpetChunkInfo(Chunk chunk) {
            super(chunk.x, chunk.z);

            this.entityCounts = new CountMap.Simple<>(new HashMap<>());
            for (ClassInheritanceMultiMap<Entity> entityList : chunk.getEntityLists()) {
                for (Entity entity : entityList) {
                    this.entityCounts.increment(entity.getType());
                }
            }
        }

        @Override
        public CountMap<EntityType<?>> getEntityCounts() {
            return this.entityCounts;
        }

        @Override
        public String entityTypeName(EntityType<?> type) {
            return EntityType.getId(type).toString();
        }
    }

}

