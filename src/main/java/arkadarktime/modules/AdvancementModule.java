package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.interfaces.ModuleListener;
import arkadarktime.utils.CustomUtils;
import arkadarktime.utils.FileManager;
import arkadarktime.utils.CookiesComponentBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.Arrays;

public class AdvancementModule implements ModuleListener {
    private final CookiesPower plugin;
    private final CustomUtils customUtils;

    public AdvancementModule(CookiesPower plugin) {
        this.plugin = plugin;
        this.customUtils = new CustomUtils(plugin);
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

        if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceChat()) {
            String advancementType = advancement.getDisplay().getType().toString().toLowerCase();
            String advancementTitleKey = "advancements." + advancement.getKey().getKey().replace("/", ".") + ".title";
            String advancementDescriptionKey = "advancements." + advancement.getKey().getKey().replace("/", ".") + ".description";

            TranslatableComponent advancementTitle = new TranslatableComponent(advancementTitleKey);
            TranslatableComponent advancementDescription = new TranslatableComponent(advancementDescriptionKey);

            CookiesComponentBuilder advancementText = new CookiesComponentBuilder(langFileManager.getColoredString(player, "advancement." + advancementType + ".text"));
            advancementText.replace("%player%", player.getDisplayName());
            advancementText.replace("%name%", advancementTitle);
            advancementText.replace("%description%", advancementDescription);
            CookiesComponentBuilder advancementHover = new CookiesComponentBuilder(langFileManager.getColoredString(player, "advancement." + advancementType + ".hover"));
            advancementHover.replace("%player%", player.getDisplayName());
            advancementHover.replace("%name%", advancementTitle);
            advancementHover.replace("%description%", advancementDescription);

            BaseComponent[] advancementHoverBuilt = advancementHover.build();

            advancementTitle.setColor(customUtils.getLastColor(Arrays.toString(advancementHoverBuilt)));
            advancementTitle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(advancementHoverBuilt)));

            BaseComponent[] advancementTextBuilt = advancementText.build();
            plugin.getServer().spigot().broadcast(advancementTextBuilt);
            plugin.getServer().getConsoleSender().spigot().sendMessage(advancementTextBuilt);
        }
    }
}
