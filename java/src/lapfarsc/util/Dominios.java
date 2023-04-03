package lapfarsc.util;

public class Dominios {
	
	/*
	 * Table infomaquina;
	 * */
	public enum InfoMaquinaEnum{
		IP(1),
		USUARIO(2),
		SENHA(3),
		CPU_TOTAL(4),
		CPU_MPI(5),
		ROOT_WORK_PATH(6),
		CPU_OCIOSA(7),
		JAVA_HOME(8);
		int index;
		InfoMaquinaEnum(int index){this.index=index;}
		public int getIndex(){return this.index;}
		public static InfoMaquinaEnum getByIndex(int index){
			for (InfoMaquinaEnum e : InfoMaquinaEnum.values()) {
				if( e.index == index ){
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
