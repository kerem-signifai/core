package com.keremc.core.scoreboard;

import com.keremc.core.CorePlugin;
import org.bukkit.entity.Player;

final class ScoreboardThread extends Thread {

    public ScoreboardThread() {
        super("Core - Scoreboard Thread");
        setDaemon(false);
    }

    public void run() {
        while (true) {
            for (Player online : CorePlugin.getInstance().getServer().getOnlinePlayers()) {
                try {
                    BMScoreboardHandler.updateScoreboard(online);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(BMScoreboardHandler.getUpdateInterval() * 50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}