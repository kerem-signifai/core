package com.keremc.core.xpacket;

import com.google.common.base.Preconditions;
import com.keremc.core.CorePlugin;
import com.keremc.core.redis.RedisCommand;
import lombok.Getter;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

public final class BMXPacketHandler {

    @Getter private static boolean initiated = false;

    // Static class -- cannot be created.
    private BMXPacketHandler() {
    }

    /**
     * Initiates the XPacket handler.
     * This can only be called once, and is called automatically when Core enables.
     */
    public static void init() {
        // Only allow the CoreXPacketHandler to be initiated once.
        // Note the '!' in the .checkState call.
        Preconditions.checkState(!initiated);
        initiated = true;

        new BukkitRunnable() {

            public void run() {
                while (CorePlugin.getInstance().isEnabled()) {
                    CorePlugin.getInstance().runBackboneRedisCommand(new RedisCommand<Object>() {

                        @Override
                        public Object execute(Jedis redis) {
                            redis.subscribe(new XPacketPubSub(), "XPacket:All");
                            return (null);
                        }

                    });
                }

                while (CorePlugin.getInstance().isEnabled()) {
                    CorePlugin.getInstance().runRedisCommand(new RedisCommand<Object>() {

                        @Override
                        public Object execute(Jedis redis) {
                            redis.subscribe(new XPacketPubSub(), "XPacket:All");
                            return (null);
                        }

                    });
                }
            }

        }.runTaskAsynchronously(CorePlugin.getInstance());
    }

    /**
     * Sends a packet to all other servers (connected to the Core backbone)
     *
     * @param packet
     */
    public static void sendToAll(final XPacket packet) {
        if (!CorePlugin.getInstance().isEnabled()) {
            return;
        }

        new BukkitRunnable() {

            public void run() {
                CorePlugin.getInstance().runBackboneRedisCommand(new RedisCommand<Object>() {

                    @Override
                    public Object execute(Jedis redis) {
                        redis.publish("XPacket:All", packet.getClass().getName() + "||" + CorePlugin.PLAIN_GSON.toJson(packet));
                        return (null);
                    }

                });
            }

        }.runTaskAsynchronously(CorePlugin.getInstance());
    }

    /**
     * Sends a packet to all other servers (connected to the local redis)
     *
     * @param packet
     */
    public static void sendToAllViaLocal(final XPacket packet) {
        if (!CorePlugin.getInstance().isEnabled()) {
            return;
        }

        new BukkitRunnable() {

            public void run() {
                CorePlugin.getInstance().runRedisCommand(new RedisCommand<Object>() {

                    @Override
                    public Object execute(Jedis redis) {
                        redis.publish("XPacket:All", packet.getClass().getName() + "||" + CorePlugin.PLAIN_GSON.toJson(packet));
                        return (null);
                    }

                });
            }

        }.runTaskAsynchronously(CorePlugin.getInstance());
    }

    public static void sendToServer(XPacket packet, String server) {
        throw (new NotImplementedException());
    }

    public static void sendToServerViaLocal(XPacket packet, String server) {
        throw (new NotImplementedException());
    }

    public static void sendToServerGroup(XPacket packet, String serverGroup) {
        throw (new NotImplementedException());
    }

    public static void sendToServerGroupViaLocal(XPacket packet, String serverGroup) {
        throw (new NotImplementedException());
    }

}