package lapfarsc.dto;

public class FarmacoProtocoloDTO {

	private Integer farmacoCodigo;
	private Integer protocoloCodigo;
	private Integer etapaCodigo;
	private Integer tarefaCodigo;
	private Integer jarLeituraCodigo;
	
	private MsgDTO msgDTO;
	
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
	public Integer getEtapaCodigo() {
		return etapaCodigo;
	}
	public void setEtapaCodigo(Integer etapaCodigo) {
		this.etapaCodigo = etapaCodigo;
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
	public MsgDTO getMsgDTO() {
		return msgDTO;
	}
	public void setMsgDTO(MsgDTO msgDTO) {
		this.msgDTO = msgDTO;
	}
	
	
	
}
