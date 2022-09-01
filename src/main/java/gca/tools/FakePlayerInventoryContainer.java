package gca.tools;

import carpet.helpers.EntityPlayerActionPack;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import gca.api.menu.CustomMenu;
import gca.api.menu.control.AutoResetButton;
import gca.api.menu.control.Button;
import gca.api.menu.control.RadioList;
import gca.api.tools.text.ComponentTranslate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FakePlayerInventoryContainer extends CustomMenu {

    public final NonNullList<ItemStack> items;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offhand;
    private final NonNullList<ItemStack> buttons = NonNullList.withSize(13, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments;
    private final EntityPlayer player;
    private final EntityPlayerActionPack ap;

    public FakePlayerInventoryContainer(EntityPlayer player) {
        this.player = player;
        this.items = this.player.inventory.mainInventory;
        this.armor = this.player.inventory.armorInventory;
        this.offhand = this.player.inventory.offHandInventory;
        this.ap = ((EntityPlayerMP) this.player).actionPack;
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand, this.buttons);
        this.createButton();
        this.player.inventory.currentItem = 0;
    }

    @Override
    public int getSizeInventory() {
        return this.items.size() + this.armor.size() + this.offhand.size() + this.buttons.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        for (ItemStack itemStack : this.armor) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        for (ItemStack itemStack : this.offhand) {
            if (itemStack.isEmpty()) {
                continue;
            }
            return false;
        }
        return true;
    }

    @Override
    public @Nonnull ItemStack getStackInSlot(int slot) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        if (pair != null) {
            return pair.getFirst().get(pair.getSecond());
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public @Nonnull ItemStack decrStackSize(int slot, int count) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            return ItemStackHelper.getAndSplit(list, slot, count);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @Nonnull ItemStack removeStackFromSlot(int slot) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null && !list.get(slot).isEmpty()) {
            ItemStack itemStack = list.get(slot);
            list.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
        Pair<NonNullList<ItemStack>, Integer> pair = getItemSlot(slot);
        NonNullList<ItemStack> list = null;
        if (pair != null) {
            list = pair.getFirst();
            slot = pair.getSecond();
        }
        if (list != null) {
            list.set(slot, stack);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        if (this.player.removed) {
            return false;
        }
        return !(player.getDistanceSq(this.player) > 64.0);
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (List<ItemStack> list : this.compartments) {
            list.clear();
        }
    }

    public Pair<NonNullList<ItemStack>, Integer> getItemSlot(int slot) {
        if (slot == 0) {
            return new Pair<>(buttons, 0);
        } else if (slot >= 1 && slot <= 4) {
            return new Pair<>(armor, 4 - slot);
        } else if (slot >= 5 && slot <= 6) {
            return new Pair<>(buttons, slot - 4);
        } else if (slot == 7) {
            return new Pair<>(offhand, 0);
        } else if (slot >= 8 && slot <= 17) {
            return new Pair<>(buttons, slot - 5);
        } else if (slot >= 18 && slot <= 44) {
            return new Pair<>(items, slot - 9);
        } else if (slot >= 45 && slot <= 53) {
            return new Pair<>(items, slot - 45);
        } else return null;
    }

    private void createButton() {
        List<Button> hotBarList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ITextComponent hotBarComponent = ComponentTranslate.trans(
                    "Hotbar: %s",
                    TextFormatting.WHITE,
                    new Style().setBold(true).setItalic(false),
                    i + 1
            );
            boolean defaultState = i == 0;
            Button button = new Button(defaultState, i + 1,
                    hotBarComponent,
                    hotBarComponent
            );
            int finalI = i;
            button.addTurnOnFunction(() -> this.player.inventory.currentItem = finalI);
            this.addButton(i + 9, button);
            hotBarList.add(button);
        }
        this.addButtonList(new RadioList(hotBarList, true));

        Button stopAll = new AutoResetButton("Stop all action");
        Button attackInterval14 = new Button(false, "Attack every 14 gt: %s");
        Button attackContinuous = new Button(false, "Attack continuous: %s");
        Button useContinuous = new Button(false, "Use continuous: %s");

        stopAll.addTurnOnFunction(() -> {
            attackInterval14.turnOffWithoutFunction();
            attackContinuous.turnOffWithoutFunction();
            useContinuous.turnOffWithoutFunction();
            ap.stop();
        });

        attackInterval14.addTurnOnFunction(() -> {
            ap.setAttack(14, 0);
            attackContinuous.turnOffWithoutFunction();
        });
        attackInterval14.addTurnOffFunction(ap::attackOnce);

        attackContinuous.addTurnOnFunction(() -> {
            ap.setAttackForever();
            attackInterval14.turnOffWithoutFunction();
        });
        attackContinuous.addTurnOffFunction(ap::attackOnce);

        useContinuous.addTurnOnFunction(ap::setUseForever);
        useContinuous.addTurnOffFunction(ap::useOnce);

        this.addButton(0, stopAll);
        this.addButton(5, attackInterval14);
        this.addButton(6, attackContinuous);
        this.addButton(8, useContinuous);
    }

    @Override
    public @Nonnull ITextComponent getName() {
        return this.player.getName();
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return this.player.getName();
    }
}
