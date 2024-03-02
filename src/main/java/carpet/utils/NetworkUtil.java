/*
 * This file is part of the Carpet TIS Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2024  Fallen_Breath and contributors
 *
 * Carpet TIS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Carpet TIS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Carpet TIS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package carpet.utils;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class NetworkUtil
{
	/**
	 * See <a href="https://wiki.vg/NBT">https://wiki.vg/NBT</a>
	 * for the nbt changes between mc < 1.20.2 and mc >= 1.20.2
	 */
	public enum NbtStyle
	{
		UNKNOWN,
		OLD,  // <  1.20.2
		NEW;  // >= 1.20.2

		public static final NbtStyle CURRENT = OLD;
	}

	private static final int TAG_ID_COMPOUND = 0x0A;

	// Notes: reader index untouched
	public static NbtStyle guessNbtStyle(PacketBuffer buf)
	{
		int n = buf.readableBytes();

		buf.markReaderIndex();
		try
		{
			if (n < 2)
			{
				return NbtStyle.UNKNOWN;
			}

			byte typeId = buf.readByte();
			if (typeId != TAG_ID_COMPOUND)
			{
				return NbtStyle.UNKNOWN;
			}

			if (n == 2)
			{
				if (buf.readByte() == 0)
				{
					// >=1.20.2, empty nbt
					return NbtStyle.NEW;
				}
				return NbtStyle.UNKNOWN;
			}
			else  // n > 2
			{
				byte[] bytes = new byte[2];
				buf.readBytes(bytes);

				// Double 0x00 for the empty root tag name
				if (bytes[0] == 0 && bytes[1] == 0)
				{
					return NbtStyle.OLD;
				}
				// A valid nbt type id
				else if (0 <= bytes[0] && bytes[0] < 13)
				{
					return NbtStyle.NEW;
				}
			}
		}
		finally
		{
			buf.resetReaderIndex();
		}

		return NbtStyle.UNKNOWN;
	}

	/**
	 * Read an NBT from a {@link PacketBuffer}
	 *
	 * Compatible with both mc >= 1.20.2 and mc < 1.20.2 formats
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	@Nullable
	public static NBTTagCompound readNbt(PacketBuffer buf)
	{
		NbtStyle nbtStyle = guessNbtStyle(buf);

		if (nbtStyle == NbtStyle.NEW)
		{
			// I'm < mc1.20.2 (OLD), trying to read a nbt in NEW style

			buf.markReaderIndex();
			PacketBuffer tweakedBuf = new PacketBuffer(Unpooled.buffer());
			tweakedBuf.writeByte(buf.readByte());  // 0x0A, tag type
			tweakedBuf.writeByte(0).writeByte(0);  // 2* 0x00
			tweakedBuf.writeBytes(buf);
			buf.resetReaderIndex();

			NBTTagCompound nbt = tweakedBuf.readCompoundTag();

			int n = tweakedBuf.readerIndex();
			buf.readBytes(Math.max(0, n - 2));

			return nbt;
		}
		else if (nbtStyle == NbtStyle.OLD)
		{
			// do nothing
		}

		return buf.readCompoundTag();
	}
}
