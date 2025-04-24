package org.example.fd.input;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.example.fd.engine.DecisionResult;
import org.example.fd.engine.SimpleRuleEngine;
import org.example.fd.engine.customization.RiskEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;

@Slf4j
public abstract class BaseConsumer implements ApplicationListener<WebServerInitializedEvent>, SmartLifecycle {
	private volatile boolean isRunning;

	private final String name;
	private final SimpleRuleEngine simpleRuleEngine;

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public BaseConsumer(String name, SimpleRuleEngine simpleRuleEngine) {
		this.name = name;
		this.simpleRuleEngine = simpleRuleEngine;
	}

	protected SimpleRuleEngine getSimpleRuleEngine() {
		return this.simpleRuleEngine;
	}

	protected void setRunning(boolean running) {
		this.isRunning = running;
	}

	protected abstract void consume();
	protected abstract void sendDecisionResult(DecisionResult decisionResult);

	@Override
	public void onApplicationEvent(WebServerInitializedEvent event) {
		log.info("starting fraud detection engine {}", this.name);
		Thread pollThread = new Thread(this::consume);
		pollThread.setDaemon(true);
		pollThread.start();
		scheduler.scheduleAtFixedRate(this::reloadRules, 0, 5, TimeUnit.MINUTES);
	}

	protected void reloadRules() {
		simpleRuleEngine.reloadRules();
	}

	protected void processEvent(RiskEvent event) {
		if(event != null) {
			simpleRuleEngine.processEvent(event, (result) -> sendDecisionResult(result));
		}
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
		scheduler.shutdown();
		try {
			scheduler.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		isRunning = false;
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}
}
