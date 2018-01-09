package com.keremc.core.menu;

import com.keremc.core.CorePlugin;
import com.keremc.core.menu.pagination.PaginatedMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class ButtonListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onButtonPress(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        Menu openMenu = Menu.currentlyOpenedMenus.get(player.getName());

        if (openMenu != null) {

            if (event.getSlot() != event.getRawSlot()) {
                if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
                    event.setCancelled(true);

                    if (openMenu.isNoncancellingInventory()) {

                        if (event.getCurrentItem() != null) {
                            player.getOpenInventory().getTopInventory().addItem(event.getCurrentItem());
                            event.setCurrentItem(null);
                        }
                    }
                }
                return;

            }
            if (openMenu.getButtons().containsKey(event.getSlot())) {
                Button button = openMenu.getButtons().get(event.getSlot());

                boolean cancel = button.shouldCancel(player, event.getSlot(), event.getClick());

                if (!cancel && (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
                    event.setCancelled(true);
                    if (event.getCurrentItem() != null) {
                        player.getInventory().addItem(event.getCurrentItem());
                    }
                } else {
                    event.setCancelled(cancel);

                }


                button.clicked(player, event.getSlot(), event.getClick(), event.getHotbarButton());

                if (Menu.currentlyOpenedMenus.containsKey(player.getName())) {
                    Menu newMenu = Menu.currentlyOpenedMenus.get(player.getName());

                    if (newMenu == openMenu && newMenu.isUpdateAfterClick()) {
                        newMenu.openMenu(player);
                    }
                }

                if (event.isCancelled()) {
                    Bukkit.getScheduler().runTaskLater(CorePlugin.getInstance(), () -> player.updateInventory(), 1L);
                }
            } else {
                if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
                    event.setCancelled(true);

                    if (openMenu.isNoncancellingInventory()) {

                        if (event.getCurrentItem() != null) {
                            player.getOpenInventory().getTopInventory().addItem(event.getCurrentItem());
                            event.setCurrentItem(null);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        Menu openMenu = Menu.currentlyOpenedMenus.get(player.getName());

        if (openMenu != null) {
            openMenu.onClose(player);

            Menu.cancelCheck(player);
            Menu.currentlyOpenedMenus.remove(player.getName());

            if (openMenu instanceof PaginatedMenu) {
                return;
            }
        }

        player.setMetadata("scanglitch", new FixedMetadataValue(CorePlugin.getInstance(), true));


        /*if (player.hasMetadata("menuUpdated") && player.getMetadata("menuUpdated").get(0).asLong() > System.currentTimeMillis()) {
            player.removeMetadata("menuUpdated", Core.getInstance());

            player.closeInventory();
            //TODO: Add suckmyass potatos
        }*/
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("scanglitch")) {
            player.removeMetadata("scanglitch", CorePlugin.getInstance());

            for (ItemStack it : player.getInventory().getContents()) {
                if (it != null) {
                    ItemMeta meta = it.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {

                        if (meta.getDisplayName().contains("§k§e§r§e§m")) {
                            player.getInventory().remove(it);
                        }
                    }
                }
            }
        }
    }

}