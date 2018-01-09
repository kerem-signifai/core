package com.keremc.core.redis;

import redis.clients.jedis.Jedis;

public interface RedisCommand<T> {

    T execute(Jedis redis);

}