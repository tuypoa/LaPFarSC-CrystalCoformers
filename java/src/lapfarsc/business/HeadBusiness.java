/**
 * @author tuypoa
 *
 *
 */
package lapfarsc.business;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.NoRouteToHostException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import lapfarsc.qe.dashboard.dto.CmdTopDTO;
import lapfarsc.qe.dashboard.dto.ComandoDTO;
import lapfarsc.qe.dashboard.dto.MaquinaDTO;
import lapfarsc.qe.dashboard.util.Dominios.ArgTypeEnum;
import lapfarsc.qe.dashboard.util.Dominios.ComandoEnum;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;


public class HeadBusiness {
	
	private DatabaseBusiness db = null;
	private List<MaquinaDTO> listMaquinaDTO = null;
	
	public HeadBusiness(Connection conn){
		this.db = new DatabaseBusiness(conn);
	}
	
	public void acessarTodasMaquinas() throws Exception {
		this.listMaquinaDTO = db.selectListMaquinaDTO();
		List<MaquinaDTO> atualiza = new ArrayList<MaquinaDTO>(); 
		for (MaquinaDTO maqDTO : listMaquinaDTO) {			
			atualiza.add( sshInicialMaquinaDTO(maqDTO) );
		}		
		db.updateOfflineTodasMaquinasDTO();		
		db.updateOnlineMaquinasDTO(atualiza);		
	}
	
		
	private MaquinaDTO sshInicialMaquinaDTO(MaquinaDTO maqDTO) throws Exception{
		Session session = null;
		Channel channel = null;
		try{				
			session = getSessionSSH(maqDTO.getSsh(), maqDTO.getSenha());
			session.connect();
			
			System.out.println(maqDTO.getSsh()+"> SSH OK");
			maqDTO.setOnline( Boolean.TRUE );
			
			String cmd = null;
			//acionamento do SLAVE1 para as maquinas ON
			if(maqDTO.getOnline() && !maqDTO.getIgnorar()){
				ComandoDTO comandoDTO = db.selectComandoDTO( ComandoEnum.JAVA_JAR.getIndex() );
				if(comandoDTO==null){
					return maqDTO;
				}
				cmd = comandoDTO.getTemplate();
				//java -jar @JARPATH @ARG &
				cmd = cmd.replace("@JARPATH", maqDTO.getJarPath());
				cmd = cmd.replace("@ARG", ArgTypeEnum.SLAVE1.getArg()+" "+maqDTO.getCodigo());
			}
			
			String[] commands = new String[]{
					db.verificarMaquinaCPUOciosa(maqDTO.getCodigo())? "top -b1 |grep Cpu" : "top -bn1 |grep Cpu",
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
			maqDTO.setCpuUsed( cmdDTO.getCpuUsed() );
			maqDTO.setMemUsed( cmdDTO.getMemUsed() );
			
		} catch (Throwable e) {
			if(e.getCause() instanceof NoRouteToHostException){
				maqDTO.setOnline( Boolean.FALSE );
				System.out.println(maqDTO.getSsh()+"> OFFLINE: "+e.getCause());
			}else{
				System.out.println(maqDTO.getSsh()+"> ERRO: "+e.getCause());
			}
		}finally{
			if(channel!=null && !channel.isClosed()) channel.disconnect();
			if(session!=null) session.disconnect();
		}
		try{Thread.sleep(1000);}catch(Exception ee){}
		return maqDTO;
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

	public static Session getSessionSSH(final String ssh, final String senha) throws Throwable {
		JSch jsch = new JSch();
		String sshsp[] = ssh.split("@");
		Session session = jsch.getSession(sshsp[0], sshsp[1], 22);
		UserInfo ui = new UserInfo() {
			@Override
			public void showMessage(String m) {
				System.out.println(ssh+"> SSH CONNECTION MSG: "+ m);
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
	
		
}
