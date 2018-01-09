package com.keremc.core.scoreboard;

import com.google.common.base.Preconditions;
import com.keremc.core.CorePlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BMScoreboardHandler {

    private static Map<String, BMScoreboard> boards = new ConcurrentHashMap<>();
    @Getter @Setter private static ScoreboardConfiguration configuration = null;
    private static boolean initiated = false;
    @Getter @Setter private static int updateInterval = 2; // In ticks

    // Static class -- cannot be created.
    private BMScoreboardHandler() {
    }

    /**
     * Initiates the scoreboard handler.
     * This can only be called once, and is called automatically when Core enables.
     */
    public static void init() {
        // Only allow the CoreScoreboardHandler to be initiated once.
        // Note the '!' in the .checkState call.
        Preconditions.checkState(!initiated);
        initiated = true;

        (new ScoreboardThread()).start();
        CorePlugin.getInstance().getServer().getPluginManager().registerEvents(new ScoreboardListener(), CorePlugin.getInstance());
    }

    /**
     * Creates and registers a new scoreboard. Only for internal use.
     *
     * @param player The player to create the scoreboard for.
     */
    protected static void create(Player player) {
        // We check the configuration here as this is a 'short circuit' to disable
        // the entire system, which is what we want if no one registered a config.
        if (configuration != null) {
            boards.put(player.getName(), new BMScoreboard(player));
        }
    }

    /**
     * Updates (ticks) a player's scoreboard. Only for internal use.
     *
     * @param player The player whose scoreboard should be ticked.
     */
    protected static void updateScoreboard(Player player) {
        if (boards.containsKey(player.getName())) {
            boards.get(player.getName()).update();
        }
    }

    /**
     * Removes a player's scoreboard. Only for internal use.
     *
     * @param player The player whose scoreboard should be removed.
     */
    protected static void remove(Player player) {
        boards.remove(player.getName());
    }

}