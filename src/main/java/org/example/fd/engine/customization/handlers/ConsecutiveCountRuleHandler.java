package org.example.fd.engine.customization.handlers;

import java.util.function.Consumer;
import org.example.fd.engine.DecisionResult;
import org.example.fd.engine.customization.RiskEvent;
import org.example.fd.engine.RiskLevel;
import org.example.fd.engine.UserState;
import org.example.fd.engine.customization.EventType;
import org.example.fd.engine.customization.RuleType;
import org.springframework.stereotype.Component;

@Component
public class ConsecutiveCountRuleHandler extends RuleHandler {

	public ConsecutiveCountRuleHandler() {
		this.setRuleType(RuleType.CONSECUTIVE_COUNT);
	}

	@Override
	protected boolean canHandle(RiskEvent event) {
		return event != null && EventType.TRANSFER.equals(event.getEventType());
	}

	@Override
	protected void process(UserState userState, RiskEvent event, Consumer<DecisionResult> resultHandler) {
		int consecutiveCount = userState.getConsecutiveCount(event.getEventType().name());
		if (consecutiveCount > userState.getRuleConfig().getMaxConsecutiveCount()) {
			//userState.resetConsecutiveCounter(EventType.TRANSFER.name());

			DecisionResult result = new DecisionResult();
			result.setUserId(event.getUserId());
			result.setRuleName("高频转账预警");
			result.setMessage("用户[" + event.getUserId() + "]在" +
				userState.getRuleConfig().getTimeWindow().toMinutes() + "分钟内转账" +
				consecutiveCount + "次，超过限制[" +
				userState.getRuleConfig().getMaxConsecutiveCount() + "]");
			result.setRiskLevel(RiskLevel.HIGH.name());
			result.setCreateTime(System.currentTimeMillis());
			resultHandler.accept(result);
		}
	}
}
