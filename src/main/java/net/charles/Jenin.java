package net.charles;

import com.google.gson.ExclusionStrategy;
import com.google.gson.GsonBuilder;
import net.charles.parser.JeninParser;
import net.charles.parser.KeyManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;

public class Jenin extends JeninParser {
    private final JedisPool pool;

    public Jenin(final JedisPool jedisPool) {
        this.pool = jedisPool;
    }

    @Override
    public JeninParser updateExclusionStrategy(ExclusionStrategy serializationExclusionStrategy, ExclusionStrategy deserializationExclusionStrategy) {
        rebuildGson(new GsonBuilder().serializeNulls().addSerializationExclusionStrategy(serializationExclusionStrategy).addDeserializationExclusionStrategy(deserializationExclusionStrategy));
        return this;
    }

    @Override
    public JeninParser updateExclusionStrategy(ExclusionStrategy... exclusionStrategy) {
        rebuildGson(new GsonBuilder().serializeNulls().setExclusionStrategies(exclusionStrategy));
        return this;
    }

    @Override
    public void compactPush(String key, String json) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, json);
        }
    }

    @Override
    public <T> void compactPush(T t) throws IllegalAccessException {
        try (Jedis jedis = pool.getResource()) {
            String key = KeyManager.findKey(t);
            jedis.set(key, getGson().toJson(t, t.getClass()));
        }
    }

    @Override
    public <T> void push(T t) throws IllegalAccessException {
        try (Jedis jedis = pool.getResource()) {
            String key = KeyManager.findKey(t);
            jedis.hset(key, convertToHashSet(t));
        }
    }

    @Override
    public void push(String key, Map<String, String> obj) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hset(key, obj);
        }
    }

    @Override
    public <T> T compactSearch(String key, Class<T> clazz) {
        try (Jedis jedis = pool.getResource()) {
            return convertToObject(jedis.hgetAll(key), clazz);
        }
    }

    @Override
    public <T> T compactSearch(String key, String fieldName) {
        return null;
    }

    @Override
    public <T> T search(String key) {
        return null;
    }

    @Override
    public <T, V> T[] search(String fieldName, V fieldValue) {
        return null;
    }
}

