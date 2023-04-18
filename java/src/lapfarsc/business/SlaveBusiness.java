/**
 * @author tuypoa
 *
 *
 */
package lapfarsc.business;

import lapfarsc.dto.FarmacoProtocoloDTO;
import lapfarsc.dto.MaquinaDTO;

public class SlaveBusiness {
	
	private DatabaseBusiness db = null;
	private TarefaBusiness tb = null;
	private MaquinaDTO maquinaDTO = null;
	private Integer jarLeituraCodigo = null;
	
	public SlaveBusiness(DatabaseBusiness db, MaquinaDTO maquinaDTO) throws Exception{
		this.db = db;
		this.maquinaDTO = maquinaDTO;
		this.tb = new TarefaBusiness(db, maquinaDTO);
	}

	public void gravarJarLeitura(Integer maquinaStatusCodigo) throws Exception {				
		this.jarLeituraCodigo = db.incluirJarLeitura(maquinaDTO.getJavaDeployDTO().getCodigo(), maquinaStatusCodigo);
	}
	
	public void verificarFarmacoProtocolo() throws Exception {
		//pegar farmaco_protocolo sem jarleitura definido
		FarmacoProtocoloDTO farmacoProtocoloDTO = db.selectFarmacoProtocoloDTODisponivel();
		if(farmacoProtocoloDTO!=null) {
			//atualizar jarleitura em farmaco_protocolo / pegar o ticket
			farmacoProtocoloDTO.setJarLeituraCodigo(jarLeituraCodigo);
			db.updateFarmacoProtocoloDTOJarLeitura(farmacoProtocoloDTO);
			//buscar tarefa
			
			tb.iniciarProcessoAutomatico(farmacoProtocoloDTO);
		}
	}
	
	public void verificarListLabJob() throws Exception {
		tb.verificarProcessosAutomaticos(jarLeituraCodigo);
	}
	
	public void verificarListResultado() throws Exception {
		tb.verificarResultados(jarLeituraCodigo);
	}
	
}

