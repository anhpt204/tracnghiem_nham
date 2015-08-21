package pbts.datamanipulation;

import java.util.*;
import java.io.*;

import pbts.simulation.RoadMap;
public class ParkingsSanFrancisco {

	public void collectParkingDepots(String fLatLng, String fo, String mapFilename, int nbTaxis){
		try{
			pbts.simulation.RoadMap M = new pbts.simulation.RoadMap();
			M.loadData(mapFilename);
			
			Scanner in = new Scanner(new File(fLatLng));
			PrintWriter out = new PrintWriter(fo);
			ArrayList<Integer> P = new ArrayList<Integer>();
			while(true){
				String line = in.nextLine();
				if(line.equals("-1")) break;
				String[] s = line.split(",");
				double lat = Double.valueOf(s[0]);
				double lng = Double.valueOf(s[1]);
				int p = M.findNearestPoint(lat, lng, 1000);
				System.out.println("ParkingsSanFrancisco, findPoint (" + lat + "," + lng + ") = " + p);
				if(p != -1){
					P.add(p);
				}
			}
			
			out.println("Depots");
			int nbTaxisPerDepot = nbTaxis/P.size();
			for(int i = 0; i< P.size(); i++){
				for(int j = 0; j < nbTaxisPerDepot; j++)
					out.print(P.get(i) + " ");
			}
			for(int i = 0; i < nbTaxis%P.size(); i++){
				out.print(P.get(i) + " "); 
			}
			
			out.println(-1);
			out.println("Parkings");
			
			int capacity = 100;
			for(int i = 0; i < P.size(); i++)
				out.println(P.get(i) + "  " + capacity);
			out.println(-1);
			out.close();
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParkingsSanFrancisco PS = new ParkingsSanFrancisco();
		//String dir = "C:\\DungPQ\\research\\projects\\prediction-based-transport-scheduling\\data\\SanFrancisco";
		String dir = "SanFrancisco";
		int nbTaxis = 600;
		
		PS.collectParkingDepots(dir + "\\parkings.txt", dir + "\\depot" + nbTaxis + "-parkings54.txt", 
				dir + "\\SanFranciscoRoad-connected-contracted-5.txt",nbTaxis);
		
	}

}
