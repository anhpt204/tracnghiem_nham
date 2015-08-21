package org.matsim.contrib.matsim4opus.utils.io.writer;

import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.matsim4opus.constants.InternalConstants;
import org.matsim.contrib.matsim4opus.gis.Zone;
import org.matsim.core.utils.io.IOUtils;


public class AnalysisCellBasedAccessibilityCSVWriterV2 {
	
	private static final Logger log = Logger.getLogger(AnalysisCellBasedAccessibilityCSVWriterV2.class);
	private static BufferedWriter accessibilityDataWriter = null;
	public static final String FILE_NAME= "accessibility_indicators.csv";
	
	/**
	 * writes the header of accessibility data csv file
	 */
	public static void initAnalysisCellBasedAccessibilityCSVWriterV2(){
		try{
			log.info("Initializing AnalysisCellBasedAccessibilityCSVWriterV2 ...");
			accessibilityDataWriter = IOUtils.getBufferedWriter( InternalConstants.MATSIM_4_OPUS_TEMP + FILE_NAME );
			
			// create header
			accessibilityDataWriter.write( InternalConstants.ZONE_ID + "," +
										   InternalConstants.X_COORDINATE + "," +
										   InternalConstants.Y_COORDINATE + "," + 
										   InternalConstants.NEARESTNODE_ID + "," +
										   InternalConstants.NEARESTNODE_X_COORD + "," +
										   InternalConstants.NEARESTNODE_Y_COORD + "," + 
										   InternalConstants.ACCESSIBILITY_BY_FREESPEED + "," +
										   InternalConstants.ACCESSIBILITY_BY_CAR + "," +
										   InternalConstants.ACCESSIBILITY_BY_BIKE + "," +
										   InternalConstants.ACCESSIBILITY_BY_WALK + "," +
										   InternalConstants.ACCESSIBILITY_BY_PT);
			accessibilityDataWriter.newLine();
			
			log.info("... done!");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * writing the accessibility measures into csv file
	 * 
	 * @param startZone
	 * @param coordFromZone
	 * @param node
	 * @param freeSpeedAccessibility
	 * @param carAccessibility
	 * @param bikeAccessibility
	 * @param walkAccessibility
	 */
	public static void write(Zone<Id> startZone, 
							 Coord coordFromZone,
							 Node node, 
							 double freeSpeedAccessibility,
							 double carAccessibility, 
							 double bikeAccessibility,
							 double walkAccessibility,
							 double ptAccessibility){
		
		try{
			assert(AnalysisCellBasedAccessibilityCSVWriterV2.accessibilityDataWriter != null);
			accessibilityDataWriter.write( ((Id)startZone.getAttribute()) + "," +
										   coordFromZone.getX() + "," +
										   coordFromZone.getY() + "," +
										   node.getId() + "," + 
										   node.getCoord().getX() + "," +  
										   node.getCoord().getY() + "," + 
										   freeSpeedAccessibility + "," +
										   carAccessibility + "," +
										   bikeAccessibility + "," + 
										   walkAccessibility + "," +
										   ptAccessibility);
			accessibilityDataWriter.newLine();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * finalize and close csv file
	 */
	public static void close(){
		try {
			log.info("Closing AnalysisCellBasedAccessibilityCSVWriterV2 ...");
			assert(AnalysisCellBasedAccessibilityCSVWriterV2.accessibilityDataWriter != null);
			accessibilityDataWriter.flush();
			accessibilityDataWriter.close();
			log.info("... done!");
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
