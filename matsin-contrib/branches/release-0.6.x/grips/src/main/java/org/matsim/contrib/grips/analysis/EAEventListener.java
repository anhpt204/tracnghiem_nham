/* *********************************************************************** *
 * project: org.matsim.*
 * RoadClosuresEditor.java
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

package org.matsim.contrib.grips.analysis;

import java.awt.event.MouseEvent;

import org.matsim.contrib.grips.control.Controller;
import org.matsim.contrib.grips.control.eventlistener.AbstractListener;

public class EAEventListener extends AbstractListener
{

	public EAEventListener(Controller controller)
	{
		super(controller);
	}
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		this.controller.paintLayers();
	}

}
