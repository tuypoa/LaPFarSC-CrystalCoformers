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
import lapfarsc.dto.MsgDTO;
import lapfarsc.util.Dominios.ComandoEnum;
import lapfarsc.util.Dominios.TipoArquivoEnum;
import lapfarsc.util.Dominios.TipoMensagemDTOEnum;

public class OtimizarGeometriaMMFF94 implements TarefaExec {

	@Override
	public MsgDTO prepararExecucao(TarefaBusiness tb) {
		System.out.println("> Tarefa<OtimizarGeometriaMMFF94>.executar();");
		MsgDTO msgDTO = null;
		try {
			//Bot-Farmaco-[0-9]/protocolo-[0-9]/tarefa-[0-9]/jarleitura-[0-9]/
			File workPath = tb.definirWorkPathTarefa(); 
	
			//gravar arquivo
			File arquivoEnviado = tb.gravarArquivo(workPath, TipoArquivoEnum.ENVIADO);
			
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
					cmdTemplate = cmdTemplate.replace("@OUTPUTFILE", outputFile.substring(0, outputFile.lastIndexOf("."))+"-MMFF94"+outputFile.substring(outputFile.lastIndexOf(".")+1) );
					String comandoPrincipal = cmdTemplate;
					
					cmdLog.append("$").append(comandoPrincipal);//.append("\n");
					
					builder = new ProcessBuilder("/bin/sh","-c","cd "+workPath+" && "+comandoPrincipal);
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
						tb.incluirLabJob(ComandoEnum.MOLCONVERT_MMFF94_FINE, comandoPrincipal, cmdLog.toString());
						
						return new MsgDTO(TipoMensagemDTOEnum.OK, null);
					}
				}
			}
	        
		}catch (Throwable e) {
			e.printStackTrace();
			msgDTO = null;
			StackTraceElement[] ary = e.getStackTrace();
			for (StackTraceElement ste: ary) {
				if(ste.getClassName().indexOf(this.getClass().getName())!=-1) {
					msgDTO = new MsgDTO(TipoMensagemDTOEnum.EXCEPTION, ste.getFileName()+"(Ln"+ste.getLineNumber()+"): "+ e.getMessage()); 
					break;
				}
			}
			if(msgDTO!=null) {
				return new MsgDTO(TipoMensagemDTOEnum.EXCEPTION, e.getMessage());
			}
		}
		if(msgDTO==null) {
			msgDTO = new MsgDTO(TipoMensagemDTOEnum.ERRO, "Desconhecido: "+this.getClass().getName());
		}
		return msgDTO;
	}

	@Override
	public MsgDTO verificarExecucao(TarefaBusiness tb) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MsgDTO parseExecucao(TarefaBusiness tb) {
		// TODO Auto-generated method stub
		return null;
	}


}
