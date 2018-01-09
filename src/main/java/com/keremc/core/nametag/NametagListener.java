package com.keremc.core.nametag;

import com.keremc.core.CorePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

final class NametagListener implements Listener {

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            Thread.sleep(2000);
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (BMNametagHandler.isInitiated()) {
            event.getPlayer().setMetadata("keremTag-LoggedIn", new FixedMetadataValue(CorePlugin.getInstance(), true));
            BMNametagHandler.initiatePlayer(event.getPlayer());
            BMNametagHandler.reloadPlayer(event.getPlayer());
            BMNametagHandler.reloadOthersFor(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().removeMetadata("keremTag-LoggedIn", CorePlugin.getInstance());
        BMNametagHandler.getTeamMap().remove(event.getPlayer().getName());
    }

}