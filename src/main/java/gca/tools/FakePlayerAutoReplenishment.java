package gca.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class FakePlayerAutoReplenishment {

    public static void autoReplenishment(EntityPlayer fakePlayer) {
        NonNullList<ItemStack> itemStackList = fakePlayer.inventory.mainInventory;
        replenishment(fakePlayer.getHeldItemMainhand(), itemStackList);
        replenishment(fakePlayer.getHeldItemOffhand(), itemStackList);
    }

    public static void replenishment(ItemStack itemStack, NonNullList<ItemStack> itemStackList) {
        int count = itemStack.getMaxStackSize() / 2;
        if (itemStack.getCount() <= 8 && count > 8) {
            for (ItemStack itemStack1 : itemStackList) {
                if (itemStack1 == ItemStack.EMPTY || itemStack1 == itemStack) continue;
                if (isSameItemSameTags(itemStack1, itemStack)) {
                    if (itemStack1.getCount() > count) {
                        itemStack.setCount(itemStack.getCount() + count);
                        itemStack1.setCount(itemStack1.getCount() - count);
                    } else {
                        itemStack.setCount(itemStack.getCount() + itemStack1.getCount());
                        itemStack1.setCount(0);
                    }
                    break;
                }
            }
        }
    }

    public static boolean isSameItemSameTags(ItemStack stack, ItemStack other) {
        return stack.isItemEqual(other) && ItemStack.areItemStackTagsEqual(stack, other);
    }
}
