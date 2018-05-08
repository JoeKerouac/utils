package com.joe.utils.cluster;

/**
 * topic message listener
 *
 * @author joe
 * @version 2018.05.08 11:38
 */
public interface TopicMessageListener<M> {
    /**
     * Invokes on every message in topic
     *
     * @param channel of topic
     * @param msg     topic message
     */
    void onMessage(String channel, M msg);
}
