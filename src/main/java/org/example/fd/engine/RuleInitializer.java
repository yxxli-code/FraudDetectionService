package org.example.fd.engine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.example.fd.config.JsonHandler;
import org.example.fd.config.RuleStaticSettings;
import org.example.fd.engine.customization.RuleType;
import org.example.fd.engine.customization.handlers.RuleHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RuleInitializer {
	private static final String X_RULE_CONFIG ="x-rule:config:";

	private KVCache kvCache;
	private Map<String, RuleHandler> ruleTypeHandlerMap = new ConcurrentHashMap<>();
	private RuleStaticSettings ruleStaticSettings;

	public RuleInitializer(KVCache kvCache, RuleStaticSettings ruleStaticSettings, List<RuleHandler> ruleHandlers) {
		this.kvCache = kvCache;
		this.ruleStaticSettings = ruleStaticSettings;
		for(RuleHandler ruleHandler: ruleHandlers) {
			ruleTypeHandlerMap.put(ruleHandler.getRuleType().name(), ruleHandler);
		}
	}

	private RuleHandler getRuleHandler(RuleType ruleType) {
		return ruleTypeHandlerMap.get(ruleType.name());
	}

	public List<RiskRule> loadRules() {
		List<RiskRule> rules = new ArrayList<>();
		for(String rulePri: ruleStaticSettings.getRules()) {
			String[] typeAndPri = rulePri.split("#");
			RuleType ruleType = RuleType.valueOf(typeAndPri[0]);
		    rules.add(new RiskRule(ruleType, Integer.valueOf(typeAndPri[1]), this.getRuleHandler(ruleType)));
		}
		rules.sort(Comparator.naturalOrder());
		return rules;
	}

	public RuleContextConfig loadRuleConfig() {
		RuleContextConfig ruleContextConfig = new RuleContextConfig();
		ruleContextConfig.setVersion(ruleStaticSettings.getVersion());
		ruleContextConfig.setMaxEventCount(ruleStaticSettings.getMaxEventCount());
		ruleContextConfig.setMaxConsecutiveCount(ruleStaticSettings.getMaxConsecutiveCount());
		ruleContextConfig.setAmountThreshold(ruleStaticSettings.getAmountThreshold());
		ruleContextConfig.setTimeWindow(Duration.ofMinutes(ruleStaticSettings.getTimeWindowMinutes()));
		log.info("loaded rule config {}", JsonHandler.getInstance().writeAsString(ruleContextConfig));
		return ruleContextConfig;
	}

	public void saveRuleConfig(RuleContextConfig ruleContextConfig) {
		String jsonValue = JsonHandler.getInstance().writeAsString(ruleContextConfig);
		kvCache.set(X_RULE_CONFIG + ruleStaticSettings.getVersion(), jsonValue);
	}

	public RuleContextConfig reloadRuleConfig() {
        //load from redis
		String json = kvCache.get(X_RULE_CONFIG + ruleStaticSettings.getVersion());
		if(json != null) {
			RuleContextConfig ruleContextConfig = (RuleContextConfig) JsonHandler.getInstance().readValue(json, RuleContextConfig.class);
			log.info("reloaded rule config {}", JsonHandler.getInstance().writeAsString(ruleContextConfig));
			return ruleContextConfig;
		}
		return null;
	}
}
