package lapfarsc.dto;

public class LabJobDTO {

	private Integer codigo;
	private Integer jarLeituraCodigo;
	private Integer tarefaCodigo;
	private ComandoDTO comandoDTO;
	private String workPath;
	private String comando; //executado
	private Integer jarLeituraCodigoVerificado;
	private Long pid;
	private Boolean interrompido;
	private Boolean executando;
	private Boolean concluido;
	private MsgDTO msgDTO;
	
	public Integer getCodigo() {
		return codigo;
	}
	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}
	public Integer getJarLeituraCodigo() {
		return jarLeituraCodigo;
	}
	public void setJarLeituraCodigo(Integer jarLeituraCodigo) {
		this.jarLeituraCodigo = jarLeituraCodigo;
	}
	public Integer getTarefaCodigo() {
		return tarefaCodigo;
	}
	public void setTarefaCodigo(Integer tarefaCodigo) {
		this.tarefaCodigo = tarefaCodigo;
	}
	public ComandoDTO getComandoDTO() {
		return comandoDTO;
	}
	public void setComandoDTO(ComandoDTO comandoDTO) {
		this.comandoDTO = comandoDTO;
	}
	public String getWorkPath() {
		return workPath;
	}
	public void setWorkPath(String workPath) {
		this.workPath = workPath;
	}
	public Integer getJarLeituraCodigoVerificado() {
		return jarLeituraCodigoVerificado;
	}
	public void setJarLeituraCodigoVerificado(Integer jarLeituraCodigoVerificado) {
		this.jarLeituraCodigoVerificado = jarLeituraCodigoVerificado;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public Boolean getInterrompido() {
		return interrompido;
	}
	public void setInterrompido(Boolean interrompido) {
		this.interrompido = interrompido;
	}
	public Boolean getExecutando() {
		return executando;
	}
	public void setExecutando(Boolean executando) {
		this.executando = executando;
	}
	public Boolean getConcluido() {
		return concluido;
	}
	public void setConcluido(Boolean concluido) {
		this.concluido = concluido;
	}
	public MsgDTO getMsgDTO() {
		return msgDTO;
	}
	public void setMsgDTO(MsgDTO msgDTO) {
		this.msgDTO = msgDTO;
	}
	public String getComando() {
		return comando;
	}
	public void setComando(String comando) {
		this.comando = comando;
	}

	
	
	
}
