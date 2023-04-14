package lapfarsc.business;

import lapfarsc.dto.FarmacoProtocoloDTO;
import lapfarsc.dto.LabJobDTO;
import lapfarsc.dto.MsgDTO;

public interface TarefaExec {

	public MsgDTO prepararExecucao(TarefaBusiness tb, FarmacoProtocoloDTO farmacoProtocoloDTO);
	
	public LabJobDTO verificarExecucao(TarefaBusiness tb, LabJobDTO labJobDTO);
	
	public MsgDTO parseExecucao(TarefaBusiness tb);
	
}
