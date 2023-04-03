/**
 * @author tuypoa
 *
 *
 */
package lapfarsc.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import lapfarsc.dto.CmdTopDTO;
import lapfarsc.dto.ComandoDTO;
import lapfarsc.dto.JavaDeployDTO;
import lapfarsc.dto.MaquinaDTO;
import lapfarsc.dto.MaquinaStatusDTO;
import lapfarsc.util.Dominios.ComandoEnum;
import lapfarsc.util.Dominios.InfoMaquinaEnum;


public class HeadBusiness {
	
	private DatabaseBusiness db = null;
	private MaquinaDTO maquinaHead = null;
	private List<MaquinaDTO> listMaquinaDTO = null;
	
	public HeadBusiness(DatabaseBusiness db, MaquinaDTO maquinaDTO){
		this.db = db;
		this.maquinaHead = maquinaDTO;
	}
	
	public void fazerJavaDeploy(JavaDeployDTO ultimoDeploy) throws Exception {
		this.listMaquinaDTO = db.selectListMaquinaDTOParaJavaDeploy(ultimoDeploy.getCodigo());
		List<MaquinaDTO> atualiza = new ArrayList<MaquinaDTO>();
		for (MaquinaDTO maqDTO : listMaquinaDTO) {
			maqDTO.setInfoMaquina(db.selectListMaquinaInfoDTO( maqDTO.getCodigo() ));
			if(scpTransferirJavaDeploy(maqDTO, ultimoDeploy)) {
				atualiza.add( maqDTO );
			}
		}	
		db.incluirJavaDeployMaquinaDTO(ultimoDeploy.getCodigo(), atualiza);
	}
	
	
	public void acessarTodasMaquinas(JavaDeployDTO ultimoDeploy) throws Exception {
		this.listMaquinaDTO = db.selectListMaquinaDTOElegivel(ultimoDeploy.getCodigo());
		List<MaquinaStatusDTO> atualiza = new ArrayList<MaquinaStatusDTO>(); 
		for (MaquinaDTO maqDTO : listMaquinaDTO) {
			maqDTO.setInfoMaquina(db.selectListMaquinaInfoDTO( maqDTO.getCodigo() ));
			atualiza.add( sshInicialMaquinaDTO(maqDTO) );
		}
		db.incluirMaquinaStatusDTO(atualiza);	
	}
	
		
	private MaquinaStatusDTO sshInicialMaquinaDTO(MaquinaDTO maqDTO) throws Exception{
		Session session = null;
		Channel channel = null;
		MaquinaStatusDTO statusDTO = new MaquinaStatusDTO();
		statusDTO.setMaquinaCodigo(maqDTO.getCodigo());
		statusDTO.setOnline( Boolean.FALSE );
		try{				
			session = getSessionSSH(
						maqDTO.getInfoMaquina().get(InfoMaquinaEnum.USUARIO).getValor(),
						maqDTO.getInfoMaquina().get(InfoMaquinaEnum.IP).getValor(), 
						maqDTO.getInfoMaquina().get(InfoMaquinaEnum.SENHA).getValor());
			session.connect();
			
			System.out.println(maqDTO.getHostname()+"> SSH OK");
			statusDTO.setOnline( Boolean.TRUE );
			
			String cmd = null;
			//acionamento do SLAVE para as maquinas ON
			if(!maqDTO.getIgnorar()){
				ComandoDTO comandoDTO = db.selectComandoDTO( ComandoEnum.JAVA_JAR.getIndex() );
				
				cmd = comandoDTO.getTemplate();
				//java -jar @JARPATH @ARG &
				String rootPath = maqDTO.getInfoMaquina().get(InfoMaquinaEnum.ROOT_WORK_PATH).getValor();
				if(!rootPath.endsWith("/")) {
					rootPath += "/";
				}
				cmd = cmd.replace("@JARPATH", rootPath+maqDTO.getJavaDeployDTO().getPath());
				cmd = cmd.replace("@ARG", maquinaHead.getInfoMaquina().get(InfoMaquinaEnum.IP).getValor()); //DATABASE IP = HEAD IP
				if(maqDTO.getInfoMaquina().get(InfoMaquinaEnum.JAVA_HOME)!=null) {
					cmd = maqDTO.getInfoMaquina().get(InfoMaquinaEnum.JAVA_HOME).getValor()+ "bin/" + cmd;
				}
			}
			
			//System.out.println(cmd);
			
			String[] commands = new String[]{
					"top -bn1 |grep Cpu",
					"top -bn1 |grep Mem" ,
					cmd
			};
			
			StringBuilder sb = new StringBuilder();	
			int z = 0;
			for (String linhaComando : commands) {
				if(linhaComando!=null){
					channel = session.openChannel("exec");
					((ChannelExec) channel).setCommand(linhaComando);
					channel.setInputStream(null);
					((ChannelExec) channel).setErrStream(System.err);
					InputStream in = channel.getInputStream();
					channel.connect();		
					byte[] tmp=new byte[1024];
					while(true){
						while(in.available()>0){
							int i=in.read(tmp, 0, 1024);
							if(i<0)break;
							sb.append(new String(tmp, 0, i));
						}
						if(channel.isClosed()){
							if(in.available()>0) continue;						
							break;
						}
						if(z==0 && sb.indexOf("\n")!=-1 && sb.indexOf("\n",sb.indexOf("\n")+1)!=-1){
							sb = new StringBuilder(sb.substring(sb.indexOf("\n")+1, sb.indexOf("\n",sb.indexOf("\n")+1)));
							break;
						}else{
							try{Thread.sleep(500);}catch(Exception ee){}	
						}
					}					
					in.close();
					//System.out.println(sb.toString());
				}
				z++;
			}			
			CmdTopDTO cmdDTO = getCommandTopInfos(sb.toString());
			statusDTO.setCpuUsed( cmdDTO.getCpuUsed() );
			statusDTO.setMemUsed( cmdDTO.getMemUsed() );
			
		} catch (Throwable e) {
			if(e.getCause() instanceof NoRouteToHostException){
				statusDTO.setOnline( Boolean.FALSE );
				System.out.println(maqDTO.getHostname()+"> OFFLINE: "+e.getCause());
			}else{
				System.out.println(maqDTO.getHostname()+"> ERRO (Senha?): "+e.getCause());
			}
		}finally{
			if(channel!=null && !channel.isClosed()) channel.disconnect();
			if(session!=null) session.disconnect();
		}
		try{Thread.sleep(1000);}catch(Exception ee){}
		return statusDTO;
		//System.out.println(maqDTO.getSsh()+"> SSH DISCONNECTED: "+channel.getExitStatus());
	}
	
	public static CmdTopDTO getCommandTopInfos(String cabecalho){
		//System.out.println(cabecalho);
		CmdTopDTO dto = new CmdTopDTO();
		
		String kw = "%Cpu(s):";
		int kwid = cabecalho.indexOf(kw);
		if(kwid!=-1){
			String cpuused = cabecalho.substring(kwid+kw.length(), cabecalho.indexOf("us",kwid+kw.length()) );
			dto.setCpuUsed(BigDecimal.valueOf( Double.parseDouble( cpuused.replace(",", ".") ) ) );
		}
		kw = "Mem";
		kwid = cabecalho.indexOf(kw);
		if(kwid!=-1){
			String info = cabecalho.substring(kwid+kw.length(), cabecalho.indexOf("used",kwid+kw.length()) ).trim();
			String memtot = info.substring(info.indexOf(":")+1, info.indexOf("total") );
			String memused = info.substring( info.lastIndexOf(",")+4>info.length()? info.substring(0,info.length()-4).lastIndexOf(",")+1 : info.lastIndexOf(",")+1, info.length() );
			dto.setMemUsed(BigDecimal.valueOf( 100*(Double.parseDouble( memused.replace(",", ".") ) / Double.parseDouble( memtot.replace(",", ".")) ) ) );
		}
		return dto;
	}

	public static Session getSessionSSH(final String usuario,final String ip, final String senha) throws Throwable {
		JSch jsch = new JSch();
		Session session = jsch.getSession(usuario, ip, 22);
		UserInfo ui = new UserInfo() {
			@Override
			public void showMessage(String m) {
				System.out.println(usuario+"@"+ip+"> SSH CONNECTION MSG: "+ m);
			}			
			@Override
			public boolean promptYesNo(String arg0) { return true; }
			@Override
			public boolean promptPassword(String arg0) { return true; }
			@Override
			public boolean promptPassphrase(String arg0) { return true; }
			@Override
			public String getPassword() {	
				return senha;
			}
			@Override
			public String getPassphrase() { return null; }
		};
		session.setUserInfo(ui);
		
		java.util.Properties config = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		
		return session;
	}
	
	
	public boolean scpTransferirJavaDeploy(MaquinaDTO maqDTO, JavaDeployDTO ultimoDeploy) {
		Session session = null;
		Channel channel = null;
		FileInputStream fis = null;
		OutputStream out = null;
	    InputStream in = null;
		try {
			session = getSessionSSH(
					maqDTO.getInfoMaquina().get(InfoMaquinaEnum.USUARIO).getValor(),
					maqDTO.getInfoMaquina().get(InfoMaquinaEnum.IP).getValor(), 
					maqDTO.getInfoMaquina().get(InfoMaquinaEnum.SENHA).getValor());
			session.connect();
			
			String origemPath = maquinaHead.getInfoMaquina().get(InfoMaquinaEnum.ROOT_WORK_PATH).getValor();
			if(!origemPath.endsWith("/")) {
				origemPath += "/";
			}
			String lfile = origemPath+ultimoDeploy.getPath().replace("bin/", "bin/javadeploy/");
			
			String destinoPath = maqDTO.getInfoMaquina().get(InfoMaquinaEnum.ROOT_WORK_PATH).getValor();
			if(!destinoPath.endsWith("/")) {
				destinoPath += "/";
			}
			String rfile = destinoPath+""+ultimoDeploy.getPath();
			
			File _lfile = new File(lfile);
		    if(!_lfile.exists()) {
		    	System.out.println(">> JavaDeploy JAR HEAD NOT FOUND: "+_lfile.getPath());
		    	return false;
		    }
			
			boolean ptimestamp = false;
			rfile=rfile.replace("'", "'\"'\"'");
			rfile="'"+rfile+"'";
			String command="scp " + (ptimestamp ? "-p" :"") +" -t "+rfile;
		    channel = session.openChannel("exec");
		    ((ChannelExec)channel).setCommand(command);
		    
		    // get I/O streams for remote scp
		    out = channel.getOutputStream();
		    in = channel.getInputStream();

		    channel.connect();

		    if(checkAck(in)!=0){
		    	return false;
		    }
		    
		    if(ptimestamp){
		    	command="T "+(_lfile.lastModified()/1000)+" 0";
		    	// The access time should be sent here,
		    	// but it is not accessible with JavaAPI ;-<
		    	command+=(" "+(_lfile.lastModified()/1000)+" 0\n"); 
		    	out.write(command.getBytes()); out.flush();
		    	if(checkAck(in)!=0){
		    		return false;
		    	}
		    }

		    // send "C0644 filesize filename", where filename should not include '/'
		    long filesize=_lfile.length();
		    command="C0644 "+filesize+" ";
		    if(lfile.lastIndexOf('/')>0){
		    	command+=lfile.substring(lfile.lastIndexOf('/')+1);
		    }
		    else{
		    	command+=lfile;
		    }
		    command+="\n";
		    out.write(command.getBytes()); out.flush();
		    if(checkAck(in)!=0){
		    	return false;
		    }

		    // send a content of lfile
		    fis = new FileInputStream(lfile);
		    byte[] buf=new byte[1024];
		    while(true){
		    	int len=fis.read(buf, 0, buf.length);
		    	if(len<=0) break;
		    	out.write(buf, 0, len); //out.flush();
		    }
		    fis.close();
		    fis=null;
		    // send '\0'
		    buf[0]=0; out.write(buf, 0, 1); out.flush();
		    if(checkAck(in)!=0){
		    	return false;
		    }

		    System.out.println(maqDTO.getHostname()+"> SCP javadeploy OK");
			return true;
		} catch (Throwable e) {
			if(e.getCause() instanceof NoRouteToHostException){
				System.out.println(maqDTO.getHostname()+"> OFFLINE javadeploy: "+e.getCause());
			}else{
				System.out.println(maqDTO.getHostname()+"> ERRO javadeploy (Senha?): "+e.getCause());
			}
			return false;
		}finally{
			if(out!=null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(channel!=null && !channel.isClosed()) channel.disconnect();
			if(session!=null) session.disconnect();
			if(fis!=null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static int checkAck(InputStream in) throws IOException{
		int b=in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if(b==0) return b;
		if(b==-1) return b;

		if(b==1 || b==2){
			StringBuffer sb=new StringBuffer();
			int c;
			do {
				c=in.read();
				sb.append((char)c);
			}
			while(c!='\n');
			if(b==1){ // error
				System.out.print(sb.toString());
			}
			if(b==2){ // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}
}
