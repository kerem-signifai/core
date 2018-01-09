package com.keremc.core.uuid;

import java.util.UUID;

public interface UUIDCache {

    UUID uuid(String name);

    String name(UUID uuid);

    void ensure(UUID uuid);

    void update(UUID uuid, String name);

}