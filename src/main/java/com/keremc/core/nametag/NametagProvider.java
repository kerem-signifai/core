package com.keremc.core.nametag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * A class that can 'provide' nametags for players.
 */
@AllArgsConstructor
public abstract class NametagProvider {

    @Getter private String name;
    @Getter private int weight;

    /**
     * Takes in the player to refresh and who to refresh that player for,
     * and returns the prefix and suffix they should be given.
     *
     * @param toRefresh  The player whose nametag is getting refreshed.
     * @param refreshFor The player who this nametag chage is going to be visible to.
     * @return The nametag that refreshFor should see above toRefresh's head.
     */
    public abstract NametagInfo fetchNametag(Player toRefresh, Player refreshFor);

    /**
     * Wrapper method to create a NametagInfo object.
     *
     * @param prefix The prefix the nametag has.
     * @param suffix The suffix the nametag has.
     * @return The created NametagInfo object.
     */
    public static final NametagInfo createNametag(String prefix, String suffix) {
        return (BMNametagHandler.getOrCreate(prefix, suffix));
    }

    protected static final class DefaultNametagProvider extends NametagProvider {

        public DefaultNametagProvider() {
            super("Default Provider", 0);
        }

        @Override
        public NametagInfo fetchNametag(Player toRefresh, Player refreshFor) {
            return (createNametag("", ""));
        }

    }

}