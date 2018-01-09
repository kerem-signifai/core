package com.keremc.core.menu;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public abstract class Button {

    public static Button placeholder(final Material material, final byte data, String... title) {
        return (new Button() {

            public String getName(Player player) {
                return (title != null ? StringUtils.join(title) : " ");
            }

            public List<String> getDescription(Player player) {
                return (ImmutableList.of());
            }

            public Material getMaterial(Player player) {
                return (material);
            }

            public byte getDamageValue(Player player) {
                return (data);
            }

            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            }

            public ItemStack getButtonItem(Player player) {
                ItemStack it = new ItemStack(getMaterial(player), 1, getDamageValue(player));

                ItemMeta meta = it.getItemMeta();
                meta.setDisplayName(getName(player));

                it.setItemMeta(meta);
                return it;
            }
        });
    }

    public abstract String getName(Player player);

    public abstract List<String> getDescription(Player player);

    public abstract Material getMaterial(Player player);

    public abstract byte getDamageValue(Player player);

    public abstract void clicked(Player player, int slot, ClickType clickType, int hotbarButton);

    public boolean shouldCancel(Player player, int slot, ClickType clickType) {
        return (true);
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return null;
    }

    public int getAmount(Player player) {
        return 1;
    }

    public ItemStack getButtonItem(Player player) {
        ItemStack buttonItem = new ItemStack(getMaterial(player), getAmount(player), getDamageValue(player));
        ItemMeta meta = buttonItem.getItemMeta();

        if (getName(player) != null) {
            meta.setDisplayName(getName(player));
        }

        List<String> description = getDescription(player);

        if (description != null) {
            meta.setLore(description);
        }

        buttonItem.setItemMeta(meta);

        if (getEnchantments() != null) {
            for (Map.Entry<Enchantment, Integer> ench : getEnchantments().entrySet()) {
                if (ench.getValue() > 0) {
                    buttonItem.addUnsafeEnchantment(ench.getKey(), ench.getValue());
                } else {
                    buttonItem.removeEnchantment(ench.getKey());
                }
            }
        }

        Attributes attr = new Attributes(buttonItem);

        attr.clear();

        return (attr.getStack());
    }


    public static void playFail(Player player) {
        player.playSound(player.getLocation(), Sound.DIG_GRASS, 20F, 0.1F);

    }

    public static void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
    }

    public static void playNeutral(Player player) {
        player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);
    }

}