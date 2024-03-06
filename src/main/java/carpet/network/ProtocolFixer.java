package carpet.network;

import carpet.utils.NetworkUtil;
import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.util.List;
import java.util.Objects;

public class ProtocolFixer
{
	public static PacketBuffer fixCarpetPacket(PacketBuffer buf)
	{
		int prevReaderIndex = buf.readerIndex();
		try
		{
			// try the old v1 protocol
			int id = buf.readVarInt();
			switch (id)
			{
				case CarpetClient.HI:
				case CarpetClient.HELLO:
					buf.readString(64);
					break;
				case CarpetClient.DATA:
					NetworkUtil.readNbt(buf);
					break;
			}
			return buf;  // ok, return the buf directly
		}
		catch (Exception ignored)
		{
		}
		finally
		{
			buf.readerIndex(prevReaderIndex);
		}

		// try protocol v2 from fabric-carpet >= 1.4.114
		NBTTagCompound nbt = Objects.requireNonNull(NetworkUtil.readNbt(buf));
		PacketBuffer newBuf = new PacketBuffer(Unpooled.buffer());

		List<String> keys = Lists.newArrayList(nbt.keySet());
		String id = keys.get(0);
		if (id.equals("69") || id.equals("420"))
		{
			// v1 carpet hi / hello, format: varint (69 or 420) + string
			newBuf.writeVarInt(Integer.parseInt(id));
			newBuf.writeString(nbt.getString(id));
		}
		else
		{
			// v1 carpet data, format: varint + nbt
			newBuf.writeVarInt(CarpetClient.DATA);
			newBuf.writeCompoundTag(nbt);
		}
		return newBuf;
	}
}
