package com.keremc.core.uuid.impl;

import com.keremc.core.CorePlugin;
import com.keremc.core.redis.RedisCommand;
import com.keremc.core.uuid.BMUUIDCache;
import com.keremc.core.uuid.UUIDCache;
import com.keremc.core.xpacket.BMXPacketHandler;
import com.keremc.core.xpacket.XPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DistributedUUIDCache implements UUIDCache {

    private static Map<UUID, String> uuidToName = new ConcurrentHashMap<>();
    private static Map<String, UUID> nameToUuid = new ConcurrentHashMap<>();

    public DistributedUUIDCache() {
        CorePlugin.getInstance().runRedisCommand(new RedisCommand<Object>() {

            @Override
            public Object execute(Jedis redis) {
                Map<String, String> cache = redis.hgetAll("UUIDCache");

                for (Map.Entry<String, String> cacheEntry : cache.entrySet()) {
                    UUID uuid = UUID.fromString(cacheEntry.getKey());
                    String name = cacheEntry.getValue();

                    uuidToName.put(uuid, name);
                    nameToUuid.put(name.toLowerCase(), uuid);
                }

                return (null);
            }

        });
    }

    public UUID uuid(String name) {
        return (nameToUuid.get(name.toLowerCase()));
    }

    public String name(UUID uuid) {
        return (uuidToName.get(uuid));
    }

    public void ensure(UUID uuid) {
        if (String.valueOf(name(uuid)).equals("null")) {
            CorePlugin.getInstance().getLogger().warning(uuid + " didn't have a cached name.");
        }
    }

    public void update(UUID uuid, String name) {
        update0(uuid, name, true);
    }

    private void update0(final UUID uuid, final String name, final boolean distributedToOthers) {
        uuidToName.put(uuid, name);

        // Flush any old entries out of the cache.
        for (Map.Entry<String, UUID> entry : (new HashMap<>(nameToUuid)).entrySet()) {
            if (entry.getValue().equals(uuid)) {
                nameToUuid.remove(entry.getKey());
            }
        }

        nameToUuid.put(name.toLowerCase(), uuid);

        if (distributedToOthers) {
            // We only update Redis once, and we do it here.
            // (on the server to request the change)
            new BukkitRunnable() {

                public void run() {
                    CorePlugin.getInstance().runRedisCommand(new RedisCommand<Object>() {

                        @Override
                        public Object execute(Jedis redis) {
                            redis.hset("UUIDCache", uuid.toString(), name);
                            return (null);
                        }

                    });
                }

            }.runTaskAsynchronously(CorePlugin.getInstance());

            DistributedUUIDCacheUpdatePacket packet = new DistributedUUIDCacheUpdatePacket(uuid, name);
            BMXPacketHandler.sendToAll(packet);
        }
    }

    @AllArgsConstructor
    public static class DistributedUUIDCacheUpdatePacket implements XPacket {

        @Getter private UUID uuid;
        @Getter private String name;

        // We have to have this for XPacket to do its thing.
        public DistributedUUIDCacheUpdatePacket() {
        }

        public void onReceive() {
            if (BMUUIDCache.getImpl() instanceof DistributedUUIDCache) {
                ((DistributedUUIDCache) BMUUIDCache.getImpl()).update0(uuid, name, false);
            }
        }

    }

}