package pbts.datamanipulation;

import java.io.*;
import java.util.*;
import pbts.entities.*;
import pbts.gismap.googlemaps.GoogleMapsQuery;
import pbts.gismap.googlemaps.*;
public class DataGenerator {

	/**
	 * @param args
	 */
	int n,m,k,p;
	int N, xichma;
	
	Location[] points;
	double[][] d;// d[i][j]: distance from location i to location j
	int[][] t; //t[i][j]: travel time from location i to location j
	ArrayList<Integer>[] paths;
	HashMap<Integer, Integer> wait_t;
	HashMap<Integer, Integer> arrive_t;
	HashMap<Integer, Integer> depart_t;
	HashSet<Integer> parkings;
	HashSet<Integer> stops;
	HashSet<Integer> depots;
	HashSet<Integer> originPassenger;
	HashSet<Integer> destinationPassenger;
	HashSet<Integer> originParcel;
	HashSet<Integer> destinationParcel;
	
	public DataGenerator(){
		
	}
	public void readData(String filename){
		try{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line = in.readLine();
			line = in.readLine();
			String[] s = line.split(" ");
			n = Integer.valueOf(s[0]);
			m = Integer.valueOf(s[1]);
			k = Integer.valueOf(s[2]);
			p = Integer.valueOf(s[3]);
			System.out.println(n + " " + m + " " + k + "  " + p);
			N = 2*(n+m)+k+p;
			xichma = n+m;
			points = new Location[N+1];
			stops = new HashSet<Integer>();
			depots = new HashSet<Integer>();
			originPassenger = new HashSet<Integer>();
			destinationPassenger = new HashSet<Integer>();
			originParcel = new HashSet<Integer>();
			destinationParcel = new HashSet<Integer>();
			
			int ind = 0;
			line = in.readLine();
			for(int i = 0; i < n; i++){
				line = in.readLine();
				s = line.split(" ");
				int id = Integer.valueOf(s[0]);
				double lat = Double.valueOf(s[1]);
				double lng = Double.valueOf(s[2]);
				points[id] = new Location(lat,lng);
				stops.add(id);
				originPassenger.add(id);
			}
			line = in.readLine();
			for(int i = 0; i < m; i++){
				line = in.readLine();
				s = line.split(" ");
				int id = Integer.valueOf(s[0]);
				double lat = Double.valueOf(s[1]);
				double lng = Double.valueOf(s[2]);
				points[id] = new Location(lat,lng);
				stops.add(id);
				originParcel.add(id);
			}
			line = in.readLine();
			for(int i = 0; i < n; i++){
				line = in.readLine();
				s = line.split(" ");
				int id = Integer.valueOf(s[0]);
				double lat = Double.valueOf(s[1]);
				double lng = Double.valueOf(s[2]);
				points[id] = new Location(lat,lng);
				stops.add(id);
				destinationPassenger.add(id);
			}
			line = in.readLine();
			for(int i = 0; i < m; i++){
				line = in.readLine();
				s = line.split(" ");
				int id = Integer.valueOf(s[0]);
				double lat = Double.valueOf(s[1]);
				double lng = Double.valueOf(s[2]);
				points[id] = new Location(lat,lng);
				stops.add(id);
				destinationParcel.add(id);
			}
			line = in.readLine();
			parkings = new HashSet<Integer>();
			for(int i = 0; i < p; i++){
				line = in.readLine();
				s = line.split(" ");
				int id = Integer.valueOf(s[0]);
				double lat = Double.valueOf(s[1]);
				double lng = Double.valueOf(s[2]);
				points[id] = new Location(lat,lng);
				parkings.add(id);
			}
			line = in.readLine();
			for(int i = 0; i < k; i++){
				line = in.readLine();
				s = line.split(" ");
				int id = Integer.valueOf(s[0]);
				double lat = Double.valueOf(s[1]);
				double lng = Double.valueOf(s[2]);
				points[id] = new Location(lat,lng);
				depots.add(id);
			}
			
			// read distance information
			line = in.readLine();
			d = new double[N+1][N+1];
			t = new int[N+1][N+1];
			for(int i = 1; i <= N; i++){
				for(int j = 1; j <= N; j++){
					line = in.readLine();
					//System.out.println("line = " + line);
					s = line.split("\t");
					int src = Integer.valueOf(s[0].trim());
					int des = Integer.valueOf(s[1].trim());
					double distance = Double.valueOf(s[2].trim());
					int traveltime = Integer.valueOf(s[3].trim());
					traveltime = traveltime/100;
					if(traveltime == 0) traveltime = 1;
					d[src][des] = distance;
					t[src][des] = traveltime;
				}
			}
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();		
		}
	}
	private int composeParkingTaxi2Key(int p_id, int taxi){
		return p_id*10000 + taxi;
	}
	public void readPaths(String filename){
		try{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line = in.readLine();
			line = in.readLine();
			int k = Integer.valueOf(line);
			paths = new ArrayList[k+1];
			for(int i = 1; i <= k; i++){
				line = in.readLine();
				line = in.readLine();
				String[] s = line.split(" ");
				paths[i] = new ArrayList<Integer>();
				for(int j = 0; j < s.length; j++){
					paths[i].add(Integer.valueOf(s[j].trim()));
				}
			}
			for(int i = 1; i <= k; i++){
				for(int j = 0; j < paths[i].size(); j++)
					System.out.print(paths[i].get(j) + " ");
				System.out.println();
			}
			
			line = in.readLine();
			line = in.readLine();
			int nbStops = Integer.valueOf(line.trim());
			line = in.readLine();
			wait_t = new HashMap<Integer, Integer>();
			for(int i = 1; i <= nbStops; i++){
				line = in.readLine();
				String[] s = line.split(" ");
				int s_id = Integer.valueOf(s[0].trim());
				int wt = Integer.valueOf(s[1].trim());
				wt = 1;
				wait_t.put(s_id, wt);
				//System.out.println("wait_t.put(" + s_id + "," + wt);
			}
			line = in.readLine();
			line = in.readLine();
			//System.out.println(line);
			int nbTaxis = Integer.valueOf(line.trim());
			line = in.readLine();
			for(int i = 1; i <= nbTaxis; i++){
				line = in.readLine();
				String[] s = line.split(" ");
				int p_id = Integer.valueOf(s[0].trim());
				int taxi = Integer.valueOf(s[1].trim());
				int wt = Integer.valueOf(s[2].trim());
				wt = 10;
				int p_taxi = composeParkingTaxi2Key(p_id, taxi);
				wait_t.put(p_taxi, wt);	
				//System.out.println("wait_t.put(" + p_taxi + "," + wt);
			}
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void getDistanceTime(){
		GoogleMapsQuery G = new GoogleMapsQuery();
		d = new double[N+1][N+1];
		t = new int[N+1][N+1];
		int count = 0;
		int total = N*N;
		for(int i = 1; i <= N; i++){
			Location pi = points[i];
			for(int j = 1; j <= N; j++){
				Location pj = points[j];
				Direction dir = G.getDirection(pi.lat, pi.lng, pj.lat, pj.lng, "driving");
				try{
					Thread.sleep(500);
				}catch(Exception ex){
					ex.printStackTrace();
				}
				d[i][j] = dir.getDistances();
				t[i][j] = dir.getDurations();
				count++;
				System.out.println("Finished " + count + "/" + total + ", d[" + i + "," + j + "] = " + d[i][j] + ", t[" + i + "," + j + "] = " + t[i][j]);
			}
		}
		
		try{
			PrintWriter out = new PrintWriter("distance.txt");
			for(int i = 1; i <= N; i++){
				out.println(i + " " + points[i].lat + "," + points[i].lng);
			}
			for(int i = 1; i <= N; i++){
				for(int j = 1; j <= N; j++){
					out.println(i + "\t" + j + "\t" + d[i][j] + "\t" + t[i][j] + "\t" + points[i].lat + "," + points[i].lng + "\t" + points[j].lat + "," + points[j].lng);
					System.out.println(i + "\t" + j + "\t" + d[i][j] + "\t" + t[i][j]);
				}
			}
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void genTimePoint(String filename){
		int[] start = new int[k+1];
		start[1] = 1;
		start[2] = 5;
		arrive_t = new HashMap<Integer, Integer>();
		depart_t = new HashMap<Integer, Integer>();
		for(int i = 1; i <= k; i++){
			int u = paths[i].get(0);
			depart_t.put(u,start[i]);
			for(int j = 1; j < paths[i].size(); j++){
				int v = paths[i].get(j);
				int kv = v;
				int ku = u;
				if(parkings.contains(v)) kv = composeParkingTaxi2Key(v, i);
				if(parkings.contains(u)) ku = composeParkingTaxi2Key(u, i);
				arrive_t.put(kv,depart_t.get(ku) + t[u][v]);
				//System.out.println("wait_t.get(" + kv + ") = ");
				
				
				
				if(j < paths[i].size()-1){// v is not the depot
					int wt = wait_t.get(kv);
					depart_t.put(kv, arrive_t.get(kv) + wt);
				}
				u = v;
			}
		}
		
		try{
			PrintWriter out = new PrintWriter(filename);
			for(int i = 1; i <= k; i++){
				out.println("Taxi " + i);
				for(int j = 0; j < paths[i].size(); j++){
					int v = paths[i].get(j);
					int kv = v;
					if(parkings.contains(v)) kv = composeParkingTaxi2Key(v, i);
					if(j == 0){
						out.println(paths[i].get(j) + "\t" + "-" + "\t" + depart_t.get(kv));
					}else if(j == paths[i].size() - 1){
						out.println(paths[i].get(j) + "\t" + arrive_t.get(kv) + "\t" + "-");
					}else{
						out.println(paths[i].get(j) + "\t" + arrive_t.get(kv) + "\t" + depart_t.get(kv));
					}
					/*
					if(arrive_t.get(kv) != null){
						out.println("arrive" + "\t" + v + "\t" + arrive_t.get(kv));
					}
					if(depart_t.get(kv) != null){
						out.println("depart" + "\t" + v + "\t" + depart_t.get(kv));
					}
					*/
				}
			}
			out.close();
			
			out = new PrintWriter("input.txt");
			out.println("#nbPassengers  #nbParcels  #nbTaxis  #nbParkings");
			out.println(n + "\t" + m + "\t" + k + "\t" + p);
			out.println("-1");
			out.println("#id	#lat	#lng (source passengers)");
			for(int i = 1; i <= n; i++)
				out.println(i + "\t" + points[i].lat + "\t" + points[i].lng);
			out.println("-1");
			out.println("#id	#lat	#lng (source parcels)");
			for(int i = n+1; i <= n+m; i++)
				out.println(i + "\t" + points[i].lat + "\t" + points[i].lng);
			out.println("-1");
			out.println("#id	#lat	#lng (destination passengers)");
			for(int i = n+m+1; i <= 2*n+m; i++)
				out.println(i + "\t" + points[i].lat + "\t" + points[i].lng);
			out.println("-1");
			
			out.println("#id	#lat	#lng (destination parcels");
			for(int i = 2*n+m+1; i <= 2*n+2*m; i++)
				out.println(i + "\t" + points[i].lat + "\t" + points[i].lng);
			out.println("-1");
			out.println("#id	#lat	#lng (parkings)");
			for(int i = 2*n+2*m+1; i <= 2*n+2*m+p; i++)
				out.println(i + "\t" + points[i].lat + "\t" + points[i].lng);
			out.println("-1");
			out.println("#id	#lat	#lng (depots of taxis)");
			for(int i = 2*n+2*m+p+1; i <= 2*n+2*m+p+k; i++)
				out.println(i + "\t" + points[i].lat + "\t" + points[i].lng);
			out.println("-1");
			
			out.println("#id  #early  #late  #max_wait_time  #request");
			for(int i = 1; i <= k; i++){
				for(int j = 0; j < paths[i].size(); j++){
					int v = paths[i].get(j);
					if(stops.contains(v)){
						int ev = arrive_t.get(v) - 1;
						int lv = arrive_t.get(v) + 1;
						int max_wait_t = 2;
						int request = 0;
						if(originParcel.contains(v)) request = 1; else if(destinationParcel.contains(v)) request = -1;
						
						out.println(v + "\t" + ev + "\t" + lv + "\t" + max_wait_t + "\t" + request);
					}
				}
			}
			out.println("-1");
			out.println("#passenger_src_id  #Di  #Ti   #MaxDi  #MaxTi	#eta");
			for(int i = 1; i <= n; i++){
				double Di = d[i][i+xichma];
				int Ti = t[i][i+xichma];
				int MTi = 2*Ti;
				double MDi = 2*Di;
				int eta = 2;
				out.println(i + "\t" + Di + "\t" + Ti + "\t" + MDi + "\t" + MTi + "\t" + eta);
			}
			out.println("-1");
			int T = 0;
			for(int i = 1; i <= k; i++){
				int dep = paths[i].get(0);
				if(T < arrive_t.get(dep)) T = arrive_t.get(dep) + 900;
			}
			int Qk = 1;
			int eta = 2;
			int alpha = 10000;
			int beta = 10000;
			int gamma1 = 9000;
			int gamma2 = 5000;
			int gamma3 = 20; // fuel cost: 20 VND per meter = 20000 VND per km
			int gamma4 = 40000;
			out.println("T #Qk #eta  #alpha  #beta  #gamma1  #gamma2  #gama3  #gama4");
			out.println(T + "\t" + Qk + "\t" + eta + "\t" + alpha + "\t" + beta + "\t" + gamma1 + "\t" + gamma2 + "\t" + gamma3 + "\t" + gamma4);
			out.println("#parking_id  #capacity");
			out.println("23\t" + 1);
			for(int i = 24; i <= 26; i++){
				out.println(i + "\t" + 0);
			}
			out.println("-1");
			out.println("#src  #des  #distance (m)  #min_travel_time  #max_travel_time");
			for(int i = 1; i <= N; i++){
				for(int j = 1; j <= N; j++){
					int mint = t[i][j];
					int maxt = 3*mint;
					out.println(i + "\t" + j + "\t" + d[i][j] + "\t" + mint + "\t" + maxt);
				}
			}
			out.println("-1");
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataGenerator gen = new DataGenerator();
		String filename = "static-data.txt";
		gen.readData(filename);
		//gen.getDistanceTime();
		gen.readPaths("solution.txt");
		gen.genTimePoint("arrive-depart-time-points.txt");
	}

}
