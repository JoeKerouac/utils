package com.joe.utils.cluster.redis;

import com.joe.utils.cluster.ClusterManager;
import com.joe.utils.common.Assert;
import org.junit.Test;

/**
 * 测试RedisClusterManager
 *
 * @author joe
 * @version 2018.08.01 16:22
 */
public class RedisClusterManagerTest {
    private String         host       = "192.168.2.222";
    private int            port       = 7001;

    @Test
    public void test() throws Exception{
        ClusterManager clusterManager = ClusterManager.getInstance(host, port);
        Assert.notNull(clusterManager);
        clusterManager.shutdown();
    }
}
