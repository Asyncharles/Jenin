package net.charles.messaging;

import net.charles.exceptions.messaging.UnregisteredChannelException;

import java.util.Collection;
import java.util.LinkedHashMap;

public class ChannelManager {
    private final LinkedHashMap<String, Channel<?>> registeredChannels;

    public ChannelManager() {
        registeredChannels = new LinkedHashMap<>();
    }

    public void registerChannel(Channel<?> channel) {
        registeredChannels.put(channel.getName(), channel);
    }

    public void registerChannels(Channel<?>... channels) {
        for (Channel<?> channel : channels) registeredChannels.put(channel.getName(), channel);
    }

    public Channel<?> queryChannel(String name) {
        final Channel<?> channel =  registeredChannels.get(name);
        if (channel == null) throw new UnregisteredChannelException("Unknown Channel. Please make sure to register each active channel");
        return channel;
    }

    public Collection<Channel<?>> getChannels() {
        return registeredChannels.values();
    }
}
