package gca.api.tools.text;

import carpet.settings.CarpetSettings;
import carpet.utils.Translations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class ComponentTranslate {

    private final Map<String, String> lang = getTranslations(CarpetSettings.language);

    public static ITextComponent trans(String key, Object... args) {
        return trans(key, TextFormatting.WHITE, args);
    }

    public static ITextComponent trans(String key, TextFormatting color, Object... args) {
        return trans(key, color, new Style(), args);
    }

    public static ITextComponent trans(String key, TextFormatting color, Style style, Object... args) {
        ComponentTranslate componentTranslate = new ComponentTranslate();
        if (componentTranslate.lang != null) {
            try {
                return new TextComponentString(
                        String.format(componentTranslate.lang.get(key), args)
                ).setStyle(style.setColor(color));
            } catch (ClassCastException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return new TextComponentString(String.format(key, args));
    }

    public static Map<String, String> getTranslations(String lang) {
        String dataJSON;
        try {
            dataJSON = IOUtils.toString(
                    Objects.requireNonNull(
                            Translations.class
                                    .getClassLoader()
                                    .getResourceAsStream(String.format("assets/gca/lang/%s.json", lang))
                    ),
                    StandardCharsets.UTF_8
            );
        } catch (NullPointerException | IOException e) {
            try {
                dataJSON = IOUtils.toString(
                        Objects.requireNonNull(
                                Translations.class
                                        .getClassLoader()
                                        .getResourceAsStream("assets/gca/lang/en_us.json")
                        ),
                        StandardCharsets.UTF_8
                );
            } catch (NullPointerException | IOException ex) {
                return null;
            }
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.fromJson(dataJSON, new TypeToken<Map<String, String>>() {
        }.getType());
    }
}
