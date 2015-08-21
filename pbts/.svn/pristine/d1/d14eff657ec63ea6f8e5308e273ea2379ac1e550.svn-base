package pbts.gismap;
import java.util.*;
import java.io.*;

public class ShortestPath {

	/**
	 * @param args
	 */
	private Map m;

	public ShortestPath(Map m) {
		this.m = m;
	}

	private Point findMin(HashSet<Point> S, HashMap<Point, Double> d) {
		double minD = 1000000000;
		Point min_p = null;
		Iterator it = S.iterator();
		while (it.hasNext()) {
			Point p = (Point) it.next();
			double di = d.get(p);
			if (di < minD) {
				minD = di;
				min_p = p;
			}
		}
		return min_p;
	}

	public double dijkstra(double lat1, double lng1, double lat2, double lng2, String fn) {
		Point s = m.findClosestPoint(lat1, lng1);
		Point t = m.findClosestPoint(lat2, lng2);
		double length = 0;
		System.out.println("s = " + s.getdLat() + "," + s.getdLong());
		System.out.println("t = " + t.getdLat() + "," + t.getdLong());
		
		ArrayList<Point> N = m.getPoints();// list of points
		int n = N.size();
		HashMap<Point, ArrayList<Point>> A = m.getAdjacent();// A[p] is the list
																// of adjacent
																// points of p

		// Set S
		// array d: d[p] distance from s to p
		// array prev: prev[p] is the previous point of p on the path from s to
		// p
		// forall point p of points
		// d[p] = distance(s,p);
		// prev[p] = s;*
		// S.add(p);
		// endfor
		HashMap<Point, Double> d = new HashMap<Point, Double>();// d.get(p) la
																// khoang cach
																// tu diem p den
																// s
		HashMap<Point, Point> prev = new HashMap<Point, Point>();// prev.get(i)
																	// is dinh
																	// truoc
																	// dinh p
																	// tren
																	// duong di
		HashSet<Point> S = new HashSet<Point>();
		for (int i = 0; i < n; i++){
			Point v = N.get(i);
			if(v != s)
				S.add(v);
		}

		Approximation apx = new Approximation();

		// Khoi tao
		for(int i = 0; i < N.size(); i++){
			Point pi = N.get(i);
			double dis = 100000000;
			d.put(pi,dis);
		}
		Iterator it = A.get(s).iterator();
		while(it.hasNext()){			
			Point pi = (Point)it.next();
			double di = apx.computeDistanceHav(s.getdLong(),s.getdLat(), 
					pi.getdLong(), pi.getdLat());
			d.put(pi, di);
			prev.put(pi, s);
		}
		
		for(int i = 0; i < N.size(); i++){
			Point v = N.get(i);
			//System.out.println("d[" + v.getdLat() + "," + v.getdLong() + "] = " + d.get(v));
		}

		// Lap
		while (S.size() > 0) {
			// chon dinh u trong S co d[u] nho nhat
			Point u = findMin(S, d);
			//System.out.println("S.size() = " + S.size() + ", find u = " + u.getdLat() + "," + u.getdLong());
			if (u == t) {
				length = d.get(u);
				System.out.println("u = t -> break");
				break;
			}

			S.remove(u);

			// cap nhat khoang cach
			it = A.get(u).iterator();
			while (it.hasNext()) {
				Point v = (Point) it.next();
				double c = apx.computeDistanceHav( u.getdLong(), u.getdLat(),
						v.getdLong(),  v.getdLat());
				double nc = d.get(u) + c;
				//System.out.println("d[v] = " + d.get(v) + ", nc = " + nc + ", d[u] = " + d.get(u) + ", c = " + c);
				if (d.get(v) > nc) {
				
					d.put(v, nc);
					prev.put(v, u);
					//System.out.println("Update d[" + v.getdLat() + "," + v.getdLong() + "] = " + nc + ", prev[" + v.getdLat() + "," + v.getdLong() + "] = " + prev.get(v).getdLat() + "," + prev.get(v).getdLong() + "");
				}
			}

		}
		for(int i = 0; i < N.size(); i++){
			Point v = N.get(i);
			Point pv = prev.get(v);
			//System.out.println("prev[" + v.getdLat() + "," + v.getdLong() + "] = " + pv.getdLat() + "," + pv.getdLong());
		}
		//write to file
		try{
			PrintWriter out = new PrintWriter(fn);
			for(int i = 0; i < N.size(); i++){
				Point vi = N.get(i);
				it = A.get(vi).iterator();
				while(it.hasNext()){
					Point ui = (Point)it.next();
					for(int z = 0; z <= 6; z++){
						out.println("[POLYLINE]");
						out.println("Type=0x5");
					
						out.println("Data" + z + "=(" + vi.getdLat() + "," + vi.getdLong() + "),(" + ui.getdLat() + "," + ui.getdLong() + ")");
						out.println("[END]");
					}
				}
			}
			
			// duong di
			Point v = t;
			while(prev.get(v) != s){
				Point pv = prev.get(v);
				System.out.println("write to file (" + pv.getdLat() + "," + pv.getdLong() + "),(" + v.getdLat() + "," + v.getdLong() + ")");
				
				for(int z = 0; z <= 6; z++){
					out.println("[POLYLINE]");
					out.println("Type=0x1");
				
					out.println("Data" + z + "=(" + pv.getdLat() + "," + pv.getdLong() + "),(" + v.getdLat() + "," + v.getdLong() + ")");
					out.println("[END]");
				}
				
				v = pv;
			}
			Point pv = prev.get(v);
			System.out.println("write to file (" + pv.getdLat() + "," + pv.getdLong() + "),(" + v.getdLat() + "," + v.getdLong() + ")");
			for(int z = 0; z <= 6; z++){
				out.println("[POLYLINE]");
				out.println("Type=0x1");
			
				out.println("Data" + z + "=(" + pv.getdLat() + "," + pv.getdLong() + "),(" + v.getdLat() + "," + v.getdLong() + ")");
				out.println("[END]");
			}
			
			
			out.close();

		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		// LOOP
		// while(!S.isEmpty()){
		// u is the point of S such that d[u] is minimal
		// if(u == t) break;
		// forall v in S do
		// if d[v] > d[u] + distance(u,v) then
		// d[v] = d[u] + distance(u,v);
		// prev[v] = u;
		//
		// endwhile

		return length;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//String mapFileName = "D:\\streets_approximate\\newhanoi1.mp";
		String mapFileName = "/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/map-hanoi.mp";
		Map newMap = new Map();
		newMap.readData(mapFileName);
		
		newMap.establishGraph();
			
		ShortestPath spth = new ShortestPath(newMap);

		/*
		Scanner input = new Scanner(System.in);
		double lat1 = 0, lng1 = 0;
		double lat2 = 0, lng2 = 0;
		//diem dau
		System.out.println("Input Lat1  : ");
		lat1 = input.nextDouble();
		System.out.println("Input Long1 : ");
		lng1 = input.nextDouble();
		// >> diem cuoi
		System.out.println("Input Lat2  : ");
		lat2 = input.nextDouble();
		System.out.println("Input Long2 : ");
		lng2 = input.nextDouble();
		*/
		
		double lat1 = 21.02706;
		double lng1 = 105.84385;
		double lat2 = 21.02004;
		double lng2 = 105.8555;
		
		// Duong ngan nhat
		double minTh = spth.dijkstra(lat1, lng1, lat2, lng2, "path.mp");
		
	
		System.out.println("The shotest path is : " + minTh);

	}
}
