package com.keremc.core.packet;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public final class ScoreboardTeamPacketMod {

    private PacketPlayOutScoreboardTeam packet;

    private static String fieldPrefix = "c";
    private static String fieldSuffix = "d";
    private static String fieldPlayers = "g";
    private static String fieldTeamName = "a";
    private static String fieldParamInt = "h";
    private static String fieldPackOption = "i";
    private static String fieldDisplayName = "b";

    public ScoreboardTeamPacketMod(String name, String prefix, String suffix, Collection players, int paramInt) {
        packet = new PacketPlayOutScoreboardTeam();

        setField(fieldTeamName, name);
        setField(fieldParamInt, paramInt);

        if (paramInt == 0 || paramInt == 2) {
            setField(fieldDisplayName, name);
            setField(fieldPrefix, prefix);
            setField(fieldSuffix, suffix);
            setField(fieldPackOption, 1);
        }

        if (paramInt == 0) {
            addAll(players);
        }
    }

    public ScoreboardTeamPacketMod(String name, Collection players, int paramInt) {
        packet = new PacketPlayOutScoreboardTeam();

        if (players == null) {
            players = new ArrayList<String>();
        }

        setField(fieldTeamName, name);
        setField(fieldParamInt, paramInt);
        addAll(players);
    }

    public void sendToPlayer(Player bukkitPlayer) {
        ((CraftPlayer) bukkitPlayer).getHandle().playerConnection.sendPacket(packet);
    }

    public void setField(String field, Object value) {
        try {
            Field fieldObject = packet.getClass().getDeclaredField(field);

            fieldObject.setAccessible(true);
            fieldObject.set(packet, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addAll(Collection col) {
        try {
            Field fieldObject = packet.getClass().getDeclaredField(fieldPlayers);

            fieldObject.setAccessible(true);
            ((Collection) fieldObject.get(packet)).addAll(col);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}