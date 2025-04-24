package org.example.fd.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.example.fd.engine.customization.RiskEvent;
import org.springframework.stereotype.Service;

@Service
public class SimpleRuleEngine {
	private RuleContextConfig ruleConfig;
	private final RuleInitializer ruleInitializer;
	private final List<RiskRule> ruleQueue = new ArrayList<>();
	private final UserStateTracker userStateTracker;

	public SimpleRuleEngine(RuleInitializer ruleInitializer, UserStateTracker userStateTracker) {
		this.ruleInitializer = ruleInitializer;
		this.userStateTracker = userStateTracker;
	}

	public void setRuleConfig(RuleContextConfig ruleConfig) {
		this.ruleConfig = ruleConfig;
		this.ruleInitializer.saveRuleConfig(ruleConfig);
	}

	public RuleContextConfig getRuleConfig() {
		return this.ruleConfig;
	}

	public void loadRules() {
		setRuleConfig(this.ruleInitializer.loadRuleConfig());
		ruleQueue.addAll(this.ruleInitializer.loadRules());
	}

	public void processEvent(RiskEvent event, Consumer<DecisionResult> resultConsumer) {
		UserState userState = userStateTracker.computeIfAbsent(
			event.getUserId(), k ->  new UserState(), ruleConfig
		);
		// 1. 更新用户转账历史
		userState.add(event);

		//do it in async?
		userStateTracker.saveState(event.getUserId(), userState);

		// 2. 执行规则检查
		ruleQueue.forEach(rule -> rule.getAction().handle(userState, event, resultConsumer));
	}

	public void reloadRules() {
		ruleConfig = this.ruleInitializer.reloadRuleConfig();
	}
}
