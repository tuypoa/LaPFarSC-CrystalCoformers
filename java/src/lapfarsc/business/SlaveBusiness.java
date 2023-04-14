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
	private MaquinaDTO maquinaDTO = null;
	private Integer jarLeituraCodigo = null;
	
	public SlaveBusiness(DatabaseBusiness db, MaquinaDTO maquinaDTO) throws Exception{
		this.db = db;
		this.maquinaDTO = maquinaDTO;
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
			TarefaBusiness tb = new TarefaBusiness(db, maquinaDTO);
			tb.iniciarProcessoAutomatico(farmacoProtocoloDTO);
		}
	}
	
	public void verificarListLabJob() throws Exception {
		TarefaBusiness tb = new TarefaBusiness(db, maquinaDTO);
		tb.verificarProcessosAutomaticos(jarLeituraCodigo);
	}
}

