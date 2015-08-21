package pbts.gismap;
import java.util.*;
import java.io.*;
public class Polyline {

	/**
	 * @param args
	 */
	
	private ArrayList<Point> points;
	private String name;
	private String type;
	private boolean directional;
	
	public Polyline(ArrayList<Point> points, String name, String type, boolean directional){
		this.points = points;
		this.name = name;
		this.type = type;
		this.directional = directional;
	}
	public ArrayList<Point> getPoints(){
		return this.points;
	}
	public String getName(){ return this.name;}
	public String getType(){ return this.type;}
	public boolean directional(){ return directional;}
	public void setDirectional(boolean directional){ this.directional = directional;}
	public void println(){
		System.out.println("Polyline::println, sz = " + points.size());
		for(int i = 0; i < points.size(); i++)
			System.out.println(points.get(i).toString());
		System.out.println("------");
	}
	public void printToFile(PrintWriter out, int zoomLevel){
		for(int z = 0; z <= zoomLevel; z++){
			out.println("[POLYLINE]");
			out.println("Type=0x1");
			out.println("Label=" + name);
			out.print("Data" + z + "=");
			for(int i = 0; i < points.size()-1; i++){
				Point p = points.get(i);
				out.print("(" + p.getdLat() + "," + p.getdLong() + "),");
			}
			Point p = points.get(points.size()-1);
			out.println("(" + p.getdLat() + "," + p.getdLong() + ")");
			out.println("[END]");
		}
	}
	public void intersect(Polyline pl){
		System.out.println("Polyline::intersect, init:");
		println();
		pl.println();
		
		ArrayList<Point> P = pl.getPoints();
		boolean stop = false;
		int it = 0;
		while(!stop){
			it++;
			stop = true;
			for(int i = 0; i < points.size()-1; i++){
				
			
				Point p1 = points.get(i);
				Point p2 = points.get(i+1);
				Line li = new Line(p1.getdLong(),p1.getdLat(),p2.getdLong(),p2.getdLat());
				
				for(int j = 0; j < P.size()-1; j++){
					Point p3 = P.get(j);
					Point p4 = P.get(j+1);
					Line lj = new Line(p3.getdLong(),p3.getdLat(),p4.getdLong(),p4.getdLat());
					
					Point p = new Point(0,0);
					TWO_SEGMENTS_RELATION R = li.intersectSegment(lj, p);
					System.out.println("Polyline::intersect " + p1.toString() + "--" + p2.toString() + " AND " +
							p3.toString() + "--" + p4.toString());
					
					if(R == TWO_SEGMENTS_RELATION.SEGMENT_INTERSECTIONAL){
						if(!p.equals(p1) && !p.equals(p2) && !p.equals(p3) && !p.equals(p4)){
							points.add(i+1,p);
							P.add(j+1,p);
							System.out.println("Polyline::intersect --> find an intersection (" + p.getdLat() + "," + p.getdLong() + ")");
							println();
							pl.println();
							stop = false;
							break;
						}
					}else{
						System.out.println("Polyline::intersect does not intesect");
					}
				}
				if(!stop) break;
			}
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<Integer> L = new ArrayList<Integer>();
		L.add(1);
		L.add(2);
		L.add(3);
		L.add(4);
		L.add(1,10);
		for(int i = 0; i < L.size(); i++)
			System.out.println(L.get(i));
	}

}
