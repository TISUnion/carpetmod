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

import carpet.utils.TISCMConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Interface for all world edit accesses
 */
public class CarpetWorldEditAccess
{
    public static void onInitialize()
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            CarpetWorldEdit.inst.onInitialize();
        }
    }

    public static void onStartingServer(MinecraftServer minecraftServer)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            CarpetWorldEdit.inst.onStartingServer(minecraftServer);
        }
    }

    public static void onStartServer(MinecraftServer minecraftServer)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            CarpetWorldEdit.inst.onStartServer(minecraftServer);
        }
    }

    public static void onStopServer(MinecraftServer minecraftServer)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            CarpetWorldEdit.inst.onStopServer(minecraftServer);
        }
    }

    public static void onEndServerTick(MinecraftServer minecraftServer)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            CarpetWorldEdit.inst.onEndServerTick(minecraftServer);
        }
    }

    public static void onPlayerDisconnect(EntityPlayerMP player)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            CarpetWorldEdit.inst.onPlayerDisconnect(player);
        }
    }

    public static EnumActionResult onLeftClickBlock(EntityPlayer playerEntity, World world, EnumHand hand, BlockPos blockPos, EnumFacing direction)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            return CarpetWorldEdit.inst.onLeftClickBlock(playerEntity, world, hand, blockPos, direction);
        }
        return EnumActionResult.PASS;
    }

    public static EnumActionResult onRightClickBlock(EntityPlayer playerEntity, World world, EnumHand hand, EnumFacing facing, BlockPos blockPos)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            return CarpetWorldEdit.inst.onRightClickBlock(playerEntity, world, hand, facing, blockPos);
        }
        return EnumActionResult.PASS;
    }

    @Nullable
    public static ActionResult<ItemStack> onRightClickAir(EntityPlayer playerEntity, World world, EnumHand hand)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            return CarpetWorldEdit.inst.onRightClickAir(playerEntity, world, hand);
        }
        return null;
    }

    public static void registerCommands(CommandDispatcher<CommandSource> dispatcher)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            CarpetWorldEdit.inst.registerCommands(dispatcher);
        }
    }

    public static void onCuiPacket(CPacketCustomPayload packet, EntityPlayerMP player)
    {
        if (TISCMConfig.MOD_WORLDEDIT)
        {
            if (CarpetWorldEdit.CUI_IDENTIFIER.equals(packet.getChannel()))
            {
                CarpetWorldEdit.inst.onCuiPacket(packet.getData(), player);
            }
        }
    }
}
