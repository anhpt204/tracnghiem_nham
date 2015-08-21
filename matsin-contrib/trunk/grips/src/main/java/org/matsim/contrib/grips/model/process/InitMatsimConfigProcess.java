/* *********************************************************************** *
 * project: org.matsim.*
 * MyMapViewer.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.contrib.grips.model.process;

import org.matsim.contrib.grips.control.Controller;

public class InitMatsimConfigProcess extends BasicProcess
{

	
	public InitMatsimConfigProcess(Controller controller)
	{
		super(controller);
	}
	
	@Override
	public void start()
	{
		// check if Matsim config (including the OSM network) has been loaded
		if (!controller.isMatsimConfigOpened())
			if (!controller.openMastimConfig())
				controller.exit(locale.msgOpenMatsimConfigFailed());
	}
		
	

}
