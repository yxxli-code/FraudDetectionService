package org.example.fd.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	public DefaultKafkaConsumerFactory<String, String> consumerFactory(KafkaConfig kafkaConfig) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
			kafkaConfig.getBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG,
			kafkaConfig.getGroupId());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
			StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
			StringDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(props);
	}
}