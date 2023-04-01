package lapfarsc.qe.dashboard.util;

public class Dominios {
	
	public enum ArgTypeEnum{
		HEAD("HEAD"),
		SLAVE1("SLAVE1");
		String arg;
		ArgTypeEnum(String arg){this.arg=arg;}
		public String getArg(){return this.arg;}
		public static ArgTypeEnum getByName(String index){
			for (ArgTypeEnum e : ArgTypeEnum.values()) {
				if( e.arg.equals( index ) ){
					return e;
				}
			}
			return null;
		}
	}
	
	public enum ComandoEnum{
		JAVA_JAR(1),
		MPIRUN_PW(2),
		PW(3);
		int index;
		ComandoEnum(int index){this.index=index;}
		public int getIndex(){return this.index;}
		public static ComandoEnum getByIndex(int index){
			for (ComandoEnum e : ComandoEnum.values()) {
				if( e.index == index ){
					return e;
				}
			}
			return null;
		}
	}
	
}
