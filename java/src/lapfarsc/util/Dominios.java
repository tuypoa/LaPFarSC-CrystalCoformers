package lapfarsc.util;

public class Dominios {
	
	/*
	 * Table tipoarquivo
	 * */
	public enum TipoArquivoEnum{
		ENVIADO(1),
		OTIMIZADO_MMFF94(2);
		int index;
		TipoArquivoEnum(int index){this.index=index;}
		public int getIndex(){return this.index;}
		public static TipoArquivoEnum getByIndex(int index){
			for (TipoArquivoEnum e : TipoArquivoEnum.values()) {
				if( e.index == index ){
					return e;
				}
			}
			return null;
		}
	}
	
	/*
	 * Table infomaquina
	 * */
	public enum InfoMaquinaEnum{
		IP(1),
		USUARIO(2),
		SENHA(3),
		CPU_TOTAL(4),
		CPU_MPI(5),
		ROOT_WORK_PATH(6),
		CPU_OCIOSA(7),
		JAVA_HOME(8),
		MOPAC_HOME(9);
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
	
	
	/*
	 * Table comando
	 * */
	public enum ComandoEnum{
		JAVA_JAR_SLAVE(1),
		MPIRUN_PWSCF(2),
		PWSCF(3),
		MOLCONVERT_MMFF94_FINE(4),
		OBABEL_CONVERTFILE(5),
		MOPAC(6);
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
