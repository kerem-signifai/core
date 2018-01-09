package com.keremc.core.util;

import com.keremc.core.CorePlugin;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class BungeeUtils {

    // Static utility class -- cannot be created.
    private BungeeUtils() {
    }

    /**
     * Sends a player to a server.
     *
     * @param player The player to move.
     * @param server The server to move the given player to.
     */
    public static void send(Player player, String server) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException e) {

        }

        player.sendPluginMessage(CorePlugin.getInstance(), "BungeeCord", b.toByteArray());
    }

    /**
     * Sends all players to a server.
     *
     * @param server The server to move all players to.
     */
    public static void sendAll(String server) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException e) {
            // Can never happen
        }

        for (Player player : CorePlugin.getInstance().getServer().getOnlinePlayers()) {
            player.sendPluginMessage(CorePlugin.getInstance(), "BungeeCord", b.toByteArray());
        }
    }

}