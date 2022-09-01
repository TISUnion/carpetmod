package gca.api.menu.control;

import gca.api.tools.text.ComponentTranslate;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class AutoResetButton extends Button {

    public AutoResetButton(String key) {
        super(false,
                ComponentTranslate.trans(key, TextFormatting.WHITE, new Style().setBold(true).setItalic(false)),
                ComponentTranslate.trans(key, TextFormatting.WHITE, new Style().setBold(true).setItalic(false))
        );
        this.addTurnOnFunction(this::turnOffWithoutFunction);
    }
}
