package ktuan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import pbts.simulation.AnalysisTemplate;

public class Report {

	double benefit=0.0;
	double discount=0.0;
	double travel_distance=0.0;
	double fuel=0.0;
	int servedParcels=0;
	int servedPeople=0;
	double revenueParcels=0.0;
	double revenuePeople=0.0;
	int sharedPeopleServices=0;
	public Report(AnalysisTemplate a) {
		this.benefit= a.benefits;
		this.discount= a.discount;
		this.travel_distance= a.travelDistance;
		this.fuel= a.fuelCost;
		this.servedParcels= a.nbServedParcels;
		this.servedPeople= a.nbServedPassengers;
		this.revenueParcels=a.revenueParcels;
		this.revenuePeople= a.revenuePassengers;
		this.sharedPeopleServices= a.nbSharedPeopleService;
	}
	public Report() {
		// TODO Auto-generated constructor stub
	}
	public String toString(){
		String line3= String.format("analyze solution --> total benefits = %.9f\n", benefit);
		String line4= String.format("analyze solution --> total discount = %.9f\n", discount);
		String line5= String.format("analyze solution --> total travel distance = %.9f\n", travel_distance);
		String line6= String.format("analyze solution --> total fuel = %.9f\n", fuel);
		String line7= String.format("analyze solution --> total served parcels = %d\n", servedParcels);
		String line8= String.format("analyze solution --> total served people = %d\n", servedPeople);
		String line9= String.format("analyze solution --> total revenue parcels = %.9f\n", revenueParcels);
		String line10= String.format("analyze solution --> total revenue people = %.9f\n", revenuePeople);
		String line11= String.format("analyze solution --> number of shared people services = %d\n", sharedPeopleServices);
		return line3+line4+line5+line6+line7+line8+line9+line10+line11;
	}
	public void writeStringToFile(String path, String conten){
		System.out.println("Report: Start: writeStringToFile");
		System.out.println("Path: "+path);
		try {
			File file = new File(path);
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file.getAbsolutePath()), "utf-8"));
			writer.write(conten);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Report: Finish: writeStringToFile");
	}
}
