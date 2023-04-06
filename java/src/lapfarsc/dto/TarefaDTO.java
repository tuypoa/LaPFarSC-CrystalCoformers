package lapfarsc.dto;

public class TarefaDTO {

	private Integer codigo;
	private String nome;
	private Boolean manual;
	private String javaClass;
	private Integer etapaCodigo;
	private Integer protocoloCodigo;
	private Integer secaoCodigo;
	private String secaoRootPath;
	
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
	public Boolean getManual() {
		return manual;
	}
	public void setManual(Boolean manual) {
		this.manual = manual;
	}
	public String getJavaClass() {
		return javaClass;
	}
	public void setJavaClass(String javaClass) {
		this.javaClass = javaClass;
	}
	public Integer getEtapaCodigo() {
		return etapaCodigo;
	}
	public void setEtapaCodigo(Integer etapaCodigo) {
		this.etapaCodigo = etapaCodigo;
	}
	public Integer getProtocoloCodigo() {
		return protocoloCodigo;
	}
	public void setProtocoloCodigo(Integer protocoloCodigo) {
		this.protocoloCodigo = protocoloCodigo;
	}
	public Integer getSecaoCodigo() {
		return secaoCodigo;
	}
	public void setSecaoCodigo(Integer secaoCodigo) {
		this.secaoCodigo = secaoCodigo;
	}
	public String getSecaoRootPath() {
		return secaoRootPath;
	}
	public void setSecaoRootPath(String secaoRootPath) {
		this.secaoRootPath = secaoRootPath;
	}

}
