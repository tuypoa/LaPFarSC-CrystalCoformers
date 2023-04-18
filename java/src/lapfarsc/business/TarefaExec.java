package lapfarsc.business;

import lapfarsc.dto.FarmacoProtocoloDTO;
import lapfarsc.dto.LabJobDTO;
import lapfarsc.dto.MsgDTO;
import lapfarsc.dto.FarmacoResultadoDTO;

public interface TarefaExec {

	public MsgDTO prepararExecucao(TarefaBusiness tb, FarmacoProtocoloDTO farmacoProtocoloDTO);
	
	public LabJobDTO verificarExecucao(TarefaBusiness tb, LabJobDTO labJobDTO);
	
	public FarmacoResultadoDTO parseExecucao(TarefaBusiness tb, FarmacoResultadoDTO resultadoDTO);
	
}
