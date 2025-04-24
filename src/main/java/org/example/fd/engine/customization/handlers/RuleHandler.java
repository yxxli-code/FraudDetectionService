package org.example.fd.engine.customization.handlers;

import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.example.fd.engine.UserState;
import org.example.fd.engine.DecisionResult;
import org.example.fd.engine.customization.RiskEvent;
import org.example.fd.engine.customization.RuleType;

@Slf4j
public abstract class RuleHandler {
	private RuleType ruleType;
	private RuleHandler nextHandler;

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public RuleType getRuleType() {
		return this.ruleType;
	}

	public void setNext(RuleHandler next) {
		this.nextHandler = next;
	}

	public void handle(UserState userState, RiskEvent event, Consumer<DecisionResult> resultHandler) {
		try {
			if (canHandle(event)) {
				process(userState, event, resultHandler);
			} else if (nextHandler != null) {
				nextHandler.handle(userState, event, resultHandler);
			}
		} catch (Exception e) {
			log.error("failed to handle {}", event);
		}
	}

	protected abstract boolean canHandle(RiskEvent event);
	protected abstract void process(UserState userState, RiskEvent event, Consumer<DecisionResult> resultHandler);
}
