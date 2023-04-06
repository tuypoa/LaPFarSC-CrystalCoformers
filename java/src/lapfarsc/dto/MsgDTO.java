package lapfarsc.dto;

import lapfarsc.util.Dominios.TipoMensagemDTOEnum;

public class MsgDTO {

	private TipoMensagemDTOEnum tipo;
	private String msg;
	
	/*
	public MsgDTO() {
	}
	*/
	public MsgDTO(TipoMensagemDTOEnum tipo, String msg) {
		this.tipo = tipo;
		this.msg = msg;
	}
	
	public TipoMensagemDTOEnum getTipo() {
		return tipo;
	}
	public void setTipo(TipoMensagemDTOEnum tipo) {
		this.tipo = tipo;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
