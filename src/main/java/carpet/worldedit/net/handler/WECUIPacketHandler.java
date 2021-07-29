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

package carpet.worldedit.net.handler;

import carpet.worldedit.FabricAdapter;
import carpet.worldedit.FabricPlayer;
import carpet.worldedit.FabricWorldEdit;
import com.sk89q.worldedit.LocalSession;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.nio.charset.StandardCharsets;

public final class WECUIPacketHandler {
    private WECUIPacketHandler() {
    }

    public static final ResourceLocation CUI_IDENTIFIER = new ResourceLocation(FabricWorldEdit.MOD_ID, FabricWorldEdit.CUI_PLUGIN_CHANNEL);

    public static void onPacket(PacketBuffer buf, EntityPlayerMP player) {
        LocalSession session = FabricWorldEdit.inst.getSession(player);
        String text = buf.toString(StandardCharsets.UTF_8);
        FabricPlayer actor = FabricAdapter.adaptPlayer(player);
        session.handleCUIInitializationMessage(text, actor);
    }
}
