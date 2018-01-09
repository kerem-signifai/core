package com.keremc.core.uuid;

import com.google.common.base.Preconditions;
import com.keremc.core.CorePlugin;
import lombok.Getter;

import java.util.UUID;

public final class BMUUIDCache {

    @Getter private static UUIDCache impl = null;
    private static boolean initiated = false;

    // Static class -- cannot be created.
    private BMUUIDCache() {
    }

    /**
     * Initiates the UUID cache.
     * This can only be called once, and is called automatically when Core enables.
     */
    public static void init() {
        // Only allow the cache to be initiated once.
        // Note the '!' in the .checkState call.
        Preconditions.checkState(!initiated);
        initiated = true;

        try {
            impl = (UUIDCache) Class.forName(CorePlugin.getInstance().getConfig().getString("UUIDCache.Backend", "RedisUUIDCache")).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        CorePlugin.getInstance().getServer().getPluginManager().registerEvents(new UUIDListener(), CorePlugin.getInstance());
    }

    public static UUID uuid(String name) {
        return (impl.uuid(name));
    }

    public static String name(UUID uuid) {
        return (impl.name(uuid));
    }

    public static void ensure(UUID uuid) {
        impl.ensure(uuid);
    }

    public static void update(UUID uuid, String name) {
        impl.update(uuid, name);
    }

}