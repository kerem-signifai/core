package com.keremc.core.serialization;

import com.google.gson.*;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;

public class VectorAdapter implements JsonDeserializer<Vector>, JsonSerializer<Vector> {

    @Override
    public Vector deserialize(JsonElement src, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        return fromJson(src);
    }


    @Override
    public JsonElement serialize(Vector src, Type type, JsonSerializationContext context) {
        return toJson(src);
    }


    public static JsonObject toJson(Vector src) {
        if (src == null) {
            return null;
        }

        final JsonObject object = new JsonObject();

        object.addProperty("x", src.getX());
        object.addProperty("y", src.getY());
        object.addProperty("z", src.getZ());

        return object;
    }

    public static Vector fromJson(JsonElement src) {
        if (src == null || !src.isJsonObject()) {
            return null;
        }
        final JsonObject json = src.getAsJsonObject();

        final double x = json.get("x").getAsDouble();
        final double y = json.get("y").getAsDouble();
        final double z = json.get("z").getAsDouble();

        return new Vector(x, y, z);
    }
}
