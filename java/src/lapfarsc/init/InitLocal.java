/**
 * @author tuypoa
 *
 *
 */
package lapfarsc.init;

import java.sql.Connection;
import java.sql.DriverManager;

import lapfarsc.business.HeadBusiness;
import lapfarsc.business.Slave1Business;
import lapfarsc.util.Dominios.ArgTypeEnum;

public class InitLocal {

	//private static String POSTGRES_ADDRESS = "192.168.0.102:5432"; //TROCAR!!! Anguirel
	private static String POSTGRES_ADDRESS = "localhost:5432"; //TROCAR!!!
	public static String PATH_MONITORAMENTO = "05-quantum/PW-output/";
	public static String PATH_PW_QUANTUM_ESPRESSO = "Documents/qe-7.0/bin/";
	
	public static void main(String[] args) throws Exception{
		/*
		 ARGS:
		 0 = TIPO DE JAR (HEAD OU SLAVE1) 
		 */
		if(args==null || args.length < 1) {
			System.out.println("--> Arg0 IS REQUIRED.");
			return;
		}
		
		ArgTypeEnum execType = ArgTypeEnum.getByName( args[0] );
		if(execType != null){
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
			
			//ACESSAR POSTGRES
			String url = "jdbc:postgresql://"+POSTGRES_ADDRESS+"/dashboard4qe?user=postgres&password=postgres";
			Connection conn = null;
			try {
				conn = DriverManager.getConnection(url);				
				//iniciar tarefa
				switch (execType) {
				case HEAD:
					HeadBusiness head = new HeadBusiness(conn);
					head.acessarTodasMaquinas();						
					break;
				case SLAVE1:						
					Slave1Business slave1 = new Slave1Business(conn, Integer.parseInt(args[1]));					
					slave1.lerTodosProcessos();
					//verificar execucoes que foram interrompidas, gerar continuacao
					slave1.verificarProcessosInterrompidos();
					//sempre depois de ler os processos em execucao
					slave1.analisarTodosOutputs();
					//verificar se esta ociosa
					slave1.iniciarProcessos();					
					break;						
				default:
					System.out.println("--> Arg0 NOT FOUND.");
					break;
				}
			}finally{
				if(conn!=null) conn.close();
				System.out.println(execType.getArg() + " END");
			}
		}else{
			System.out.println("--> Arg0 NOT FOUND.");
		}
		System.out.println("***");
	}	
}