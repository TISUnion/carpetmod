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

import carpet.spark.plugin.CarpetSparkPlugin;
import me.lucko.spark.common.command.sender.AbstractCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

public class CarpetCommandSender extends AbstractCommandSender<ICommandSource> {
    private static final UUID NIL_UUID = new UUID(0, 0);

    private final CarpetSparkPlugin plugin;

    public CarpetCommandSender(ICommandSource ICommandSource, CarpetSparkPlugin plugin) {
        super(ICommandSource);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        if (super.delegate instanceof EntityPlayer) {
            return ((EntityPlayer) super.delegate).getGameProfile().getName();
        } else if (super.delegate instanceof MinecraftServer) {
            return "Console";
        } else if (super.delegate instanceof RConConsoleSource) {
            return "RCON Console";
        } else {
            return "unknown:" + super.delegate.getClass().getSimpleName();
        }
    }

    @Override
    public UUID getUniqueId() {
        if (super.delegate instanceof EntityPlayer) {
            return ((EntityPlayer) super.delegate).getUniqueID();
        }
        return null;
    }

    @Override
    public void sendMessage(Component message) {
        ITextComponent component = ITextComponent.Serializer.fromJson(GsonComponentSerializer.gson().serialize(message));
        super.delegate.sendMessage(component);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.plugin.hasPermission(super.delegate, permission);
    }
}
