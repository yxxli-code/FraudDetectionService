package org.example.fd.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.example.fd.config.JsonHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserStateTracker {
	private static final String X_USER_STATE = "x-user:state:";
	private final KVCache kvCache;
	private Map<String, UserState> userStateMap = new ConcurrentHashMap<>();

	public UserStateTracker(KVCache kvCache) {
		this.kvCache = kvCache;
	}

	public UserState computeIfAbsent(String userId, Function<String, UserState> userStateFunction, RuleContextConfig ruleConfig) {
		String key = X_USER_STATE + ruleConfig.getVersion() + ":" + userId;
		UserState userState = userStateMap.get(userId);
		if (userState == null) {
			//not found in local cache, try to get it from redis
			String json = kvCache.get(key);
			if (json != null) {
				//found in redis
				log.info("load from redis {}", key);
				userState = (UserState) JsonHandler.getInstance().readValue(json, UserState.class);

				if(userState != null) {
					//remove expired events
					long windowEnd = System.currentTimeMillis() - ruleConfig.getTimeWindow().toMillis();
					userState.getEventWindow().removeIf(e -> e.getEventTime() < windowEnd);
					//reset consecutiveEventCounter
					Map<String, Integer> consecutiveCounts = new ConcurrentHashMap<>();
					userState.getEventWindow().stream().forEach(e -> {
						String eventType = e.getEventType().name();
						consecutiveCounts.put(eventType, consecutiveCounts.getOrDefault(eventType, 0) + 1);
					});
					userState.setConsecutiveEventCounter(consecutiveCounts);
				} else {
					userState = userStateFunction.apply(userId);
					kvCache.set(key, JsonHandler.getInstance().writeAsString(userState), 1, TimeUnit.DAYS);
				}
			} else {
				//not found in redis either
				userState = userStateFunction.apply(userId);
				kvCache.set(key, JsonHandler.getInstance().writeAsString(userState), 1, TimeUnit.DAYS);
			}
			//set it to local cache
			userState.setRuleConfig(ruleConfig);
			userStateMap.put(userId, userState);
		} else {
			//TODO load userState from KVCache on process of every event so that the rule to handle consecutive events can be handled properly if the service is deployed with multiple instances
		}
		return userState;
	}

	public void saveState(String userId, UserState userState) {
		//refresh the data and expiration
		String key = X_USER_STATE + userState.getRuleConfig().getVersion() + ":" + userId;
		kvCache.set(key, JsonHandler.getInstance().writeAsString(userState), 1, TimeUnit.DAYS);
	}
}
