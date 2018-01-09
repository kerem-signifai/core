package com.keremc.core.menu.pagination;

import com.keremc.core.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class JumpToPageButton extends Button {
    private int page;
    private PaginatedMenu menu;
    private boolean current;

    @Override
    public String getName(Player player) {
        return "§ePage " + page;
    }

    @Override
    public List<String> getDescription(Player player) {
        if (current) {
            List<String> lore = new ArrayList<>();

            lore.add("");
            lore.add("§aCurrent page");

            return lore;
        }
        return null;
    }

    @Override
    public Material getMaterial(Player player) {
        return current ? Material.ENCHANTED_BOOK : Material.BOOK;
    }

    @Override
    public int getAmount(Player player) {
        return page;
    }

    @Override
    public byte getDamageValue(Player player) {
        return 0;
    }

    @Override
    public void clicked(Player player, int i, ClickType clickType, int hb) {
        menu.modPage(player, page - menu.getPage());
        Button.playNeutral(player);
    }
}
