package ktuan;

public class WWW_RequestSARP {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RequestSARP rqs= new RequestSARP();
		rqs.readListRequestFromFile(ParameterSARP.PATH_REQUEST);
	}

}
