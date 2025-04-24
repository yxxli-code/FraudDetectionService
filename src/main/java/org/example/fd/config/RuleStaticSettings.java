package org.example.fd.config;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "x-rule")
public class RuleStaticSettings {
	private int version;

	// 金额规则配置
	private double amountThreshold;       // 金额阈值

	// 用户规则配置
	private Set<String> targetUserIds = new HashSet<>();    // 目标用户集合

	// 次数规则配置
	private int timeWindowMinutes;          // 时间窗口
	private int maxEventCount;         // 窗口时间内最大允许次数
	private int maxConsecutiveCount;        // 同一事件连续出现次数

	// 规则优先级配置
	private Set<String> rules = new HashSet<>();
}
