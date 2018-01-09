package com.keremc.core.scoreboard;

import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@NoArgsConstructor
public class TitleGetter {

    private String defaultTitle;

    public TitleGetter(String defaultTitle) {
        this.defaultTitle = ChatColor.translateAlternateColorCodes('&', defaultTitle);
    }

    /**
     * Gets the title of the scoreboard. If defaultTitle is set,
     * and this method is not override, defaultTitle will be used.
     * This method should be overrode if the title of the scoreboard is NOT static.
     *
     * @param player The player to fetch the title for.
     * @return The String title this scoreboard should have (for the given player)
     */
    public String getTitle(Player player) {
        return (defaultTitle);
    }

}