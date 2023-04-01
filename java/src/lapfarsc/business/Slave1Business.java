/**
 * @author tuypoa
 *
 *
 */
package lapfarsc.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.JarLeituraDTO;
import lapfarsc.dto.MaquinaDTO;
import lapfarsc.init.InitLocal;
import lapfarsc.qe.dashboard.dto.CmdTopDTO;
import lapfarsc.qe.dashboard.dto.MaqArqHashDTO;
import lapfarsc.qe.dashboard.dto.MaquinaQeArquivoInDTO;
import lapfarsc.qe.dashboard.dto.MoleculaDTO;
import lapfarsc.qe.dashboard.dto.ParseNomeDTO;
import lapfarsc.qe.dashboard.dto.PsauxDTO;
import lapfarsc.qe.dashboard.dto.QeArquivoInDTO;
import lapfarsc.qe.dashboard.dto.QeResumoDTO;
import lapfarsc.util.Dominios.ComandoEnum;

public class Slave1Business {
	
	private DatabaseBusiness db = null;
	private MaquinaDTO maquinaDTO = null;
	
	public Slave1Business(Connection conn, Integer maqId) throws Exception{
		this.db = new DatabaseBusiness(conn);
		this.maquinaDTO = db.selectMaquinaDTO(maqId);
	}

	public void gravarJarLeitura(boolean force) throws Exception {		
		Process process = Runtime.getRuntime().exec("top -bn1");
		int exitCode = process.waitFor();
		if (exitCode == 0) {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			String output = "";
			int i=0;
			while((line = bufferedReader.readLine()) != null){
				output += line + "\n";
				if(i++==5) break;
			}
			
			CmdTopDTO cmdDTO = HeadBusiness.getCommandTopInfos(output);
			if(force || cmdDTO.getCpuUsed().doubleValue() < 60){
				JarLeituraDTO jlDTO = new JarLeituraDTO();
				jlDTO.setCpuUsed( maquinaDTO.getCpuUsed() );
				jlDTO.setMemUsed( cmdDTO.getMemUsed() );
				jlDTO.setMaquinaCodigo(maquinaDTO.getCodigo());
				db.incluirJarLeituraDTO(jlDTO);
			}
		}
	}
	
	public void lerTodosProcessos() throws Exception {
		List<ComandoDTO> listComandoDTO = db.selectListComandoDTO();		
		Process process = Runtime.getRuntime().exec("ps aux");
		int exitCode = process.waitFor();
		if (exitCode == 0) {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			HashMap<ComandoEnum,List<String>> map = new HashMap<ComandoEnum,List<String>>();
			while((line = bufferedReader.readLine()) != null){
				for (ComandoDTO comandoDTO : listComandoDTO) {
					int poscmd = line.indexOf(comandoDTO.getPrefixo());					
					if(poscmd!=-1){
						ComandoEnum tipo = ComandoEnum.getByIndex(comandoDTO.getCodigo()); 
						switch (tipo) {
						case MPIRUN_PW:
						case PW:						
							List<String> l = map.get(tipo);
							if(l==null){
								l = new ArrayList<String>();
								map.put(tipo, l);
							}
							//System.out.println(line);							
							l.add(line);
							break;
						case JAVA_JAR:
							break;
						}
						break;
					}
				}
			}
			//PROCESSAR LINHAS DO PS AUX
			List<PsauxDTO> listPsauxDTO = new ArrayList<PsauxDTO>();
			//System.out.println(map.get(ComandoEnum.MPIRUN_PW).size());
			db.updateNaoExecutandoTodosQeResumoDTO(maquinaDTO.getCodigo());
			Set<ComandoEnum> s = map.keySet();
			for (ComandoEnum cs : s) {
				List<String> l = map.get(cs);
				for (String p : l) {
					while(p.indexOf("  ")!=-1){
						p = p.replaceAll("  ", " ");
					}
					//user 17962 0.0 0.0 88188 5228 pts/0 S+ Mar15 0:47 mpirun -np 8 pw.x -in arquivo.in
					String col[] = p.split(" ");
					PsauxDTO dto = new PsauxDTO();
					dto.setMaquinaCodigo(maquinaDTO.getCodigo());
					dto.setComandoCodigo( cs.getIndex() );
					dto.setPid( Integer.parseInt(col[1]) );
					dto.setUid( col[0].trim() );
					dto.setCpu( BigDecimal.valueOf( Double.parseDouble(col[2].replace(",","."))) );
					dto.setMem( BigDecimal.valueOf( Double.parseDouble(col[3].replace(",","."))) );
					dto.setConteudo( p.substring( p.indexOf(col[9])+col[9].length()+1 ) );
					if(dto.getConteudo().length()>=150){
						dto.setConteudo(dto.getConteudo().substring(0,149));
					}
					QeArquivoInDTO arquivoInDTO = localizarArquivoInDTOPeloNome( col[col.length-1]);
					if(arquivoInDTO!=null){
						dto.setQeArquivoInCodigo(arquivoInDTO.getCodigo());
						QeResumoDTO resumoDTO = localizarResumoDTO(arquivoInDTO);
						if(resumoDTO!=null){
							dto.setQeResumoCodigo(resumoDTO.getCodigo());
						}
					}
					listPsauxDTO.add(dto);
				}
			}		
			db.incluirListPsauxDTO(listPsauxDTO, maquinaDTO.getCodigo());
		}
	}
	
	private QeArquivoInDTO localizarArquivoInDTOPeloNome(String nome) throws Exception{
		//ENCONTRAR MOLECULA no nome do arquivo
		MoleculaDTO moleculaDTO = null;
		List<MoleculaDTO> listMoleculaDTO = db.selectListMoleculaDTO();
		for (MoleculaDTO m : listMoleculaDTO) {
			if(nome.indexOf(m.getNome().toLowerCase())!=-1){
				moleculaDTO = m;
				break;
			}
		}
		if(moleculaDTO!=null){
			String rootPathArquivo = maquinaDTO.getRootPath()+File.separator+
					moleculaDTO.getNome()+File.separator+
					InitLocal.PATH_MONITORAMENTO;
			
			return localizarArquivoInDTOPeloNome(nome, rootPathArquivo, moleculaDTO.getCodigo());
		}
		return null;
	}
	
	private QeArquivoInDTO localizarArquivoInDTOPeloNome(String nome, String rootPathArquivo, Integer moleculaCodigo) throws Exception{
		String filename = rootPathArquivo+File.separator+nome;
		
		File arquivoIn = new File(filename);
		if(arquivoIn.exists() && arquivoIn.isFile()){		
			InputStream is = Files.newInputStream(Paths.get(filename));
			String hashArq = DigestUtils.md5Hex(is);				
			is.close();
			
			ParseNomeDTO nomeDTO = parseNomeArquivoIn(nome);
			
			String hashMaqArq = DigestUtils.md5Hex( maquinaDTO.getCodigo() +";"+ hashArq );				
			QeArquivoInDTO arquivoInDTO = db.selectQeArquivoInDTOPeloHash(hashMaqArq);
			if(arquivoInDTO==null){	
				arquivoInDTO = new QeArquivoInDTO();
				if(nomeDTO!=null){
					arquivoInDTO.setDescricao(nomeDTO.toString());
					arquivoInDTO.setQtdeMolecula(nomeDTO.getMoleculaQtde());
					arquivoInDTO.setNomeMolecula(nomeDTO.getMoleculaNome());
					arquivoInDTO.setQtdeCoformador(nomeDTO.getCoformadorQtde());
					arquivoInDTO.setNomeCoformador(nomeDTO.getCoformadorNome());
					arquivoInDTO.setTipoCelula(nomeDTO.getTipo());
				}else{
					arquivoInDTO.setDescricao(nome);
				}				
				arquivoInDTO.setHashMaqArq(hashMaqArq);
				arquivoInDTO.setNome(nome);				
				arquivoInDTO.setMoleculaCodigo(moleculaCodigo);
				String cin = loadTextFile(arquivoIn);				
				int key = cin.indexOf("&SYSTEM");
				if(key!=-1){
					key = cin.indexOf("nat",key);				
					if(key!=-1){
						arquivoInDTO.setQtdeAtomo( Integer.parseInt( cin.substring( cin.indexOf("=", key) , cin.indexOf("\n",key) ) ) );
					}
				}
				arquivoInDTO.setConteudo(cin);
				db.incluirQeArquivoInDTO(arquivoInDTO);
			}
			arquivoInDTO = db.selectQeArquivoInDTOPeloHash(hashMaqArq);
			arquivoInDTO.setNome(nome); //caso tenha mais de 1 OUTPUT
			
			//incluir vinculo com a maquina
			MaquinaQeArquivoInDTO maDTO = db.selectMaquinaQeArquivoInDTO(maquinaDTO.getCodigo(), arquivoInDTO.getCodigo());
			if(maDTO==null){
				maDTO = new MaquinaQeArquivoInDTO();
				maDTO.setMaquinaCodigo(maquinaDTO.getCodigo());
				maDTO.setQeArquivoInCodigo(arquivoInDTO.getCodigo());
				maDTO.setHashArqIn(hashArq);
				maDTO.setRootPath( arquivoIn.getParent()+File.separator );
				maDTO.setOrdem( nomeDTO.getOrdem() );
				db.incluirMaquinaQeArquivoInDTO(maDTO);
			}
			//System.out.println(parseNomeArquivoIn(nome));
			return arquivoInDTO;
		}
		return null;
	}
	
	private List<QeArquivoInDTO> localizarTodosArquivoInDTO() throws Exception {
		List<QeArquivoInDTO> listArquivoInDTO = new ArrayList<QeArquivoInDTO>();		
		List<MoleculaDTO> listMoleculaDTO = db.selectListMoleculaDTO();
		for (MoleculaDTO m : listMoleculaDTO) {
			String rootPathArquivo = maquinaDTO.getRootPath()+File.separator+
				m.getNome()+File.separator+
				InitLocal.PATH_MONITORAMENTO; 
			
			File pasta = new File(rootPathArquivo);
			if(pasta.exists() && pasta.isDirectory()){				
				File[] aIn = pasta.listFiles(new FileFilter() {	
					@Override
					public boolean accept(File arg0) {
						return arg0.getName().toLowerCase().endsWith(".in");
					}
				});
				for (File file : aIn) {
					QeArquivoInDTO aDTO = localizarArquivoInDTOPeloNome(file.getName(), file.getParent(), m.getCodigo());
					listArquivoInDTO.add(aDTO);
				}
			}
		}
		return listArquivoInDTO;
	}
	
	
	private ParseNomeDTO parseNomeArquivoIn(String nome){
		//UC-default_00_1macitentan-1maltitol-VEMRAI.in
		ParseNomeDTO dto = new ParseNomeDTO();		
		try{ 
			if(nome.startsWith("UC-")){
				//StringBuilder sb = new StringBuilder();
				nome = nome.replace("UC-", "").replace(".in", "");
				dto.setOrdem( Integer.parseInt( nome.substring(nome.indexOf("_")+1 , nome.indexOf("_",nome.indexOf("_")+1) ) ) );
				String s2 = nome.substring(nome.indexOf("_",nome.indexOf("_")+1)+1 );
				//sb.append( s2.substring(0,1) ).append(" ");
				dto.setMoleculaQtde( Integer.parseInt( s2.substring(0,1) ) );
				//sb.append( s2.substring(1,2).toUpperCase() ).append(s2.substring(2, s2.indexOf("-")) );//Macitentan
				dto.setMoleculaNome( s2.substring(1,2).toUpperCase()+s2.substring(2, s2.indexOf("-")) );
				
				if(s2.indexOf("-", s2.indexOf("-")+1) != -1){
					//sb.append(" : ").append( s2.substring(s2.indexOf("-")+1,s2.indexOf("-")+2) ).append(" ");
					dto.setCoformadorQtde( Integer.parseInt( s2.substring(s2.indexOf("-")+1,s2.indexOf("-")+2) ) );
					//sb.append( s2.substring(s2.indexOf("-")+2, s2.indexOf("-")+3).toUpperCase() ).append( s2.substring(s2.indexOf("-")+3, s2.indexOf("-", s2.indexOf("-")+1) ) );//Maltitol
					dto.setCoformadorNome( s2.substring(s2.indexOf("-")+2, s2.indexOf("-")+3).toUpperCase()+s2.substring(s2.indexOf("-")+3, s2.indexOf("-", s2.indexOf("-")+1) ) );	
				}
				String s1 = nome.substring(0, nome.indexOf("_"));
				s1 = s1.substring(0,1).toUpperCase() + s1.substring(1);
				//sb.append(" (").append( s1 ).append(")"); //(Default)
				dto.setTipo( s1 );
				return dto;
			}
		}catch (Exception e){
			//ignore
		}			
		return null;	
	}
	
	
	private QeResumoDTO localizarResumoDTO(QeArquivoInDTO arquivoInDTO) throws Exception{
		MaquinaQeArquivoInDTO maDTO = db.selectMaquinaQeArquivoInDTO(maquinaDTO.getCodigo(), arquivoInDTO.getCodigo());
		String nome = arquivoInDTO.getNome().substring(0, arquivoInDTO.getNome().lastIndexOf("."))+".out";
		QeResumoDTO resumoDTO = db.selectQeResumoDTOPeloNome(arquivoInDTO.getCodigo(), nome);
		
		String filename = maDTO.getRootPath()+nome;
		
		File arquivoOut = new File(filename);
		if(arquivoOut.exists() && arquivoOut.isFile()){		
			InputStream is = Files.newInputStream(Paths.get(filename));
			String md5 = DigestUtils.md5Hex(is);
			is.close();			
			double tamanhoKb = (double) ((double) Files.size(Paths.get(filename))) / 1024;
			
			if(resumoDTO==null || (!resumoDTO.getHashOutput().equals(md5) && tamanhoKb<resumoDTO.getTamanhoKb() )){
				resumoDTO = new QeResumoDTO();
				resumoDTO.setQeArquivoInCodigo(arquivoInDTO.getCodigo());
				resumoDTO.setHashOutput(md5);
				resumoDTO.setNome(nome);
				resumoDTO.setTamanhoKb(tamanhoKb);
				resumoDTO.setExecutando(Boolean.TRUE);
				db.incluirQeResumoDTO(resumoDTO, maquinaDTO.getCodigo());
				resumoDTO = db.selectQeResumoDTOPeloNome(arquivoInDTO.getCodigo(), nome);
				
			}else if(!resumoDTO.getHashOutput().equals(md5)){
				//atualizar hashoutput , tamanhokb, executando
				resumoDTO.setHashOutput(md5);
				resumoDTO.setTamanhoKb(tamanhoKb);
				resumoDTO.setExecutando(Boolean.TRUE);
				db.updateQeResumoDTOHash(resumoDTO);
			}else{
				resumoDTO.setExecutando(Boolean.TRUE);
				db.updateQeResumoDTOExecutando(resumoDTO);
			}
			
		}
		return resumoDTO;
	}
	
	public void analisarTodosOutputs() throws Exception {
		
		//ATUALIZAR OS CONCLUIDOS ou INTERROMPIDOS
		List<QeArquivoInDTO> listArqIn = localizarTodosArquivoInDTO();
		for (QeArquivoInDTO qaiDTO : listArqIn) {
			System.out.println(qaiDTO.getNome());
			MaquinaQeArquivoInDTO maDTO = db.selectMaquinaQeArquivoInDTO(maquinaDTO.getCodigo(), qaiDTO.getCodigo());
			String nomeOut = qaiDTO.getNome().substring(0, qaiDTO.getNome().lastIndexOf("."))+".out";
			//listar todos .OUT
			String filename = maDTO.getRootPath()+nomeOut;			
			File arquivoOut = new File(filename);
			if(arquivoOut.exists() && arquivoOut.isFile()){
				InputStream is = Files.newInputStream(Paths.get(filename));
				String md5 = DigestUtils.md5Hex(is);
				is.close();	
				double tamanhoKb = (double) ((double) Files.size(Paths.get(filename))) / 1024;
				
				QeResumoDTO qrDTO = db.selectQeResumoDTOPeloNome(qaiDTO.getCodigo(), nomeOut);
				if(qrDTO==null){		
					qrDTO = new QeResumoDTO();
					qrDTO.setQeArquivoInCodigo(qaiDTO.getCodigo());
					qrDTO.setHashOutput(md5);
					qrDTO.setNome(nomeOut);
					qrDTO.setTamanhoKb(tamanhoKb);
					qrDTO.setExecutando(Boolean.FALSE);
					db.incluirQeResumoDTO(qrDTO, maquinaDTO.getCodigo());
				}else if(!qrDTO.getExecutando()){
					if(!qrDTO.getHashOutput().equals(md5)){
						//atualizar hashoutput , tamanhokb, executando
						qrDTO.setHashOutput(md5);
						qrDTO.setTamanhoKb(tamanhoKb);
						qrDTO.setExecutando(Boolean.FALSE);
						db.updateQeResumoDTOHash(qrDTO);
					}else{
						qrDTO.setExecutando(Boolean.FALSE);
						db.updateQeResumoDTOExecutando(qrDTO);
					}
				}
			}
		}
		
		OutputQeBusiness ob = new OutputQeBusiness(db);
		//analisar os em andamento: processar TRUE
		List<QeResumoDTO> listResumoDTO = db.selectQeResumoDTOAProcessar();
		for (QeResumoDTO qeResumoDTO : listResumoDTO) {
			MaquinaQeArquivoInDTO maDTO = db.selectMaquinaQeArquivoInDTO(maquinaDTO.getCodigo(), qeResumoDTO.getQeArquivoInCodigo());
			if(maDTO!=null){
				ob.processarArquivoOutput(qeResumoDTO, new File( maDTO.getRootPath()+qeResumoDTO.getNome() ));
			}
		}
		
		//if(listResumoDTO.size()>0 ){ //OU TEVE concluidos 
		this.gravarJarLeitura(listResumoDTO.size()>0); //OU TEVE concluidos
		//}
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
	
	
	
	public void iniciarProcessos() throws Exception {		
		if(db.verificarMaquinaCPUOciosa(maquinaDTO.getCodigo()) && maquinaDTO.getCpuUsed().doubleValue() < 80){
			
			//verificar se tem arquivo nela para executar
			//TODO considerar arquivos interrompidos sem erro
			List<MaqArqHashDTO> listHash = db.verificarArquivoElegivelParaExecutar(maquinaDTO.getCodigo());
			
			if(listHash.size()>0){
				if(maquinaDTO.getIniciarJob()){ //AUTORIZADA NA ULTIMA LEITURA				
					
					MaqArqHashDTO executarDTO = listHash.get(0); //primeiro da lista
					
					//cmd mpirun -np @NCPU pw.x -in @QEARQIN > @QEARQOUT &
					ComandoDTO cmdMpirun = db.selectComandoDTO(ComandoEnum.MPIRUN_PW.getIndex());
					
					String cmd = cmdMpirun.getTemplate();
					cmd = cmd.replace("@NCPU", String.valueOf(maquinaDTO.getMinCpu()));
					cmd = cmd.replace("@QEARQIN", executarDTO.getNomeArquivo() );
					cmd = cmd.replace("@QEARQOUT", executarDTO.getNomeArquivo().substring(0, executarDTO.getNomeArquivo().lastIndexOf(".") )+".out" );
										
					//cmd = "sh -c 'cd "+executarDTO.getRootPath()+" && "+cmd+"'";					
					System.out.println("INICIAR JOB: "+cmd);
										
					String s = null;
					String pathHome = executarDTO.getRootPath().substring(0, executarDTO.getRootPath().indexOf("/","/home/".length())+1);					
					ProcessBuilder builder = new ProcessBuilder("/bin/sh","-c","export PATH="+pathHome+InitLocal.PATH_PW_QUANTUM_ESPRESSO+":$PATH && " +
							"cd "+executarDTO.getRootPath()+" && " +cmd);
			        Process p = builder.start();
			        System.out.println( p.waitFor() );
			        
					BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
					BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					// read the output from the command
					//System.out.println("Here is the standard output of the command:\n");
					while ((s = stdInput.readLine()) != null) {
						System.out.println(s);
					}
					// read any errors from the attempted command
					//System.out.println("Here is the standard error of the command (if any):\n");
					while ((s = stdError.readLine()) != null) {
						System.out.println(s);
					}					
					//desligar "iniciarjob"
					maquinaDTO.setIniciarJob(Boolean.FALSE);
					db.updateMaquinaDTOIniciarJob(maquinaDTO);					
				}else{
					//System.out.println("not iniciar job");
					//verificar se o arquivo ja nao foi, ou esta sendo, processado por outra maquina
					for (MaqArqHashDTO hash: listHash) {
						List<MaqArqHashDTO> hashOutraMaquina = db.verificarArquivoEmOutraMaquina(hash);
						if(hashOutraMaquina.size()>0){
							//arquivo ja foi executado ao menos 1 vez em outra maquina
							//marcar para "ignorar" nessa
							hash.setIgnorar(Boolean.TRUE);
							db.updateMaquinaArquivoIgnorarExecucao(hash);
						}else{
							//autorizado a executar
							//marcar para "ignorar" em outras maquinas
							for (MaqArqHashDTO mOutra : hashOutraMaquina) {
								mOutra.setIgnorar(Boolean.TRUE);
								db.updateMaquinaArquivoIgnorarExecucao(mOutra);
							}
							//ligar "iniciarjob"
							maquinaDTO.setIniciarJob(Boolean.TRUE);
							db.updateMaquinaDTOIniciarJob(maquinaDTO);
						}
					}					
				}
			}
		}		
	}
	
}

