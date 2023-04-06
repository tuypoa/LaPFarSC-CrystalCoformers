package lapfarsc.business;

import java.io.File;
import java.io.FileOutputStream;

import lapfarsc.dto.ArquivoDTO;
import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.FarmacoProtocoloDTO;
import lapfarsc.dto.MaquinaDTO;
import lapfarsc.dto.TarefaDTO;
import lapfarsc.util.Dominios.ComandoEnum;
import lapfarsc.util.Dominios.InfoMaquinaEnum;
import lapfarsc.util.Dominios.TipoArquivoEnum;

public class TarefaBusiness {

	public static String PREFIXO_PASTA_BOT = "Bot-";
	
	private DatabaseBusiness db = null;
	private MaquinaDTO maquinaDTO = null;
	private FarmacoProtocoloDTO farmacoProtocoloDTO = null;
	private TarefaDTO tarefaDTO = null;
	TarefaExec tarefaExec = null;

	public TarefaBusiness(DatabaseBusiness db, MaquinaDTO maquinaDTO, FarmacoProtocoloDTO farmacoProtocoloDTO) throws Exception{
		this.db = db;
		this.maquinaDTO = maquinaDTO;
		this.farmacoProtocoloDTO = farmacoProtocoloDTO;
		this.tarefaDTO = db.selectTarefaDTO(farmacoProtocoloDTO.getTarefaCodigo());
		//System.out.println(tarefaDTO.getJavaClass());
	}
	
	public boolean verificarProcessoAutomatico() throws Exception {
		//tarefa manual? tem classe associada?
		if(!tarefaDTO.getManual() && tarefaDTO.getJavaClass()!=null) {
			try {
				Class<?> cls = Class.forName(tarefaDTO.getJavaClass());
				this.tarefaExec = (TarefaExec) cls.getDeclaredConstructor().newInstance();
				return true;
				
			} catch (ClassNotFoundException e) {
				System.out.println("ClassNotFoundException: "+tarefaDTO.getJavaClass());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean executarProcessoAutomatico() throws Exception {
		if(tarefaExec!=null) {
			return tarefaExec.executar(this);
		}
		return false;
	}
	
	public File definirWorkPathTarefa() throws Exception {
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
		return workPath;
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
	
	public void incluirLabJob(ComandoEnum comando, String cmd, String relativeWorkpath, StringBuilder cmdLog ) {
		//
		
		
	}
}
