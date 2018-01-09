package com.keremc.core.uuid.impl;

import com.keremc.core.CorePlugin;
import com.keremc.core.uuid.UUIDCache;

import java.util.UUID;

public final class BukkitUUIDCache implements UUIDCache {

    public UUID uuid(String name) {
        return (CorePlugin.getInstance().getServer().getOfflinePlayer(name).getUniqueId());
    }

    public String name(UUID uuid) {
        return (CorePlugin.getInstance().getServer().getOfflinePlayer(uuid).getName());
    }

    public void ensure(UUID uuid) {
    } // Do nothing, as this class just delegates calls down to Bukkit.

    public void update(UUID uuid, String name) {
    } // We never need to update this, as this class just delegates calls down to Bukkit.

}