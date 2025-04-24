package org.example.fd.engine;

import java.io.Serializable;
import lombok.Data;

@Data
public class DecisionResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private int version;
	private String userId;          // 关联用户ID
	private String ruleName;        // 触发的规则名称
	private String riskLevel;       // 风险等级（HIGH/MEDIUM/LOW）
	private String message;         // 决策描述
	private Long createTime; // 决策时间
}
