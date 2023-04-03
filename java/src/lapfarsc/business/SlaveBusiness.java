/**
 * @author tuypoa
 *
 *
 */
package lapfarsc.business;

import lapfarsc.dto.MaquinaDTO;

public class SlaveBusiness {
	
	private DatabaseBusiness db = null;
	private MaquinaDTO maquinaDTO = null;
	
	public SlaveBusiness(DatabaseBusiness db, MaquinaDTO maquinaDTO) throws Exception{
		this.db = db;
		this.maquinaDTO = maquinaDTO;
	}

	public void gravarJarLeitura() throws Exception {				
		db.incluirJarLeitura(maquinaDTO.getJavaDeployDTO().getCodigo(), maquinaDTO.getCodigo());
	}
	
	
}

