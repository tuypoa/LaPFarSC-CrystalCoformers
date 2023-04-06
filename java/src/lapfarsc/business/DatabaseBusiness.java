package lapfarsc.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lapfarsc.dto.ArquivoDTO;
import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.FarmacoProtocoloDTO;
import lapfarsc.dto.JavaDeployDTO;
import lapfarsc.dto.MaquinaDTO;
import lapfarsc.dto.MaquinaInfoDTO;
import lapfarsc.dto.MaquinaStatusDTO;
import lapfarsc.dto.TarefaDTO;

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
	public Integer incluirJarLeitura(Integer javaDeployCodigo, Integer maquinaStatusCodigo) throws Exception {		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("INSERT INTO jarleitura(javadeploy_codigo,maquinastatus_codigo) values (?,?)");
			int p = 1;
			ps.setInt(p++, javaDeployCodigo);
			ps.setInt(p++, maquinaStatusCodigo);
			ps.executeUpdate();
			ps.close();
			
			ps = conn.prepareStatement("SELECT codigo FROM jarleitura "+
					" WHERE javadeploy_codigo=? AND maquinastatus_codigo = ? "+
					" ORDER BY datahora DESC LIMIT 1");
			ps.setInt(1, javaDeployCodigo);
			ps.setInt(2, maquinaStatusCodigo);
			rs = ps.executeQuery();			
			if(rs.next()){
				return rs.getInt("codigo");
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	
	
	
	/*
	 * TABELA arquivo
	 */	
	public ArquivoDTO selectArquivoDTOByFarmaco(Integer farmacoCodigo, Integer tipoCodigo) throws Exception {		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT a.codigo,a.filename,a.hash,a.conteudo "+
							" FROM arquivo a "+
							" 	INNER JOIN farmaco_arquivo fa ON a.codigo=fa.arquivo_codigo AND fa.tipoarquivo_codigo=? "+
							" WHERE fa.farmaco_codigo = ? "+
							" ORDER BY fa.datahora DESC LIMIT 1 ");
			ps.setInt(1, tipoCodigo);
			ps.setInt(2, farmacoCodigo);
			rs = ps.executeQuery();			
			if(rs.next()){
				ArquivoDTO dto = new ArquivoDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setFilename(rs.getString("filename"));
				dto.setHash(rs.getString("hash"));
				dto.setConteudo(rs.getString("conteudo"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	
	/*
	 * TABELA tarefa
	 */
	public TarefaDTO selectTarefaDTO(Integer tarefaCodigo) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT t.codigo,t.nome,t.manual,t.javaclass,t.etapa_codigo,e.protocolo_codigo,p.secao_codigo,s.rootpath "+
							" FROM tarefa t "+
							" 	INNER JOIN etapa e ON t.etapa_codigo=e.codigo "+
							" 	INNER JOIN protocolo p ON e.protocolo_codigo=p.codigo "+
							" 	INNER JOIN secao s ON p.secao_codigo=s.codigo "+
							" WHERE t.codigo = ? AND NOT p.desativado ");
			ps.setInt(1, tarefaCodigo);
			rs = ps.executeQuery();			
			if(rs.next()){
				TarefaDTO dto = new TarefaDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setNome(rs.getString("nome"));
				dto.setManual(rs.getBoolean("manual"));
				dto.setJavaClass(rs.getString("javaclass"));
				dto.setEtapaCodigo(rs.getInt("etapa_codigo"));
				dto.setProtocoloCodigo(rs.getInt("protocolo_codigo"));
				dto.setSecaoCodigo(rs.getInt("secao_codigo"));
				dto.setSecaoRootPath(rs.getString("rootpath"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	
	
	/*
	 * TABELA maquinastatus
	 */
	public MaquinaStatusDTO selectMaquinaStatusDTO(Integer maquinaCodigo) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,maquina_codigo,iniciarjob,online,cpuused,memused "+
							" FROM maquinastatus "+
							" WHERE maquina_codigo = ? "+
							" ORDER BY datahora DESC LIMIT 1");
			ps.setInt(1, maquinaCodigo);
			rs = ps.executeQuery();			
			if(rs.next()){
				MaquinaStatusDTO dto = new MaquinaStatusDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setMaquinaCodigo(rs.getInt("maquina_codigo"));
				dto.setIniciarJob(rs.getBoolean("iniciarjob"));
				dto.setOnline(rs.getBoolean("online"));
				dto.setCpuUsed(rs.getBigDecimal("cpuused"));
				dto.setMemUsed(rs.getBigDecimal("memused"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	public void incluirMaquinaStatusDTO(List<MaquinaStatusDTO> listMaquinaStatusDTO) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO maquinastatus(maquina_codigo,iniciarjob,online,cpuused,memused) values (?,?,?,?,?)");
			for (MaquinaStatusDTO dto : listMaquinaStatusDTO) {
				int p = 1;
				ps.setInt(p++, dto.getMaquinaCodigo());
				ps.setBoolean(p++, dto.getIniciarJob());
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
	 * TABELA labjob
	 */	
	
	public void incluirLabJob(Integer jarLeituraCodigo, Integer tarefaCodigo, Integer comandoCodigo, String cmdLog, String relativeWorkpath) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO labjob(jarleitura_codigo,tarefa_codigo,comando_codigo,comandos,workpath) values (?,?,?,?,?)");
			int p = 1;
			ps.setInt(p++, jarLeituraCodigo);
			ps.setInt(p++, tarefaCodigo);
			ps.setInt(p++, comandoCodigo);
			ps.setString(p++, cmdLog);
			ps.setString(p++, relativeWorkpath);
			ps.executeUpdate();
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
	
	
	/*
	 * TABELA farmaco_protocolo
	 */	
	public FarmacoProtocoloDTO selectFarmacoProtocoloDTODisponivel() throws Exception {	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT fp.farmaco_codigo,fp.protocolo_codigo,fp.etapa_codigo,fp.tarefa_codigo,fp.jarleitura_codigo "+
								" FROM farmaco_protocolo fp "+
								"    INNER JOIN farmaco f ON f.codigo=fp.farmaco_codigo "+
								"    INNER JOIN protocolo p ON p.codigo=fp.protocolo_codigo "+
								" WHERE fp.jarleitura_codigo IS NULL AND NOT f.desativado AND NOT p.desativado "+
								" ORDER BY disponivel ASC LIMIT 1");
			rs = ps.executeQuery();
			if(rs.next()){
				FarmacoProtocoloDTO dto = new FarmacoProtocoloDTO();
				dto.setFarmacoCodigo(rs.getInt("farmaco_codigo"));
				dto.setProtocoloCodigo(rs.getInt("protocolo_codigo"));
				dto.setEtapaCodigo(rs.getInt("etapa_codigo"));
				dto.setTarefaCodigo(rs.getInt("tarefa_codigo"));
				dto.setJarLeituraCodigo(rs.getInt("jarleitura_codigo"));
				return dto;
			}	
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	public void updateFarmacoProtocoloDTOJarLeitura(FarmacoProtocoloDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE farmaco_protocolo SET jarleitura_codigo=? "+
								(dto.getJarLeituraCodigo()==null?", disponivel=CURRENT_TIMESTAMP ":"")+ 
								" WHERE farmaco_codigo=? AND protocolo_codigo=?");
			int p = 1;
			if(dto.getJarLeituraCodigo()==null) {
				ps.setNull(p++, Types.NULL);
			}else {
				ps.setInt(p++, dto.getJarLeituraCodigo());
			}
			ps.setInt(p++, dto.getFarmacoCodigo());
			ps.setInt(p++, dto.getProtocoloCodigo());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	public void updateFarmacoProtocoloDTOMsgDTO(FarmacoProtocoloDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE farmaco_protocolo SET tipomsg_codigo=?,msg=SUBSTRING(? from 1 for 150) WHERE farmaco_codigo=? AND protocolo_codigo=?");
			int p = 1;
			if(dto.getMsgDTO()==null) {
				ps.setNull(p++, Types.NULL);
				ps.setNull(p++, Types.NULL);
			}else {
				ps.setInt(p++, dto.getMsgDTO().getTipo().getIndex());
				ps.setString(p++, dto.getMsgDTO().getMsg()==null?"":dto.getMsgDTO().getMsg());
			}
			ps.setInt(p++, dto.getFarmacoCodigo());
			ps.setInt(p++, dto.getProtocoloCodigo());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}
	
	
}
