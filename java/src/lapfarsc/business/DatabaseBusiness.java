package lapfarsc.business;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lapfarsc.qe.dashboard.dto.ComandoDTO;
import lapfarsc.qe.dashboard.dto.JarLeituraDTO;
import lapfarsc.qe.dashboard.dto.MaqArqHashDTO;
import lapfarsc.qe.dashboard.dto.MaquinaDTO;
import lapfarsc.qe.dashboard.dto.MaquinaQeArquivoInDTO;
import lapfarsc.qe.dashboard.dto.MoleculaDTO;
import lapfarsc.qe.dashboard.dto.PsauxDTO;
import lapfarsc.qe.dashboard.dto.QeArquivoInDTO;
import lapfarsc.qe.dashboard.dto.QeInfoIterationDTO;
import lapfarsc.qe.dashboard.dto.QeInfoScfDTO;
import lapfarsc.qe.dashboard.dto.QeResumoDTO;

public class DatabaseBusiness {
	
	private Connection conn = null;
	
	public DatabaseBusiness(Connection conn){
		this.conn = conn;
	}
	
	/*
	 * TABELA jarleitura
	 */	
	public void incluirJarLeituraDTO(JarLeituraDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO jarleitura(maquina_codigo,cpuused,memused) values (?,?,?)");
			int p = 1;
			ps.setInt(p++, dto.getMaquinaCodigo()); 
			ps.setBigDecimal(p++, dto.getCpuUsed());
			ps.setBigDecimal(p++, dto.getMemUsed());
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
	 * TABELA molecula
	 */
	public List<MoleculaDTO> selectListMoleculaDTO() throws Exception {
		//listagem 		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,nome FROM molecula ORDER BY codigo");
			rs = ps.executeQuery();			
			List<MoleculaDTO> listDTO = new ArrayList<MoleculaDTO>();	
			while(rs.next()){
				MoleculaDTO dto = new MoleculaDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setNome(rs.getString("nome"));
				listDTO.add(dto);
			}	
			return listDTO;
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
	}
	public MoleculaDTO selectMoleculaDTO(Integer id) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,nome FROM molecula WHERE codigo = ? ");
			ps.setInt(1, id);
			rs = ps.executeQuery();			
			if(rs.next()){
				MoleculaDTO dto = new MoleculaDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setNome(rs.getString("nome"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	
	/*
	 * TABELA maquina_qearquivoin
	 */	
	public MaquinaQeArquivoInDTO selectMaquinaQeArquivoInDTO(Integer maquinaId, Integer arqInId) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT maquina_codigo,qearquivoin_codigo,hasharqin,rootpath,ordem,ignorar " +
					" FROM maquina_qearquivoin WHERE maquina_codigo =? AND qearquivoin_codigo =? ");
			ps.setInt(1, maquinaId);
			ps.setInt(2, arqInId);
			rs = ps.executeQuery();			
			if(rs.next()){
				MaquinaQeArquivoInDTO dto = new MaquinaQeArquivoInDTO();
				dto.setMaquinaCodigo(rs.getInt("maquina_codigo"));
				dto.setQeArquivoInCodigo(rs.getInt("qearquivoin_codigo"));
				dto.setHashArqIn(rs.getString("hasharqin"));
				dto.setRootPath(rs.getString("rootpath"));
				dto.setOrdem(rs.getInt("ordem"));
				dto.setIgnorar(rs.getBoolean("ignorar"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
		
	public void incluirMaquinaQeArquivoInDTO(MaquinaQeArquivoInDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO maquina_qearquivoin(" +
					" maquina_codigo,qearquivoin_codigo,hasharqin,rootpath,ordem) values (?,?,?,?,?)");
			int p = 1;
			ps.setInt(p++, dto.getMaquinaCodigo());
			ps.setInt(p++, dto.getQeArquivoInCodigo());
			ps.setString(p++, dto.getHashArqIn());
			ps.setString(p++, dto.getRootPath());
			ps.setInt(p++, dto.getOrdem());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	
	
	/*
	 * TABELA qearquivoin
	 */	
	public QeArquivoInDTO selectQeArquivoInDTOPeloHash(String hashMaqArq) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,hashmaqarq,nome,conteudo,molecula_codigo FROM qearquivoin WHERE hashmaqarq LIKE ? ");
			ps.setString(1, hashMaqArq);
			rs = ps.executeQuery();			
			if(rs.next()){
				QeArquivoInDTO dto = new QeArquivoInDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setHashMaqArq(rs.getString("hashmaqarq"));
				dto.setNome(rs.getString("nome"));
				dto.setConteudo(rs.getString("conteudo"));
				dto.setMoleculaCodigo(rs.getInt("molecula_codigo"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	public QeArquivoInDTO selectQeArquivoInDTO(Integer id) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,hashmaqarq,nome,conteudo,molecula_codigo FROM qearquivoin WHERE codigo =? ");
			ps.setInt(1, id);
			rs = ps.executeQuery();			
			if(rs.next()){
				QeArquivoInDTO dto = new QeArquivoInDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setHashMaqArq(rs.getString("hashmaqarq"));
				dto.setNome(rs.getString("nome"));
				dto.setConteudo(rs.getString("conteudo"));
				dto.setMoleculaCodigo(rs.getInt("molecula_codigo"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	
	public void incluirQeArquivoInDTO(QeArquivoInDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO qearquivoin(hashmaqarq,nome,descricao,conteudo,molecula_codigo," +
					"qearquivoin_codigo,nomemolecula,qtdemolecula,nomecoformador,qtdecoformador,qtdeatomo,tipocelula) values (?,?,?,?,?," +
					"?,?,?,?,?,?,?)");
			int p = 1;
			ps.setString(p++, dto.getHashMaqArq());
			ps.setString(p++, dto.getNome());
			ps.setString(p++, dto.getDescricao());
			ps.setString(p++, dto.getConteudo());
			ps.setInt(p++, dto.getMoleculaCodigo());
			if(dto.getQeArquivoInCodigoContinuacao()!=null){
				ps.setInt(p++, dto.getQeArquivoInCodigoContinuacao());
			}else{
				ps.setNull(p++, Types.NULL);	
			}
			if(dto.getNomeMolecula()!=null){
				ps.setString(p++, dto.getNomeMolecula());
			}else{
				ps.setNull(p++, Types.NULL);	
			}
			if(dto.getQtdeMolecula()!=null){
				ps.setInt(p++, dto.getQtdeMolecula());
			}else{
				ps.setNull(p++, Types.NULL);	
			}
			if(dto.getNomeCoformador()!=null){
				ps.setString(p++, dto.getNomeCoformador());
			}else{
				ps.setNull(p++, Types.NULL);	
			}
			if(dto.getQtdeCoformador()!=null){
				ps.setInt(p++, dto.getQtdeCoformador());
			}else{
				ps.setNull(p++, Types.NULL);	
			}
			if(dto.getQtdeAtomo()!=null){
				ps.setInt(p++, dto.getQtdeAtomo());
			}else{
				ps.setNull(p++, Types.NULL);	
			}
			if(dto.getTipoCelula()!=null){
				ps.setString(p++, dto.getTipoCelula());
			}else{
				ps.setNull(p++, Types.NULL);	
			}
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	
	/*
	 * TABELA qeresumo
	 */	
	public QeResumoDTO selectQeResumoDTOPeloNome(Integer arquivoInCodigo, String nome) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT codigo,qearquivoin_codigo,hashoutput,processar,nome,tamanhokb,qtdecpu," +					
					"   TO_CHAR(ultimalida,'DD/MM/YYYY HH24:MI:SS') AS ultimalida," +
					"   concluido,executando, erro FROM qeresumo " +
					" WHERE qearquivoin_codigo = ? AND nome LIKE ? ORDER BY codigo DESC LIMIT 1 ");
			ps.setInt(1, arquivoInCodigo);
			ps.setString(2, nome);
			rs = ps.executeQuery();			
			if(rs.next()){
				QeResumoDTO dto = new QeResumoDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setQeArquivoInCodigo(rs.getInt("qearquivoin_codigo"));
				dto.setHashOutput(rs.getString("hashoutput"));
				dto.setProcessar(rs.getBoolean("processar"));
				dto.setNome(rs.getString("nome"));
				dto.setTamanhoKb(rs.getDouble("tamanhokb"));
				dto.setQtdeCpu(rs.getInt("qtdecpu"));
				dto.setUltimaLida(rs.getString("ultimalida"));
				dto.setConcluido(rs.getBoolean("concluido"));
				dto.setExecutando(rs.getBoolean("executando"));
				dto.setErro(rs.getString("erro"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	
	public List<QeResumoDTO> selectQeResumoDTOAProcessar() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<QeResumoDTO> listDTO = new ArrayList<QeResumoDTO>();
		try{
			ps = conn.prepareStatement("SELECT codigo,qearquivoin_codigo,hashoutput,processar,nome,tamanhokb,qtdecpu," +					
					"   TO_CHAR(ultimalida,'DD/MM/YYYY HH24:MI:SS') AS ultimalida," +
					"   concluido,executando FROM qeresumo " +
					" WHERE processar = TRUE ORDER BY ultimalida ");
			rs = ps.executeQuery();
			while(rs.next()){
				QeResumoDTO dto = new QeResumoDTO();
				dto.setCodigo(rs.getInt("codigo"));
				dto.setQeArquivoInCodigo(rs.getInt("qearquivoin_codigo"));
				dto.setHashOutput(rs.getString("hashoutput"));
				dto.setProcessar(rs.getBoolean("processar"));
				dto.setNome(rs.getString("nome"));
				dto.setTamanhoKb(rs.getDouble("tamanhokb"));
				dto.setQtdeCpu(rs.getInt("qtdecpu"));
				dto.setUltimaLida(rs.getString("ultimalida"));
				dto.setConcluido(rs.getBoolean("concluido"));
				dto.setExecutando(rs.getBoolean("executando"));
				listDTO.add(dto);
			}
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return listDTO;
	}
	public void incluirQeResumoDTO(QeResumoDTO dto, Integer maquinaCodigo) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO qeresumo(" +
					"	qearquivoin_codigo,hashoutput,nome,tamanhokb,executando,qtdecpu,ultimalida" +
					" ) values (?,?,?,?,?,(SELECT mincpu FROM maquina WHERE codigo=?),NOW())");
			int p = 1;
			ps.setInt(p++, dto.getQeArquivoInCodigo());
			ps.setString(p++, dto.getHashOutput());
			ps.setString(p++, dto.getNome());
			ps.setDouble(p++, dto.getTamanhoKb());
			ps.setBoolean(p++, dto.getExecutando());
			ps.setInt(p++, maquinaCodigo);
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	public void updateQeResumoDTOHash(QeResumoDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE qeresumo SET " +
					"  hashoutput=?, processar=TRUE, tamanhokb=?, executando=? WHERE codigo = ?");
			int p = 1;
			ps.setString(p++, dto.getHashOutput());
			ps.setDouble(p++, dto.getTamanhoKb());
			ps.setBoolean(p++, dto.getExecutando());
			ps.setInt(p++, dto.getCodigo());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	public void updateQeResumoDTOExecutando(QeResumoDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE qeresumo SET executando=? WHERE codigo = ?");
			int p = 1;
			ps.setBoolean(p++, dto.getExecutando());
			ps.setInt(p++, dto.getCodigo());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	
	public void updateNaoExecutandoTodosQeResumoDTO(Integer maquinaCodigo) throws Exception {
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE qeresumo SET executando=FALSE WHERE qearquivoin_codigo IN (" +
					"	SELECT qearquivoin_codigo FROM maquina_qearquivoin WHERE maquina_codigo = ? ) ");
			ps.setInt(1, maquinaCodigo);
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}
	public void updateQeResumoDTOQtdeCpu(QeResumoDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE qeresumo SET qtdecpu=? WHERE codigo = ?");
			int p = 1;
			ps.setInt(p++, dto.getQtdeCpu());
			ps.setInt(p++, dto.getCodigo());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}
	public void updateQeResumoDTOSituacao(QeResumoDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE qeresumo SET ultimalida= NOW(), concluido=?, processar=?, erro = ? WHERE codigo = ?");
			int p = 1;
			ps.setBoolean(p++, dto.getConcluido());
			ps.setBoolean(p++, dto.getProcessar());
			if(dto.getErro()==null){
				ps.setNull(p++, Types.NULL);
			}else{
				ps.setString(p++, dto.getErro());
			}
			ps.setInt(p++, dto.getCodigo());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}
		
	
	/*
	 * TABELA qeinfoscf
	 */		
	public QeInfoScfDTO selectQeInfoScfDTOUltimoPeloResumo(Integer resumoCodigo) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT qeresumo_codigo,scfcycles,bfgssteps,enthalpy,volume,density,iterations FROM qeinfoscf " +
					" WHERE qeresumo_codigo = ? ORDER BY scfcycles DESC LIMIT 1");
			ps.setInt(1, resumoCodigo);
			rs = ps.executeQuery();			
			if(rs.next()){
				QeInfoScfDTO dto = new QeInfoScfDTO();
				dto.setQeResumoCodigo(rs.getInt("qeresumo_codigo"));
				dto.setScfCycles(rs.getInt("scfcycles"));
				dto.setBfgsSteps(rs.getInt("bfgssteps"));
				dto.setEnthalpy(rs.getDouble("enthalpy"));
				dto.setVolume(rs.getDouble("volume"));
				dto.setDensity(rs.getDouble("density"));
				dto.setIterations(rs.getInt("iterations"));
				return dto;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}
	public void incluirQeInfoScfDTO(QeInfoScfDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO qeinfoscf(" +
					" qeresumo_codigo,scfcycles,bfgssteps,enthalpy,volume,density,iterations,cellparams,atomicpositions" +
					" ) values (?,?,?,?,?,?,?,?,?)");
			int p = 1;
			ps.setInt(p++, dto.getQeResumoCodigo());
			ps.setInt(p++, dto.getScfCycles());
			ps.setInt(p++, dto.getBfgsSteps());			
			ps.setDouble(p++, dto.getEnthalpy());
			ps.setDouble(p++, dto.getVolume());
			ps.setDouble(p++, dto.getDensity());
			ps.setInt(p++, dto.getIterations());
			ps.setString(p++, dto.getCellparams());
			ps.setString(p++, dto.getAtomicpositions());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	
	/*
	 * TABELA qeinfoiteration
	 */		
	public void incluirQeInfoIterationDTO(QeInfoIterationDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("INSERT INTO qeinfoiteration(qeresumo_codigo,scfcycles,iteration,cputime) values (?,?,?,?)");
			int p = 1;
			ps.setInt(p++, dto.getQeResumoCodigo());
			ps.setInt(p++, dto.getScfCycles());
			ps.setInt(p++, dto.getIteration());			
			ps.setLong(p++, dto.getCputime());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	
	
	/*
	 * TABELA psaux
	 */	
	public void incluirListPsauxDTO(List<PsauxDTO> listDTO, Integer maquinaCodigo) throws Exception {		
		PreparedStatement ps = null;
		PreparedStatement psDel = null;
		try{
			psDel = conn.prepareStatement("DELETE FROM psaux WHERE maquina_codigo = "+maquinaCodigo);
			psDel.executeUpdate();
			
			ps = conn.prepareStatement("INSERT INTO psaux(maquina_codigo,comando_codigo,pid,uid, " +
					" qearquivoin_codigo,qeresumo_codigo,cpu,mem,conteudo) values (?,?,?,?,?,?,?,?,?)");
			for (PsauxDTO dto : listDTO) {
				int p = 1;
				ps.setInt(p++, dto.getMaquinaCodigo());
				ps.setInt(p++, dto.getComandoCodigo());
				ps.setInt(p++, dto.getPid());
				ps.setString(p++, dto.getUid());
				if(dto.getQeArquivoInCodigo()==null){
					ps.setNull(p++, Types.NULL);
				}else{
					ps.setInt(p++, dto.getQeArquivoInCodigo());
				}
				if(dto.getQeResumoCodigo()==null){
					ps.setNull(p++, Types.NULL);
				}else{
					ps.setInt(p++, dto.getQeResumoCodigo());
				}
				ps.setBigDecimal(p++, dto.getCpu());
				ps.setBigDecimal(p++, dto.getMem());
				ps.setString(p++, dto.getConteudo());
				ps.executeUpdate();	
			}
		}finally{
			if(ps!=null) ps.close();
			if(psDel!=null) psDel.close();
		}
	}
	
	/*
	 * TABELA maquina
	 */	
	public MaquinaDTO selectMaquinaDTO(Integer id) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT " +
					" codigo,nome,ssh,senha,rootpath,jarpath,mincpu,maxcpu," +
					"	COALESCE(cpuused,0) AS cpuused, COALESCE(memused,0) AS memused," +
					"   TO_CHAR(ultimoacesso,'DD/MM/YYYY HH24:MI:SS') AS ultimoacesso," +
					" iniciarjob,online,ignorar " +
					" FROM maquina WHERE codigo = ? ");
			ps.setInt(1, id);
			rs = ps.executeQuery();
			
			if(rs.next()){
				MaquinaDTO maq = new MaquinaDTO();
				maq.setCodigo(rs.getInt("codigo"));
				maq.setNome(rs.getString("nome"));
				maq.setSsh(rs.getString("ssh"));
				maq.setSenha(rs.getString("senha"));
				maq.setRootPath(rs.getString("rootpath"));
				maq.setJarPath(rs.getString("jarpath"));
				maq.setMinCpu(rs.getInt("mincpu"));
				maq.setMaxCpu(rs.getInt("maxcpu"));
				
				maq.setCpuUsed(rs.getBigDecimal("cpuused"));
				maq.setMemUsed(rs.getBigDecimal("memused"));
				maq.setUltimoAcesso(rs.getString("ultimoacesso"));
				
				maq.setIniciarJob(rs.getBoolean("iniciarjob"));
				maq.setOnline(rs.getBoolean("online"));
				maq.setOnline(rs.getBoolean("ignorar"));
				return maq;
			}			
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return null;
	}	

	public List<MaquinaDTO> selectListMaquinaDTO() throws Exception {
		//listagem de maquinas		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement("SELECT " +
					" codigo,nome,ssh,senha,rootpath,jarpath,mincpu,maxcpu," +
					"	COALESCE(cpuused,0) AS cpuused, COALESCE(memused,0) AS memused," +
					"   TO_CHAR(ultimoacesso,'DD/MM/YYYY HH24:MI:SS') AS ultimoacesso," +
					" iniciarjob,online,ignorar " +
					" FROM maquina ORDER BY ultimoacesso DESC");
			rs = ps.executeQuery();
			
			List<MaquinaDTO> listMaquinaDTO = new ArrayList<MaquinaDTO>();	
			while(rs.next()){
				MaquinaDTO maq = new MaquinaDTO();
				maq.setCodigo(rs.getInt("codigo"));
				maq.setNome(rs.getString("nome"));
				maq.setSsh(rs.getString("ssh"));
				maq.setSenha(rs.getString("senha"));
				maq.setRootPath(rs.getString("rootpath"));
				maq.setJarPath(rs.getString("jarpath"));
				maq.setMinCpu(rs.getInt("mincpu"));
				maq.setMaxCpu(rs.getInt("maxcpu"));
				
				maq.setCpuUsed(rs.getBigDecimal("cpuused"));
				maq.setMemUsed(rs.getBigDecimal("memused"));
				maq.setUltimoAcesso(rs.getString("ultimoacesso"));
				
				maq.setIniciarJob(rs.getBoolean("iniciarjob"));
				maq.setOnline(rs.getBoolean("online"));
				maq.setIgnorar(rs.getBoolean("ignorar"));
				listMaquinaDTO.add(maq);
			}	
			return listMaquinaDTO;
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
	}
	
	public void updateOfflineTodasMaquinasDTO() throws Exception {
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE maquina SET online=FALSE ");
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}
		
	public void updateOnlineMaquinasDTO(List<MaquinaDTO> listMaquinaDTO) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE maquina SET online=?, cpuused=?, memused=?, ultimoacesso=NOW() WHERE codigo = ? ");
			for (MaquinaDTO m : listMaquinaDTO) {
				if(m.getOnline()){
					int p = 1;
					ps.setBoolean(p++, m.getOnline()); 
					ps.setBigDecimal(p++, m.getCpuUsed());
					ps.setBigDecimal(p++, m.getMemUsed());
					ps.setInt(p++, m.getCodigo());
					ps.executeUpdate();
				}
			}
		}finally{
			if(ps!=null) ps.close();
		}
	}
	
	public boolean verificarMaquinaCPUOciosa(Integer maquinaCodigo) throws Exception {		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(
					"SELECT (COALESCE(SUM(r.qtdecpu),0) < m.maxcpu)  as ociosa "+
					" FROM maquina m "+
					"	LEFT JOIN maquina_qearquivoin ma ON m.codigo=ma.maquina_codigo "+
					"	LEFT JOIN qeresumo r ON ma.qearquivoin_codigo=r.qearquivoin_codigo AND r.executando "+
					" WHERE NOT m.ignorar "+ 
					"	AND m.codigo = ? "+
					" GROUP BY m.codigo, m.maxcpu");
			ps.setInt(1, maquinaCodigo);
			rs = ps.executeQuery();			
			if(rs.next()){
				return rs.getBoolean("ociosa");
			}		
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return false; 
	}
	public List<MaqArqHashDTO> verificarArquivoElegivelParaExecutar(Integer maquinaCodigo) throws Exception {		
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<MaqArqHashDTO> listHash = new ArrayList<MaqArqHashDTO>(); 
		try{
			ps = conn.prepareStatement(
					"SELECT ma.qearquivoin_codigo, ma.hasharqin, a.hashmaqarq, ma.rootpath, a.nome "+
					" FROM maquina m "+
					"	INNER JOIN maquina_qearquivoin ma ON m.codigo=ma.maquina_codigo "+
					"	INNER JOIN qearquivoin a ON a.codigo=ma.qearquivoin_codigo "+
					"	LEFT JOIN qeresumo r ON r.qearquivoin_codigo=a.codigo "+
					" WHERE NOT m.ignorar AND NOT ma.ignorar AND m.codigo = ? "+
					" GROUP BY ma.ordem, ma.qearquivoin_codigo, ma.hasharqin, a.hashmaqarq, ma.rootpath, a.nome "+
					" HAVING COUNT(r.qearquivoin_codigo)=0 "+
					" ORDER BY ma.ordem ASC");
			ps.setInt(1, maquinaCodigo);
			rs = ps.executeQuery();			
			while(rs.next()){
				MaqArqHashDTO dto = new MaqArqHashDTO();
				dto.setMaquinaCodigo(maquinaCodigo);
				dto.setArquivoInCodigo(rs.getInt("qearquivoin_codigo") );
				dto.setHashArquivoIn(rs.getString("hasharqin") );
				dto.setHashMaquinaArquivoIn(rs.getString("hashmaqarq") );
				dto.setRootPath(rs.getString("rootpath") );
				dto.setNomeArquivo(rs.getString("nome") );
				listHash.add( dto );
			}		
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return listHash; 
	}
	public List<MaqArqHashDTO> verificarArquivoEmOutraMaquina(MaqArqHashDTO hashDTO) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<MaqArqHashDTO> listHash = new ArrayList<MaqArqHashDTO>(); 
		try{
			ps = conn.prepareStatement(
					"SELECT ma.maquina_codigo, ma.qearquivoin_codigo,ma.hasharqin, a.hashmaqarq "+
					" FROM maquina m "+
					"	INNER JOIN maquina_qearquivoin ma ON m.codigo=ma.maquina_codigo "+
					"	INNER JOIN qearquivoin a ON a.codigo=ma.qearquivoin_codigo "+
					"	INNER JOIN qeresumo r ON r.qearquivoin_codigo=a.codigo "+
					" WHERE m.codigo != ? AND ma.hasharqin LIKE ? "+
					" GROUP BY ma.maquina_codigo, ma.qearquivoin_codigo,ma.hasharqin,a.hashmaqarq");
			ps.setInt(1, hashDTO.getMaquinaCodigo());
			ps.setString(2, hashDTO.getHashArquivoIn());
			rs = ps.executeQuery();			
			while(rs.next()){
				MaqArqHashDTO dto = new MaqArqHashDTO();
				dto.setMaquinaCodigo( rs.getInt("maquina_codigo") );
				dto.setArquivoInCodigo(rs.getInt("qearquivoin_codigo") );
				dto.setHashArquivoIn(rs.getString("hasharqin") );
				dto.setHashMaquinaArquivoIn(rs.getString("hashmaqarq") );
				listHash.add( dto );
			}		
		}finally{
			if(rs!=null) rs.close();
			if(ps!=null) ps.close();
		}
		return listHash; 
	}
	public void updateMaquinaArquivoIgnorarExecucao(MaqArqHashDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE maquina_qearquivoin SET ignorar=? WHERE maquina_codigo=? AND qearquivoin_codigo=? ");
			int p = 1;
			ps.setBoolean(p++, dto.getIgnorar());
			ps.setInt(p++, dto.getMaquinaCodigo());
			ps.setInt(p++, dto.getArquivoInCodigo());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	
	public void updateMaquinaDTOIniciarJob(MaquinaDTO dto) throws Exception {		
		PreparedStatement ps = null;
		try{
			ps = conn.prepareStatement("UPDATE maquina SET iniciarjob=? WHERE codigo=?");
			int p = 1;
			ps.setBoolean(p++, dto.getIniciarJob());
			ps.setInt(p++, dto.getCodigo());
			ps.executeUpdate();
		}finally{
			if(ps!=null) ps.close();
		}
	}	

	
}
