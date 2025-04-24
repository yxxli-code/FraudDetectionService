package org.example.fd.engine;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
public class RuleContextConfig {
	private int version;
	private double amountThreshold;
	private Set<String> targetUserIds = new HashSet<>();
	private Duration timeWindow;
	private int maxEventCount;
	private int maxConsecutiveCount;
}
