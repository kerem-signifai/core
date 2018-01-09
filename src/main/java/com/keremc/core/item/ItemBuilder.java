package com.keremc.core.item;

import com.keremc.core.util.Callback;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Predicate;

public class ItemBuilder {
    private Material material;
    private int amount = 1;
    private byte data = 0;
    private String display;
    private String[] lore;
    private String description;
    private String color;

    private ItemMeta meta;

    private Callback<Player> clicked;

    private Map<Action, Callback<Player>> interactables = new HashMap<>();
    private Map<Enchantment, Integer> enchants = new HashMap<>();

    public ItemBuilder withMaterial(Material mat) {
        this.material = mat;
        return this;
    }

    public ItemBuilder withAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder withData(byte data) {
        this.data = data;
        return this;
    }

    public ItemBuilder withDisplayName(String name) {
        this.display = name;
        return this;
    }

    public ItemBuilder withLore(String[] lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder withLore(Collection<String> lore) {
        this.lore = lore.toArray(new String[]{});
        return this;
    }

    public ItemBuilder withMeta(ItemMeta meta) {
        this.meta = meta;
        return this;
    }

    public ItemBuilder withDescription(String desc, String color) {
        this.description = desc;
        this.color = color;
        return this;
    }


    public ItemBuilder interact(Callback<Player> callback) {
        for (Action action : Action.values()) {
            action(action, callback);
        }

        return this;
    }

    public ItemBuilder action(Action action, Callback<Player> callback) {
        interactables.put(action, callback);
        return this;
    }

    public ItemBuilder action(Predicate<Action> pred, Callback<Player> callback) {
        for (Action action : Action.values()) {
            if (pred.test(action)) {
                action(action, callback);
            }
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        enchants.put(enchantment, level);
        return this;
    }

    public ItemBuilder clicked(Callback<Player> clicked) {
        this.clicked = clicked;
        return this;
    }


    public ItemBuilder reset() {
        description = null;
        meta = null;
        lore = null;
        color = null;
        display = null;
        data = 0;
        amount = 1;
        material = null;
        interactables.clear();
        clicked = null;

        return this;
    }


    public ItemStack create() {
        final ItemStack item = new ItemStack(material, amount, data);

        if (meta != null) {
            item.setItemMeta(meta);
        } else {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(display);

            List<String> lore = new ArrayList<>();

            if (lore != null) {
                lore.addAll(lore);
            } else if (description != null) {
                lore.addAll(wrap(description, color));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

        }

        enchants.forEach((e, i) -> item.addUnsafeEnchantment(e, i));


        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbt = nmsStack.getTag();

        if (nbt == null) {
            nbt = new NBTTagCompound();
        }

        for (Map.Entry<Action, Callback<Player>> entry : interactables.entrySet()) {
            nbt.set("interact_" + entry.getKey().name(), new ExecutableNBTTag(entry.getValue()));
        }

        if (clicked != null) {
            nbt.set("clicked", new ExecutableNBTTag(clicked));
        }

        nmsStack.setTag(nbt);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static List<String> wrap(String string, String color, int length) {
        String[] split = string.split(" ");
        string = "";
        ArrayList<String> newString = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            if (string.length() > length || string.endsWith(".") || string.endsWith("!")) {
                newString.add(color + string);
                if (string.endsWith(".") || string.endsWith("!"))
                    newString.add("");
                string = "";
            }
            string += (string.length() == 0 ? "" : " ") + split[i];
        }
        newString.add(color + string);
        return newString;
    }


    public static List<String> wrap(String string, String color) {
        return wrap(string, color, 20);
    }

}
