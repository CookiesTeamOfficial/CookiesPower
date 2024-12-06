package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.ModuleListener;
import arkadarktime.utils.CookiesComponentBuilder;
import arkadarktime.utils.FileManager;
import arkadarktime.utils.MinecraftLangManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AdvancementModule implements ModuleListener {
    private final CookiesPower plugin;

    public AdvancementModule(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.advancements.enable")) return;

        ModuleListener.super.enable();
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false));
        registerListener(this, plugin);
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();
        boolean advancementModuleEnable = plugin.getConfig().getBoolean("modules.advancements.enable");

        if (advancement.getDisplay() != null && advancementModuleEnable) {
            MinecraftLangManager minecraftLangManager = new MinecraftLangManager(plugin);
            String advancementKey = advancement.getKey().getKey().replace("/", ".");
            String minecraftAdvancementTitle = minecraftLangManager.getAdvancementTitle(advancementKey);
            String minecraftAdvancementDescription = minecraftLangManager.getAdvancementDescription(advancementKey);

            String advancementType = advancement.getDisplay().getType().toString().toLowerCase();
            String advancementText = langFileManager.getColoredString(player, "advancement." + advancementType + ".text").replace("%player%", player.getDisplayName()).replace("%description%", minecraftAdvancementDescription.equals("Не найдено") ? advancement.getDisplay().getDescription() : minecraftAdvancementDescription);
            String advancementHover = langFileManager.getColoredString(player, "advancement." + advancementType + ".hover").replace("%player%", player.getDisplayName()).replace("%name%", minecraftAdvancementTitle.equals("Не найдено") ? advancement.getDisplay().getTitle() : minecraftAdvancementTitle).replace("%description%", minecraftAdvancementDescription.equals("Не найдено") ? advancement.getDisplay().getDescription() : minecraftAdvancementDescription);

            CookiesComponentBuilder componentBuilder = new CookiesComponentBuilder(advancementText);

            TextComponent nameComponent = new TextComponent(minecraftAdvancementTitle);
            nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(advancementHover)));
            ChatColor nameColor = org.bukkit.ChatColor.getByChar(org.bukkit.ChatColor.getLastColors(advancementHover).charAt(1)).asBungee();
            nameComponent.setColor(nameColor);

            componentBuilder.replace("%name%", nameComponent);

            BaseComponent[] advancementMsg = componentBuilder.build();
            plugin.getServer().spigot().broadcast(advancementMsg);
            plugin.getServer().getConsoleSender().spigot().sendMessage(advancementMsg);
        }
    }
}
