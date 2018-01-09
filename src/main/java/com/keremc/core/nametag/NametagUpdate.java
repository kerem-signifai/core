package com.keremc.core.nametag;

import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * A nametag update that is queued to happen.
 * Commonly the update is queued from a sync. thread.
 */
final class NametagUpdate {

    @Getter private String toRefresh;
    @Getter private String refreshFor;

    /**
     * Refreshes one player for all players online.
     *
     * @param toRefresh The player to refresh.
     */
    public NametagUpdate(Player toRefresh) {
        this.toRefresh = toRefresh.getName();
    }

    /**
     * Refreshes one player for another player only.
     *
     * @param toRefresh  The player to refresh.
     * @param refreshFor The player to refresh toRefresh for.
     */
    public NametagUpdate(Player toRefresh, Player refreshFor) {
        this.toRefresh = toRefresh.getName();
        this.refreshFor = refreshFor.getName();
    }

}