package lapfarsc.business;

public interface TarefaExec {

	public boolean executar(TarefaBusiness tb) throws Exception;
	
	public void verificar(TarefaBusiness tb) throws Exception;
	
	public void reiniciar(TarefaBusiness tb) throws Exception;
	
	public void concluir(TarefaBusiness tb) throws Exception;
	
}
