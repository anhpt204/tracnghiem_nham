/* *********************************************************************** *
 * project: org.matsim.*
 * 
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.vis.otfvis.checklists;

import org.matsim.core.controler.Controler;
import org.matsim.vis.otfvis.OTFFileWriterFactory;

/**
 * @author florian ostermann
 */
public class T2_StartControler {
	
	private static String config = "./test/input/org/matsim/vis/otfvis/checklists/config.xml";
	private static String config2 = "./test/input/org/matsim/vis/otfvis/checklists/config-qsim.xml";
	
	public static void main(String[] args) {
		Controler con = new Controler(config);
		con.setOverwriteFiles(true);
		con.addSnapshotWriterFactory("otfvis", new OTFFileWriterFactory());
		con.run();
		System.out.println("\n Queue-Sim is done. Output:" + con.getConfig().controler().getOutputDirectory());
		con = new Controler(config2);
		con.setOverwriteFiles(true);
		con.addSnapshotWriterFactory("otfvis", new OTFFileWriterFactory());
		con.run();
		System.out.println("\n QSim is done. Output:" + con.getConfig().controler().getOutputDirectory());
	}

}
