package com.keremc.core.menu.pagination;

import com.keremc.core.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PageButton extends Button {
    private int mod;
    private PaginatedMenu menu;

    @Override
    public void clicked(Player player, int i, ClickType clickType, int hb) {
        if (clickType == ClickType.RIGHT) {
            new ViewAllPagesMenu(menu).openMenu(player);
            playNeutral(player);
        } else {
            if (hasNext(player)) {
                menu.modPage(player, mod);
                Button.playNeutral(player);

            } else {
                Button.playFail(player);
            }
        }
    }

    private boolean hasNext(Player player) {
        int pg = menu.getPage() + mod;
        return pg > 0 && menu.getPages(player) >= pg;
    }

    @Override
    public String getName(Player player) {
        if (!hasNext(player)) {
            return mod > 0 ? "§7Last page" : "§7First page";
        }

        String str = "(§e" + (menu.getPage() + mod) + "/§e" + menu.getPages(player) + "§a)";

        return mod > 0 ? "§aNext page" : "§cPrev page";
    }

    @Override
    public List<String> getDescription(Player player) {
        return new ArrayList<String>() {
            {
                add("");
                add("§eRight click to");
                add("§ejump to a page");
            }
        };
    }

    @Override
    public byte getDamageValue(Player player) {
        return hasNext(player) ? (byte) 11 : (byte) 7;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.CARPET;
    }
}
