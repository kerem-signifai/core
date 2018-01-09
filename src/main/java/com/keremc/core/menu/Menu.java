package com.keremc.core.menu;

import com.keremc.core.CorePlugin;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Menu {

    static {
        CorePlugin.getInstance().getServer().getPluginManager().registerEvents(new ButtonListener(), CorePlugin.getInstance());
    }

    @Getter private ConcurrentHashMap<Integer, Button> buttons = new ConcurrentHashMap<>();

    @Getter @Setter private boolean autoUpdate = false;
    @Getter @Setter private boolean updateAfterClick = true;
    @Getter @Setter private boolean placeholder = false;
    @Getter @Setter private boolean noncancellingInventory = false;

    public static Map<String, Menu> currentlyOpenedMenus = new HashMap<>();
    public static Map<String, BukkitRunnable> checkTasks = new HashMap<>();

    public Inventory createInventory(Player player) {
        Inventory inv = Bukkit.createInventory(player, size(player), getTitle(player));

        for (Map.Entry<Integer, Button> buttonEntry : getButtons(player).entrySet()) {
            buttons.put(buttonEntry.getKey(), buttonEntry.getValue());

            ItemStack item = createItemStack(player, buttonEntry.getValue());

            inv.setItem(buttonEntry.getKey(), item);

        }

        if (isPlaceholder()) {
            Button placeholder = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15);

            for (int index = 0; index < size(player); index++) {
                if (getButtons(player).get(index) == null) {
                    buttons.put(index, placeholder);
                    inv.setItem(index, placeholder.getButtonItem(player));

                }
            }
        }

        return inv;
    }

    private ItemStack createItemStack(Player player, Button button) {
        ItemStack item = button.getButtonItem(player);

        if (item.getType() != Material.SKULL_ITEM) {

            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasDisplayName()) {
                meta.setDisplayName(meta.getDisplayName() + "§k§e§r§e§m");
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public void openMenu(final Player player) {

        EntityPlayer ep = ((CraftPlayer) player).getHandle();

        Inventory inv = createInventory(player);

        try {
            Method me = CraftHumanEntity.class.getDeclaredMethod("openCustomInventory", Inventory.class, EntityPlayer.class, String.class);
            me.setAccessible(true);
            me.invoke(player, inv, ep, "minecraft:chest");
            update(player);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void update(final Player player) {

        player.setMetadata("menuUpdated", new FixedMetadataValue(CorePlugin.getInstance(), System.currentTimeMillis() + 200));
        cancelCheck(player);
        currentlyOpenedMenus.put(player.getName(), this);
        onOpen(player);

        BukkitRunnable runnable = new BukkitRunnable() {

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelCheck(player);
                    currentlyOpenedMenus.remove(player.getName());
                }

                if (isAutoUpdate()) {
                    Inventory inv = player.getOpenInventory().getTopInventory();

                    if (inv.getType() == InventoryType.ANVIL) {
                        return;
                    }

                    inv.setContents(createInventory(player).getContents());

                }
            }

        };

        runnable.runTaskTimer(CorePlugin.getInstance(), 10L, 5L);
        checkTasks.put(player.getName(), runnable);
    }

    public static void updateButton(Player player, Button button) {
        Menu menu = currentlyOpenedMenus.get(player.getName());

        if (menu != null) {
            for (Map.Entry<Integer, Button> ent : menu.getButtons().entrySet()) {
                if (ent.getValue() == button) {
                    player.getOpenInventory().getTopInventory().setItem(ent.getKey(), menu.createItemStack(player, button));
                }
            }
        }
    }

    public static void cancelCheck(Player player) {
        if (checkTasks.containsKey(player.getName())) {
            checkTasks.remove(player.getName()).cancel();
        }
    }

    public int size(Player player) {
        int highest = 0;

        for (int buttonValue : getButtons(player).keySet()) {
            if (buttonValue > highest) {
                highest = buttonValue;
            }
        }

        return (int) (Math.ceil((highest + 1) / 9D) * 9D);
    }

    public int getSlot(int x, int y) {
        return ((9 * y) + x);
    }


    public abstract String getTitle(Player player);

    public abstract Map<Integer, Button> getButtons(Player player);

    public void onOpen(Player player) {
    }

    public void onClose(Player player) {
    }

}