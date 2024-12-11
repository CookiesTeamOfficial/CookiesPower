package arkadarktime.utils;

import arkadarktime.CookiesPower;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager implements Listener {
    private final CookiesPower plugin;
    private final CustomInventoryHolder customInventoryHolder;

    public InventoryManager(CookiesPower plugin) {
        this.plugin = plugin;
        this.customInventoryHolder = new CustomInventoryHolder(plugin.getPluginIdentifierId());
    }


    /**
     * Creates an inventory with the specified title and size.
     *
     * @param title The title of the inventory.
     * @param size  The size of the inventory.
     * @return The created inventory.
     */
    public Inventory createInventory(String title, int size) {
        return Bukkit.createInventory(customInventoryHolder, size, title);
    }

    /**
     * Adds an item to the inventory at the specified slot.
     *
     * @param inventory   The inventory to which the item will be added.
     * @param material    The material of the item.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @param slot        The slot in the inventory where the item will be placed.
     */
    public void addItemToInventory(Inventory inventory, Material material, String displayName, List<String> lore, int slot) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }

        inventory.setItem(slot, item);
    }

    /**
     * Adds an item to the inventory in the next available slot.
     *
     * @param inventory   The inventory to which the item will be added.
     * @param material    The material of the item.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     */
    public void addItemToInventory(Inventory inventory, Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }

        inventory.addItem(item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof CustomInventoryHolder inventoryHolder) {
            if (inventoryHolder.pluginIdentifierId().equals(plugin.getPluginIdentifierId())) {
                ArrayList<String> notNeedCancelledInventoryList = new ArrayList<>();
                notNeedCancelledInventoryList.add("");
                if (!notNeedCancelledInventoryList.contains(event.getView().getTitle())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}