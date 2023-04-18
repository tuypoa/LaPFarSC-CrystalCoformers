/**
 * @author tuypoa
 *
 *
 */
package lapfarsc.init;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.regex.Pattern;

import lapfarsc.business.DatabaseBusiness;
import lapfarsc.business.HeadBusiness;
import lapfarsc.business.SlaveBusiness;
import lapfarsc.dto.JavaDeployDTO;
import lapfarsc.dto.MaquinaDTO;
import lapfarsc.dto.MaquinaStatusDTO;

public class InitLocal {

	public static String DATABASENAME = "lapfarsc-crystalcoformers";
	public static String DATABASEUSER = "postgres";
	public static String DATABASEPWD = "postgres";
	
	public static void main(String[] args) throws Exception{
		/*
		 ARGS:
		 0 = Database IP (127.0.0.1 or 192.168.0.102)
		 1 = (OPCIONAL) SLAVE
		 */
		if(args==null || args.length < 1) {
			System.err.println("--> Arg0 IS REQUIRED: Database IP.");
			return;
		}
		String databaseAddress = args[0];
		boolean isMatch = Pattern.compile("^(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
	               .matcher(databaseAddress)
	               .find(); 

		if(isMatch) {
			/*
			//VERIFICAR SE JA ESTA EXECUTANDO
			Runtime run = Runtime.getRuntime();
			Process pr = run.exec("ps aux");
			pr.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			int i = 0;
			while ((line=buf.readLine())!=null) {
				if(line.indexOf("Dashboard4qe")!=-1 && line.indexOf(execType.getArg())!=-1){
					if(i==1){
						System.out.println("--> THERE IS ANOTHER PROCESS STILL RUNNING.");
						return;
					}
					i++;
				}
			}	
			*/	
			
			boolean isSlave = false;
			if(args.length > 1) {
				isSlave = args[1].equals("SLAVE");
			}
			
			//ACESSAR POSTGRES
			String url = "jdbc:postgresql://"+databaseAddress+"/"+DATABASENAME+"?user="+DATABASEUSER+"&password="+DATABASEPWD;
			Connection conn = null;
			try {
				conn = DriverManager.getConnection(url);
				DatabaseBusiness db = new DatabaseBusiness(conn);
				
				if(db.checkConexao()) {
					String hostname = getHostname();
					MaquinaDTO maquina = db.selectMaquinaDTOByHostname( hostname );
					JavaDeployDTO ultimoDeploy = db.selectUltimoJavaDeployDTO();
					
					if(maquina!=null) {
						maquina.setInfoMaquina(db.selectListMaquinaInfoDTO( maquina.getCodigo() ));
						
						if(!isSlave && maquina.getHead()) {
							HeadBusiness head = new HeadBusiness(db, maquina);
							//verificar versao do JAR e fazer deploy
							head.fazerJavaDeploy(ultimoDeploy);
							head.acessarTodasMaquinas(ultimoDeploy);
						}else{
							SlaveBusiness slave = new SlaveBusiness(db, maquina);
							if(maquina.getJavaDeployDTO()!=null && maquina.getJavaDeployDTO().getCodigo() == ultimoDeploy.getCodigo()) {
								MaquinaStatusDTO ultimoStatusDTO = db.selectMaquinaStatusDTO(maquina.getCodigo());
								slave.gravarJarLeitura(ultimoStatusDTO.getCodigo());
								
								//verificar comandos dessa maquina atraves do "ps aux"
								//atualizar dados na tabela "labjob"
								slave.verificarListLabJob();
								
								//atualizar dados na tabela "resultados"
								slave.verificarListResultado();
								
								if(ultimoStatusDTO.getIniciarJob()) {
									//verificar CPU novamente (?), ultimo a verificar foi o HEAD
									
									slave.verificarFarmacoProtocolo();
								}
							}else {
								System.err.println("--> Slave sem javadeploy atualizado: "+hostname);		
							}
						}
					}else {
						System.err.println("--> Hostname NOT FOUND: "+hostname);
					}
				}else {
					System.err.println("--> Arg0 CONNECTION ERROR: Database IP "+args[0]);	
				}
			}finally{
				if(conn!=null) conn.close();
			}
		}else {
			System.err.println("--> Arg0 IS INVALID: Database IP "+args[0]);
		}
		System.out.println("***");
	}	
	
	
	private static String getHostname() throws Exception {
		Runtime run = Runtime.getRuntime();
		Process pr = run.exec("hostname");
		pr.waitFor();
		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = "";
		while ((line=buf.readLine())!=null) {
			return line;
		}	
		return null;
	}
	
	
}