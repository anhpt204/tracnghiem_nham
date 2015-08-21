package pbts.gismap;

import java.util.*;
import java.io.*;
public class Approximation {

	/**
	 * @param args
	 */
	ArrayList danhsachData = new ArrayList<Point>();
	ArrayList<String> list1 = new ArrayList<String>();
	ArrayList<String> listBestP = new ArrayList<String>();
	private static final double eps = 0.05;

	PrintWriter log = null;
    double computeDistanceHav(double long1, double lat1, double long2, double lat2) {
        long1 = long1 * 1.0;
        lat1 = lat1 * 1.0;
        long2 = long2 * 1.0;
        lat2 = lat2 * 1.0;
        
        double dlat1 = lat1 * Math.PI / 180;
        double dlong1 = long1 * Math.PI / 180;
        double dlat2 = lat2 * Math.PI / 180;
        double dlong2 = long2 * Math.PI / 180;
        
        double dlong = dlong2 - dlong1;
        double dlat = dlat2 - dlat1;
        
        double aHarv = Math.pow(Math.sin(dlat / 2), 2.0) + Math.cos(dlat1) * Math.cos(dlat2) * Math.pow(Math.sin(dlong / 2), 2.0);
        double cHarv = 2 * Math.atan2(Math.sqrt(aHarv), Math.sqrt(1.0 - aHarv));
        
        double R = 6378.137;
        return R * cHarv * 1000; // in m
    }

    /*
     * Herong 
     */
    double computeDistancePoint2Edge(double long1, double lat1, double long2, double lat2, double long3, double lat3) {
        double d1 = computeDistanceHav(long1, lat1, long2, lat2);
        double d2 = computeDistanceHav(long1, lat1, long3, lat3);
        double d12 = computeDistanceHav(long2, lat2, long3, lat3);
        double p = (d1 + d2 + d12) / 2;
        double S = Math.sqrt(p * (p - d1) * (p - d2) * (p - d12));
        double h = 2 * S / d12;
        return h;
    }
    
    private int findBest(ArrayList<Point> P, int s, int e) {
        double maxDis = -10000;//eps;
        int ind = -1;
        double s_lat = P.get(s).getdLat();//Double.parseDouble(P.get(s).getdLat() + "");
        double s_lng = P.get(s).getdLong();//Double.parseDouble(P.get(s).getdLong() + "");
        double e_lat = P.get(e).getdLat();//Double.parseDouble(P.get(e).getdLat() + "");
        double e_lng = P.get(e).getdLong();//Double.parseDouble(P.get(e).getdLong() + "");
        String strList = "Data0=";
        for (int i = s + 1; i < e; i++) {
            Point v = P.get(i);
            double d = computeDistancePoint2Edge(Double.parseDouble(v.getdLong() + ""), Double.parseDouble(v.getdLat() + ""), s_lng, s_lat, e_lng, e_lat);
            if (d > maxDis) {
                maxDis = d;
                ind = i;                
               // System.out.println("####################(" + v.getdLong() + "," + v.getdLat() + "####################");
                strList += "(" + v.getdLat() + "," + v.getdLong() + "),";
            }
        }
        listBestP.add(strList);
        System.out.println("findBest s = " + s + " e = " + e + " maxDis = " + maxDis + " ind = " +  ind);
        if(maxDis < eps) ind = -1;
        return ind;
    }
    
    public void print(ArrayList<Point> p){
        for(int i = 0; i < p.size(); i++){
            Point pi = p.get(i);
            System.out.print("(" + pi.getdLat() + "," + pi.getdLong() + ") ");
        }
    }
    
    public ArrayList<Integer> approximateIndex(ArrayList<Point> P, ArrayList<Integer> I, int s, int e){
    	ArrayList<Integer> a = new ArrayList<Integer>();
        int pos = findBest(P, s, e);
        System.out.println(" --> pos = " + pos);
        if (pos == -1) 
        {
            a.add(s);
            a.add(e);
            return a;
        }
        ArrayList<Integer> V1 = approximateIndex(P, I, s, pos);
        
        ArrayList<Integer> V2 = approximateIndex(P, I, pos, e);
        
        for (int i = 0; i < V1.size(); i++) {
            a.add(V1.get(i));
        }
        for (int i = 1; i < V2.size(); i++) {
            a.add(V2.get(i));           
        }
        
        V1.clear();
        V2.clear();
    	
    	return a;
    }
    public ArrayList<Point> approximate(ArrayList<Point> P, int s, int e) {
        System.out.print("approximate START. P.sz = " + P.size() + " s = " + s + " e  " + e);
       // System.out.print(s);
        //System.out.print(",");
        //System.out.print(e);
        //System.out.print(")");
       // System.out.print("\n");
        ArrayList<Point> V = new ArrayList<Point>();
        int pos = findBest(P, s, e);
        //System.out.print("approximate(");
        //System.out.print(s);
        //System.out.print(",");
        //System.out.print(e);
       // print(P);
        System.out.println(" --> pos = " + pos);
        //System.out.print(pos);
        //System.out.print("\n");
        if (pos == -1) 
            // not found
        {
            V.add(P.get(s));
            V.add(P.get(e));
            /*
            System.out.print("approximate(");
            System.out.print(s);
            System.out.print(",");
            System.out.print(e);
            System.out.print("), V1 = ");
            System.out.print(0);
            System.out.print(" V2 = ");
            System.out.print(0);
            System.out.print(" pos = ");
            System.out.print(pos);
            System.out.print(" V = ");
            System.out.print(V.size());
            System.out.print("\n");
            */
            return V;
        }
        
       // System.out.println("Pos = " + pos);
        ArrayList<Point> V1 = approximate(P, s, pos);
        
        ArrayList<Point> V2 = approximate(P, pos, e);
        
        for (int i = 0; i < V1.size(); i++) {
            V.add(V1.get(i));
        }
        for (int i = 1; i < V2.size(); i++) {
            V.add(V2.get(i));           
        }
        
        
        /*
        System.out.print("approximate(");
        System.out.print(s);
        System.out.print(",");
        System.out.print(e);
        System.out.print("), V1 = ");
        System.out.print(V1.size());
        System.out.print(" V2 = ");
        System.out.print(V2.size());
        System.out.print(" pos = ");
        System.out.print(pos);
        System.out.print(" V = ");
        System.out.print(V.size());
        System.out.print("\n");
        */
        V1.clear();
        V2.clear();
        
        return V;
    }

   
        private Point convert(String s) {
        String tag = ",";
        int idx = s.indexOf(tag);
        String x = s.substring(0, idx);
        String y = s.substring(idx + tag.length(), s.length());
        System.out.println("x = " + x + " y = " + y);
        Point p = new Point(Double.valueOf(x), Double.valueOf(y));
        return p;
    }
    
    private ArrayList<Point> extractPoints(String dataValue) {
        ArrayList<Point> P = new ArrayList<Point>();
        String tag1 = "Data0=";
        int idx = dataValue.indexOf(tag1);
        dataValue = dataValue.substring(idx + tag1.length(), dataValue.length());
        //System.out.println("dataValue = " + dataValue);
        log.println("cut Data0, remain = " + dataValue);
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
            log.println("extract s = " + s);
            Point p = convert(s);
            P.add(p);
        }
        return P;
    }
        
    private void readFile(String fn) throws Exception {
        try {
            
            log = new PrintWriter("log.txt");
            
            BufferedReader in = new BufferedReader(new FileReader(fn));
            String line = "";
            String dataValue = "";
            while((line = in.readLine()) != null){
            	if(line.startsWith("[POLYLINE]")){
            		while(true){
            			dataValue = in.readLine();
                        if (dataValue.startsWith("Data0")) {
                            // System.out.println("DataValue = " + dataValue);
                        	log.println("DataValue = " + dataValue);
                            ArrayList<Point> P = extractPoints(dataValue);                         
                            for (int i = 0; i < P.size(); i++) {
                                Point p = P.get(i);
                                System.out.println("Point[" + i + "] = " + p.getdLat() + "," + p.getdLong());
                                log.println("Point[" + i + "] = " + p.getdLat() + "," + p.getdLong());
                                System.out.println("-----------------------------------------");
                                danhsachData.add(p);
                            }
                             
                        }else if(dataValue.startsWith("[END]")){
                        	break;
                        }
            		}
            	}    
            }
            log.close();
            
        } catch (Exception ex) {
        	ex.printStackTrace();
        }        
    }

    public void createMapFile(ArrayList<Point> V, String fn){
    	try{
            PrintWriter out = new PrintWriter(fn);
            BufferedReader in = new BufferedReader(new FileReader("header.txt"));
            String line = "";
            while((line = in.readLine()) != null){
            	out.println(line);
            }
            
            for(int z = 0; z <= 5; z++){
            	out.println("[POLYLINE]");
            	out.println("Type=0x5");
            	out.print("Data" + z + "=");
            	for(int i = 0; i < V.size()-1; i++){
            		Point p = V.get(i);
            		out.print("(" + p.getdLat() + "," + p.getdLong() + "),");            		
            	}
            	out.println("(" + V.get(V.size()-1).getdLat() + "," + V.get(V.size()-1).getdLong() + ")");
            	out.println("[END]");
            }
            
            out.close();
    		
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    private void approximate(String input, String output) {
        try {
            readFile(input);
            //if(true) return;
            
            ArrayList<Point> V  = approximate(danhsachData, 0, danhsachData.size() - 1);
            ArrayList<Integer> a = approximateIndex(danhsachData, null, 0, danhsachData.size() - 1); 
            for(int i = 0; i < a.size(); i++){
            	Point p = V.get(i);
            	System.out.println(a.get(i) + " : " + p.getdLat() + "," + p.getdLong());
            }
            
            createMapFile(danhsachData, "tmp.mp");
            createMapFile(V, output);
            /*
            PrintWriter out = new PrintWriter(output);
            BufferedReader in = new BufferedReader(new FileReader("header.txt"));
            String line = "";
            while((line = in.readLine()) != null){
            	out.println(line);
            }
            
            for(int z = 0; z <= 5; z++){
            	out.println("[POLYLINE]");
            	out.println("Type=0x5");
            	out.print("Data" + z + "=");
            	for(int i = 0; i < V.size()-1; i++){
            		Point p = V.get(i);
            		out.print("(" + p.getdLat() + "," + p.getdLong() + "),");            		
            	}
            	out.println("(" + V.get(V.size()-1).getdLat() + "," + V.get(V.size()-1).getdLong() + ")");
            	out.println("[END]");
            }
            
            out.close();
            */
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }//
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String map1 = "";
		String map2 = "";
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("--map1")) 
				map1 = args[i+1];
			else if(args[i].equals("--map2")) 
				map2 = args[i+1];
		}
		
		Approximation ap = new Approximation();
		ap.approximate(map1, map2);
		//String mapFileName = "quang_trung.mp";
		//ap.approximate("D:\\dungpq\\Dang-Van-huy\\DataGPStrack\\" + mapFileName, "D:\\dungpq\\Dang-Van-huy\\streets\\" + mapFileName);
	}

}
