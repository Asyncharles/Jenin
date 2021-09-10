package net.charles.messaging;

import com.google.gson.Gson;
import net.charles.exceptions.messaging.SubscriptionException;
import net.charles.exceptions.messaging.UnregisteredChannelException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public final class ChannelManager {
    private static ChannelManager instance;

    private final LinkedHashMap<String, Channel<?>> registeredChannels;
    private Jedis jedis;
    private Gson gson;
    private boolean isSubscribed;

    private ChannelManager(Jedis jedis, Gson gson) {
        this.registeredChannels = new LinkedHashMap<>();
        this.jedis = jedis;
        this.gson = gson;
        this.isSubscribed = false;
    }

    public void registerChannels(Channel<?>... channels) {
        if (isSubscribed) throw new SubscriptionException("Channels are already registered, please reload the Redis PubSub.");
        for (Channel<?> channel : channels) registeredChannels.put(channel.getName(), channel);
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                Channel<?> c = registeredChannels.get(channel);
                if (c != null) {
                    c.getReceiver().onMessage(gson.fromJson(message, (Type) c.getObjClass()));
                }
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                Channel<?> c = registeredChannels.get(channel);
                if (c != null) {
                    c.getSubscriptionHandler().onSubscribe(channel);
                }
            }
        };
        jedis.subscribe(jedisPubSub, Arrays.stream(channels).map(Channel::getName).toArray(String[]::new));
        isSubscribed = true;
    }

    public void reloadRedisMessagingWithOldChannels(Channel<?>... newChannels) {

    }

    public Channel<?> queryChannel(String name) {
        final Channel<?> channel =  registeredChannels.get(name);
        if (channel == null) throw new UnregisteredChannelException("Unknown Channel. Please make sure to register each active channel");
        return channel;
    }

    public Collection<Channel<?>> getChannels() {
        return registeredChannels.values();
    }

    public static ChannelManager getInstance(Jedis jedis, Gson gson) {
        if (instance == null) {
            instance = new ChannelManager(jedis, gson);
        }
        instance.jedis = jedis;
        instance.gson = gson;
        return instance;
    }
}
