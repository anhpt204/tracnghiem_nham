/* *********************************************************************** *
 * project: org.matsim.*
 * MatsimNetworkReader.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.network;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.internal.MatsimSomeReader;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

/**
 * A reader for network-files of MATSim. This reader recognizes the format of the network-file and uses
 * the correct reader for the specific network-version, without manual setting.
 *
 * @author mrieser
 */
public class MatsimNetworkReader extends MatsimXmlParser implements MatsimSomeReader {

	private final static Logger log = Logger.getLogger(MatsimNetworkReader.class);
	private final static String NETWORK_V1 = "network_v1.dtd";

	private final Scenario scenario;
	private MatsimXmlParser delegate = null;

	/**
	 * Creates a new reader for MATSim configuration files.
	 *
	 * @param scenario A scenario containing the network where to store the loaded data.
	 */
	public MatsimNetworkReader(final Scenario scenario) {
		this.scenario = scenario;
	}

	@Override
	public void startTag(final String name, final Attributes atts, final Stack<String> context) {
		this.delegate.startTag(name, atts, context);
	}

	@Override
	public void endTag(final String name, final String content, final Stack<String> context) {
		this.delegate.endTag(name, content, context);
	}

	/**
	 * Parses the specified network file. This method calls {@link #parse(String)}, but handles all
	 * possible exceptions on its own.
	 *
	 * @param filename The name of the file to parse.
	 */
	public void readFile(final String filename) {
		parse(filename);
		if (this.scenario.getNetwork() instanceof NetworkImpl) {
			((NetworkImpl) this.scenario.getNetwork()).connect();
		}
	}

	@Override
	protected void setDoctype(final String doctype) {
		super.setDoctype(doctype);
		// Currently the only network-type is v1
		if (NETWORK_V1.equals(doctype)) {
			this.delegate = new NetworkReaderMatsimV1(this.scenario);
			log.info("using network_v1-reader.");
		} else {
			throw new IllegalArgumentException("Doctype \"" + doctype + "\" not known.");
		}
	}

}
