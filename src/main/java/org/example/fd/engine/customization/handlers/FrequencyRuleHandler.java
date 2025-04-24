package org.example.fd.engine.customization.handlers;

import java.util.function.Consumer;
import org.example.fd.engine.DecisionResult;
import org.example.fd.engine.customization.RiskEvent;
import org.example.fd.engine.RiskLevel;
import org.example.fd.engine.UserState;
import org.example.fd.engine.customization.RuleType;
import org.springframework.stereotype.Component;

@Component
public class FrequencyRuleHandler extends RuleHandler{

	public FrequencyRuleHandler() {
	   this.setRuleType(RuleType.BEHAVIOR_FREQUENCY);
	}

	@Override
	protected boolean canHandle(RiskEvent event) {
		return event != null && event.getEventType() != null;
	}

	@Override
	protected void process(UserState userState, RiskEvent event, Consumer<DecisionResult> resultHandler) {
		int eventCount = userState.getEventCountInWindow();
		if (eventCount > userState.getRuleConfig().getMaxEventCount()) {
			DecisionResult result = new DecisionResult();
			result.setUserId(event.getUserId());
			result.setRuleName("高频操作预警");
			result.setMessage("用户[" + event.getUserId() + "]在" +
				userState.getRuleConfig().getTimeWindow().toMinutes() + "分钟内操作" +
				eventCount + "次，超过限制[" +
				userState.getRuleConfig().getMaxEventCount() + "]");
			result.setRiskLevel(RiskLevel.MEDIUM.name());
			result.setCreateTime(System.currentTimeMillis());
			resultHandler.accept(result);
		}
	}
}
