package org.example.fd.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.example.fd.config.RuleStaticSettings;
import org.example.fd.engine.DecisionResult;
import org.example.fd.engine.KVCache;
import org.example.fd.engine.RuleContextConfig;
import org.example.fd.engine.RuleInitializer;
import org.example.fd.engine.SimpleRuleEngine;
import org.example.fd.engine.UserStateTracker;
import org.example.fd.engine.customization.EventType;
import org.example.fd.engine.customization.RiskEvent;
import org.example.fd.engine.customization.handlers.AmountRuleHandler;
import org.example.fd.engine.customization.handlers.ConsecutiveCountRuleHandler;
import org.example.fd.engine.customization.handlers.FrequencyRuleHandler;
import org.example.fd.engine.customization.handlers.RuleHandler;
import org.example.fd.engine.customization.handlers.SpecificUserRuleHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CoreTest {
	private KVCache kvCache;
	private RuleStaticSettings ruleStaticSettings;
	private UserStateTracker userStateTracker;
	private SimpleRuleEngine simpleRuleEngine;
	private RuleInitializer ruleInitializer;

	@BeforeEach
	public void beforeTest() {
		kvCache = new MemoryKVCache();
		ruleStaticSettings = new RuleStaticSettings();
		ruleStaticSettings.setMaxEventCount(20);
		ruleStaticSettings.setMaxConsecutiveCount(5);
		ruleStaticSettings.setAmountThreshold(5000);
		ruleStaticSettings.setTimeWindowMinutes(1);

		Set<String> rulePris = new HashSet<>();
		rulePris.add("AMOUNT_LIMIT#1");
		rulePris.add("BEHAVIOR_FREQUENCY#2");
		rulePris.add("SPECIFIC_USER#2");
		rulePris.add("CONSECUTIVE_COUNT#3");

		ruleStaticSettings.setRules(rulePris);

		List<RuleHandler> rulesHandlers = new ArrayList<>();
		rulesHandlers.add(new AmountRuleHandler());
		rulesHandlers.add(new FrequencyRuleHandler());
		rulesHandlers.add(new SpecificUserRuleHandler());
		rulesHandlers.add(new ConsecutiveCountRuleHandler());

		ruleInitializer = new RuleInitializer(kvCache, ruleStaticSettings, rulesHandlers);

		userStateTracker = new UserStateTracker(kvCache);

		simpleRuleEngine = new SimpleRuleEngine(ruleInitializer, userStateTracker);

	}

	@Test
	public void testRules1() throws InterruptedException {
		// 1. load rules
		simpleRuleEngine.loadRules();

		RuleContextConfig ruleContextConfig = new RuleContextConfig();
		ruleContextConfig.setMaxEventCount(20);
		ruleContextConfig.setMaxConsecutiveCount(5);
		ruleContextConfig.setAmountThreshold(5000);
		ruleContextConfig.setTimeWindow(Duration.ofMinutes(1));
		ruleContextConfig.getTargetUserIds().add("高危用户001");

		//2. update rule config
		simpleRuleEngine.setRuleConfig(ruleContextConfig);

		RiskEvent riskEvent1 = new RiskEvent();
		riskEvent1.setUserId("高危用户001");
		riskEvent1.setEventTime(System.currentTimeMillis());
		riskEvent1.setEventType(EventType.TRANSFER);
		riskEvent1.setAmount(6000);

		//3. process events
		List<DecisionResult> resultList = new ArrayList<>();
		simpleRuleEngine.processEvent(riskEvent1, decisionResult ->
		{
			System.out.println(decisionResult);
			resultList.add(decisionResult);
		});

		long startTime=System.currentTimeMillis();
		while(resultList.isEmpty() && System.currentTimeMillis() - startTime <=5000) {
			TimeUnit.MILLISECONDS.sleep(100);
		}
		assertEquals(2, resultList.size());
		assertTrue(resultList.get(0).getMessage().contains("检测到目标用户[高危用户001]发起转账"));
		assertTrue(resultList.get(1).getMessage().contains("单次转账金额[6000.0]超过阈值[5000.0]"));
	}

	@Test
	public void testRules2() throws InterruptedException {
		//1. load rules
		simpleRuleEngine.loadRules();

		RuleContextConfig ruleContextConfig = new RuleContextConfig();
		ruleContextConfig.setMaxEventCount(2);
		ruleContextConfig.setMaxConsecutiveCount(2);
		ruleContextConfig.setAmountThreshold(5000);
		ruleContextConfig.setTimeWindow(Duration.ofSeconds(60));
		ruleContextConfig.getTargetUserIds().add("高危用户001");

		//2. update rule config
		simpleRuleEngine.setRuleConfig(ruleContextConfig);

		//3. process events
		List<DecisionResult> resultList = new ArrayList<>();
		Consumer<DecisionResult> resultHandler = decisionResult ->
		{
			System.out.println(decisionResult);
			resultList.add(decisionResult);
		};

		RiskEvent riskEvent1 = new RiskEvent();
		riskEvent1.setUserId("用户001");
		riskEvent1.setEventTime(System.currentTimeMillis());
		riskEvent1.setEventType(EventType.TRANSFER);
		riskEvent1.setAmount(100);
		simpleRuleEngine.processEvent(riskEvent1, resultHandler);
		TimeUnit.MILLISECONDS.sleep(200);

		RiskEvent riskEvent2 = new RiskEvent();
		riskEvent2.setUserId("用户001");
		riskEvent2.setEventTime(System.currentTimeMillis());
		riskEvent2.setEventType(EventType.TRANSFER);
		riskEvent2.setAmount(10);
		simpleRuleEngine.processEvent(riskEvent2, resultHandler);
		TimeUnit.MILLISECONDS.sleep(300);

		RiskEvent riskEvent3 = new RiskEvent();
		riskEvent3.setUserId("用户001");
		riskEvent3.setEventTime(System.currentTimeMillis());
		riskEvent3.setEventType(EventType.TRANSFER);
		riskEvent3.setAmount(20);
		simpleRuleEngine.processEvent(riskEvent3, resultHandler);

		long startTime=System.currentTimeMillis();
		while(resultList.isEmpty() && System.currentTimeMillis() - startTime <=5000) {
			TimeUnit.MILLISECONDS.sleep(100);
		}
		assertEquals(2, resultList.size());
		assertTrue(resultList.get(0).getRuleName().contains("高频转账预警"));
		assertTrue(resultList.get(1).getRuleName().contains("高频操作预警"));
	}

}
