package arkadarktime.utils;

import arkadarktime.interfaces.BukkitConsole;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Builder class to construct and format messages with placeholders and components.
 */
public class CookiesComponentBuilder implements BukkitConsole {
    private final String message;
    private final LinkedHashMap<String, Replace> replacements = new LinkedHashMap<>();
    private final ComponentBuilder componentBuilder = new ComponentBuilder("");

    /**
     * Default constructor. Initializes with an empty message.
     */
    public CookiesComponentBuilder() {
        this.message = "";
    }

    /**
     * Constructor that initializes the builder with the given message.
     *
     * @param message The message to initialize the builder with.
     */
    public CookiesComponentBuilder(String message) {
        this.message = message;
    }

    /**
     * Constructor that initializes the builder with the given BaseComponent array.
     *
     * @param components The BaseComponent array to initialize the builder with.
     */
    public CookiesComponentBuilder(BaseComponent[] components) {
        this.message = "";
        this.componentBuilder.append(components);
    }

    /**
     * Appends text or components to the builder.
     *
     * @param text The text or BaseComponent to append.
     * @return The current instance of CookiesComponentBuilder.
     */
    public CookiesComponentBuilder append(@NotNull Object text) {
        if (text instanceof String) {
            componentBuilder.append(new TextComponent((String) text), ComponentBuilder.FormatRetention.NONE);
        } else if (text instanceof BaseComponent) {
            componentBuilder.append((BaseComponent) text, ComponentBuilder.FormatRetention.NONE);
        }
        return this;
    }

    /**
     * Replaces placeholders in the message with custom actions or components.
     *
     * @param placeholder The placeholder to replace.
     * @param replacement The replacement (String, TextComponent, or BaseComponent).
     * @return The current instance of CookiesComponentBuilder.
     */
    public CookiesComponentBuilder replace(@NotNull String placeholder, @NotNull Object replacement) {
        replacements.put(placeholder, (componentBuilder, color) -> {
            if (replacement instanceof String) {
                componentBuilder.append(new TextComponent(color + replacement), ComponentBuilder.FormatRetention.NONE);
            } else if (replacement instanceof TextComponent) {
                componentBuilder.append((TextComponent) replacement, ComponentBuilder.FormatRetention.NONE);
            } else if (replacement instanceof BaseComponent) {
                componentBuilder.append((BaseComponent) replacement, ComponentBuilder.FormatRetention.NONE);
            }
        });
        return this;
    }

    /**
     * Builds the final array of BaseComponent objects with the applied formatting.
     *
     * @return The array of BaseComponent objects ready for display.
     */
    public BaseComponent[] build() {
        List<String> strings = splitLine(message, replacements.keySet());
        String mainColor = "";

        for (String placeholder : strings) {
            Replace replace = replacements.get(placeholder);

            if (replace == null) {
                componentBuilder.append(new TextComponent(mainColor + placeholder), ComponentBuilder.FormatRetention.NONE);
                mainColor = ChatColor.getLastColors(mainColor + placeholder);
                continue;
            }

            replace.action(componentBuilder, mainColor);
        }

        return componentBuilder.create();
    }

    /**
     * Splits the given line into parts based on the placeholders to apply replacements.
     *
     * @param line         The line to split.
     * @param placeholders The set of placeholders to split by.
     * @return A list of strings split by placeholders.
     */
    private List<String> splitLine(@NotNull String line, @NotNull Set<String> placeholders) {
        List<String> split = new ArrayList<>(Collections.singletonList(line));
        for (String placeholder : placeholders) {
            List<String> newSplit = new ArrayList<>();
            for (String part : split) {
                newSplit.addAll(Arrays.asList(part.split("(?=" + placeholder + ")|(?<=" + placeholder + ")")));
            }
            split = newSplit;
        }
        return split;
    }

    /**
     * Functional interface for replacing a placeholder with an action.
     */
    @FunctionalInterface
    public interface Replace {
        /**
         * Action to be performed when the placeholder is found.
         *
         * @param componentBuilder The ComponentBuilder to append to.
         * @param color            The color to apply.
         */
        void action(ComponentBuilder componentBuilder, String color);
    }
}