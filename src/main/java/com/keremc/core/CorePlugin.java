package com.keremc.core;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keremc.core.autoreboot.AutoRebootHandler;
import com.keremc.core.command.BMCommandHandler;
import com.keremc.core.event.HalfHourEvent;
import com.keremc.core.event.HourEvent;
import com.keremc.core.item.ItemListener;
import com.keremc.core.nametag.BMNametagHandler;
import com.keremc.core.redis.RedisCommand;
import com.keremc.core.scoreboard.BMScoreboardHandler;
import com.keremc.core.serialization.*;
import com.keremc.core.terrain.EmptyChunkGenerator;
import com.keremc.core.util.ItemUtils;
import com.keremc.core.util.TPSUtils;
import com.keremc.core.uuid.BMUUIDCache;
import com.keremc.core.xpacket.BMXPacketHandler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public final class CorePlugin extends JavaPlugin {

    @Getter private static CorePlugin instance;
    @Getter private long localRedisLastError;
    @Getter private long backboneRedisLastError;

    public static final Random RANDOM = new Random();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter())
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
            .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
            .registerTypeHierarchyAdapter(Vector.class, new VectorAdapter())
            .registerTypeAdapter(BlockVector.class, new BlockVectorAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    public static final Gson PLAIN_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter())
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
            .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
            .registerTypeHierarchyAdapter(Vector.class, new VectorAdapter())
            .registerTypeAdapter(BlockVector.class, new BlockVectorAdapter())
            .serializeNulls()
            .create();

    private JedisPool localJedisPool;
    private JedisPool backboneJedisPool;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        try {
            localJedisPool = new JedisPool(new JedisPoolConfig(), getConfig().getString("Redis.Host"), 6379, 20_000, null, 0, null);
        } catch (Exception e) {
            localJedisPool = null;
            e.printStackTrace();
            getLogger().warning("Couldn't connect to a Redis instance at " + getConfig().getString("Redis.Host") + ".");
        }

        try {
            backboneJedisPool = new JedisPool(new JedisPoolConfig(), getConfig().getString("BackboneRedis.Host"), 6379, 20_000, null, 0, null);
        } catch (Exception e) {
            backboneJedisPool = null;
            e.printStackTrace();
            getLogger().warning("Couldn't connect to a Redis instance at " + getConfig().getString("BackboneRedis.Host") + ".");
        }

        BMCommandHandler.init();
        BMNametagHandler.init();
        BMScoreboardHandler.init();
        BMUUIDCache.init();
        BMXPacketHandler.init();
        AutoRebootHandler.init();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TPSUtils(), 1L, 1L);
        ItemUtils.load();

        getServer().getPluginManager().registerEvents(new ItemListener(), this);

        Calendar date = Calendar.getInstance();

        date.set(Calendar.MINUTE, 60);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        (new Timer("Core - Hour Scheduler")).schedule(new TimerTask() {

            @Override
            public void run() {
                new BukkitRunnable() {

                    public void run() {
                        getServer().getPluginManager().callEvent(new HourEvent(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
                    }

                }.runTask(CorePlugin.this);
            }

        }, date.getTime(), TimeUnit.MINUTES.toMillis(60));

        (new Timer("Core - Half Hour Scheduler")).schedule(new TimerTask() {

            @Override
            public void run() {
                new BukkitRunnable() {

                    public void run() {
                        getServer().getPluginManager().callEvent(new HalfHourEvent(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE)));
                    }

                }.runTask(CorePlugin.this);
            }

        }, date.getTime(), TimeUnit.MINUTES.toMillis(30));
    }

    @Override
    public void onDisable() {
        localJedisPool.close();
        backboneJedisPool.close();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new EmptyChunkGenerator();
    }

    public <T> T runRedisCommand(RedisCommand<T> redisCommand) {
        Jedis jedis = localJedisPool.getResource();
        T result = null;

        try {
            result = redisCommand.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            localRedisLastError = System.currentTimeMillis();

            if (jedis != null) {
                localJedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (jedis != null) {
                localJedisPool.returnResource(jedis);
            }
        }

        return (result);
    }

    public <T> T runBackboneRedisCommand(RedisCommand<T> redisCommand) {
        Jedis jedis = backboneJedisPool.getResource();
        T result = null;

        try {
            result = redisCommand.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            backboneRedisLastError = System.currentTimeMillis();

            if (jedis != null) {
                backboneJedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (jedis != null) {
                backboneJedisPool.returnResource(jedis);
            }
        }

        return (result);
    }

    /*

    The only command this plugin registers is /eval <command>.
     This is a custom entrypoint for our command system.
     We DO listen for ServerCommandEvent (and use it for Console commands)
     We use this for executors which aren't Players or the Console (IE services like BuyCraft)

     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // This shouldn't be changed -- we NEVER want to let someone
        // in game use /eval, for obvious reasons.
        if (sender instanceof ConsoleCommandSender) {
            BMCommandHandler.evalCommand(sender, Joiner.on(" ").join(args));
        } else {
            sender.sendMessage(ChatColor.RED + "This is a console-only utility command. It cannot be used from game.");
        }

        return (true);
    }

}
