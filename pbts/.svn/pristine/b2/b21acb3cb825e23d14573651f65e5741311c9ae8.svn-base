package ktuan;

public class WWW_GraphSARP {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SystemSARP sys= new SystemSARP();
		sys.readConfix_Parameters(ParameterSARP.PATH_CONFIG_PARAMETER);
		System.out.println(sys.toString());
		MapSARP map= new MapSARP(sys);
		map.readMap_Connected(ParameterSARP.PATH_MAP_CONNECTED);
		GraphSARP graph= new GraphSARP(map);
		graph.dijkstraHeap(21300);
		System.out.println(graph._d[21301]);
		System.out.println(graph._timeMin[21301]);
		System.out.println(graph._timeMax[21301]);
	}

}
