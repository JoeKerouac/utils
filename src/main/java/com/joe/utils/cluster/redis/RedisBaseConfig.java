package com.joe.utils.cluster.redis;

import java.io.Serializable;

import lombok.Data;

/**
 * RedisBaseConfig
 *
 * @author joe
 * @version 2018.04.18 10:41
 */
@Data
public abstract class RedisBaseConfig implements Serializable {
    /**
     * If pooled connection not used for a timeout time
     * and current connections amount bigger than minimum idle connections pool size,
     * then it will closed and removed from pool.
     * Value in milliseconds.
     */
    private int    idleConnectionTimeout      = 10000;

    /**
     * Ping timeout used in Node.ping and Node.pingAll operation.
     * Value in milliseconds.
     */
    private int    pingTimeout                = 1000;

    /**
     * Timeout during connecting to any Redis server.
     * Value in milliseconds.
     */
    private int    connectTimeout             = 10000;

    /**
     * Redis server response timeout. Starts to countdown when Redis command was succesfully sent.
     * Value in milliseconds.
     */
    private int    timeout                    = 3000;

    private int    retryAttempts              = 3;

    private int    retryInterval              = 1500;

    /**
     * Reconnection attempt timeout to Redis server then
     * it has been excluded from internal list of available servers.
     * <p>
     * On every such timeout event Redisson tries
     * to connect to disconnected Redis server.
     *
     * @see #failedAttempts
     */
    private int    reconnectionTimeout        = 3000;

    /**
     * Redis server will be excluded from the list of available nodes
     * when sequential unsuccessful execution attempts of any Redis command
     * reaches failedAttempts.
     */
    private int    failedAttempts             = 3;

    /**
     * Password for Redis authentication. Should be null if not needed
     */
    private String password;

    /**
     * Subscriptions per Redis connection limit
     */
    private int    subscriptionsPerConnection = 5;

    /**
     * Name of client connection
     */
    private String clientName;
}
