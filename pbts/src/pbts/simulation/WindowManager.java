package pbts.simulation;

import java.util.*;
public class WindowManager {

	/**
	 * @param args
	 */
	public RoadMap M;
	public Window[][] windows;
	public int height;
	public int width;
	public double minLat;
	public double maxLat;
	public double minLng;
	public double maxLng;
	
	HashMap<Window, ArrayList<Integer>> lstPoints;
	HashMap<Window, Window> farest;
	ArrayList<Window> notEmptyWindows;
	
	public WindowManager(RoadMap map){
		M = map;
	}
	public void generateWindows(int width, int height){
		this.height = height;
		this.width = width;
		minLat = 999999999;
		maxLat = -minLat;
		minLng = 999999999;
		maxLng = -minLng;
		for(int i = 0; i < M.V.size(); i++){
			int v = M.V.get(i);
			pbts.entities.LatLng p = M.mLatLng.get(v);
			minLat = minLat < p.lat ? minLat : p.lat;
			maxLat = maxLat > p.lat ? maxLat : p.lat;
			minLng = minLng < p.lng ? minLng : p.lng;
			maxLng = maxLng > p.lng ? maxLng : p.lng;
		}
		
		double d_height = (maxLat - minLat)/height;
		double d_width = (maxLng - minLng)/width;
		lstPoints = new HashMap<Window, ArrayList<Integer>>();
		windows = new Window[height][width];
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				double max_lat = maxLat - i*d_height;
				double min_lat = max_lat - d_height;
				double min_lng = minLng + j*d_width;
				double max_lng = min_lng + d_width;
				windows[i][j] = new Window(min_lat,max_lat,min_lng,max_lng);
				lstPoints.put(windows[i][j], new ArrayList<Integer>());
			}
		}
		
		
		for(int k = 0; k < M.V.size(); k++){
			int v = M.V.get(k);
			pbts.entities.LatLng p = M.mLatLng.get(v);
			Window W = findWindowContains(p.lat,p.lng);
			if(W != null)
				lstPoints.get(W).add(v);
			System.out.println("WindowManager::generate, finish " + k + "/" + M.V.size());			
		}
		
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				System.out.println("W[" + i + "," + j + "].sz = " + lstPoints.get(windows[i][j]).size());
			}
		}
		
		
		farest = new HashMap<Window, Window>();
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				int f_i = -1;
				int f_j = -1;
				int D = -1;
				for(int i1 = 0; i1 < height; i1++){
					for(int j1 = 0; j1 < width; j1++){
						if(lstPoints.get(windows[i1][j1]).size() == 0) continue;
						int d = Math.abs(i-i1) + Math.abs(j-j1);
						if(d > D){
							D = d;
							f_i = i1; f_j = j1;
						}
					}
				}
				farest.put(windows[i][j], windows[f_i][f_j]);
			}
		}
		
		notEmptyWindows = new ArrayList<Window>();
		for(int i = 0; i < height; i++)
			for(int j = 0; j < width; j++)
				if(lstPoints.get(windows[i][i]).size() > 0)
					notEmptyWindows.add(windows[i][j]);
	
	}
	public Window findWindowContains(double lat, double lng){
		for(int i = 0; i < height; i++)
			for(int j = 0; j < width; j++)
				if(windows[i][j].contains(lat, lng))
					return windows[i][j];
		return null;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
