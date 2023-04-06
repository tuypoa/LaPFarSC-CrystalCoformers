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
		//atualizar jarleitura em farmaco_protocolo / pegar o ticket
		farmacoProtocoloDTO.setJarLeituraCodigo(jarLeituraCodigo);
		db.updateFarmacoProtocoloDTOJarLeitura(farmacoProtocoloDTO);
		
		if(farmacoProtocoloDTO!=null) {
			//buscar tarefa 
			TarefaBusiness tb = new TarefaBusiness(db, maquinaDTO, farmacoProtocoloDTO);
			if(tb.verificarProcessoAutomatico()) {
				tb.iniciarProcessoAutomatico();
			}
		}
	}
	
}

