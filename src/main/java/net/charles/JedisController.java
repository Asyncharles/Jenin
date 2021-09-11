package net.charles;

import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

public interface JedisController {
    void withJedis(Consumer<Jedis> consumer);

    void withJedisAsync(Consumer<Jedis> consumer);

    <R> R getWithJedis(Function<Jedis, R> function);

    <R> Future<R> getWithJedisAsync(Function<Jedis, R> function);

    static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newFixedThreadPool(2);
}
