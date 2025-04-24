package org.example.fd.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fd.engine.customization.RuleType;
import org.example.fd.engine.customization.handlers.RuleHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRule implements Comparable<RiskRule> {
	private RuleType type;
	private int priority;

	@JsonIgnore
	private RuleHandler action;

	@Override
	public int compareTo(RiskRule o) {
		return Integer.compare(o.priority, this.priority); // 优先级高的先处理
	}
}
