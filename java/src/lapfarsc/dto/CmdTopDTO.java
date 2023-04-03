package lapfarsc.dto;

import java.math.BigDecimal;

public class CmdTopDTO {

	private BigDecimal cpuUsed;
	private BigDecimal memUsed;
	
	
	public BigDecimal getCpuUsed() {
		return cpuUsed;
	}
	public void setCpuUsed(BigDecimal cpuUsed) {
		this.cpuUsed = cpuUsed;
	}
	public BigDecimal getMemUsed() {
		return memUsed;
	}
	public void setMemUsed(BigDecimal memUsed) {
		this.memUsed = memUsed;
	}
	
	
}
