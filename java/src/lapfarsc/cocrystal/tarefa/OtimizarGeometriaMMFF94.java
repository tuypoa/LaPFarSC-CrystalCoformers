package lapfarsc.cocrystal.tarefa;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import lapfarsc.business.TarefaBusiness;
import lapfarsc.business.TarefaExec;
import lapfarsc.dto.ComandoDTO;
import lapfarsc.util.Dominios.ComandoEnum;
import lapfarsc.util.Dominios.TipoArquivoEnum;

public class OtimizarGeometriaMMFF94 implements TarefaExec {

	@Override
	public boolean executar(TarefaBusiness tb) throws Exception {
		System.out.println("> Tarefa<OtimizarGeometriaMMFF94>.executar();");
		try {
			//Bot-Farmaco-[0-9]/protocolo-[0-9]/tarefa-[0-9]/jarleitura-[0-9]/
			File workPath = tb.definirWorkPathTarefa(); 
	
			//gravar arquivo
			File arquivoEnviado = tb.gravarArquivo(workPath, TipoArquivoEnum.ENVIADO);
			
			String extensao = arquivoEnviado.getName().substring(arquivoEnviado.getName().lastIndexOf(".")+1);
			String outputFile = arquivoEnviado.getName().substring(0,arquivoEnviado.getName().lastIndexOf("."))+".sdf";
			
			StringBuilder cmdLog = new StringBuilder();
			String relativeWorkpath = workPath.getAbsolutePath().substring( workPath.getAbsolutePath().indexOf(TarefaBusiness.PREFIXO_PASTA_BOT) );
			//converter arquivo para .SDF 
			//obabel -imol2 VEMRAI.mol2 -osdf > VEMRAI.sdf
			ComandoDTO cmdDTO = tb.obterComandoDTO(ComandoEnum.OBABEL_CONVERTFILE);
			String cmdTemplate = cmdDTO.getTemplate();
			cmdTemplate = cmdTemplate.replace("@TYPEIN", extensao.toLowerCase());
			cmdTemplate = cmdTemplate.replace("@INPUTFILE", arquivoEnviado.getName());
			cmdTemplate = cmdTemplate.replace("@TYPEOUT", "sdf");
			cmdTemplate = cmdTemplate.replace("@OUTPUTFILE", outputFile);
			String cmd = cmdTemplate;
			
			System.out.println("cd "+workPath+" && "+cmd);
			
			cmdLog.append("$").append(cmd).append("\n");
			ProcessBuilder builder = new ProcessBuilder("/bin/sh","-c","cd "+workPath+" && "+cmd);
	        Process p = builder.start();
	        int exitCode = p.waitFor();
	        if (exitCode != 0) {
				System.out.println(exitCode);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = "";
				String output = "";
				int i=0;
				while((line = bufferedReader.readLine()) != null){
					output += line + "\n";
					if(i++==5) break;
				}
				System.out.println("----------------------------------");
				System.out.println(cmd);
				System.out.println(output);
				System.out.println("----------------------------------");
			}else {
				//verificar existencia do arquivo convertido .SDF
				
				//executar comando principal com "&" no final da linha de comando
				cmdDTO = tb.obterComandoDTO(ComandoEnum.MOLCONVERT_MMFF94_FINE);
				String comandoPrincipal = null;
				
				//usar comando "molconvert" para otimizar
				//molconvert -3:S{fine}[mmff94]L{3}[E] mol VEMRAI.sdf -o VEMRAI-MMFF94.sdf
				tb.incluirLabJob(ComandoEnum.MOLCONVERT_MMFF94_FINE, comandoPrincipal, relativeWorkpath, cmdLog);
				
				return true;
			}
		}catch (Exception e) {
			e.printStackTrace();
			
		}
		return false;
	}

	@Override
	public void verificar(TarefaBusiness tb) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reiniciar(TarefaBusiness tb) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void concluir(TarefaBusiness tb) throws Exception {
		// TODO Auto-generated method stub
		
	}


}
