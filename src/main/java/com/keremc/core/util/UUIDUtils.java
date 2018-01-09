package com.keremc.core.util;

import com.keremc.core.uuid.BMUUIDCache;
import com.mongodb.BasicDBList;

import java.util.Collection;
import java.util.UUID;

public final class UUIDUtils {

    // Static utility class -- cannot be created.
    private UUIDUtils() {
    }

    /**
     * Gets the name associated with a UUID.
     *
     * @param uuid The UUID object to fetch the name for.
     * @return The name associated with the UUID given.
     */
    public static String name(UUID uuid) {
        return (BMUUIDCache.name(uuid));
    }

    /**
     * Gets the UUID associated with a name.
     *
     * @param name The name to fetch the UUID for.
     * @return The UUID associated with the name given.
     */
    public static UUID uuid(String name) {
        return (BMUUIDCache.uuid(name));
    }

    /**
     * Formats a UUID and its name.
     *
     * @param uuid The UUID to format.
     * @return The formatted String.
     */
    public static String formatPretty(UUID uuid) {
        return (name(uuid) + " [" + uuid + "]");
    }

    /**
     * Converts a Collection of UUIDs into a String-based BasicDBList (for storage in MongoDB)
     *
     * @param toConvert The UUIDs to convert.
     * @return A BasicDBList containing the UUIDs in String form.
     */
    public static BasicDBList uuidsToStrings(Collection<UUID> toConvert) {
        if (toConvert == null || toConvert.isEmpty()) {
            return (new BasicDBList());
        }

        BasicDBList dbList = new BasicDBList();

        for (UUID uuid : toConvert) {
            dbList.add(uuid.toString());
        }

        return (dbList);
    }

}