package lapfarsc.dto;

import java.math.BigDecimal;

public class MaquinaDTO {

	private Integer codigo;
	private String nome;
	private String ssh;
	private String senha;
	private String rootPath;
	private String jarPath;
	private Integer minCpu;
	private Integer maxCpu;
	
	private BigDecimal cpuUsed;
	private BigDecimal memUsed;
	private String ultimoAcesso;
	
	private Boolean iniciarJob;
	private Boolean online;
	private Boolean ignorar;
	
	public Integer getCodigo() {
		return codigo;
	}
	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getSsh() {
		return ssh;
	}
	public void setSsh(String ssh) {
		this.ssh = ssh;
	}
	public String getSenha() {
		return senha;
	}
	public void setSenha(String senha) {
		this.senha = senha;
	}
	public String getRootPath() {
		return rootPath;
	}
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
	public String getJarPath() {
		return jarPath;
	}
	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}
	public Integer getMinCpu() {
		return minCpu;
	}
	public void setMinCpu(Integer minCpu) {
		this.minCpu = minCpu;
	}
	public Integer getMaxCpu() {
		return maxCpu;
	}
	public void setMaxCpu(Integer maxCpu) {
		this.maxCpu = maxCpu;
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
	public String getUltimoAcesso() {
		return ultimoAcesso;
	}
	public void setUltimoAcesso(String ultimoAcesso) {
		this.ultimoAcesso = ultimoAcesso;
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
	public Boolean getIgnorar() {
		return ignorar;
	}
	public void setIgnorar(Boolean ignorar) {
		this.ignorar = ignorar;
	}
	
	
}


