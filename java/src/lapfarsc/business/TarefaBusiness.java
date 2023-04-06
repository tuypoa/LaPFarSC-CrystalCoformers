package lapfarsc.business;

import java.io.File;
import java.io.FileOutputStream;

import lapfarsc.dto.ArquivoDTO;
import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.FarmacoProtocoloDTO;
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
	private FarmacoProtocoloDTO farmacoProtocoloDTO = null;
	private TarefaDTO tarefaDTO = null;
	
	private File workPathTarefa = null;

	public TarefaBusiness(DatabaseBusiness db, MaquinaDTO maquinaDTO, FarmacoProtocoloDTO farmacoProtocoloDTO) throws Exception{
		this.db = db;
		this.maquinaDTO = maquinaDTO;
		this.farmacoProtocoloDTO = farmacoProtocoloDTO;
		this.tarefaDTO = db.selectTarefaDTO(farmacoProtocoloDTO.getTarefaCodigo());
		//System.out.println(tarefaDTO.getJavaClass());
	}
	
	public boolean verificarProcessoAutomatico() throws Exception {
		//tarefa manual? tem classe associada?
		return (!tarefaDTO.getManual() && tarefaDTO.getJavaClass()!=null);
	}
	
	public boolean iniciarProcessoAutomatico() throws Exception {
		MsgDTO msgDTO = null;
		try {
			Class<?> cls = Class.forName(tarefaDTO.getJavaClass());
			TarefaExec tarefaExec = (TarefaExec) cls.getDeclaredConstructor().newInstance();
			msgDTO = tarefaExec.prepararExecucao(this);
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
		return false;
	}
	
	public File definirWorkPathTarefa() throws Exception {
		if(this.workPathTarefa==null) {
			StringBuilder sb = new StringBuilder();
			sb.append( maquinaDTO.getInfoMaquina().get(InfoMaquinaEnum.ROOT_WORK_PATH).getValor() );
			if(!sb.toString().endsWith("/")) {
				sb.append("/");
			}
			sb.append(tarefaDTO.getSecaoRootPath());
			if(!sb.toString().endsWith("/")) {
				sb.append("/");
			}
			//TODO colocar switch/case quando tiver outra secao
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
			this.workPathTarefa = workPath;
		}
		return this.workPathTarefa;
	}
	
	public File gravarArquivo(File workPath, TipoArquivoEnum tipo) throws Exception {
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
	
	public void incluirLabJob(ComandoEnum cmdPrincipal, String comandoOK, String cmdLog ) throws Exception {
		//
		String relativeWorkpath = definirWorkPathTarefa().getAbsolutePath().substring( definirWorkPathTarefa().getAbsolutePath().indexOf(TarefaBusiness.PREFIXO_PASTA_BOT) );
		
		db.incluirLabJob(farmacoProtocoloDTO.getJarLeituraCodigo(), tarefaDTO.getCodigo(), cmdPrincipal.getIndex(), cmdLog.toString(), relativeWorkpath);
		
	}
}
