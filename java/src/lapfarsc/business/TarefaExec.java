package lapfarsc.business;

import lapfarsc.dto.MsgDTO;

public interface TarefaExec {

	public MsgDTO prepararExecucao(TarefaBusiness tb);
	
	public MsgDTO verificarExecucao(TarefaBusiness tb);
	
	public MsgDTO parseExecucao(TarefaBusiness tb);
	
}
