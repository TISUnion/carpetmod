package carpet.utils;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class NbtUtil
{
	public static NBTTagCompound stringList2Nbt(List<String> list)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.putInt("length", list.size());
		for (int i = 0; i < list.size(); i++)
		{
			nbt.putString(String.valueOf(i), list.get(i));
		}
		return nbt;
	}

	public static List<String> nbt2StringList(NBTTagCompound nbt)
	{
		List<String> list = Lists.newArrayList();
		int length = nbt.getInt("length");
		for (int i = 0; i < length; i++)
		{
			list.add(nbt.getString(String.valueOf(i)));
		}
		return list;
	}
}
