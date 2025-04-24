package org.example.fd.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.example.fd.engine.KVCache;

public class MemoryKVCache implements KVCache {
	private Map<String, String> localMap = new ConcurrentHashMap<>();

	@Override
	public void set(String key, String jsonValue) {
		localMap.put(key, jsonValue);
	}

	@Override
	public String get(String key) {
		return localMap.get(key);
	}

	@Override
	public void set(String key, String jsonValue, int timeout, TimeUnit timeUnit) {
		localMap.put(key, jsonValue);
	}

	@Override
	public void remove(String key) {
		localMap.remove(key);
	}
}
