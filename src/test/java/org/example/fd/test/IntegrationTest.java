package org.example.fd.test;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.example.fd.engine.RuleContextConfig;
import org.example.fd.engine.SimpleRuleEngine;
import org.example.fd.input.KafkaTransferEventConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class IntegrationTest {
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private SimpleRuleEngine simpleRuleEngine;

	@Autowired
	private KafkaTransferEventConsumer kafkaTransferEventConsumer;

	@Test
	public void testRiskEvents() throws InterruptedException {
		RuleContextConfig ruleContextConfig = new RuleContextConfig();
		ruleContextConfig.setMaxEventCount(20);
		ruleContextConfig.setMaxConsecutiveCount(5);
		ruleContextConfig.setAmountThreshold(5000);
		ruleContextConfig.setTimeWindow(Duration.ofMinutes(1));
		ruleContextConfig.getTargetUserIds().add("高危用户001");

		simpleRuleEngine.setRuleConfig(ruleContextConfig);

		boolean waiting = false;
		while(!kafkaTransferEventConsumer.isRunning()) {
			if(!waiting) {
				System.out.println("waiting consumer...");
				waiting = true;
			}
		}

		double[] amounts = new double[] {1000, 5000, 12000};
		Random random = new Random();
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() < startTime + 5*1000) {
			int amountIndex = random.nextInt(amounts.length);
			String msg = "{"
				+ "  \"userId\": \"用户002\","
				+ "  \"eventType\": \"TRANSFER\","
				+ "  \"amount\": \"" + amounts[amountIndex] + "\","
				+ "  \"eventTime\": \""+ System.currentTimeMillis() +"\"\n"
				+ "}";
			System.out.println("Send kafka msg: " + msg);
			kafkaTemplate.send("risk-events", msg);
			TimeUnit.MILLISECONDS.sleep(200);
		}
		TimeUnit.SECONDS.sleep(5);
	}
}
