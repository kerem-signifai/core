package com.keremc.core.autoreboot.commands;

import com.keremc.core.autoreboot.AutoRebootHandler;
import com.keremc.core.command.Command;
import com.keremc.core.command.param.Parameter;
import com.keremc.core.util.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AutoRebootCommand {

    @Command(names = {"restat"}, permissionNode = "op")
    public static void reboot(Player sender, @Parameter(name = "time") String unparsedTime) {
        int time = TimeUtils.parseTime(unparsedTime);
        AutoRebootHandler.rebootServer(time);
        sender.sendMessage(ChatColor.YELLOW + "Started auto reboot.");
    }

}