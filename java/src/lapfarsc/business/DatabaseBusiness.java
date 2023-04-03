package lapfarsc.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.JavaDeployDTO;
import lapfarsc.dto.MaquinaDTO;
import lapfarsc.dto.MaquinaInfoDTO;
import lapfarsc.dto.MaquinaStatusDTO;

public class DatabaseBusiness {
	
	private Connection conn = null;
	
	public DatabaseBusiness(Connection conn){
		this.conn = conn;
	}
	
	public boolean checkConexao() {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("SELECT NOW()");
			ps.execute();
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}finally{
			if(ps!=null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	/*
	 * TABELA jarleitura
	 */	
	public void incluirJarLeitura(Integer javaDeployCodigo, Integer maquinaCodigo) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO jarleitura(javadeploy_codigo,maquina_codigo) values (?,?)");
			int p = 1;
			ps.setInt(p++, javaDeployCodigo);
			ps.setInt(p++, maquinaCodigo);
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}
	
	/*
	 * TABELA maquinastatus
	 */		
	public void incluirMaquinaStatusDTO(List<MaquinaStatusDTO> listMaquinaStatusDTO) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO maquinastatus(maquina_codigo,online,cpuused,memused) values (?,?,?,?)");
			for (MaquinaStatusDTO dto : listMaquinaStatusDTO) {
				int p = 1;
				ps.setInt(p++, dto.getMaquinaCodigo());
				ps.setBoolean(p++, dto.getOnline());
				if(dto.getOnline()){
					ps.setBigDecimal(p++, dto.getCpuUsed());
					ps.setBigDecimal(p++, dto.getMemUsed());
				}else {
					ps.setNull(p++, Types.NULL);
					ps.setNull(p++, Types.NULL);
				}
				ps.executeUpdate();
			
			}
		}finally{
			if(ps!=null) ps.close();
		}
	}	


	/*
	 * TABELA javadeploy
	 */	
	public JavaDeployDTO selectUltimoJavaDeployDTO() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,maquina_codigo,versao,path FROM javadeploy ORDER BY datahora DESC LIMIT 1");
			rs = ps.executeQuery();			
			if(rs.next()){
				JavaDeployDTO dto = new JavaDeployDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setMaquinaCodigo(rs.getInt("maquina_codigo"));
				dto.setVersao(rs.getString("versao"));
				dto.setPath(rs.getString("path"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	public void incluirJavaDeployMaquinaDTO(Integer javaDeployCodigo, List<MaquinaDTO> listMaquinaDTO) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO javadeploy_maquina(javadeploy_codigo,maquina_codigo) values (?,?)");
			for (MaquinaDTO dto : listMaquinaDTO) {
				int p = 1;
				ps.setInt(p++, javaDeployCodigo);
				ps.setInt(p++, dto.getCodigo());
				ps.executeUpdate();
			
			}
		}finally{
			if(ps!=null) ps.close();
		}
	}

	/*
	 * TABELA comando
	 */	
	public ComandoDTO selectComandoDTO(Integer id) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,cmdtemplate,cmdprefixo FROM comando WHERE codigo = ? ");
			ps.setInt(1, id);
			rs = ps.executeQuery();			
			if(rs.next()){
				ComandoDTO dto = new ComandoDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setTemplate(rs.getString("cmdtemplate"));
				dto.setPrefixo(rs.getString("cmdprefixo"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}

	public List<ComandoDTO> selectListComandoDTO() throws Exception {
		//listagem 		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,cmdtemplate,cmdprefixo FROM comando ORDER BY codigo");
			rs = ps.executeQuery();			
			List<ComandoDTO> listComandoDTO = new ArrayList<ComandoDTO>();	
			while(rs.next()){
				ComandoDTO dto = new ComandoDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setTemplate(rs.getString("cmdtemplate"));
				dto.setPrefixo(rs.getString("cmdprefixo"));
				listComandoDTO.add(dto);
			}	
			return listComandoDTO;
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
	}
	
	
	/*
	 * TABELA maquina
	 */	
	public MaquinaDTO selectMaquinaDTOByHostname(String hostname) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT m.codigo,m.hostname,m.head,m.ignorar,jm.javadeploy_codigo,j.versao,j.path "+ 
								" FROM maquina m "+
								"	LEFT JOIN javadeploy_maquina jm ON m.codigo=jm.maquina_codigo "+
								"   LEFT JOIN javadeploy j ON j.codigo=jm.javadeploy_codigo "+
								" WHERE UPPER(m.hostname) LIKE UPPER(?) "+
								" ORDER BY j.datahora DESC LIMIT 1");
			ps.setString(1, hostname);
			rs = ps.executeQuery();
			
			if(rs.next()){
				MaquinaDTO maq = new MaquinaDTO();
				maq.setCodigo(rs.getInt("codigo"));
				maq.setHostname(rs.getString("hostname"));
				maq.setHead(rs.getBoolean("head"));
				maq.setIgnorar(rs.getBoolean("ignorar"));
				
				if(rs.getString("versao") !=null){
					JavaDeployDTO deploy = new JavaDeployDTO();
					deploy.setCodigo(rs.getInt("javadeploy_codigo"));
					deploy.setMaquinaCodigo(maq.getCodigo());
					deploy.setVersao(rs.getString("versao"));
					deploy.setPath(rs.getString("path"));
					maq.setJavaDeployDTO(deploy);
				}
				return maq;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}	

	public List<MaquinaDTO> selectListMaquinaDTOParaJavaDeploy(Integer ultimoJavaDeploy) throws Exception {	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT m.codigo,m.hostname,m.head,m.ignorar "+
								" FROM maquina m "+
								"    LEFT JOIN javadeploy_maquina jm ON m.codigo=jm.maquina_codigo AND jm.javadeploy_codigo = ? "+
								" WHERE jm.javadeploy_codigo IS NULL AND NOT m.ignorar ");
			ps.setInt(1, ultimoJavaDeploy);
			rs = ps.executeQuery();
			List<MaquinaDTO> listMaquinaDTO = new ArrayList<MaquinaDTO>();	
			while(rs.next()){
				MaquinaDTO maq = new MaquinaDTO();
				maq.setCodigo(rs.getInt("codigo"));
				maq.setHostname(rs.getString("hostname"));
				maq.setHead(rs.getBoolean("head"));
				maq.setIgnorar(rs.getBoolean("ignorar"));
				listMaquinaDTO.add(maq);
			}	
			return listMaquinaDTO;
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
	}
	
	public List<MaquinaDTO> selectListMaquinaDTOElegivel(Integer ultimoJavaDeploy) throws Exception {	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT m.codigo,m.hostname,m.head,m.ignorar,j.versao,j.path "+
								" FROM maquina m "+
								"    INNER JOIN javadeploy_maquina jm ON m.codigo=jm.maquina_codigo "+
								"    INNER JOIN javadeploy j ON j.codigo=jm.javadeploy_codigo "+
								" WHERE j.codigo = ? ");
			ps.setInt(1, ultimoJavaDeploy);
			rs = ps.executeQuery();
			
			List<MaquinaDTO> listMaquinaDTO = new ArrayList<MaquinaDTO>();	
			while(rs.next()){
				MaquinaDTO maq = new MaquinaDTO();
				maq.setCodigo(rs.getInt("codigo"));
				maq.setHostname(rs.getString("hostname"));
				maq.setHead(rs.getBoolean("head"));
				maq.setIgnorar(rs.getBoolean("ignorar"));
				
				JavaDeployDTO deploy = new JavaDeployDTO();
				deploy.setCodigo(ultimoJavaDeploy);
				deploy.setMaquinaCodigo(maq.getCodigo());
				deploy.setVersao(rs.getString("versao"));
				deploy.setPath(rs.getString("path"));
			
				maq.setJavaDeployDTO(deploy);
				listMaquinaDTO.add(maq);
			}	
			return listMaquinaDTO;
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
	}
	
	/*
	 * TABELA maquina_infomaquina
	 */	
	public List<MaquinaInfoDTO> selectListMaquinaInfoDTO(Integer maquinaCodigo) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT i.codigo,i.nome,mi.valor "
					+ " FROM maquina_infomaquina mi "
					+ "		INNER JOIN infomaquina i ON i.codigo=mi.infomaquina_codigo "
					+ "WHERE mi.maquina_codigo = ?");
			ps.setInt(1, maquinaCodigo);
			rs = ps.executeQuery();
			List<MaquinaInfoDTO> listMI = new ArrayList<MaquinaInfoDTO>();	
			while(rs.next()){
				MaquinaInfoDTO mi = new MaquinaInfoDTO();
				mi.setInfoCodigo(rs.getInt("codigo"));
				mi.setNome(rs.getString("nome"));
				mi.setValor(rs.getString("valor"));
				listMI.add(mi);
			}	
			return listMI;
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
	}	

	
}
