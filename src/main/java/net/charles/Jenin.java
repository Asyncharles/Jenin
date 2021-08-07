package net.charles;

import net.charles.json.JeninParser;
import net.charles.json.KeyManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Jenin extends JeninParser {
    private final JedisPool pool;

    public Jenin(final JedisPool jedisPool) {
        this.pool = jedisPool;
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
}

