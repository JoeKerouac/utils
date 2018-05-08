package com.joe.utils.cluster;

import java.util.List;

/**
 * @author joe
 * @version 2018.05.08 11:36
 */
public interface Topic<M> {
    /**
     * Get topic channel names
     *
     * @return channel names
     */
    List<String> getChannelNames();

    /**
     * Publish the message to all subscribers of this topic
     *
     * @param message to send
     * @return the number of clients that received the message
     */
    long publish(M message);

    /**
     * Subscribes to this topic.
     * <code>MessageListener.onMessage</code> is called when any message
     * is published on this topic.
     *
     * @param listener for messages
     * @return locally unique listener id
     * @see TopicMessageListener
     */
    int addListener(TopicMessageListener<M> listener);

    /**
     * Removes the listener by <code>id</code> for listening this topic
     *
     * @param listenerId - listener id
     */
    void removeListener(int listenerId);
}
