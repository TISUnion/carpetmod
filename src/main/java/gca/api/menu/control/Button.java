package gca.api.menu.control;

import gca.api.Function;
import gca.api.tools.text.ComponentTranslate;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class Button {

    private boolean init = false;
    private boolean flag;
    private final IItemProvider onItem;
    private final IItemProvider offItem;
    private final int itemCount;
    private final ITextComponent onText;
    private final ITextComponent offText;
    NBTTagCompound compoundTag = new NBTTagCompound();

    private final List<Function> turnOnFunctions = new ArrayList<>();

    private final List<Function> turnOffFunctions = new ArrayList<>();

    public Button() {
        this(true, Blocks.BARRIER, Blocks.STRUCTURE_VOID);
    }

    public Button(boolean defaultState) {
        this(defaultState, Blocks.BARRIER, Blocks.STRUCTURE_VOID);
    }

    public Button(boolean defaultState, int itemCount) {
        this(defaultState, Blocks.BARRIER, Blocks.STRUCTURE_VOID, itemCount);
    }

    public Button(boolean defaultState, int itemCount, ITextComponent onText, ITextComponent offText) {
        this(defaultState, Blocks.BARRIER, Blocks.STRUCTURE_VOID, itemCount, onText, offText);
    }

    public Button(boolean defaultState, ITextComponent onText, ITextComponent offText) {
        this(defaultState, Blocks.BARRIER, Blocks.STRUCTURE_VOID, 1, onText, offText);
    }

    public Button(boolean defaultState, String key) {
        this(defaultState, Blocks.BARRIER, Blocks.STRUCTURE_VOID, 1,
                ComponentTranslate.trans(key, TextFormatting.GREEN, new Style().setBold(true).setItalic(false), "on"),
                ComponentTranslate.trans(key, TextFormatting.RED, new Style().setBold(true).setItalic(false), "off")
        );
    }

    public Button(boolean defaultState, IItemProvider onItem, IItemProvider offItem) {
        this(defaultState, onItem, offItem, 1);
    }

    public Button(boolean defaultState, IItemProvider onItem, IItemProvider offItem, int itemCount) {
        this(defaultState, onItem, offItem, itemCount,
                ComponentTranslate.trans("on", TextFormatting.GREEN, new Style().setBold(true).setItalic(false)),
                ComponentTranslate.trans("off", TextFormatting.RED, new Style().setBold(true).setItalic(false))
        );
    }

    public Button(boolean defaultState, IItemProvider onItem, IItemProvider offItem, int itemCount, ITextComponent onText, ITextComponent offText) {
        this.flag = defaultState;
        this.onText = onText;
        this.offText = offText;
        this.onItem = onItem;
        this.offItem = offItem;
        this.itemCount = itemCount;
        this.compoundTag.putBoolean("GcaClear", true);
    }

    public void checkButton(IInventory container, int slot) {
        ItemStack onItemStack = new ItemStack(this.onItem, this.itemCount);
        onItemStack.setTag(compoundTag);
        onItemStack.setDisplayName(this.onText);

        ItemStack offItemStack = new ItemStack(this.offItem, this.itemCount);
        offItemStack.setTag(compoundTag.copy());
        offItemStack.setDisplayName(this.offText);

        if (!this.init) {
            updateButton(container, slot, onItemStack, offItemStack);
            this.init = true;
        }

        ItemStack item = container.getStackInSlot(slot);

        if (item.isEmpty()) {
            this.flag = !flag;
            if (flag) {
                runTurnOnFunction();
            } else {
                runTurnOffFunction();
            }
        }

        updateButton(container, slot, onItemStack, offItemStack);
    }

    public void updateButton(IInventory container, int slot, ItemStack onItemStack, ItemStack offItemStack) {
        if (!(
                container.getStackInSlot(slot).getItem() == onItemStack.getItem() ||
                        container.getStackInSlot(slot).getItem() == offItemStack.getItem() ||
                        container.getStackInSlot(slot).isEmpty()
        )) {
            return;
        }
        if (flag) {
            container.setInventorySlotContents(slot,onItemStack);
        } else {
            container.setInventorySlotContents(slot,offItemStack);
        }
    }

    public void addTurnOnFunction(Function function) {
        this.turnOnFunctions.add(function);
    }

    public void addTurnOffFunction(Function function) {
        this.turnOffFunctions.add(function);
    }

    public void turnOnWithoutFunction() {
        this.flag = true;
    }

    public void turnOffWithoutFunction() {
        this.flag = false;
    }

    public void turnOn() {
        this.flag = true;
        runTurnOnFunction();
    }

    public void turnOff() {
        this.flag = false;
        runTurnOffFunction();
    }

    public void runTurnOnFunction() {
        for (Function turnOnFunction : this.turnOnFunctions) {
            turnOnFunction.accept();
        }
    }

    public void runTurnOffFunction() {
        for (Function turnOffFunction : this.turnOffFunctions) {
            turnOffFunction.accept();
        }
    }

    public boolean getFlag() {
        return flag;
    }
}
