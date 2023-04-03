package lapfarsc.dto;

import java.math.BigDecimal;

public class MaquinaStatusDTO {

	private Integer codigo;
	private Integer maquinaCodigo;
	
	private Boolean iniciarJob;
	private Boolean online;
	private BigDecimal cpuUsed;
	private BigDecimal memUsed;
	
	
	public Integer getCodigo() {
		return codigo;
	}
	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}
	public Integer getMaquinaCodigo() {
		return maquinaCodigo;
	}
	public void setMaquinaCodigo(Integer maquinaCodigo) {
		this.maquinaCodigo = maquinaCodigo;
	}
	public Boolean getIniciarJob() {
		return iniciarJob;
	}
	public void setIniciarJob(Boolean iniciarJob) {
		this.iniciarJob = iniciarJob;
	}
	public Boolean getOnline() {
		return online;
	}
	public void setOnline(Boolean online) {
		this.online = online;
	}
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


