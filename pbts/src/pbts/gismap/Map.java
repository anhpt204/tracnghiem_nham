package pbts.gismap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
	
public class  Map {

	/**
	 * @param args
	 */
	
	private ArrayList<Point> points;
	private ArrayList<Polyline> lines;
	private HashMap<Point, ArrayList<Point>> A;//A[p] is the set of outgoing point from p
	
	public ArrayList<Point> getPoints(){
		return points;
	}
	public HashMap<Point, ArrayList<Point>> getAdjacent(){
		return A;
	}
	private Point getPoint(double lat, double lng, ArrayList<Point> P){
		for(int i = 0; i < P.size(); i++){
			Point p = P.get(i);
			if(Utility.equals(p.getdLat(), lat) && Utility.equals(p.getdLong(), lng))
				return p;
		}
		return null;
	}
	
	public double computeLength(){
		double L = 0;
		Approximation ap = new Approximation();
		for(int i = 0; i < lines.size(); i++){
			Polyline pl = lines.get(i);
			ArrayList<Point> p = pl.getPoints();
			for(int j = 0; j < p.size()-1; j++){
				Point p1 = p.get(j);
				Point p2 = p.get(j+1);
				L = L + ap.computeDistanceHav(p1.getdLat(), p1.getdLong(), p2.getdLat(), p2.getdLong());
			}
		}
		return L;
	}
	
	public Point findClosestPoint(double lat, double lng){
		//TODO
		// find the point p of the list points which is closest to (lat, lng)
		Point p = null;
		Point minP = null;
		Approximation  apx = new Approximation();
		double minD = 10000000;		
		if(points != null && points.size() > 0){		
			for(int i = 0; i<= points.size()-1; i++){
				p = points.get(i);
				double d = apx.computeDistanceHav(lng, lat, p.getdLong(), p.getdLat());
				if(minD > d){
					minD = d;
					minP = p;
				}					
			}
		}		
		return minP;
	}
	public void establishGraph(){
		points = new ArrayList<Point>();
		for(int i = 0; i < lines.size(); i++){
			Polyline pl = lines.get(i);
			ArrayList<Point> P = pl.getPoints();
			for(int j = 0; j < P.size(); j++){
				Point pj = P.get(j);
				Point p = getPoint(pj.getdLat(), pj.getdLong(), points);
				if(p != null){
					System.out.println("Point exists... do not insert");
				}else{
					pj = new Point(pj.getdLat(),pj.getdLong());
					points.add(pj);
				}
			}
		}
		A = new HashMap<Point, ArrayList<Point>>();
		for(int i = 0; i < points.size(); i++){
			Point p = points.get(i);
			A.put(p,  new ArrayList<Point>());
		}
		int countDuplications = 0;
		for(int i = 0; i < lines.size(); i++){
			Polyline pl = lines.get(i);
			ArrayList<Point> P = pl.getPoints();
			for(int j = 0; j < P.size()-1; j++){
				Point pj = P.get(j);
				Point pj1 = P.get(j+1);
				Point u = getPoint(pj.getdLat(), pj.getdLong(), points);
				Point u1 = getPoint(pj1.getdLat(), pj1.getdLong(), points);
				if(u == u1){
					countDuplications++;
					System.out.println(countDuplications + " U = U1 --> IGNORE");
					continue;
				}
				A.get(u).add(u1);
				if(!pl.directional()){
					A.get(u1).add(u);
				}
			}
		}
		
		for(int i = 0; i < points.size(); i++){
			Point p = points.get(i);
			ArrayList<Point> Ap = A.get(p);
			System.out.print("A[" + p.toString() + "] = ");
			for(int j = 0; j < Ap.size(); j++){
				System.out.print(Ap.get(j).toString() + ",");
			}
			System.out.println();
		}
	}
	
    private Point convert(String s) {
	    String tag = ",";
	    int idx = s.indexOf(tag);
	    String x = s.substring(0, idx);
	    String y = s.substring(idx + tag.length(), s.length());
	    //System.out.println("x = " + x + " y = " + y);
	    Point p = new Point(Double.valueOf(x), Double.valueOf(y));
	    return p;
    }

    private ArrayList<Point> extractPoints(String dataValue) {
        ArrayList<Point> P = new ArrayList<Point>();
        String tag1 = "Data0=";
        int idx = dataValue.indexOf(tag1);
        dataValue = dataValue.substring(idx + tag1.length(), dataValue.length());
        //System.out.println("dataValue = " + dataValue);
        while (true) {
            String tag2 = "(";
            idx = dataValue.indexOf(tag2);
            if (idx == -1) {
                break;
            }
            dataValue = dataValue.substring(idx + tag2.length(), dataValue.length());
            //System.out.println("dataValue = " + dataValue);
            tag2 = ")";
            idx = dataValue.indexOf(tag2);
            if (idx == -1) {
                break;
            }
            String s = dataValue.substring(0, idx);
            //System.out.println("s = " + s);
            Point p = convert(s);
            P.add(p);
        }
        return P;
    }

    private String extractType(String s){
    	String tag = "Type";
    	int idx = s.indexOf(s);
    	s = s.substring(tag.length()+1);
    	return s;
    }
    private String extractName(String s){
    	String tag = "Label";
    	int idx = s.indexOf(s);
    	s = s.substring(tag.length()+1);
    	return s;
    }
    private String extractDirIndicator(String s){
    	String tag = "DirIndicator";
    	int idx = s.indexOf(s);
    	s = s.substring(tag.length()+1);
    	return s;
    }
    public void writeToFile(String fn, int zoomLevel){
    	try{
    		BufferedReader in = new BufferedReader(new FileReader("header.txt"));
    		PrintWriter out = new PrintWriter(fn);
    		String line = "";
    		while((line=in.readLine())!=null){
    			out.println(line);
    			System.out.println(line);
    		}
    		for(int i = 0; i < lines.size(); i++){
    			Polyline pl = lines.get(i);
    			pl.printToFile(out, zoomLevel);
    		}
    		out.close();
    		in.close();

    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
	public void readData(String fn){
		lines = new ArrayList<Polyline>();
		//points = new ArrayList<Point>();
		
        try {
            
            
            BufferedReader in = new BufferedReader(new FileReader(fn));
            String line = "";
            String dataValue = "";
            while((line = in.readLine()) != null){
            	if(line.startsWith("[POLYLINE]")){
            		ArrayList<Point> P = null;
            		String name = "noname";
            		String type = "";
            		String dir = "";
            		while(true){
            			dataValue = in.readLine();
                        if (dataValue.startsWith("Data0")) {
                            // System.out.println("DataValue = " + dataValue);
                            P = extractPoints(dataValue);                         
                            for (int i = 0; i < P.size(); i++) {
                                Point p = P.get(i);
                                System.out.println("Point[" + i + "] = " + p.getdLat() + "," + p.getdLong());
                                System.out.println("-----------------------------------------");
                            }
                             
                        }else if(dataValue.startsWith("Type")){
                        	type = extractType(dataValue);
                        }else if(dataValue.startsWith("Label")){
                        	name = extractName(dataValue);
                        }else if(dataValue.startsWith("DirIndicator")){
                        	dir = extractDirIndicator(dataValue);
                        }else if(dataValue.startsWith("[END]")){
                        	break;
                        }
            		}
            		if(P != null){
            			boolean directional = false;
            			if(dir.equals("1")) directional = true;
            			Polyline pl = new Polyline(P,name,type,directional);
            			lines.add(pl);
            		}
            	}    
            }
            
            for(int i = 0; i < lines.size(); i++){
            	Polyline pl = lines.get(i);
            	ArrayList<Point> p = pl.getPoints();
            	System.out.println("Name = " + pl.getName());
            	System.out.println("Type = " + pl.getType());
            	System.out.print("Data0 = ");
            	for(int j = 0; j < p.size(); j++)
            		System.out.print("(" + p.get(j).getdLat() + "," + p.get(j).getdLong() + ")");
            	System.out.println();
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }        

	}
	
	public ArrayList<Polyline> getPolylines(){
		return this.lines;
	}
    public void createMapFile(String fn){
    	try{
            PrintWriter out = new PrintWriter(fn);
            BufferedReader in = new BufferedReader(new FileReader("header.txt"));
            String line = "";
            while((line = in.readLine()) != null){
            	out.println(line);
            }
            
            for(int j = 0; j < lines.size(); j++){
            	Polyline pl = lines.get(j);
            	ArrayList<Point> V = pl.getPoints();
            	for(int z = 0; z <= 5; z++){
            		out.println("[POLYLINE]");
            		out.println("Type=" + pl.getType());
            		out.println("Label=" + pl.getName());
            		out.print("Data" + z + "=");
            		for(int i = 0; i < V.size()-1; i++){
            			Point p = V.get(i);
            			out.print("(" + p.getdLat() + "," + p.getdLong() + "),");            		
            		}
            		out.println("(" + V.get(V.size()-1).getdLat() + "," + V.get(V.size()-1).getdLong() + ")");
            		out.println("[END]");
            	}
            }
            out.close();
    		
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    public void setPolyline(ArrayList<Polyline> PL){
    	lines = PL;
    }
    public Map mergeMap(Map m){
    	Map newMap = new Map();
    	ArrayList<Polyline> PL = m.getPolylines();
    	
    	for(int i = 0; i < lines.size(); i++){
    		for(int j = 0; j < PL.size(); j++){
    			Polyline pli = lines.get(i);
    			Polyline plj = PL.get(j);
    			pli.intersect(plj);
    		}
    	}
    	ArrayList<Polyline> newPL = new ArrayList<Polyline>();
    	for(int i = 0; i < lines.size(); i++)
    		newPL.add(lines.get(i));
    	for(int i = 0; i < PL.size(); i++)
    		newPL.add(PL.get(i));
    	
    	newMap.setPolyline(newPL);
    	
    	return newMap;
    }
    public Map generate2PolylineMap(){
    	Map m = new Map();
    	ArrayList<Polyline> newPL = new ArrayList<Polyline>();
    	for(int i = 0; i < lines.size(); i++){
    		Polyline pl = lines.get(i);
    		ArrayList<Point> P = pl.getPoints();
    		for(int j = 0; j < P.size()-1; j++){
    			Point p1 = P.get(j);
    			Point p2 = P.get(j+1);
    			ArrayList<Point> points = new ArrayList<Point>();
    			points.add(p1);
    			points.add(p2);
    			Polyline newpl = new Polyline(points,pl.getName(),pl.getType(),pl.directional());
    			newPL.add(newpl);
    		}
    	}
    	m.setPolyline(newPL);
    	return m;
    }
    public int getNbDirectionalPolylines(){
    	int c = 0;
    	for(int i = 0; i < lines.size(); i++){
    		Polyline pl = lines.get(i);
    		if(pl.directional()) c++;
    	}
    	return c;
    }
    public void write2PlainText(String filename){
    	try{
    		Approximation ap = new Approximation();
    		PrintWriter out = new PrintWriter(filename);
    		HashMap<Point, Integer> mP = new HashMap<Point, Integer>();
    		for(int i = 0; i < points.size(); i++){
    			mP.put(points.get(i), i+1);
    		}
    		
    		for(int i = 0; i < points.size(); i++){
    			Point pi = points.get(i);
    			out.println(mP.get(pi) + "\t" + pi.getdLat() + "\t" + pi.getdLong());
    		}
    		out.println("-1");
    		for(int i = 0; i < points.size(); i++){
    			Point p = points.get(i);
    			int idx_p = mP.get(p);
    			Iterator it = A.get(p).iterator();
    			while(it.hasNext()){
    				Point pi = (Point)it.next();
    				int idx_pi = mP.get(pi);
    				double d = ap.computeDistanceHav(p.getdLong(), p.getdLat(), pi.getdLong(), pi.getdLat());
    				out.println(idx_p + "\t" + idx_pi + "\t" + d);
    			}
    		}
    		out.println(-1);
    		out.close();
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		String map1 = "";
		String map2 = "";
		String newmap = "";
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("--map1")) 
				map1 = args[i+1];
			else if(args[i].equals("--map2")) 
				map2 = args[i+1];
			else if(args[i].equals("--newmap")) 
				newmap = args[i+1];
		}
		
		Map m1 = new Map();
		m1.readData(map1);
		
		Map m2 = new Map();
		m2.readData(map2);
		
		Map newm = m1.mergeMap(m2);
		newm = newm.generate2PolylineMap();
		newm.createMapFile(newmap);
		*/
		
		
		Map m = new Map();
		
		
		//String mapFileName = "m-saigon.mp";
		String mapFileName = "map-hanoi.mp";
		//m.readData("D:\\dungpq\\Dang-Van-huy\\streets\\" + mapFileName);
		//m.readData("/Users/dungpq/research/projects/MapBuilder/" + mapFileName);
		m.readData("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/" + mapFileName);
		m.establishGraph();
		System.out.println("total length = " + m.computeLength() + ", nbPoints = " + m.getPoints().size() + ", nblines = " + m.getPolylines().size());
		System.out.println("Number of directional lines = " + m.getNbDirectionalPolylines());
		m.write2PlainText("/Users/dungpq/research/projects/prediction-based-transport-scheduling/data/map-hanoi.txt");
		//m.writeToFile("/Users/dungpq/research/projects/MapBuilder/m-saigon-new.mp", 6);
		//Map hanoi = new Map();
		//hanoi.readData("D:\\dungpq\\Dang-Van-huy\\streets\\tran_hung_dao.mp");
		//Map newhanoi = hanoi.mergeMap(m);
		
		//newhanoi = newhanoi.generate2PolylineMap();
		//newhanoi.createMapFile("D:\\dungpq\\Dang-Van-huy\\streets\\newhanoi.mp");
		
	}

}
