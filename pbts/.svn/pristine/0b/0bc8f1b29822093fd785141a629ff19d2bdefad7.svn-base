package ktuan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class RequestSARP {

	public ArrayList<PeopleRequestDungPQ> _listPeopleRq;
	public ArrayList<ParcelRequestDungPQ> _listParcelRq;

	public void readListRequestFromFile(String path) {
		System.out.println("Start: read List Request From File");
		File dataFile= new File(path);
		try {
			BufferedReader br= new BufferedReader(new FileReader(dataFile.getAbsolutePath()));
			for(int cs=0;cs<2;cs++){
				String line=br.readLine().trim();
				if (line.startsWith("People requests")|| line.startsWith("people requests")){
					_listPeopleRq= new ArrayList<PeopleRequestDungPQ>();
					line= br.readLine().trim();
					while (!line.startsWith("-1")){
						line= TextManagerSARP.parserSpace(line);
						String s[]= line.split("\t");
						int id= Integer.parseInt(s[0]);
						int time_call= Integer.parseInt(s[1]);
						int pickup_point= Integer.parseInt(s[2]);
						int delivery_point= Integer.parseInt(s[3]);
						int early_pickup_time= Integer.parseInt(s[4]);
						int late_pickup_time= Integer.parseInt(s[5]);
						int early_delivery_time= Integer.parseInt(s[6]);
						int late_delivery_time= Integer.parseInt(s[7]);
						double max_travel_distance= Double.parseDouble(s[8]);
						int maxNbStops= Integer.parseInt(s[9]);
						PeopleRequestDungPQ rq= new PeopleRequestDungPQ(id, time_call, pickup_point, delivery_point, early_pickup_time, late_pickup_time, early_delivery_time, late_delivery_time, max_travel_distance, maxNbStops);
						_listPeopleRq.add(rq);
						System.out.println(rq);
						line= br.readLine();
					}
				}else 
				if (line.startsWith("Parcel requests")||line.startsWith("parcel requests")){
					_listParcelRq= new  ArrayList<ParcelRequestDungPQ>();
					line= br.readLine().trim();
					while (!line.startsWith("-1")){
						line= TextManagerSARP.parserSpace(line);
						String s[]= line.split("\t");
						int id= Integer.parseInt(s[0]);
						int time_call= Integer.parseInt(s[1]);
						int pickup_point= Integer.parseInt(s[2]);
						int delivery_point= Integer.parseInt(s[3]);
						int early_pickup_time= Integer.parseInt(s[4]);
						int late_pickup_time= Integer.parseInt(s[5]);
						int early_delivery_time= Integer.parseInt(s[6]);
						int late_delivery_time= Integer.parseInt(s[7]);
						ParcelRequestDungPQ rq= new ParcelRequestDungPQ(id, time_call, pickup_point, delivery_point, early_pickup_time, late_pickup_time, early_delivery_time, late_delivery_time);
						_listParcelRq.add(rq);
						System.out.println(rq);
						line= br.readLine();
					}
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finish: read List Request From File");
	}

	public void reWriteRequestToFile(String fileReWriteRequest) {
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileReWriteRequest), "utf-8"));
			if (_listPeopleRq!=null){
				writer.write(String.format("people requests\n").toCharArray());
				for (int cs=0;cs<_listPeopleRq.size();cs++) writer.write(String.format("%s\n", _listPeopleRq.get(cs).toStringFormat()));
				writer.write(new String("-1\n").toCharArray());
			}
			if (_listParcelRq!=null){
				writer.write(String.format("parcel requests\n").toCharArray());
				for (int cs=0;cs<_listParcelRq.size();cs++)writer.write(String.format("%s\n", _listParcelRq.get(cs).toStringFormat()));
				writer.write(new String("-1\n").toCharArray());
			}
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}

}
