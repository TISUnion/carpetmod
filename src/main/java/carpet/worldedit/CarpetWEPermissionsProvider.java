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

import carpet.settings.CarpetSettings;
import carpet.settings.SettingsManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;

public interface CarpetWEPermissionsProvider
{

    boolean hasPermission(EntityPlayerMP player, String permission);

    void registerPermission(String permission);

    class VanillaPermissionsProvider implements CarpetWEPermissionsProvider
    {

        private final CarpetWEPlatform platform;

        public VanillaPermissionsProvider(CarpetWEPlatform platform) {
            this.platform = platform;
        }

        @Override
        public boolean hasPermission(EntityPlayerMP player, String permission) {
            CarpetWEConfiguration configuration = platform.getConfiguration();
            return configuration.cheatMode
                || player.server.getPlayerList().canSendCommands(player.getGameProfile())
                || (configuration.creativeEnable && player.interactionManager.getGameType() == GameType.CREATIVE);
        }

        @Override
        public void registerPermission(String permission) {
        }
    }

    class CarpetPermissionsProvider extends VanillaPermissionsProvider {
        public CarpetPermissionsProvider(CarpetWEPlatform platform) {
            super(platform);
        }

        @Override
        public boolean hasPermission(EntityPlayerMP player, String permission) {
            return super.hasPermission(player, permission) && SettingsManager.canUseCommand(player.getCommandSource(), CarpetSettings.worldEdit);
        }
    }
}
