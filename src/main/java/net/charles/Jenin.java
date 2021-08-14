package net.charles;

import com.google.gson.ExclusionStrategy;
import com.google.gson.GsonBuilder;
import net.charles.parser.JeninParser;
import net.charles.parser.KeyManager;
import net.charles.parser.SearchFilter;
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
    public void push(String key, String json) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, json);
        }
    }

    @Override
    public <T> void push(T t) throws IllegalAccessException {
        try (Jedis jedis = pool.getResource()) {
            String key = KeyManager.findKey(t);
            jedis.set(key, getGson().toJson(t, t.getClass()));
        }
    }

    @Override
    public <T> void pushToHashSet(T t) throws IllegalAccessException {
        try (Jedis jedis = pool.getResource()) {
            String key = KeyManager.findKey(t);
            jedis.hset(key, convertToHashSet(t));
        }
    }

    @Override
    public void pushToHashSet(String key, Map<String, String> obj) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hset(key, obj);
        }
    }

    @Override
    public <T> T search(String key, Class<T> clazz) {
        try (Jedis jedis = pool.getResource()) {
            return getGson().fromJson(jedis.get(key), clazz);
        }
    }

    @Override
    public String search(String key, String fieldName, Class<?> clazz) {
        try (Jedis jedis = pool.getResource()) {
            Object obj = getGson().fromJson(jedis.get(key), clazz);
            return getGson().toJsonTree(obj).getAsJsonObject().get(fieldName).getAsString();
        }
    }

    @Override
    public <T> T hashSearch(String key, Class<T> clazz) {
        try (Jedis jedis = pool.getResource()) {
            return convertToObject(jedis.hgetAll(key), clazz);
        }
    }

    @Override
    public <V, C> C[] hashSearch(SearchFilter<V, C>[] searchFilters) {
        try (Jedis jedis = pool.getResource()) {
            for (String v : jedis.scan("0").getResult()) {

            }
            return null;
        }
    }
}

