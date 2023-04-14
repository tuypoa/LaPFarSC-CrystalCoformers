package lapfarsc.business;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import lapfarsc.dto.ArquivoDTO;
import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.FarmacoProtocoloDTO;
import lapfarsc.dto.LabJobDTO;
import lapfarsc.dto.MaquinaDTO;
import lapfarsc.dto.MsgDTO;
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
				TarefaExec tarefaExec = (TarefaExec) cls.getDeclaredConstructor().newInstance();
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
		List<LabJobDTO> listJabLob = db.selectListLabJobDTOMaquinaExecutando(maquinaDTO.getCodigo(), TipoMensagemDTOEnum.OK.getIndex());;
		for (LabJobDTO labJobDTO : listJabLob) {
			TarefaDTO tarefaDTO = db.selectTarefaDTO(labJobDTO.getTarefaCodigo());
			if(!tarefaDTO.getManual() && tarefaDTO.getJavaClass()!=null) {
				try {
					Class<?> cls = Class.forName(tarefaDTO.getJavaClass());
					TarefaExec tarefaExec = (TarefaExec) cls.getDeclaredConstructor().newInstance();
					labJobDTO = tarefaExec.verificarExecucao(this, labJobDTO);
				} catch (Exception e) {
					e.printStackTrace();
					labJobDTO.setMsgDTO( new MsgDTO(TipoMensagemDTOEnum.ERRO, e.getMessage()) );
				}
				//atualizar labjob
				labJobDTO.setJarLeituraCodigoVerificado(jarLeituraCodigo);
				db.updateLabJobDTOMsgDTO(labJobDTO);
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
	
	public ComandoDTO obterComandoDTO(ComandoEnum comandoTipo) throws Exception {
		return db.selectComandoDTO(comandoTipo.getIndex());
	}
	
	public void incluirLabJobFarmacoProtocolo(FarmacoProtocoloDTO farmacoProtocoloDTO, ComandoEnum cmdPrincipal, File workPath, String comandoOK, String cmdLog ) throws Exception {
		TarefaDTO tarefaDTO = db.selectTarefaDTO(farmacoProtocoloDTO.getTarefaCodigo());
		String relativeWorkpath = workPath.getAbsolutePath().substring( workPath.getAbsolutePath().indexOf(TarefaBusiness.PREFIXO_PASTA_BOT) );
		db.incluirLabJob(farmacoProtocoloDTO.getJarLeituraCodigo(), tarefaDTO.getCodigo(), cmdPrincipal.getIndex(), comandoOK, cmdLog.toString(), relativeWorkpath);
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
