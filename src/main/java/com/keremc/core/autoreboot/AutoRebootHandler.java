package com.keremc.core.autoreboot;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.keremc.core.CorePlugin;
import com.keremc.core.autoreboot.listeners.AutoRebootListener;
import com.keremc.core.command.BMCommandHandler;
import com.keremc.core.util.TimeUtils;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public final class AutoRebootHandler {

    @Getter private static List<Integer> rebootTimes;
    @Getter private static boolean initiated = false;

    // Static class -- cannot be created.
    private AutoRebootHandler() {
    }

    /**
     * Initiates the autoreboot handler.
     * This can only be called once, and is called automatically when Core enables.
     */
    public static void init() {
        // Only allow the AutoRebootHandler to be initiated once.
        // Note the '!' in the .checkState call.
        Preconditions.checkState(!initiated);
        initiated = true;

        BMCommandHandler.loadCommandsFromPackage(CorePlugin.getInstance(), "com.keremc.core.autoreboot.commands");
        rebootTimes = ImmutableList.copyOf(CorePlugin.getInstance().getConfig().getIntegerList("AutoRebootTimes"));
        CorePlugin.getInstance().getServer().getPluginManager().registerEvents(new AutoRebootListener(), CorePlugin.getInstance());
    }

    public static void rebootServer(int seconds) {
        new BukkitRunnable() {

            int i = seconds;

            public void run() {
                if (i == 60) {
                    CorePlugin.getInstance().getServer().setWhitelist(true);
                } else if (i == 0) {
                    CorePlugin.getInstance().getServer().setWhitelist(false);
                    CorePlugin.getInstance().getServer().shutdown();
                }

                switch (i) {
                    case 5 * 60:
                    case 4 * 60:
                    case 3 * 60:
                    case 2 * 60:
                    case 60:
                    case 30:
                    case 15:
                    case 10:
                    case 5:
                        CorePlugin.getInstance().getServer().broadcastMessage(ChatColor.RED + "⚠ " + ChatColor.DARK_RED.toString() + ChatColor.STRIKETHROUGH + "------------------------" + ChatColor.RED + " ⚠");
                        CorePlugin.getInstance().getServer().broadcastMessage(ChatColor.RED + "Server rebooting in " + TimeUtils.formatIntoDetailedString(i) + ".");
                        CorePlugin.getInstance().getServer().broadcastMessage(ChatColor.RED + "⚠ " + ChatColor.DARK_RED.toString() + ChatColor.STRIKETHROUGH + "------------------------" + ChatColor.RED + " ⚠");
                        break;
                    default:
                        break;
                }

                i--;
            }

        }.runTaskTimer(CorePlugin.getInstance(), 20L, 20L);
    }

}