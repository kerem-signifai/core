package com.keremc.core.command.commands;

import com.keremc.core.command.Command;
import com.keremc.core.command.param.Parameter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Broadcast {

    @Command(names = "broadcast", permissionNode = "core.broadcast")
    public static void bc(Player sender, @Parameter(wildcard = true, name = "message") String broadcast) {
        String msg = broadcast.replaceAll("(&([a-f0-9l-or]))", "\u00A7$2");
        Bukkit.broadcastMessage("§e[Announcement] §f" + msg);
    }

}
