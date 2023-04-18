package lapfarsc.dto;

public class FarmacoResultadoDTO {

	private Integer codigo;
	private Integer farmacoCodigo;
	private Integer protocoloCodigo;
	private Integer tarefaCodigo;
	private Integer labJobCodigo;
	private Integer jarLeituraCodigo;
	private String resultPath;
	private Boolean digerido;
	private MsgDTO msgDTO;
	
	
	public Integer getCodigo() {
		return codigo;
	}
	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}
	public Integer getFarmacoCodigo() {
		return farmacoCodigo;
	}
	public void setFarmacoCodigo(Integer farmacoCodigo) {
		this.farmacoCodigo = farmacoCodigo;
	}
	public Integer getProtocoloCodigo() {
		return protocoloCodigo;
	}
	public void setProtocoloCodigo(Integer protocoloCodigo) {
		this.protocoloCodigo = protocoloCodigo;
	}
	public Integer getTarefaCodigo() {
		return tarefaCodigo;
	}
	public void setTarefaCodigo(Integer tarefaCodigo) {
		this.tarefaCodigo = tarefaCodigo;
	}
	public Integer getJarLeituraCodigo() {
		return jarLeituraCodigo;
	}
	public void setJarLeituraCodigo(Integer jarLeituraCodigo) {
		this.jarLeituraCodigo = jarLeituraCodigo;
	}
	public String getResultPath() {
		return resultPath;
	}
	public void setResultPath(String resultPath) {
		this.resultPath = resultPath;
	}
	public Boolean getDigerido() {
		return digerido;
	}
	public void setDigerido(Boolean digerido) {
		this.digerido = digerido;
	}
	public MsgDTO getMsgDTO() {
		return msgDTO;
	}
	public void setMsgDTO(MsgDTO msgDTO) {
		this.msgDTO = msgDTO;
	}
	public Integer getLabJobCodigo() {
		return labJobCodigo;
	}
	public void setLabJobCodigo(Integer labJobCodigo) {
		this.labJobCodigo = labJobCodigo;
	}
	
	
}
