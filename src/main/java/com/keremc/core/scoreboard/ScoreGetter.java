package com.keremc.core.scoreboard;

import org.bukkit.entity.Player;

public interface ScoreGetter {

    String[] getScores(Player player);

}