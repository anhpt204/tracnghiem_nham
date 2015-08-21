package ktuan;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class Herictic {
	private SystemSARP sys;
	private MapSARP map;
	private GraphSARP graph;
	private VirtureGraph grV;
	private RequestSARP rqs;
	private Data data;
	private ArrayList<SequenTaxi> _result;

	public Herictic(){
		sys= new SystemSARP();
		sys.readConfix_Parameters(ParameterSARP.PATH_CONFIG_PARAMETER);
		map = new MapSARP(sys);
		map.readMap_Connected(ParameterSARP.PATH_MAP_CONNECTED);
		graph= new GraphSARP(map);
		//................
		rqs= new  RequestSARP();
		rqs.readListRequestFromFile(ParameterSARP.PATH_REQUEST);
		grV= new VirtureGraph();
		grV.createRequestVirtureGraph(rqs._listPeopleRq, rqs._listParcelRq,graph,ParameterSARP.PATH_PARKINGS_AND_DEPOTS);
		
		data= new Data();
		data.updateSystemSARP(sys);
		data.updateGraphAndRequest(grV);
		data.initSequenTaxisAndParking();
		data.initCreateRequests();
		data.solve();
		
		_result= data._listTaxis;
		for (int i = 0; i < _result.size(); i++) {
			_result.get(i).createTextResult(grV);
			_result.get(i).tranFromRealId(grV, data);
		}
		System.out.println(sys._maxSpeedms);
		System.out.println(sys._minSpeedms);
		//data.printfListPostTaxis(false);
		writeResult(ParameterSARP.SIMULATOR_OUT_DATA);
		System.out.println(String.format("Sum Profit:= %10.3f", data._sumProfit));
		
	}

	public Herictic(SetSARP_File setFile) {
		sys= new SystemSARP();
		sys.readConfix_Parameters(setFile._fileParameters);
		map = new MapSARP(sys);
		map.readMap_Connected(setFile._fileGraph);
		graph= new GraphSARP(map);
		//................
		rqs= new  RequestSARP();
		rqs.readListRequestFromFile(setFile._fileRequests);
		grV= new VirtureGraph();
		grV.createRequestVirtureGraph(rqs._listPeopleRq, rqs._listParcelRq,graph,setFile._fileDepotsAndParkings);
		
		data= new Data();
		data.updateSystemSARP(sys);
		data.updateGraphAndRequest(grV);
		data.initSequenTaxisAndParking();
		data.initCreateRequests();
		data.solve();
		
		_result= data._listTaxis;
		for (int i = 0; i < _result.size(); i++) {
			_result.get(i).createTextResult(grV);
			_result.get(i).tranFromRealId(grV, data);
		}
		System.out.println(sys._maxSpeedms);
		System.out.println(sys._minSpeedms);
		//data.printfListPostTaxis(false);
		writeResult(setFile._fileGenOut);
		System.out.println(String.format("Sum Profit:= %10.3f", data._sumProfit));
	}

	private void writeResult(String _fileGenOut) {
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(_fileGenOut), "utf-8"));
			// ("result_requests-people-3-parcel-3.ins5.txt"), "utf-8"));
			for (int i = 0; i < _result.size(); i++) {
				String res = _result.get(i).printfListPostReal(graph, sys,false);
				writer.write(res.toCharArray());
				/*
				writer.write(String
						.format("[Profit of taxit] : %10.3f\n===================================\n",
								_result.get(i)._profit));
				*/
			}
			String s = new String("-2\n");
			writer.write(s.toCharArray());
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}
}
