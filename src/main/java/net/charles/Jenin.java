package net.charles;

import com.google.gson.ExclusionStrategy;
import net.charles.messaging.ChannelManager;
import net.charles.mapper.JeninMapper;
import net.charles.mapper.KeyManager;
import net.charles.mapper.SearchFilter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.exceptions.JedisDataException;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public class Jenin extends JeninMapper implements JedisController {
    private final JedisPool pool;
    private final ExecutorService executorService;

    public Jenin(final JedisPool jedisPool) {
        this(jedisPool, JedisController.DEFAULT_EXECUTOR_SERVICE);
    }

    public Jenin(final JedisPool jedisPool, final int nThreads) {
        this(jedisPool, Executors.newFixedThreadPool(nThreads));
    }

    public Jenin(final JedisPool jedisPool, final ExecutorService executorService) {
        this.pool = jedisPool;
        this.executorService = executorService;
    }

    @Override
    public void getTemporaryJedisInstance(Consumer<Jedis> callback) {
        callback.andThen(pool::returnResource).accept(pool.getResource());
    }

    @Override
    public JeninMapper updateExclusionStrategy(ExclusionStrategy serializationExclusionStrategy, ExclusionStrategy deserializationExclusionStrategy) {
        rebuildGson(getGson().newBuilder().serializeNulls().addSerializationExclusionStrategy(serializationExclusionStrategy).addDeserializationExclusionStrategy(deserializationExclusionStrategy));
        return this;
    }

    @Override
    public JeninMapper updateExclusionStrategy(ExclusionStrategy... exclusionStrategy) {
        rebuildGson(getGson().newBuilder().serializeNulls().setExclusionStrategies(exclusionStrategy));
        return this;
    }

    @Override
    public JeninMapper registerTypeAdapter(Type type, Object adapter) {
        rebuildGson(getGson().newBuilder().registerTypeAdapter(type, adapter));
        return this;
    }

    @Override
    public void push(String key, String json) {
        withJedis(jedis -> jedis.set(key, json));
    }

    @Override
    public <T> void push(T t) {
        withJedis(jedis -> {
            try {
                String key = KeyManager.findKey(t);
                jedis.set(key, getGson().toJson(t, t.getClass()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public <T> void pushToHashSet(T t) {
        withJedis(jedis -> {
            try {
                String key = KeyManager.findKey(t);
                jedis.hset(key, convertToHashSet(t));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void pushToHashSet(String key, Map<String, String> obj) {
        withJedis(jedis -> jedis.hset(key, obj));
    }

    @Override
    public <T> T search(String key, Class<T> clazz) {
        return getWithJedis(jedis -> getGson().fromJson(jedis.get(key), clazz));
    }

    @Override
    public <T> List<T> searchDuplicable(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public String search(String key, String fieldName, Class<?> clazz) {
        return getWithJedis(jedis -> {
            Object obj = getGson().fromJson(jedis.get(key), clazz);
            return getGson().toJsonTree(obj).getAsJsonObject().get(fieldName).getAsString();
        });
    }

    @Override
    public List<String> searchDuplicable(String key, String fieldName, Class<?> clazz) {
        return null;
    }

    @Override
    public <T> T hashSearch(String key, Class<T> clazz) {
        return getWithJedis(jedis -> convertToObject(key, jedis.hgetAll(key), clazz));
    }

    @Override
    public <T> List<T> duplicableHashSearch(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public String hashSearch(String key, String fieldName) {
        return getWithJedis(jedis -> jedis.hget(key, fieldName));
    }

    @Override
    public List<String> duplicableHashSearch(String key, String fieldName) {
        return null;
    }

    @Override
    public <V, C> List<C> hashSearch(SearchFilter<V>[] searchFilters, Class<C> clazz) {
        return getWithJedis(jedis -> {
            final List<C> objects = new ArrayList<>();
            for (String v : jedis.scan("0", new ScanParams().match("*")).getResult()) {
                try {
                    if (jedis.hexists(v, searchFilters[0].getFieldName())) {
                        C c = convertToObject(v, jedis.hgetAll(v), clazz);
                        if (applyFilter(c, searchFilters)) objects.add(c);
                    }
                } catch (JedisDataException ignored) {
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return objects;
        });
    }

    @Override
    public ChannelManager getChannelManagerInstance() {
        return getChannelManager();
    }

    @Override
    public <R> R getWithJedis(Function<Jedis, R> function) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return function.apply(jedis);
        } finally {
            assert jedis != null;
            pool.returnResource(jedis);
        }
    }

    @Override
    public <R> Future<R> getWithJedisAsync(Function<Jedis, R> function) {
        return executorService.submit(() -> {
            Jedis jedis = null;
            try {
                jedis = pool.getResource();
                return function.apply(jedis);
            } finally {
                assert jedis != null;
                pool.returnResource(jedis);
            }
        });
    }

    @Override
    public <R> List<R> getDuplicableWithJedis(Function<Jedis, List<R>> function) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return function.apply(jedis);
        } finally {
            assert jedis != null;
            pool.returnResource(jedis);
        }
    }

    @Override
    public void withJedis(Consumer<Jedis> consumer) {
        try (Jedis jedis = pool.getResource()) {
            consumer.andThen(pool::returnResource).accept(jedis);
        }
    }

    @Override
    public void withJedisAsync(Consumer<Jedis> consumer) {
        executorService.submit(() -> consumer.andThen(pool::returnResource).accept(pool.getResource()));
    }

    public void logResourcePoolInfo(long period) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getLogger().log(Level.INFO, "Current instances active : " + pool.getNumActive());
                getLogger().log(Level.INFO, "Current instances waiting : " + pool.getNumWaiters());
                getLogger().log(Level.INFO, "Current instances idling : " + pool.getNumIdle());
            }
        }, 1000, period);
    }
}