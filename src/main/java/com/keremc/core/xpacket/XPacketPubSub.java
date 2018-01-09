package com.keremc.core.xpacket;

import com.keremc.core.CorePlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

public class XPacketPubSub extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        int index = message.indexOf("||");
        String packetClass = message.substring(0, index);
        String messageJson = message.substring(index + 2);

        try {
            final XPacket packet = (XPacket) CorePlugin.PLAIN_GSON.fromJson(messageJson, Class.forName(packetClass));

            if (CorePlugin.getInstance().isEnabled()) {
                new BukkitRunnable() {

                    public void run() {
                        packet.onReceive();
                    }

                }.runTask(CorePlugin.getInstance());
            }
        } catch (ClassNotFoundException e) {
            // Nothing. If we don't know the packet, we don't know the packet. Whatever.
        }
    }

    public void onPMessage(String s, String s2, String s3) {
    }

    public void onSubscribe(String s, int i) {
    }

    public void onUnsubscribe(String s, int i) {
    }

    public void onPUnsubscribe(String s, int i) {
    }

    public void onPSubscribe(String s, int i) {
    }

}