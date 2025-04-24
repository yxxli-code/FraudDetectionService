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
public class AmountRuleHandler extends RuleHandler{

	public AmountRuleHandler() {
		this.setRuleType(RuleType.AMOUNT_LIMIT);
	}

	@Override
	protected boolean canHandle(RiskEvent event) {
		return (event != null && EventType.TRANSFER.equals(event.getEventType()));
	}

	@Override
	protected void process(UserState userState, RiskEvent event, Consumer<DecisionResult> resultHandler) {
		if (event.getAmount() > userState.getRuleConfig().getAmountThreshold()) {
			DecisionResult result = new DecisionResult();
			result.setUserId(event.getUserId());
			result.setRuleName("金额超限预警");
			result.setMessage("单次转账金额[" + event.getAmount() + "]超过阈值[" +
					userState.getRuleConfig().getAmountThreshold() + "]");
			result.setRiskLevel(RiskLevel.MEDIUM.name());
			result.setCreateTime(System.currentTimeMillis());
			resultHandler.accept(result);
		}
	}
}
