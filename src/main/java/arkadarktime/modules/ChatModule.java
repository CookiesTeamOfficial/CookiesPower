package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.interfaces.BukkitConsole;
import arkadarktime.interfaces.ModuleListener;
import arkadarktime.utils.CookiesComponentBuilder;
import arkadarktime.utils.CustomUtils;
import arkadarktime.utils.FileManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatModule implements ModuleListener, BukkitConsole {
    private final CookiesPower plugin;
    private final CustomUtils customUtils;
    private final Pattern urlPattern = Pattern.compile("https?://(?:[a-zA-Z0-9\\u00a1-\\uffff-]+\\.)+[a-zA-Z\\u00a1-\\uffff]{2,}(?::\\d{2,5})?(?:/\\S*)?");
    public Map<UUID, BaseComponent[]> playersInteraction = new HashMap<>();

    public ChatModule(CookiesPower plugin) {
        this.plugin = plugin;
        this.customUtils = new CustomUtils(plugin);
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.chat.enable")) return;

        ModuleListener.super.enable();
        registerListener(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(event.getPlayer().getUniqueId());
        String message = event.getMessage();

        FileManager chatFileManager = new FileManager(plugin, plugin.getChatFile());
        String formattedMessage = chatFileManager.getColoredString(cookiesPlayer, "chat.format");

        CookiesComponentBuilder builder = new CookiesComponentBuilder(formattedMessage).replace("%message%", message).replace("%displayname%", createPlayerComponent(cookiesPlayer, true)).replace("%name%", createPlayerComponent(cookiesPlayer, false)).replace("%ping%", String.valueOf(cookiesPlayer.getPing())).replace("%world%", cookiesPlayer.getPlayer().getWorld().getName());


        BaseComponent[] finalMessage = processUrls(builder.build(), chatFileManager, cookiesPlayer);

        event.setCancelled(true);
        plugin.getServer().spigot().broadcast(finalMessage);
        plugin.getServer().getConsoleSender().spigot().sendMessage(finalMessage);
    }

    private TextComponent createPlayerComponent(CookiesPlayer cookiesPlayer, boolean isDisplayName) {
        FileManager chatFileManager = new FileManager(plugin, plugin.getChatFile());
        TextComponent playerNameComponent = new TextComponent(isDisplayName ? cookiesPlayer.getDisplayName() : cookiesPlayer.getName());

        if (chatFileManager.getBoolean("chat.player_nick.hover.enable")) {
            String hoverText = chatFileManager.getColoredString(cookiesPlayer, "chat.player_nick.hover.text");
            playerNameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));
        }

        if (chatFileManager.getBoolean("chat.player_nick.click.enable")) {
            List<String> actions = chatFileManager.getColoredStringList(cookiesPlayer, "chat.player_nick.click.actions");
            CookiesComponentBuilder builder = new CookiesComponentBuilder();

            playerNameComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.pluginIdentifierId + ":[sendPlayerInteraction] " + cookiesPlayer.getUniqueId()));

            actions.forEach(action -> processClickActions(action, cookiesPlayer, builder));

            playersInteraction.put(cookiesPlayer.getUniqueId(), builder.build());
        }

        return playerNameComponent;
    }

    private void processClickActions(String action, CookiesPlayer cookiesPlayer, CookiesComponentBuilder builder) {
        action = action.replace("%player%", cookiesPlayer.getName());

        Matcher matcher = Pattern.compile("\\[(.*?)]\\[(.*?)]\\[(.*?)]").matcher(action);
        while (matcher.find()) {
            String text = matcher.group(1);
            String[] commands = matcher.group(2).split(":");
            if (commands.length != 2) {
                Console(ConsoleType.ERROR, "Invalid command format: " + Arrays.toString(commands) + ". Check your chat.yml!");
                return;
            }

            String commandAction = commands[0];
            String command = commands[1];
            String hoverText = matcher.group(3);

            TextComponent textComponent = new TextComponent(text);
            textComponent.setClickEvent(new ClickEvent(customUtils.parseClickEventAction(commandAction), command));
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverText)));

            builder.append(textComponent).append("\n");
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        if (message.startsWith("/" + plugin.pluginIdentifierId)) {
            String[] commands = message.split(":", 2);
            if (commands.length == 2) {
                String[] send = commands[1].split(" ", 2);
                if (send.length == 2 && send[0].equals("[sendPlayerInteraction]")) {
                    UUID playerUUID = UUID.fromString(send[1]);
                    BaseComponent[] interactionComponents = playersInteraction.get(playerUUID);
                    if (interactionComponents != null) {
                        player.spigot().sendMessage(interactionComponents);
                    } else {
                        player.sendMessage("No interaction found for player " + playerUUID);
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    private BaseComponent[] processUrls(BaseComponent[] builtTextArray, FileManager chatFileManager, CookiesPlayer cookiesPlayer) {
        for (int i = 0; i < builtTextArray.length; i++) {
            if (builtTextArray[i] instanceof TextComponent textComponent) {
                Matcher matcher = urlPattern.matcher(textComponent.getText());
                if (matcher.find()) {
                    String url = matcher.group();

                    TextComponent urlComponent = new TextComponent(chatFileManager.getColoredString(cookiesPlayer, "chat.url.message").replace("%url%", url));
                    urlComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                    urlComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(chatFileManager.getColoredString(cookiesPlayer, "chat.url.hover"))));

                    String before = textComponent.getText().substring(0, matcher.start());
                    String after = textComponent.getText().substring(matcher.end());

                    builtTextArray[i] = new TextComponent(before);
                    builtTextArray = insertComponent(builtTextArray, i + 1, urlComponent);
                    builtTextArray = insertComponent(builtTextArray, i + 2, new TextComponent(after));
                }
            }
        }
        return builtTextArray;
    }

    private BaseComponent[] insertComponent(BaseComponent[] array, int index, BaseComponent component) {
        BaseComponent[] newArray = new BaseComponent[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, index);
        newArray[index] = component;
        System.arraycopy(array, index, newArray, index + 1, array.length - index);
        return newArray;
    }
}
