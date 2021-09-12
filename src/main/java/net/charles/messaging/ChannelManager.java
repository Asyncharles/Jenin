package net.charles.messaging;

import com.google.gson.Gson;
import net.charles.Jenin;
import net.charles.exceptions.messaging.UnregisteredChannelException;
import net.charles.mapper.JeninMapper;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;

public final class ChannelManager {
    private static ChannelManager instance = null;

    private final LinkedHashMap<String, Channel<?>> registeredChannels;
    private final JeninMapper jenin;
    private Gson gson;
    private boolean isSubscribed;

    private ChannelManager(JeninMapper jenin, Gson gson) {
        this.registeredChannels = new LinkedHashMap<>();
        this.jenin = jenin;
        this.gson = gson;
        this.isSubscribed = false;
    }

    public void registerChannels(Channel<?>... channels) {
        //if (isSubscribed) throw new SubscriptionException("Channels are already registered, please reload the Redis PubSub.");
        try {
            for (Channel<?> channel : channels) {
                registeredChannels.put(channel.getName(), channel);
            }
        } finally {
            new Thread(() -> {
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
                jenin.getTemporaryJedisInstance(jedis -> jedis.subscribe(jedisPubSub, registeredChannels.keySet().toArray(String[]::new)));
            }).start();
        }
    }

    public void unregisterChannels(String... channels) {
        new Thread(() -> {
            jenin.getTemporaryJedisInstance(jedis -> {
                JedisPubSub jedisPubSub = new JedisPubSub() {
                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        registeredChannels.get(channel).getSubscriptionHandler().onUnSubscribe(channel);
                    }
                };
                jedisPubSub.unsubscribe(channels);
                jedis.subscribe(jedisPubSub, null);
            });
        }).start();
        for (String name : channels) registeredChannels.remove(name);
    }

    public <O> void publish(Jenin jenin, String channel, O obj) {
        jenin.getTemporaryJedisInstance(j -> j.publish(channel, gson.toJson(obj)));
    }

    public Channel<?> queryChannel(String name) {
        final Channel<?> channel =  registeredChannels.get(name);
        if (channel == null) throw new UnregisteredChannelException("Unknown Channel. Please make sure to register each active channel");
        return channel;
    }

    public Collection<Channel<?>> getChannels() {
        return registeredChannels.values();
    }

    public static ChannelManager getInstance(JeninMapper jenin, Gson gson) {
        if (instance == null) {
            instance = new ChannelManager(jenin, gson);
        }
        instance.gson = gson;
        return instance;
    }

    protected static ChannelManager getRawInstance() {
        return instance;
    }
}
