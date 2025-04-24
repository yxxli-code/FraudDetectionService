package org.example.fd.output;

import lombok.extern.slf4j.Slf4j;
import org.example.fd.config.JsonHandler;
import org.example.fd.engine.DecisionResult;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaResultProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaResultProducer(KafkaTemplate kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	/**
	 * 发送决策结果到Kafka主题
	 */
	public void sendResult(DecisionResult result, String topic) {
		try {
			String json = JsonHandler.getInstance().writeAsString(result);
			kafkaTemplate.send(topic, result.getUserId(), json).addCallback(result1 -> {
			}, exception -> {
				log.error("发送决策结果失败：" + exception.getMessage());
			});
		} catch (Exception e) {
			throw new RuntimeException("决策结果序列化失败", e);
		}
	}

}
