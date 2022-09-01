package gca.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class FakePlayerAutoReplaceTool {

    public static void autoReplaceTool(EntityPlayer fakePlayer) {
        ItemStack mainHand = fakePlayer.getHeldItemMainhand();
        ItemStack offHand = fakePlayer.getHeldItemOffhand();
        if (!mainHand.isEmpty() && (mainHand.getMaxDamage() - mainHand.getDamage()) <= 10)
            replaceTool(EntityEquipmentSlot.MAINHAND, fakePlayer);
        if (!offHand.isEmpty() && (offHand.getMaxDamage() - offHand.getDamage()) <= 10)
            replaceTool(EntityEquipmentSlot.OFFHAND, fakePlayer);
    }

    public static void replaceTool(EntityEquipmentSlot slot, EntityPlayer fakePlayer) {
        ItemStack itemStack = fakePlayer.getItemStackFromSlot(slot);
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack1 = fakePlayer.inventory.getStackInSlot(i);
            if (itemStack1 == ItemStack.EMPTY || itemStack1 == itemStack) continue;
            if (itemStack1.getItem().getClass() == itemStack.getItem().getClass() && (itemStack1.getMaxDamage() - itemStack1.getDamage()) > 10) {
                ItemStack itemStack2 = itemStack1.copy();
                fakePlayer.inventory.setInventorySlotContents(i, itemStack);
                fakePlayer.setItemStackToSlot(slot, itemStack2);
                break;
            }
        }
    }
}
