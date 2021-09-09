package net.charles;

import com.google.gson.ExclusionStrategy;
import com.google.gson.GsonBuilder;
import net.charles.parser.JeninParser;
import net.charles.parser.KeyManager;
import net.charles.parser.SearchFilter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.exceptions.JedisDataException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Jenin extends JeninParser {
    private final JedisPool pool;

    public Jenin(final JedisPool jedisPool) {
        this.pool = jedisPool;
    }

    @Override
    public JeninParser updateExclusionStrategy(ExclusionStrategy serializationExclusionStrategy, ExclusionStrategy deserializationExclusionStrategy) {
        rebuildGson(getGson().newBuilder().serializeNulls().addSerializationExclusionStrategy(serializationExclusionStrategy).addDeserializationExclusionStrategy(deserializationExclusionStrategy));
        return this;
    }

    @Override
    public JeninParser updateExclusionStrategy(ExclusionStrategy... exclusionStrategy) {
        rebuildGson(getGson().newBuilder().serializeNulls().setExclusionStrategies(exclusionStrategy));
        return this;
    }

    @Override
    public JeninParser registerTypeAdapter(Type type, Object adapter) {
        rebuildGson(getGson().newBuilder().registerTypeAdapter(type, adapter));
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
            return convertToObject(key, jedis.hgetAll(key), clazz);
        }
    }

    @Override
    public String hashSearch(String key, String fieldName) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hget(key, fieldName);
        }
    }

    @Override
    public <V, C> List<C> hashSearch(SearchFilter<V>[] searchFilters, Class<C> clazz) throws NoSuchFieldException, IllegalAccessException {
        try (Jedis jedis = pool.getResource()) {
            final List<C> objects = new ArrayList<>();
            for (String v : jedis.scan("0", new ScanParams().match("*")).getResult()) {
                try {
                    if (jedis.hexists(v, searchFilters[0].getFieldName())) {
                        C c = convertToObject(v, jedis.hgetAll(v), clazz);
                        if (applyFilter(c, searchFilters)) objects.add(c);
                    }
                } catch (JedisDataException ignored) { }
            }
            return objects;
        }
    }
}