package org.example.fd.engine.customization;

import java.util.concurrent.TimeUnit;
import org.example.fd.engine.KVCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisKVCache implements KVCache {
	private StringRedisTemplate redisTemplate;

	public RedisKVCache(StringRedisTemplate redisTemplate) {
	   this.redisTemplate = redisTemplate;
	}

	@Override
	public void set(String key, String jsonValue) {
		this.redisTemplate.opsForValue().set(key, jsonValue);
	}

	@Override
	public String get(String key) {
		return this.redisTemplate.opsForValue().get(key);
	}

	@Override
	public void set(String key, String jsonValue, int timeout, TimeUnit timeUnit) {
		this.redisTemplate.opsForValue().set(key, jsonValue, timeout, timeUnit);
	}

	@Override
	public void remove(String key) {
		this.redisTemplate.delete(key);
	}

}
