package com.keremc.core.scoreboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.keremc.core.CorePlugin;
import com.keremc.core.packet.ScoreboardTeamPacketMod;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardScore;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.util.*;

final class BMScoreboard {

    private Player player;
    private Objective objective;
    private Map<String, Integer> displayedScores = new HashMap<>();
    private Set<String> sentTeamCreates = new HashSet<>();

    public BMScoreboard(Player player) {
        this.player = player;

        Scoreboard board = CorePlugin.getInstance().getServer().getScoreboardManager().getNewScoreboard();

        objective = board.registerNewObjective("Kerem", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(board);
    }

    public void update() {
        String title = BMScoreboardHandler.getConfiguration().getTitleGetter().getTitle(player);
        String[] lines = BMScoreboardHandler.getConfiguration().getScoreGetter().getScores(player);
        Collection<String> recentlyUpdatedScores = new HashSet<>();
        Collection<String> usedBaseScores = new HashSet<>();
        int nextValue = 15;

        Preconditions.checkArgument(lines.length < 16, "Too many lines passed!");
        Preconditions.checkArgument(title.length() < 32, "Title is too long!");

        // Do all validation before we apply any logic.
        for (String line : lines) {
            Preconditions.checkArgument(line.length() < 48, "Line '" + line + "' is too long!");
        }

        if (!objective.getDisplayName().equals(title)) {
            objective.setDisplayName(title);
        }

        for (String line : lines) {
            String[] seperated = separate(line, usedBaseScores);
            String prefix = seperated[0];
            String score = seperated[1];
            String suffix = seperated[2];

            recentlyUpdatedScores.add(score);

            if (!sentTeamCreates.contains(score)) {
                createAndAddMember(score);
            }

            if (!displayedScores.containsKey(score) || displayedScores.get(score) != nextValue) {
                setScore(score, nextValue);
            }

            updateScore(score, prefix, suffix);
            nextValue--;
        }

        for (String displayedScore : ImmutableSet.copyOf(displayedScores.keySet())) {
            if (recentlyUpdatedScores.contains(displayedScore)) {
                continue;
            }

            removeScore(displayedScore);
        }
    }

    private void setField(Packet packet, String field, Object value) {
        try {
            Field fieldObject = packet.getClass().getDeclaredField(field);

            fieldObject.setAccessible(true);
            fieldObject.set(packet, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This is here so that the score joins itself, this way
    // #updateScore will work as it should (that works on a 'player'), which technically we are adding to ourselves
    private void createAndAddMember(String scoreTitle) {
        ScoreboardTeamPacketMod scoreboardTeamAdd = new ScoreboardTeamPacketMod(scoreTitle, "_", "_", new ArrayList<String>(), 0);
        ScoreboardTeamPacketMod scoreboardTeamAddMember = new ScoreboardTeamPacketMod(scoreTitle, Arrays.asList(scoreTitle), 3);

        scoreboardTeamAdd.sendToPlayer(player);
        scoreboardTeamAddMember.sendToPlayer(player);
        sentTeamCreates.add(scoreTitle);
    }

    private void setScore(String score, int value) {
        PacketPlayOutScoreboardScore scoreboardScorePacket = new PacketPlayOutScoreboardScore();

        setField(scoreboardScorePacket, "a", score);
        setField(scoreboardScorePacket, "b", objective.getName());
        setField(scoreboardScorePacket, "c", value);
        setField(scoreboardScorePacket, "d", PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE);

        displayedScores.put(score, value);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(scoreboardScorePacket);
    }

    private void removeScore(String score) {
        displayedScores.remove(score);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutScoreboardScore(score));
    }

    private void updateScore(String score, String prefix, String suffix) {
        ScoreboardTeamPacketMod scoreboardTeamModify = new ScoreboardTeamPacketMod(score, prefix, suffix, null, 2);
        scoreboardTeamModify.sendToPlayer(player);
    }

    // Here be dragons.
    // Good luck maintaining this code.
    private String[] separate(String line, Collection<String> usedBaseScores) {
        line = ChatColor.translateAlternateColorCodes('&', line);
        String prefix = "";
        String score = "";
        String suffix = "";

        List<String> working = new ArrayList<>();
        StringBuilder workingStr = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '*' || (workingStr.length() == 16 && working.size() < 3)) {
                working.add(workingStr.toString());
                workingStr = new StringBuilder();

                if (c == '*') {
                    continue;
                }
            }

            workingStr.append(c);
        }

        working.add(workingStr.toString());

        switch (working.size()) {
            case 1:
                score = working.get(0);
                break;
            case 2:
                score = working.get(0);
                suffix = working.get(1);
                break;
            case 3:
                prefix = working.get(0);
                score = working.get(1);
                suffix = working.get(2);
                break;
            default:
                CorePlugin.getInstance().getLogger().warning("Failed to separate scoreboard line. Input: " + line);
                break;
        }

        if (usedBaseScores.contains(score)) {
            if (score.length() <= 14) {
                for (ChatColor chatColor : ChatColor.values()) {
                    String possibleScore = chatColor + score;

                    if (!usedBaseScores.contains(possibleScore)) {
                        score = possibleScore;
                        break;
                    }
                }

                if (usedBaseScores.contains(score)) {
                    CorePlugin.getInstance().getLogger().warning("Failed to find alternate color code for: " + score);
                }
            } else {
                CorePlugin.getInstance().getLogger().warning("Found a scoreboard base collision to shift: " + score);
            }
        }

        if (prefix.length() > 16) {
            prefix = ChatColor.DARK_RED.toString() + ChatColor.BOLD + ">16";
        }

        if (score.length() > 16) {
            score = ChatColor.DARK_RED.toString() + ChatColor.BOLD + ">16";
        }

        if (suffix.length() > 16) {
            suffix = ChatColor.DARK_RED.toString() + ChatColor.BOLD + ">16";
        }

        usedBaseScores.add(score);
        return (new String[]{prefix, score, suffix});
    }

}