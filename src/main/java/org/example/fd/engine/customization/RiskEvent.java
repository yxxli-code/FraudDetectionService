package org.example.fd.engine.customization;

import java.io.Serializable;
import lombok.Data;
import org.example.fd.config.JsonHandler;

@Data
public class RiskEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	private int version;
	private String userId;
	private Long eventTime;
	private EventType eventType;
	private double amount;

	@Override
	public String toString() {
		return JsonHandler.getInstance().writeAsString(this);
	}
}
