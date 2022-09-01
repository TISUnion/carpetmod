package gca.tools;

import carpet.helpers.EntityPlayerActionPack;
import com.mojang.datafixers.util.Pair;
import gca.GcaExtension;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class FakePlayerAutoFish {

    public static void autoFish(EntityPlayer player) {
        EntityPlayerActionPack ap = ((EntityPlayerMP) player).actionPack;
        long l = player.world.getGameTime();
        GcaExtension.planFunction.add(new Pair<>(l + 5, ap::useOnce));
        GcaExtension.planFunction.add(new Pair<>(l + 15, ap::useOnce));
    }
}
