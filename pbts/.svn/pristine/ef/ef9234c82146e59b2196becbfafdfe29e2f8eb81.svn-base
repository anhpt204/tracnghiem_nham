package ktuan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MapSARP {

	public SystemSARP _sys;
	public ArrayList<PointMap> _listPointMap;
	public ArrayList<EdgeMap> _listEdgeMap;

	public MapSARP(SystemSARP sys) {
		this._sys= sys;
	}

	public void readMap_Connected(String path) {
		System.out.println("Start: read_Map_Connected");
		File dataMap= new  File(path);
		try {
			BufferedReader br= new BufferedReader(new  FileReader(dataMap.getAbsoluteFile()));
			String line =br.readLine().trim();
			_listPointMap= new  ArrayList<PointMap>();
			while (!line.startsWith("-1")){
				line= TextManagerSARP.parserSpace(line);
				String[] s= line.split("\t");
				int id= Integer.parseInt(s[0]);
				double tungDo= Double.parseDouble(s[1]);
				double hoanhDo= Double.parseDouble(s[2]);
				_listPointMap.add(new PointMap(id,tungDo,hoanhDo));
				line= br.readLine().trim();
			}
			for(int i=0;i<_listPointMap.size();i++) System.out.println(_listPointMap.get(i).toString());
			
			_listEdgeMap = new ArrayList<EdgeMap>();
			line= br.readLine().trim();
			while (!line.startsWith("-1")){
				line= TextManagerSARP.parserSpace(line);
				String[] s= line.split("\t");
				int id1= Integer.parseInt(s[0]);
				int id2= Integer.parseInt(s[1]);
				double distance= Double.parseDouble(s[2]);
				_listEdgeMap.add(new EdgeMap(id1, id2, distance));
				line= br.readLine().trim();
			}
			if (_sys!=null) {
				for(int i=0;i<_listEdgeMap.size();i++){
					_listEdgeMap.get(i).updateMinMaxTravelTime(_sys);
				}
			}
			for(int i=0;i<_listEdgeMap.size();i++) System.out.println(_listEdgeMap.get(i).toString());
			
			System.out.println("Size List Point Map: "+_listPointMap.size());
			System.out.println("Size List Edge  Map: "+_listEdgeMap.size());
			br.close();
			System.out.println("Finish: read_Map_Connected");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
