package com.joe.utils.cache;

import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

public class RedisCacheService {
	public static void main(String[] args) {
		Config config = new Config();
		SingleServerConfig singleServerConfig = config.useSingleServer();
//		singleServerConfig.setAddress("192.168.1.5:6379");
//		singleServerConfig.setAddress("127.0.0.1:6379");
		singleServerConfig.setAddress("59.110.136.14:6379");
		singleServerConfig.setTimeout(10 * 1000);


		RedissonClient redisson = Redisson.create(config);

		RMap<String , String> map= redisson.getMap("aa");
		System.out.println("OK----------------------------------------------------------");
//		System.out.println(map1.keySet());
		System.out.println("OK----------------------------------------------------------");
		System.out.println(map.get("1"));
		map.put("123", "123");
		System.out.println(map.get("123"));
		System.out.println("OK");
//		System.out.println("0");
//		
//		new Thread(() -> {
//			System.out.println("lock1");
//			RLock lock1 = redisson.getLock("myLock");
//			lock1.lock(5 , TimeUnit.SECONDS);
//			System.out.println("lock1");
//		}).start();
//		new Thread(() -> {
//			System.out.println("lock2");
//			RLock lock2 = redisson.getLock("myLock");
//			lock2.lock(1 , TimeUnit.SECONDS);
//			System.out.println("lock2");
//		}).start();
	}
}
