package carpet.settings;

import carpet.CarpetServer;
import carpet.utils.Messenger;
import carpet.utils.Translations;
import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static carpet.utils.Translations.tr;

public final class ParsedRule<T> implements Comparable<ParsedRule> {
    public final Field field;
    public final String name;
    public final String description;
    public final ImmutableList<String> extraInfo;
    public final ImmutableList<String> categories;
    public final ImmutableList<String> options;
    public boolean isStrict;
    public final Class<T> type;
    public final List<Validator<T>> validators;
    public final T defaultValue;
    public final String defaultAsString;

    ParsedRule(Field field, Rule rule)
    {
        this.field = field;
        this.name = rule.name().isEmpty() ? field.getName() : rule.name();
        this.type = (Class<T>) field.getType();
        this.description = rule.desc();
        this.isStrict = rule.strict();
        this.extraInfo = ImmutableList.copyOf(rule.extra());
        this.categories = ImmutableList.copyOf(rule.category());
        this.validators = new ArrayList<>();
        for (Class v : rule.validate())
            this.validators.add((Validator<T>) callConstructor(v));
        this.defaultValue = get();
        this.defaultAsString = convertToString(this.defaultValue);
        if (this.type == boolean.class)
        {
            this.isStrict = true; // it has no sense otherwise
            this.options = ImmutableList.of("true", "false");
        }
        else if (this.type == String.class && categories.contains(RuleCategory.COMMAND))
        {
            this.options = ImmutableList.of("true", "false", "ops", "0", "1", "2", "3", "4");
        }
        else if (this.type.isEnum())
        {
            this.isStrict = true; // it has no sense otherwise
            this.options = Arrays.stream(this.type.getEnumConstants()).map(e -> ((Enum) e).name().toLowerCase(Locale.ROOT)).collect(ImmutableList.toImmutableList());
        }
        else
        {
            this.options = ImmutableList.copyOf(rule.options());
        }
        if (isStrict)
            this.validators.add(callConstructor(Validator._STRICT.class));
    }

    private <T> T callConstructor(Class<T> cls)
    {
        try
        {
            Constructor<T> constr = cls.getDeclaredConstructor();
            constr.setAccessible(true);
            return constr.newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
    }

    public ParsedRule<T> set(CommandSource source, String value)
    {
        if (CarpetServer.settingsManager != null && CarpetServer.settingsManager.locked)
            return null;
        try
        {
            if (type == String.class)
            {
                return set(source, (T) value, value);
            }
            else if (type == boolean.class)
            {
                return set(source, (T) (Object) Boolean.parseBoolean(value), value);
            }
            else if (type == int.class)
            {
                return set(source, (T) (Object) Integer.parseInt(value), value);
            }
            else if (type == float.class)
            {
                return set(source, (T) (Object) Float.parseFloat(value), value);
            }
            else if (type == double.class)
            {
                return set(source, (T) (Object) Double.parseDouble(value), value);
            }
            else if (type.isEnum())
            {
                String ucValue = value.toUpperCase(Locale.ROOT);
                return set(source, (T) (Object) Enum.valueOf((Class<? extends Enum>) type, ucValue), value);
            }
            else
            {
                Messenger.m(source, "r Unknown type " + type.getSimpleName());
                return null;
            }
        }
        catch (Exception e)
        {
            CarpetServer.LOGGER.warn(String.format("Fail to parse rule %s for value %s: %s", this.name, value, e));
            return null;
        }
    }

    ParsedRule<T> set(CommandSource source, T value, String stringValue)
    {
        try
        {
            for (Validator<T> validator : this.validators)
            {
                value = validator.validate(source, this, value, stringValue);
                if (value == null)
                {
                    Messenger.m(source, "r Wrong value for " + name + ": " + stringValue);
                    if (validator.description() != null)
                        Messenger.m(source, "r " + validator.description());
                    return null;
                }
            }
            if (!value.equals(get()))
            {
                this.field.set(null, value);
                SettingsManager.notifyRuleChanged(source, this, stringValue);
                if (this.categories.contains(RuleCategory.COMMAND) && CarpetServer.settingsManager != null)
                {
                    CarpetServer.settingsManager.notifyPlayersCommandsChanged();
                }
            }
        }
        catch (IllegalAccessException e)
        {
            Messenger.m(source, "r Unable to access setting for  "+name);
            return null;
        }
        return this;
    }

    public T get()
    {
        try
        {
            return (T) this.field.get(null);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public String getAsString()
    {
        return convertToString(get());
    }

    public boolean getBoolValue()
    {
        if (type == boolean.class) return (Boolean) get();
        if (type.isAssignableFrom(Number.class)) return ((Number) get()).doubleValue() > 0;
        return false;
    }

    public boolean isDefault()
    {
        return defaultValue.equals(get());
    }

    public void resetToDefault(CommandSource source)
    {
        set(source, defaultValue, defaultAsString);
    }


    private static String convertToString(Object value)
    {
        if (value instanceof Enum) return ((Enum) value).name().toLowerCase(Locale.ROOT);
        return value.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj.getClass() == ParsedRule.class && ((ParsedRule) obj).name.equals(this.name);
    }

    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }

    @Override
    public int compareTo(ParsedRule o)
    {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString()
    {
        return this.name + ": " + getAsString();
    }

    private String translationKey()
    {
        return String.format("rule.%s.name", name);
    }

    public String translatedName(){
        String key = translationKey();
        return Translations.hasTranslation(key) ? tr(key) + String.format(" (%s)", name): name;
    }

    public String translatedDescription()
    {
        return tr(String.format("rule.%s.desc", (name)), description);
    }

    public List<String> translatedExtras()
    {
        if (!Translations.hasTranslations()) return extraInfo;
        String keyBase = String.format("rule.%s.extra.", name);
        List<String> extras = new ArrayList<>();
        int i = 0;
        while (Translations.hasTranslation(keyBase+i))
        {
            extras.add(Translations.tr(keyBase+i));
            i++;
        }
        return (extras.isEmpty()) ? extraInfo : extras;
    }
}
