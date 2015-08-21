package pbts.datamanipulation;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import pbts.simulation.RoadMap;

import java.io.*;
import java.util.*;

class Node{
	public long id;
	public double lat, lng;
	public Node(long id, double lat, double lng){
		this.id = id;
		this.lat= lat;
		this.lng = lng;
	}
}
class Arc{
	public long begin, end;
	public Arc(long begin, long end){
		this.begin = begin; this.end = end;
	}
}
public class OSMapProcessor {

	ArrayList<Node> nodes;
	ArrayList<Arc> arcs;
	public OSMapProcessor(){
		
	}
	public void process(String fn, String fo){
		try{
			
			HashMap<Long, Integer> map = new HashMap<Long, Integer>();
			nodes = new ArrayList<Node>();
			arcs = new ArrayList<Arc>();
			BufferedReader in = new BufferedReader(new FileReader(fn));
			int ii = 0;
			ArrayList<Node> t_nodes = new ArrayList<Node>();
			while(true){
				ii++;
				String line = in.readLine();
				if(line == null) break;
				if(line.contains("<node")){
					//System.out.println(ii + ": " + line);
					long id;
					double lat, lng;
					String tag = "\"";
					// extract id
					int idx = line.indexOf(tag);
					//System.out.println("idx = " + idx);
					line = line.substring(idx+1,line.length());
					//System.out.println("line = " + line);
					idx = line.indexOf(tag);
					
					String sid = line.substring(0,idx);
					line = line.substring(idx+1,line.length());
					//System.out.println("sid = " + sid + ", line = " + line);
					// extract lat
					idx = line.indexOf(tag);
					line = line.substring(idx+1,line.length());
					idx = line.indexOf(tag);
					String slat = line.substring(0,idx);
					line = line.substring(idx+1,line.length());
					
					// extract lng
					idx = line.indexOf(tag);
					line = line.substring(idx+1,line.length());
					idx = line.indexOf(tag);
					String slng = line.substring(0,idx);
					line = line.substring(idx+1,line.length());
					
					System.out.println(sid + "\t"+ slat + "\t"+ slng);
					id = Long.valueOf(sid);
					lat = Double.valueOf(slat);
					lng = Double.valueOf(slng);
					
					t_nodes.add(new Node(id,lat,lng));
					//break; 
				}else if(line.contains("<way")){
					ArrayList<Long> L = new ArrayList<Long>();
					boolean oneway = false;
					boolean way = false;
					while(true){
						line = in.readLine();
						if(line.contains("</way")) break;
						if(line.contains("highway")) way = true;
						if(line.contains("<nd")){
							String tag = "\"";
							int idx = line.indexOf(tag);
							line = line.substring(idx+1,line.length());
							idx = line.indexOf(tag);
							String sid = line.substring(0,idx);
							long id = Long.valueOf(sid);
							L.add(id);
							//System.out.println("way, id = " + id);
							//System.exit(-1);
						}else if(line.contains("oneway") && line.contains("yes")){
							oneway = true;
						}
					}
					
					if(!way) continue;
					/*
					System.out.println("Way: ");
					for(int j = 0; j < L.size(); j++)
						System.out.println(L.get(j));
						*/
					for(int j = 0; j < L.size()-1; j++){
						long u = L.get(j);
						long v = L.get(j+1);
						arcs.add(new Arc(u,v));
					}
					
					if(!oneway){
						for(int j = 0; j < L.size()-1; j++){
							long u = L.get(j);
							long v = L.get(j+1);
							arcs.add(new Arc(v,u));
						}
					}else{
						System.out.println("ONEWAY");
					}
				}
			}
			in.close();
			
			//for(int i = 0; i < nodes.size(); i++){
				//System.out.println("node " + i + " = (" + nodes.get(i).id + "," + nodes.get(i).lat + ","+ nodes.get(i).lng + ")");
			//}
			//int idx = 0;
			HashSet<Long> S = new HashSet<Long>();
			for(int i = 0; i < arcs.size(); i++){
				Arc a = arcs.get(i);
				S.add(a.begin);
				S.add(a.end);
			}
			
			for(int i = 0; i < t_nodes.size(); i++){
				Node nod = t_nodes.get(i);
				if(S.contains(nod.id)){
					nodes.add(nod);
					map.put(nod.id, nodes.size());
				}
			}
			PrintWriter out =new PrintWriter(fo);
			for(int i = 0; i < nodes.size(); i++){
				Node nod = nodes.get(i);
				out.println(map.get(nod.id) + " " + nod.lat + " " + nod.lng);
				System.out.println("node("+ i + ")/" + nodes.size() + " = " + nod.id + " " + nod.lat + " " + nod.lng);
			}
			out.println(-1);
			pbts.gismap.googlemaps.GoogleMapsQuery G = new pbts.gismap.googlemaps.GoogleMapsQuery();
			
			for(int i = 0; i < arcs.size(); i++){
				Arc a = arcs.get(i);
				int u = map.get(a.begin);
				int v = map.get(a.end);
				Node n1 = nodes.get(u-1);
				Node n2 = nodes.get(v-1);
				double w = G.computeDistanceHaversine(n1.lat, n1.lng, n2.lat, n2.lng)*1000;// in meters
				out.println(u + " " + v + " " + w);
				System.out.println("arc("+ i + ")/" + arcs.size() + " = "+ map.get(a.begin) + " " + map.get(a.end));
			}
			out.println(-1);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OSMapProcessor P = new OSMapProcessor();
		P.process("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco.osm",
				"C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFranciscoRoad.txt");
		/*
		RoadMap map = new RoadMap();
		map.loadData("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco.txt");
		RoadMap newMap = map.contract(0.0001);
		newMap.writeToFile("C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco-contracted.txt");
		*/
	}

}
