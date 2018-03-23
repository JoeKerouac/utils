package com.joe.utils.cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.ResourceUnit;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

public class EhcacheService {
	public static void main(String[] args) {
		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("preConfigured", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class,
						String.class, ResourcePoolsBuilder.heap(10)))
				.build();
		cacheManager.init();

		Cache<Long, String> preConfigured = cacheManager.getCache("preConfigured", Long.class, String.class);

		ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.heap(1);
		
		Cache<Long, Object> myCache = cacheManager.createCache("myCache", CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Long.class, Object.class, ResourcePoolsBuilder.heap(Long.MAX_VALUE)).build());


		myCache.put(1L, "da one!");
		myCache.put(2L, 5);
		myCache.put(3L, 3);
		
		String value = (String)myCache.get(1L);
		int i = (int)myCache.get(2L);
		System.out.println(value);
		System.out.println(i);
		cacheManager.removeCache("preConfigured");
	}
}
