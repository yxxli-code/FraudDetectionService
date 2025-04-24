package org.example.fd.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonHandler<T> {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static volatile JsonHandler jsonHandler;

	static {
		objectMapper.registerModule(new JavaTimeModule());
	}

	public static JsonHandler getInstance() {
		if (jsonHandler == null) {
			synchronized (JsonHandler.class) {
				if (jsonHandler == null) {
					jsonHandler = new JsonHandler();
				}
			}
		}
		return jsonHandler;
	}

	private static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public T readValue(String str, Class<T> tClass) {
		try {
			return getObjectMapper().readValue(str, tClass);
		} catch (JsonProcessingException e) {
			log.error("failed to readValue {}", e.getMessage());
			return null;
		}
	}

	public String writeAsString(Object obj) {
		try {
			return getObjectMapper().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("failed to writeAsString {}", e.getMessage());
			return null;
		}
	}
}
