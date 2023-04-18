package lapfarsc.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import lapfarsc.dto.ArquivoDTO;
import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.FarmacoProtocoloDTO;
import lapfarsc.dto.LabJobDTO;
import lapfarsc.dto.MaquinaDTO;
import lapfarsc.dto.MsgDTO;
import lapfarsc.dto.FarmacoResultadoDTO;
import lapfarsc.dto.TarefaDTO;
import lapfarsc.util.Dominios.ComandoEnum;
import lapfarsc.util.Dominios.InfoMaquinaEnum;
import lapfarsc.util.Dominios.TipoArquivoEnum;
import lapfarsc.util.Dominios.TipoMensagemDTOEnum;

public class TarefaBusiness {

	public static String PREFIXO_PASTA_BOT = "Bot-";
	
	private DatabaseBusiness db = null;
	private MaquinaDTO maquinaDTO = null;
	
	public TarefaBusiness(DatabaseBusiness db, MaquinaDTO maquinaDTO) throws Exception{
		this.db = db;
		this.maquinaDTO = maquinaDTO;
	}
	
	/** FARMACO_PROTOCOLO **/
	public boolean iniciarProcessoAutomatico(FarmacoProtocoloDTO farmacoProtocoloDTO) throws Exception {
		MsgDTO msgDTO = null;
		TarefaDTO tarefaDTO = db.selectTarefaDTO(farmacoProtocoloDTO.getTarefaCodigo());
		if(!tarefaDTO.getManual() && tarefaDTO.getJavaClass()!=null) {
			try {
				Class<?> cls = Class.forName(tarefaDTO.getJavaClass());
				CocrystalTarefaInterface tarefaExec = (CocrystalTarefaInterface) cls.getDeclaredConstructor().newInstance();
				msgDTO = tarefaExec.prepararExecucao(this, farmacoProtocoloDTO);
			} catch (Exception e) {
				e.printStackTrace();
				msgDTO = new MsgDTO(TipoMensagemDTOEnum.ERRO, e.getMessage());
			}
			//gravar no farmaco_protocolo
			farmacoProtocoloDTO.setMsgDTO( msgDTO );
			db.updateFarmacoProtocoloDTOMsgDTO(farmacoProtocoloDTO);
			
			if(TipoMensagemDTOEnum.OK.equals( msgDTO.getTipo() )) {
				return true;
			}
		}
		return false;
	}
	
	/** LIST LABJOB **/
	public void verificarProcessosAutomaticos(Integer jarLeituraCodigo) throws Exception {
		//listar "labjob" com status msg=OK executados por essa maquina
		List<LabJobDTO> listJabLob = db.selectListLabJobDTOMaquinaExecutando(maquinaDTO.getCodigo(), TipoMensagemDTOEnum.OK.getIndex());
		for (LabJobDTO labJobDTO : listJabLob) {
			
			labJobDTO.setJarLeituraCodigoVerificado(jarLeituraCodigo); //sera usado no resultado!
			
			TarefaDTO tarefaDTO = db.selectTarefaDTO(labJobDTO.getTarefaCodigo());
			if(!tarefaDTO.getManual() && tarefaDTO.getJavaClass()!=null) {
				try {
					Class<?> cls = Class.forName(tarefaDTO.getJavaClass());
					CocrystalTarefaInterface tarefaExec = (CocrystalTarefaInterface) cls.getDeclaredConstructor().newInstance();
					labJobDTO = tarefaExec.verificarExecucao(this, labJobDTO);
				} catch (Exception e) {
					e.printStackTrace();
					labJobDTO.setMsgDTO( new MsgDTO(TipoMensagemDTOEnum.ERRO, e.getMessage()) );
				}
				//atualizar labjob
				db.updateLabJobDTOMsgDTO(labJobDTO);
			}
		}
	}
	
	/** LIST RESULTADO **/
	public void verificarResultados(Integer jarLeituraCodigo) throws Exception {
		//listar "labjob" com status msg=OK executados por essa maquina
		List<FarmacoResultadoDTO> listResultado = db.selectListFarmacoResultadoDTONaoDigerido(jarLeituraCodigo);
		for (FarmacoResultadoDTO resultadoDTO : listResultado) {
			TarefaDTO tarefaDTO = db.selectTarefaDTO(resultadoDTO.getTarefaCodigo());
			if(!tarefaDTO.getManual() && tarefaDTO.getJavaClass()!=null) {
				try {
					Class<?> cls = Class.forName(tarefaDTO.getJavaClass());
					CocrystalTarefaInterface tarefaExec = (CocrystalTarefaInterface) cls.getDeclaredConstructor().newInstance();
					resultadoDTO = tarefaExec.parseExecucao(this, resultadoDTO);
				} catch (Exception e) {
					e.printStackTrace();
					resultadoDTO.setMsgDTO( new MsgDTO(TipoMensagemDTOEnum.ERRO, e.getMessage()) );
				}
				db.updateFarmacoResultadoDTOMsgDTO(resultadoDTO);
			}
		}
	}
	
	
	public String getInfoMaquina(InfoMaquinaEnum infoEnum) throws Exception {
		return maquinaDTO.getInfoMaquina().get(infoEnum).getValor();
	}
	
	public File getRootWorkPathMaquina(Integer tarefaCodigo) throws Exception {
		TarefaDTO tarefaDTO = db.selectTarefaDTO(tarefaCodigo);
		StringBuilder sb = new StringBuilder();
		sb.append( maquinaDTO.getInfoMaquina().get(InfoMaquinaEnum.ROOT_WORK_PATH).getValor() );
		if(!sb.toString().endsWith("/")) {
			sb.append("/");
		}
		sb.append(tarefaDTO.getSecaoRootPath());
		return new File(sb.toString());
	}
	
	public File definirWorkPathTarefa(FarmacoProtocoloDTO farmacoProtocoloDTO) throws Exception {
		TarefaDTO tarefaDTO = db.selectTarefaDTO(farmacoProtocoloDTO.getTarefaCodigo());
	
		StringBuilder sb = new StringBuilder();
		sb.append( maquinaDTO.getInfoMaquina().get(InfoMaquinaEnum.ROOT_WORK_PATH).getValor() );
		if(!sb.toString().endsWith("/")) {
			sb.append("/");
		}
		sb.append(tarefaDTO.getSecaoRootPath());
		if(!sb.toString().endsWith("/")) {
			sb.append("/");
		}
		sb.append(PREFIXO_PASTA_BOT);
		sb.append("farmaco-"+farmacoProtocoloDTO.getFarmacoCodigo());
		sb.append("/");
		sb.append("protocolo-"+farmacoProtocoloDTO.getProtocoloCodigo());
		sb.append("/");
		sb.append("tarefa-"+tarefaDTO.getCodigo());
		sb.append("/");
		sb.append("jarleitura-"+farmacoProtocoloDTO.getJarLeituraCodigo());
		sb.append("/");
		File workPath = new File(sb.toString());
		if(!workPath.exists()) {
			workPath.mkdirs();
		}
		return workPath;
	}
	
	public File gravarArquivoFarmacoProtocolo(FarmacoProtocoloDTO farmacoProtocoloDTO, File workPath, TipoArquivoEnum tipo) throws Exception {
		ArquivoDTO arquivo = db.selectArquivoDTOByFarmaco(farmacoProtocoloDTO.getFarmacoCodigo(), tipo.getIndex());
		File fileOutput = new File( workPath.getPath() + "/arquivo-"+arquivo.getCodigo()+arquivo.getFilename().substring(arquivo.getFilename().lastIndexOf(".")) );
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(fileOutput);
			fos.write(arquivo.getConteudo().getBytes());
			fos.flush();
		}finally{
			if(fos!=null) fos.close();
		}
		return fileOutput;
		
	}
	
	public void incluirFarmacoArquivo(Integer farmacoCodigo, TipoArquivoEnum tipo, File arquivo) throws Exception {
		InputStream is = Files.newInputStream(Paths.get(arquivo.getAbsolutePath()));
		String hashArq = DigestUtils.md5Hex(is);				
		is.close();
		
		ArquivoDTO dto = db.selectArquivoDTOByHash(hashArq);
		if(dto==null) {
			dto = new ArquivoDTO();
			dto.setConteudo( loadTextFile(arquivo) );
			dto.setFilename(arquivo.getName());
			dto.setHash(hashArq);
			
			dto.setCodigo( db.incluirArquivoDTO(dto) );
		}
		Integer arquivoCodigo = db.selectFarmacoArquivoCodigo(farmacoCodigo, dto.getCodigo(), tipo.getIndex());
		if(arquivoCodigo==null) {
			//gravar em farmaco_arquivo
			db.incluirFarmacoArquivo(farmacoCodigo, dto.getCodigo(), tipo.getIndex());
		}
	}
	
	public void incluirFarmacoHistorico(FarmacoResultadoDTO resultadoDTO) throws Exception{
		TarefaDTO tarefaDTO = db.selectTarefaDTO(resultadoDTO.getTarefaCodigo());
		db.incluirFarmacoHistorico(resultadoDTO.getFarmacoCodigo(), resultadoDTO.getProtocoloCodigo(), tarefaDTO.getEtapaCodigo(), tarefaDTO.getCodigo(), resultadoDTO.getJarLeituraCodigo(), resultadoDTO.getCodigo() );
		TarefaDTO proximaTarefaDTO = db.selectTarefaDTOProxima(resultadoDTO.getTarefaCodigo());
		if(proximaTarefaDTO!=null) {
			db.updateFarmacoProtocoloProximaTarefa(resultadoDTO.getFarmacoCodigo(), resultadoDTO.getProtocoloCodigo(), proximaTarefaDTO.getEtapaCodigo(), proximaTarefaDTO.getCodigo());
		}
	}
	
	
	
	private String loadTextFile(File file) throws IOException {
		FileReader fr = null;
	    BufferedReader br = null;
		try{
			StringBuilder conteudo = new StringBuilder();
			fr = new FileReader(file);											
			br = new BufferedReader(fr);
	        int read, N = 1024;
	        char[] buffer = new char[N];
	        
	        //int i = 0;			        
	        while(true) {
	            read = br.read(buffer, 0, N);
	            String text = new String(buffer, 0, read);
	            conteudo.append(text);
	            if(read < N){
	            	if(conteudo.length()>0){
	            		return conteudo.toString();
	            	}
	            	break;
	            }		            
	        }
		}finally{
			if(br!=null) br.close();
			if(fr!=null) fr.close();
		}
		return null;
	}
	
	
	public ComandoDTO obterComandoDTO(ComandoEnum comandoTipo) throws Exception {
		return db.selectComandoDTO(comandoTipo.getIndex());
	}
	
	public void incluirLabJobFarmacoProtocolo(FarmacoProtocoloDTO farmacoProtocoloDTO, ComandoEnum cmdPrincipal, File workPath, String comandoOK, String cmdLog ) throws Exception {
		TarefaDTO tarefaDTO = db.selectTarefaDTO(farmacoProtocoloDTO.getTarefaCodigo());
		String relativeWorkpath = workPath.getAbsolutePath().substring( workPath.getAbsolutePath().indexOf(TarefaBusiness.PREFIXO_PASTA_BOT) );
		db.incluirLabJob(farmacoProtocoloDTO.getJarLeituraCodigo(), tarefaDTO.getCodigo(), cmdPrincipal.getIndex(), comandoOK, cmdLog.toString(), relativeWorkpath);
	}
		
	public void incluirResultadoFarmacoProtocolo(LabJobDTO labJobDTO) throws Exception {
		TarefaDTO tarefaDTO = db.selectTarefaDTO(labJobDTO.getTarefaCodigo());
		FarmacoProtocoloDTO farProDTO = db.selectFarmacoProtocoloDTOByLabJob(tarefaDTO.getCodigo(), labJobDTO.getJarLeituraCodigo());
		db.incluirFarmacoResultado(farProDTO.getFarmacoCodigo(), farProDTO.getProtocoloCodigo(), tarefaDTO.getCodigo(), labJobDTO.getCodigo(), labJobDTO.getJarLeituraCodigoVerificado(), labJobDTO.getWorkPath());
	}
	
	
	public MsgDTO getMsgDTOException(Throwable e) {
		e.printStackTrace();
		MsgDTO msgDTO = null;
		StackTraceElement[] ary = e.getStackTrace();
		for (StackTraceElement ste: ary) {
			if(ste.getClassName().indexOf(this.getClass().getName())!=-1) {
				msgDTO = new MsgDTO(TipoMensagemDTOEnum.EXCEPTION, ste.getFileName()+"(Ln"+ste.getLineNumber()+"): "+ e.getMessage()); 
				break;
			}
		}
		if(msgDTO!=null) {
			msgDTO = new MsgDTO(TipoMensagemDTOEnum.EXCEPTION, e.getMessage());
		}
		return msgDTO;
	}
}
