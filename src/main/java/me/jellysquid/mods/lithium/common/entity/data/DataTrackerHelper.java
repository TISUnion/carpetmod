package me.jellysquid.mods.lithium.common.entity.data;

import io.netty.handler.codec.EncoderException;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import java.util.Iterator;

public class DataTrackerHelper {
    // [VanillaCopy] DataTracker#toPacketByteBuf(PacketByteBuf)
    public static void toPacketByteBuf(PacketBuffer buf, Iterator<EntityDataManager.DataEntry<?>> entries) {
        while (entries.hasNext()) {
            writeEntryToPacket(buf, entries.next());
        }

        buf.writeByte(255);
    }

    // [VanillaCopy] DataTracker#writeEntryToPacket<T>(PacketByteBuf, DataTracker.Entry<T>)
    public static <T> void writeEntryToPacket(PacketBuffer buf, EntityDataManager.DataEntry<T> entry) {
        DataParameter<T> data = entry.getKey();

        int id = DataSerializers.getSerializerId(data.getSerializer());

        if (id < 0) {
            throw new EncoderException("Unknown serializer type " + data.getSerializer());
        } else {
            buf.writeByte(data.getId());
            buf.writeVarInt(id);

            data.getSerializer().write(buf, entry.getValue());
        }
    }
}
