package org.example.fd.input;

import java.time.Duration;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.example.fd.config.JsonHandler;
import org.example.fd.engine.DecisionResult;
import org.example.fd.engine.SimpleRuleEngine;
import org.example.fd.engine.customization.RiskEvent;
import org.example.fd.output.KafkaResultProducer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaTransferEventConsumer extends BaseConsumer {

	private final ConsumerFactory<String, String> consumerFactory;

	private KafkaConsumer<String, String> consumer;

	private final KafkaResultProducer resultProducer;

	public KafkaTransferEventConsumer(ConsumerFactory<String, String> consumerFactory, KafkaResultProducer resultProducer, SimpleRuleEngine simpleRuleEngine) {
		super("Kafka-consumer-1", simpleRuleEngine);
		this.consumerFactory = consumerFactory;
		this.resultProducer = resultProducer;
	}

	@Override
	public void consume() {
		getSimpleRuleEngine().loadRules();

		consumer = (KafkaConsumer<String, String>) consumerFactory.createConsumer();
		consumer.subscribe(Collections.singletonList("risk-events"));

		try {
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				if(!isRunning() && !consumer.assignment().isEmpty()) {
					setRunning(true);
				}
				for (ConsumerRecord<String, String> record : records) {
					RiskEvent event = (RiskEvent) JsonHandler.getInstance().readValue(record.value(), RiskEvent.class);
					log.info("Received message: {}", event);
					processEvent(event);
				}
			}
		} catch (Exception e) {
			log.warn("failed to schedule consumer " + e.getMessage());
		}
	}

	@Override
	protected void sendDecisionResult(DecisionResult result) {
		result.setCreateTime(System.currentTimeMillis());
		resultProducer.sendResult(result, "risk-decisions");
		log.info("发送决策结果：{}", result);
	}
}