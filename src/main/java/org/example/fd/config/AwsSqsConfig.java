package org.example.fd.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aws.sqs")
public class AwsSqsConfig {
	private String region;
	private String queueUrl;
	private String accessKey;
	private String secretKey;
	private int maxNumberOfMessages=1;
	private int waitTimeSeconds=1;
}
