package org.example.fd.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import org.example.fd.engine.customization.RiskEvent;

@Data
public class UserState {
	@JsonInclude
	private int version;

	@JsonInclude
	private Long lastUpdatedTime;

	@JsonIgnore
	private RuleContextConfig ruleConfig;

	// 时间窗口事件队列
	@JsonInclude
	private LinkedList<RiskEvent> eventWindow = new LinkedList<>();

	// 连续事件计数器（记录当前连续发生的事件类型及次数）
	@JsonInclude
	private Map<String, Integer> consecutiveEventCounter = new ConcurrentHashMap<>();

	public UserState() {
		this.lastUpdatedTime = System.currentTimeMillis();
	}

	public void setRuleConfig(RuleContextConfig ruleConfig) {
		this.ruleConfig = ruleConfig;
	}

	@JsonIgnore
	public RuleContextConfig getRuleConfig() {
		return this.ruleConfig;
	}

	// 添加事件到时间窗口
	public void add(RiskEvent event) {
		this.lastUpdatedTime = System.currentTimeMillis();
		// 清理超过window的事件
		long windowEnd = event.getEventTime() - this.getRuleConfig().getTimeWindow().toMillis();
		eventWindow.removeIf(e -> e.getEventTime() < windowEnd);
		eventWindow.add(event);

		String eventType = event.getEventType().name();
		consecutiveEventCounter.put(eventType, consecutiveEventCounter.getOrDefault(eventType, 0) + 1);
	}

	// 获取上次更新时间
	public Long getLastUpdatedTime() {
		return this.lastUpdatedTime;
	}

	// 获取时间窗口内事件数量
	@JsonIgnore
	public int getEventCountInWindow() {
		return eventWindow.size();
	}

	// 获取时间窗口内的转账记录
	@JsonIgnore
	public List<RiskEvent> getRecentEvents() {
		return eventWindow;
	}

	// 获取最新事件（用于预警信息）
	@JsonIgnore
	public RiskEvent getLatestEvent() {
		return eventWindow.isEmpty() ? null : eventWindow.getLast();
	}

	// 获取指定事件类型的连续发生次数
	@JsonIgnore
	public int getConsecutiveCount(String eventType) {
		return consecutiveEventCounter.getOrDefault(eventType, 0);
	}

	// 重置连续事件计数器（用于序列检测后）
	public void resetConsecutiveCounter(String eventType) {
		consecutiveEventCounter.put(eventType, 0);
	}

}
