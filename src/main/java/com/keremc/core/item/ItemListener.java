package com.keremc.core.item;

import com.keremc.core.CorePlugin;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if (item != null) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

            if (nmsStack.hasTag()) {

                for (Action action : Action.values()) {
                    String key = "interact_" + action.name();
                    if (nmsStack.getTag().hasKey(key)) {
                        NBTTagString e = (NBTTagString) nmsStack.getTag().get(key);

                        if (e instanceof ExecutableNBTTag) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if (item != null) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

            if (nmsStack != null && nmsStack.hasTag()) {

                String key = "clicked";
                if (nmsStack.getTag().hasKey(key)) {
                    NBTTagString e = (NBTTagString) nmsStack.getTag().get(key);

                    if (e instanceof ExecutableNBTTag) {
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTaskLater(CorePlugin.getInstance(), () -> ((ExecutableNBTTag) e).execute((Player) event.getWhoClicked()), 1L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item != null) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

            if (nmsStack.hasTag()) {

                String key = "interact_" + action.name();
                if (nmsStack.getTag().hasKey(key)) {
                    NBTTagString e = (NBTTagString) nmsStack.getTag().get(key);

                    if (e instanceof ExecutableNBTTag) {

                        ((ExecutableNBTTag) e).execute(player);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
