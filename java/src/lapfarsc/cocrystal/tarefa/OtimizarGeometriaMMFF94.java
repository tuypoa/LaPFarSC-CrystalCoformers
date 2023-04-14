/**
 * @author tuypoa
 *
 *
 */
package lapfarsc.cocrystal.tarefa;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import lapfarsc.business.TarefaBusiness;
import lapfarsc.business.TarefaExec;
import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.FarmacoProtocoloDTO;
import lapfarsc.dto.LabJobDTO;
import lapfarsc.dto.MsgDTO;
import lapfarsc.util.Dominios.ComandoEnum;
import lapfarsc.util.Dominios.InfoMaquinaEnum;
import lapfarsc.util.Dominios.TipoArquivoEnum;
import lapfarsc.util.Dominios.TipoMensagemDTOEnum;

public class OtimizarGeometriaMMFF94 implements TarefaExec {

	private String filenameOutputPrincipal = "MMFF94.sdf";
	
	@Override
	public MsgDTO prepararExecucao(TarefaBusiness tb, FarmacoProtocoloDTO farmacoProtocoloDTO) {
		System.out.println("> Tarefa<OtimizarGeometriaMMFF94>.executar();");
		MsgDTO msgDTO = null;
		try {
			//Bot-Farmaco-[0-9]/protocolo-[0-9]/tarefa-[0-9]/jarleitura-[0-9]/
			File workPath = tb.definirWorkPathTarefa(farmacoProtocoloDTO); 
	
			//gravar arquivo
			File arquivoEnviado = tb.gravarArquivoFarmacoProtocolo(farmacoProtocoloDTO, workPath, TipoArquivoEnum.ENVIADO);
			
			String extensao = arquivoEnviado.getName().substring(arquivoEnviado.getName().lastIndexOf(".")+1);
			String outputFile = arquivoEnviado.getName().substring(0,arquivoEnviado.getName().lastIndexOf("."))+".sdf";
			
			StringBuilder cmdLog = new StringBuilder();
			//converter arquivo para .SDF 
			//obabel -imol2 VEMRAI.mol2 -osdf > VEMRAI.sdf
			ComandoDTO cmdDTO = tb.obterComandoDTO(ComandoEnum.OBABEL_CONVERTFILE);
			String cmdTemplate = cmdDTO.getTemplate();
			cmdTemplate = cmdTemplate.replace("@TYPEIN", extensao.toLowerCase());
			cmdTemplate = cmdTemplate.replace("@INPUTFILE", arquivoEnviado.getName());
			cmdTemplate = cmdTemplate.replace("@TYPEOUT", "sdf");
			cmdTemplate = cmdTemplate.replace("@OUTPUTFILE", outputFile);
			String cmd = cmdTemplate;
			
			//System.out.println("cd "+workPath+" && "+cmd);
			
			cmdLog.append("$").append(cmd).append("\n");
			
			ProcessBuilder builder = new ProcessBuilder("/bin/sh","-c","cd "+workPath+" && "+cmd);
	        Process p = builder.start();
	        int exitCode = p.waitFor();
	        if (exitCode != 0) {
				//System.out.println(exitCode);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = "";
				String output = "";
				int i=0;
				while((line = bufferedReader.readLine()) != null){
					output += line + "\n";
					if(i++==5) break;
				}
				msgDTO = new MsgDTO(TipoMensagemDTOEnum.ERRO, cmdDTO.getPrefixo()+": "+output);
			}else {
				//verificar existencia do arquivo convertido .SDF
				File arqSDF = new File(workPath +"/"+ outputFile);
				if(!arqSDF.exists()) {
					return new MsgDTO(TipoMensagemDTOEnum.ERRO, "File "+outputFile+" not found.");
				}else {
					//executar comando principal com "&" no final da linha de comando
					cmdDTO = tb.obterComandoDTO(ComandoEnum.MOLCONVERT_MMFF94_FINE);
					cmdTemplate = cmdDTO.getTemplate();
					cmdTemplate = cmdTemplate.replace("@INPUTFILE", outputFile);
					cmdTemplate = cmdTemplate.replace("@OUTPUTFILE", "id"+farmacoProtocoloDTO.getJarLeituraCodigo()+"-"+filenameOutputPrincipal );
					String comandoPrincipal = cmdTemplate;
					
					cmdLog.append("$").append(comandoPrincipal);//.append("\n");
					
					builder = new ProcessBuilder("/bin/sh","-c","export JAVA_HOME="+tb.getInfoMaquina(InfoMaquinaEnum.JAVA_HOME)+" && cd "+workPath+" && "+comandoPrincipal);
			        p = builder.start();
			        exitCode = p.waitFor();
			        if (exitCode != 0) {
						//System.out.println(exitCode);
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";
						String output = "";
						int i=0;
						while((line = bufferedReader.readLine()) != null){
							output += line + "\n";
							if(i++==5) break;
						}
						msgDTO = new MsgDTO(TipoMensagemDTOEnum.ERRO, cmdDTO.getPrefixo()+": "+output);
					}else {
						//usar comando "molconvert" para otimizar
						//molconvert -3:S{fine}[mmff94]L{3}[E] mol VEMRAI.sdf -o VEMRAI-MMFF94.sdf
						tb.incluirLabJobFarmacoProtocolo(farmacoProtocoloDTO, ComandoEnum.MOLCONVERT_MMFF94_FINE, workPath, comandoPrincipal, cmdLog.toString());
						
						return new MsgDTO(TipoMensagemDTOEnum.OK, null);
					}
				}
			}
	        
		}catch (Throwable e) {
			msgDTO = tb.getMsgDTOException(e);
		}
		if(msgDTO==null) {
			msgDTO = new MsgDTO(TipoMensagemDTOEnum.ERRO, "Desconhecido: "+this.getClass().getName());
		}
		return msgDTO;
	}

	@Override
	public LabJobDTO verificarExecucao(TarefaBusiness tb, LabJobDTO labJobDTO) {
		System.out.println("> Tarefa<OtimizarGeometriaMMFF94>.verificarExecucao();");
		try {
			ProcessBuilder builder = new ProcessBuilder("/bin/sh","-c","ps aux | grep '"+labJobDTO.getComandoDTO().getPrefixo()+"'");
			Process pr = builder.start();
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			boolean achou = false;
			while ((line=buf.readLine())!=null) {
				//System.out.println(line);
				int cmdE = labJobDTO.getComando().lastIndexOf("&");
				String comandoSearch = labJobDTO.getComando().substring(0, cmdE==-1?labJobDTO.getComando().length():cmdE ).trim();
				comandoSearch = comandoSearch.substring(comandoSearch.lastIndexOf(" "));
				if(line.indexOf(comandoSearch)!=-1){
					//System.out.println("ACHOU");
					//se existe no ps aux, atualizar dados em "labjob": jarleitura_verificado, pid, executando, ...
					//parse da linha, pegar PID 
					do{
						line = line.replace("  ", " ");
					} while (line.indexOf("  ")!=-1);
					labJobDTO.setPid( Long.parseLong( line.split(" ")[1] ) );
					labJobDTO.setInterrompido(Boolean.FALSE);
					labJobDTO.setConcluido(Boolean.FALSE);
					labJobDTO.setExecutando(Boolean.TRUE);
					labJobDTO.setMsgDTO( new MsgDTO(TipoMensagemDTOEnum.OK, null) );
					achou = true;
					break;
				}
			}
			//se nao existe no "ps aux"
			if(!achou) {
				labJobDTO.setExecutando(Boolean.FALSE);
				
				File root = tb.getRootWorkPathMaquina(labJobDTO.getTarefaCodigo());
				File workPath = new File(root, labJobDTO.getWorkPath());
				if(workPath.exists()) {
					//verificar se foi interrompido ou concluido
					File outFile = new File(workPath, "id"+labJobDTO.getJarLeituraCodigo()+"-"+filenameOutputPrincipal);
					if(!outFile.exists() || outFile.length()==0) {
						labJobDTO.setInterrompido(Boolean.TRUE);
						labJobDTO.setConcluido(Boolean.FALSE);
						labJobDTO.setMsgDTO( new MsgDTO(TipoMensagemDTOEnum.ERRO, "Output file is empty.") );
					}else {
						labJobDTO.setInterrompido(Boolean.FALSE);
						labJobDTO.setConcluido(Boolean.TRUE);
						labJobDTO.setMsgDTO( new MsgDTO(TipoMensagemDTOEnum.OK, null) );
					}
				}else {
					labJobDTO.setMsgDTO( new MsgDTO(TipoMensagemDTOEnum.ERRO, "Job-workpath not found.") );
				}
			}
		}catch (Throwable e) {
			labJobDTO.setMsgDTO( tb.getMsgDTOException(e) );
		}
		if(labJobDTO.getMsgDTO()==null) {
			labJobDTO.setMsgDTO( new MsgDTO(TipoMensagemDTOEnum.ERRO, "Desconhecido: "+this.getClass().getName()) );
		}
		return labJobDTO;
	}

	@Override
	public MsgDTO parseExecucao(TarefaBusiness tb) {
		System.out.println("> Tarefa<OtimizarGeometriaMMFF94>.parseExecucao();");
		MsgDTO msgDTO = null;
		try {
			//fazer a digestao dos arquivos
			
			
			//atualizar dados em "labjob": jarleitura_verificado, executando, concluido,
		
		
		}catch (Throwable e) {
			msgDTO = tb.getMsgDTOException(e);
		}
		if(msgDTO==null) {
			msgDTO = new MsgDTO(TipoMensagemDTOEnum.ERRO, "Desconhecido: "+this.getClass().getName());
		}
		return msgDTO;
	}


	
	
}
