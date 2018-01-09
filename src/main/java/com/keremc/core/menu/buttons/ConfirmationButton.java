package com.keremc.core.menu.buttons;

import com.keremc.core.menu.Button;
import com.keremc.core.util.Callback;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ConfirmationButton extends Button {
    private boolean confirm;
    private Callback<Boolean> callback;
    private boolean closeAfterResponse;

    @Override
    public void clicked(Player player, int i, ClickType clickType, int hb) {
        if (confirm) {
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20f, 0.1f);
        } else {
            player.playSound(player.getLocation(), Sound.DIG_GRAVEL, 20f, 0.1F);
        }

        if (closeAfterResponse) {
            player.closeInventory();
        }

        callback.callback(confirm);
    }

    @Override
    public String getName(Player player) {
        return confirm ? "§aConfirm" : "§cCancel";
    }

    @Override
    public List<String> getDescription(Player player) {
        return new ArrayList<>();
    }

    @Override
    public byte getDamageValue(Player player) {
        return confirm ? (byte) 5 : (byte) 14;
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.WOOL;
    }
}
