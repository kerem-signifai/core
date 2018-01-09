package com.keremc.core.autoreboot.listeners;

import com.keremc.core.autoreboot.AutoRebootHandler;
import com.keremc.core.event.HourEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AutoRebootListener implements Listener {

    @EventHandler
    public void onHour(HourEvent event) {
        if (AutoRebootHandler.getRebootTimes().contains(event.getHour())) {
            AutoRebootHandler.rebootServer(5 * 60);
        }
    }

}