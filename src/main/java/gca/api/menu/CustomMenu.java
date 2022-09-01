package gca.api.menu;

import com.mojang.datafixers.util.Pair;
import gca.api.menu.control.Button;
import gca.api.menu.control.ButtonList;
import net.minecraft.inventory.IInventory;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomMenu implements IInventory {

    public final List<Pair<Integer, Button>> buttons = new ArrayList<>();
    public final List<ButtonList> buttonLists = new ArrayList<>();

    public void tick() {
        this.checkButton();
    }

    public void addButton(int slot, Button button) {
        if (getSizeInventory() < (slot + 1)) {
            return;
        }
        buttons.add(new Pair<>(slot, button));
    }

    public void addButtonList(ButtonList buttonList) {
        this.buttonLists.add(buttonList);
    }

    private void checkButton() {
        for (Pair<Integer, Button> button : buttons) {
            button.getSecond().checkButton(this, button.getFirst());
        }
    }
}

