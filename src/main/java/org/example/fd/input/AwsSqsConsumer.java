package org.example.fd.input;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.example.fd.config.AwsSqsConfig;
import org.example.fd.config.JsonHandler;
import org.example.fd.engine.DecisionResult;
import org.example.fd.engine.SimpleRuleEngine;
import org.example.fd.engine.customization.RiskEvent;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Slf4j
@Component
public class AwsSqsConsumer extends BaseConsumer {
	private AwsSqsConfig awsSqsConfig;

	public AwsSqsConsumer(AwsSqsConfig awsSqsConfig, SimpleRuleEngine simpleRuleEngine) {
		super("AWS-SQS-consumer-1", simpleRuleEngine);
		this.awsSqsConfig = awsSqsConfig;
	}

	@Override
	public void consume() {
		Region region = Region.of(awsSqsConfig.getRegion());
		String queueUrl = awsSqsConfig.getQueueUrl();

		AwsCredentialsProvider credentialsProvider;
		if(awsSqsConfig.getAccessKey() != null && awsSqsConfig.getSecretKey() != null){
			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(awsSqsConfig.getAccessKey(), awsSqsConfig.getSecretKey());
			credentialsProvider = StaticCredentialsProvider.create(awsCreds);
		} else {
			credentialsProvider = DefaultCredentialsProvider.create();
		}

		SqsClient sqsClient = SqsClient.builder()
			.region(region)
			.credentialsProvider(credentialsProvider)
			.build();

		try {
			setRunning(true);
			while (true) {
				// 接收消息
				ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
					.queueUrl(queueUrl)
					.maxNumberOfMessages(awsSqsConfig.getMaxNumberOfMessages())
					.waitTimeSeconds(awsSqsConfig.getWaitTimeSeconds())
					.build();

				List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

				if (!messages.isEmpty()) {
					for (Message message : messages) {
						// 处理消息
						log.info("Message received: " + message.body());
						RiskEvent event = (RiskEvent) JsonHandler.getInstance().readValue(message.body(), RiskEvent.class);
						processEvent(event);
						// 确认消息
						deleteMessage(sqsClient, queueUrl, message);
					}
				}
			}
		} catch (SqsException e) {
			e.printStackTrace();
		}

		sqsClient.close();
	}

	private static void deleteMessage(SqsClient sqsClient, String queueUrl, Message message) {
		try {
			DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
				.queueUrl(queueUrl)
				.receiptHandle(message.receiptHandle())
				.build();
			sqsClient.deleteMessage(deleteRequest);
			log.info("Message deleted: " + message.messageId());
		} catch (SqsException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void sendDecisionResult(DecisionResult result) {
		result.setCreateTime(System.currentTimeMillis());
		log.info("发送决策结果：{}", result);
	}

}
