package arkadarktime.modules;

import arkadarktime.CookiesPower;
import arkadarktime.enums.CookiesPlayer;
import arkadarktime.interfaces.ModuleListener;
import arkadarktime.utils.CookiesComponentBuilder;
import arkadarktime.utils.FileManager;
import arkadarktime.utils.MinecraftLangManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DeathModule implements ModuleListener {
    private final CookiesPower plugin;

    public DeathModule(CookiesPower plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        if (!plugin.getConfig().getBoolean("modules.death.enable")) return;

        ModuleListener.super.enable();
        registerListener(this, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());

        event.setDeathMessage(null);

        if (langFileManager.getBoolean("death.visible")) {
            BaseComponent[] deathMessage = getDeathMessage(event);
            plugin.getServer().spigot().broadcast(deathMessage);
            plugin.getServer().getConsoleSender().spigot().sendMessage(deathMessage);
        }
    }

    private BaseComponent[] getDeathMessage(PlayerDeathEvent event) {
        FileManager langFileManager = new FileManager(plugin, plugin.getLangFile());
        MinecraftLangManager minecraftLangManager = new MinecraftLangManager(plugin);

        Player player = event.getEntity();
        Player killer = player.getKiller();
        ItemStack itemInHand = null;
        String killerName = killer != null ? killer.getDisplayName() : "";

        String cause = event.getEntity().getLastDamageCause().getCause().toString().toLowerCase();
        String deathType = "natural";
        String dueTo = "";
        String byItem = "";

        EntityDamageByEntityEvent nEvent = null;
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            nEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
            String entityKillerName = nEvent != null ? minecraftLangManager.getEntityName(nEvent.getDamager().getName().toLowerCase()) : "Не найдено";

            if (killer != null) {
                CookiesPlayer cookiesPlayer = plugin.getPlayerDatabaseManager().getCookiesPlayer(player.getUniqueId());
                dueTo = langFileManager.applyColorsAndPlaceholders(cookiesPlayer, langFileManager.getString("death.due-to").replace("%killer%", killerName), false);
            }

            killerName = !entityKillerName.equals("Не найдено") ? entityKillerName : killerName;

            if (nEvent.getDamager() instanceof LivingEntity) {
                deathType = "mob";
                if (killer != null && nEvent.getDamager() instanceof Player) {
                    cause = "player";
                    itemInHand = ((Player) nEvent.getDamager()).getInventory().getItemInMainHand();
                } else {
                    cause = "default";
                }
            }
            byItem = (itemInHand != null && !itemInHand.getType().equals(Material.AIR)) ? langFileManager.getColoredString(player, "death.by-item") : "";
        }

        String deathMessage = langFileManager.getColoredString(player, "death." + deathType + "." + cause).replace("%due_to%", dueTo).replace("%by_item%", byItem);

        CookiesComponentBuilder componentBuilder = new CookiesComponentBuilder(deathMessage);

        TextComponent deadPlayerComponent = new TextComponent(player.getDisplayName());
        String deadPlayerHover = replacePlaceholders(player, player.getDisplayName(), langFileManager.getColoredString(player, "death.hover.player.message"));
        deadPlayerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(deadPlayerHover)));
        deadPlayerComponent.setColor(ChatColor.getByChar(deathMessage.charAt(1)));
        componentBuilder.replace("%player%", deadPlayerComponent);

        if (nEvent.getDamager() instanceof LivingEntity entityKiller) {
            String entityHoverType = entityKiller instanceof Player ? "player" : "entity";
            String entityHover = replacePlaceholders(entityKiller, killerName, langFileManager.getColoredString(player, "death.hover." + entityHoverType + ".message"));
            TextComponent entityComponent = new TextComponent(killerName);
            entityComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(entityHover)));
            entityComponent.setColor(ChatColor.getByChar(deathMessage.charAt(1)));
            componentBuilder.replace("%killer%", entityComponent);
        }

        if (itemInHand != null && !itemInHand.getType().equals(Material.AIR)) {
            Item item = new Item(itemInHand.getType().getKey().toString(), itemInHand.getAmount(), ItemTag.ofNbt(itemInHand.getItemMeta().getAsString()));
            TextComponent itemComponent = new TextComponent(new TranslatableComponent(itemInHand.getTranslationKey()));
            itemComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, item));
            ChatColor itemColor = org.bukkit.ChatColor.getByChar(org.bukkit.ChatColor.getLastColors(deathMessage).charAt(1)).asBungee();
            itemComponent.setColor(itemColor);
            componentBuilder.replace("%item%", itemComponent);
        }

        return componentBuilder.build();
    }

    public String replacePlaceholders(LivingEntity entityKiller, String killerName, String text) {
        String entityType = entityKiller.getType().toString();
        String entityUUID = entityKiller.getUniqueId().toString();
        return text.replace("%name%", killerName).replace("%type%", entityType).replace("%uuid%", entityUUID);
    }
}
