package ktuan;

public class ParameterSARP {

	public static final String PATH_CONFIG_PARAMETER = "tokyo_config-parameters.txt";
			//"config-parameters.txt";//"tokyo_config-parameters.txt";
	public static final String PATH_MAP_CONNECTED ="tokyo_map2.txt"; 
			//"map-hanoi-connected.txt";//"tokyo_map2.txt";
	public static final String PATH_REQUEST = "tokyo_requests2.txt";
			//"requests-long-people-10-parcel-10.ins1.txt";
			//"tokyo_requests2.txt";
	public static final String SIMULATOR_OUT_DATA = "out.txt";
	public static final int DIV_MINUTE = 5;
	public static final int MAX_TIME=108000/DIV_MINUTE;
	public static final int PICKUP_PEOPLE = 1;
	public static final int DELIVERY_PEOPLE=3;
	public static final int PICKUP_PARCEL=2;
	public static final int DELIVERY_PARCEL=4;
	public static final int STOP=5;
	public static final int TYPE_REQUEST_PEOPLE = 5;
	public static final int TYPE_REQUEST_PARCEL=1;
	public static final int OO = 100000;
	public static final String PATH_PARKINGS_AND_DEPOTS = "tokyo_depot2.txt";
			//"depots300-parkings20.txt";//"tokyo_depot2.txt";
	public static final double OO_Double = 1000000000.0;
	public static final double EPSILON = 0.000001;
	/*Dat khung gia tri la 2*DIV_MINUTE de tinh toan thoi gian can thiet de len xe
	 */
	public static final int TIME_WAIT_PER_REQUEST = 2;
	public static final String RE_WRITE_REQUEST = "reRequest.txt";
	

}