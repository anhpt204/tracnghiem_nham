package ktuan;

public class WWW_SystemSARP {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SystemSARP sys= new SystemSARP();
		sys.readConfix_Parameters(ParameterSARP.PATH_CONFIG_PARAMETER);
		System.out.println(sys.toString());
	}

}
