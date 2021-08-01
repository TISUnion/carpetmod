/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package carpet.worldedit;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.Stat;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;

import java.util.UUID;

public class WorldEditFakePlayer extends EntityPlayerMP
{
    private static final GameProfile FAKE_WORLDEDIT_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("worldedit".getBytes()), "[WorldEdit]");

    public WorldEditFakePlayer(WorldServer world) {
        super(world.getServer(), world, FAKE_WORLDEDIT_PROFILE, new PlayerInteractionManager(world));
    }

    @Override
    public void tick() {
    }

    @Override
    public void addStat(Stat<?> stat, int incrementer) {
    }

    @Override
    public void addStat(Stat<?> stat) {
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }
}
