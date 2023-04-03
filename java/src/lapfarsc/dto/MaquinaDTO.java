package lapfarsc.dto;

import java.util.HashMap;
import java.util.List;

import lapfarsc.util.Dominios.InfoMaquinaEnum;

public class MaquinaDTO {

	private Integer codigo;
	private String hostname;
	private Boolean head;
	private Boolean ignorar;
	
	private HashMap<InfoMaquinaEnum, MaquinaInfoDTO> infoMaquina;
	
	private JavaDeployDTO javaDeployDTO;

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Boolean getHead() {
		return head;
	}

	public void setHead(Boolean head) {
		this.head = head;
	}

	public Boolean getIgnorar() {
		return ignorar;
	}

	public void setIgnorar(Boolean ignorar) {
		this.ignorar = ignorar;
	}

	public HashMap<InfoMaquinaEnum, MaquinaInfoDTO> getInfoMaquina() {
		return infoMaquina;
	}

	public void setInfoMaquina(List<MaquinaInfoDTO> list) {
		infoMaquina = new HashMap<InfoMaquinaEnum, MaquinaInfoDTO>();
		for (MaquinaInfoDTO dto : list) {
			infoMaquina.put(InfoMaquinaEnum.getByIndex(dto.getInfoCodigo()), dto);
		}
	}

	public JavaDeployDTO getJavaDeployDTO() {
		return javaDeployDTO;
	}

	public void setJavaDeployDTO(JavaDeployDTO javaDeployDTO) {
		this.javaDeployDTO = javaDeployDTO;
	}
	
}


