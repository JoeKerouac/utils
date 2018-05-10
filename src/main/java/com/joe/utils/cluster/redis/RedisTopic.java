package com.joe.utils.cluster.redis;

import com.joe.utils.cluster.Topic;
import com.joe.utils.cluster.TopicMessageListener;
import org.redisson.api.RTopic;

import java.util.List;

/**
 * @author joe
 * @version 2018.05.08 11:40
 */
public class RedisTopic<M> implements Topic<M> {
    private final RTopic<M> topic;
    public RedisTopic(RTopic<M> topic) {
        this.topic = topic;
    }

    @Override
    public List<String> getChannelNames() {
        return topic.getChannelNames();
    }

    @Override
    public long publish(M message) {
        return topic.publish(message);
    }

    @Override
    public int addListener(TopicMessageListener<M> listener) {
        return topic.addListener(listener::onMessage);
    }

    @Override
    public void removeListener(int listenerId) {
        topic.removeListener(listenerId);
    }
}
