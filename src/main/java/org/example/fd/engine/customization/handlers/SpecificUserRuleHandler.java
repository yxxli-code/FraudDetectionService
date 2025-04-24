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
public class SpecificUserRuleHandler extends RuleHandler{

	public SpecificUserRuleHandler() {
		this.setRuleType(RuleType.SPECIFIC_USER);
	}

	@Override
	protected boolean canHandle(RiskEvent event) {
		return (event != null && EventType.TRANSFER.equals(event.getEventType()));
	}

	@Override
	protected void process(UserState userState, RiskEvent event, Consumer<DecisionResult> resultHandler) {
		if (userState.getRuleConfig().getTargetUserIds().contains(event.getUserId())) {
			DecisionResult result = new DecisionResult();
			result.setUserId(event.getUserId());
			result.setRuleName("目标用户预警");
			result.setMessage("检测到目标用户[" + event.getUserId() + "]发起转账");
			result.setRiskLevel(RiskLevel.HIGH.name());
			result.setCreateTime(System.currentTimeMillis());
			resultHandler.accept(result);
		}
	}
}
