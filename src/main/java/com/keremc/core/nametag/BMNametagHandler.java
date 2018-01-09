package com.keremc.core.nametag;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.keremc.core.CorePlugin;
import com.keremc.core.packet.ScoreboardTeamPacketMod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BMNametagHandler {

    @Getter(AccessLevel.PROTECTED) private static Map<String, Map<String, NametagInfo>> teamMap = new ConcurrentHashMap<>();
    private static List<NametagInfo> registeredTeams = Collections.synchronizedList(new ArrayList<NametagInfo>());
    private static int teamCreateIndex = 1;
    private static List<NametagProvider> providers = new ArrayList<>();
    @Getter private static boolean initiated = false;
    @Getter @Setter private static boolean async = true;
    @Getter @Setter private static int updateInterval = 2; // In ticks

    // Static class -- cannot be created.
    private BMNametagHandler() {
    }

    /**
     * Initiates the nametag handler.
     * This can only be called once, and is called automatically when Core enables.
     */
    public static void init() {
        // Only allow the CoreNametagHandler to be initiated once.
        // Note the '!' in the .checkState call.
        Preconditions.checkState(!initiated);
        initiated = true;

        (new NametagThread()).start();
        CorePlugin.getInstance().getServer().getPluginManager().registerEvents(new NametagListener(), CorePlugin.getInstance());
        registerProvider(new NametagProvider.DefaultNametagProvider());
    }

    /**
     * Registers a new NametagProvider. Note that the newProvider
     * will not always be used. It will only be used if it is the highest
     * weighted provider available.
     *
     * @param newProvider The NametagProvider to register.
     */
    public static void registerProvider(NametagProvider newProvider) {
        providers.add(newProvider);
        Collections.sort(providers, new Comparator<NametagProvider>() {

            @Override
            public int compare(NametagProvider a, NametagProvider b) {
                return (Ints.compare(b.getWeight(), a.getWeight()));
            }

        });
    }

    /**
     * Refreshes one player for all players online.
     * NOTE: This is not an instant refresh, this is queued and async.
     *
     * @param toRefresh The player to refresh.
     */
    public static void reloadPlayer(Player toRefresh) {
        NametagUpdate update = new NametagUpdate(toRefresh);

        if (async) {
            NametagThread.getPendingUpdates().put(update, true);
        } else {
            applyUpdate(update);
        }
    }

    /**
     * Reloads all OTHER players for the player provided.
     *
     * @param refreshFor The player who should have all viewable nametags refreshed.
     */
    public static void reloadOthersFor(Player refreshFor) {
        for (Player toRefresh : CorePlugin.getInstance().getServer().getOnlinePlayers()) {
            if (refreshFor == toRefresh) continue;
            reloadPlayer(toRefresh, refreshFor);
        }
    }

    /**
     * Refreshes one player for another player only.
     * NOTE: This is not an instant refresh, this is queued and async.
     *
     * @param toRefresh  The player to refresh.
     * @param refreshFor The player to refresh toRefresh for.
     */
    public static void reloadPlayer(Player toRefresh, Player refreshFor) {
        NametagUpdate update = new NametagUpdate(toRefresh, refreshFor);

        if (async) {
            NametagThread.getPendingUpdates().put(update, true);
        } else {
            applyUpdate(update);
        }
    }

    /**
     * Applies a pending nametag update. Only for internal use.
     *
     * @param nametagUpdate The nametag update to apply.
     */
    protected static void applyUpdate(NametagUpdate nametagUpdate) {
        Player toRefreshPlayer = CorePlugin.getInstance().getServer().getPlayerExact(nametagUpdate.getToRefresh());

        // Just ignore it if they logged off since the request to update was submitted
        if (toRefreshPlayer == null) {
            return;
        }

        if (nametagUpdate.getRefreshFor() == null) {
            for (Player refreshFor : CorePlugin.getInstance().getServer().getOnlinePlayers()) {
                reloadPlayerInternal(toRefreshPlayer, refreshFor);
            }
        } else {
            Player refreshForPlayer = CorePlugin.getInstance().getServer().getPlayerExact(nametagUpdate.getRefreshFor());

            if (refreshForPlayer != null) {
                reloadPlayerInternal(toRefreshPlayer, refreshForPlayer);
            }
        }
    }

    /**
     * Reloads a player sync. Only for internal use.
     *
     * @param toRefresh  The player to refresh.
     * @param refreshFor The player to refresh 'toRefresh' for.
     */
    protected static void reloadPlayerInternal(Player toRefresh, Player refreshFor) {
        if (!refreshFor.hasMetadata("keremTag-LoggedIn")) {
            return;
        }


        NametagInfo provided = null;
        int providerIndex = 0;

        while (provided == null) {
            provided = providers.get(providerIndex++).fetchNametag(toRefresh, refreshFor);
        }

        Map<String, NametagInfo> teamInfoMap = new HashMap<>();

        if (teamMap.containsKey(refreshFor.getName())) {
            teamInfoMap = teamMap.get(refreshFor.getName());
        }

        (new ScoreboardTeamPacketMod(provided.getName(), Arrays.asList(toRefresh.getName()), 3)).sendToPlayer(refreshFor);
        teamInfoMap.put(toRefresh.getName(), provided);
        teamMap.put(refreshFor.getName(), teamInfoMap);
    }

    /**
     * 'Sets up' a player. This sends them all existing teams
     * and their members. This does NOT send new nametag
     * packets for the given player. Only for internal use.
     *
     * @param player The player to setup.
     */
    protected static void initiatePlayer(Player player) {
        for (NametagInfo teamInfo : registeredTeams) {
            teamInfo.getTeamAddPacket().sendToPlayer(player);
        }
    }

    /**
     * Gets or created a NametagInfo objetc
     * with the specified prefix and suffix. Only for internal use.
     *
     * @param prefix The prefix the NametagInfo object should have.
     * @param suffix The suffix the NametagInfo object should have.
     * @return The NametagInfo object with the prefix and suffix given.
     */
    protected static NametagInfo getOrCreate(String prefix, String suffix) {
        for (NametagInfo teamInfo : registeredTeams) {
            if (teamInfo.getPrefix().equals(prefix) && teamInfo.getSuffix().equals(suffix)) {
                return (teamInfo);
            }
        }

        NametagInfo newTeam = new NametagInfo(String.valueOf(teamCreateIndex++), prefix, suffix);
        registeredTeams.add(newTeam);

        ScoreboardTeamPacketMod addPacket = newTeam.getTeamAddPacket();

        for (Player player : CorePlugin.getInstance().getServer().getOnlinePlayers()) {
            addPacket.sendToPlayer(player);
        }

        return (newTeam);
    }

}