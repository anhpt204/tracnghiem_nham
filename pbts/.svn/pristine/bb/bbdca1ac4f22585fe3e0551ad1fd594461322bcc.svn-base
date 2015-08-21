package ktuan;

import java.util.HashMap;

import pbts.entities.ItineraryTravelTime;
import pbts.simulation.AnalysisTemplate;
import pbts.simulation.Simulator;
import pbts.simulation.SolutionAnalyzer;

public class KSimualator extends Simulator {
	public Report _report;
	public AnalysisTemplate analyzeSolution(HashMap<Integer, ItineraryTravelTime> itineraries){
		SolutionAnalyzer analyzer = new SolutionAnalyzer(this);
		AnalysisTemplate AT = analyzer.analyzeSolution(itineraries);
		analyzer.finalize();
		_report= new  Report(AT);
		return AT;
	}
	public void solve(SetSARP_File setFile){
		System.out.println("KSimulator: Start: solve");
		loadMapFromTextFile(setFile._fileGraph);
		loadParameters(setFile._fileParameters);
		loadDepotParkings(setFile._fileDepotsAndParkings);
		initVehicles();
		loadRequests(setFile._fileRequests);
		HashMap<Integer, ItineraryTravelTime> itineraries = loadItineraries(setFile._fileGenOut);
		analyzeSolution(itineraries);
		finalize();
		_report.writeStringToFile(setFile._fileReport,_report.toString());
		System.out.println("KSimulator: Finish: solve");
	}
}
