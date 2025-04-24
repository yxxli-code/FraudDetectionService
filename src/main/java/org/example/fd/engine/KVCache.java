package org.example.fd.engine;

import java.util.concurrent.TimeUnit;

public interface KVCache {
    void set(String key, String jsonValue);
	String get(String key);
	void set(String key, String jsonValue, int timeout, TimeUnit timeUnit);
	void remove(String key);
}
