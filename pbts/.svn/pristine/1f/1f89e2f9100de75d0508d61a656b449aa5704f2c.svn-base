package ktuan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ParkingAndDepot {
	
	public ArrayList<Integer> _listIdDepots;
	public ArrayList<Integer> _listParking;
	public ArrayList<Integer> _listCapacity;
	public void readParkingAndDepot(String path) {
		System.out.println("Start : Read Parking And Depot");
		_listIdDepots= new ArrayList<Integer>();
		_listParking= new  ArrayList<Integer>();
		_listCapacity= new ArrayList<Integer>();
		File filedata= new  File(path);
		try {
			BufferedReader br= new BufferedReader(new FileReader(filedata.getAbsolutePath()));
			for(int cs=1;cs<=2;cs++){
				String line = br.readLine().trim();
				//System.out.println(line);
				if (line.startsWith("Depots")||line.startsWith("depots")){
					line = br.readLine().trim();
					line= TextManagerSARP.parserSpace(line);
					String[] s= line.split("\t");
					
					for(int i=0;i<s.length;i++){
						int depotId= Integer.parseInt(s[i]);
						System.out.println(depotId);
						if (depotId==-1) break;
						_listIdDepots.add(Integer.parseInt(s[i]));
					}
				}else 
				if (line.startsWith("Parkings")||line.startsWith("parkings")){
					line = br.readLine().trim();
					
					while (!line.startsWith("-1")){
						line=TextManagerSARP.parserSpace(line);
						System.out.println(line);
						String[] s= line.split("\t");
						_listParking.add(Integer.parseInt(s[0]));
						_listCapacity.add(Integer.parseInt(s[1]));
						line=br.readLine();
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finish: Read Parking And Depot");
	}

}
