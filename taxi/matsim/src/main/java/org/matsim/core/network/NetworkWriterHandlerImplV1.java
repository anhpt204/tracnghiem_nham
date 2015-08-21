/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkWriterHandlerImplV1.java
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.utils.misc.Time;

/*package*/ class NetworkWriterHandlerImplV1 implements NetworkWriterHandler {

	//////////////////////////////////////////////////////////////////////
	//
	// interface implementation
	//
	//////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////
	// <network ... > ... </network>
	//////////////////////////////////////////////////////////////////////

	@Override
	public void startNetwork(final Network network, final BufferedWriter out) throws IOException {
		out.write("<network");
		if (network instanceof NetworkImpl && (((NetworkImpl) network).getName() != null)) {
			out.write(" name=\"" + ((NetworkImpl) network).getName() + "\"");
		}
		out.write(">\n\n");
	}

	@Override
	public void endNetwork(final BufferedWriter out) throws IOException {
		out.write("</network>\n");
	}

	//////////////////////////////////////////////////////////////////////
	// <nodes ... > ... </nodes>
	//////////////////////////////////////////////////////////////////////

	@Override
	public void startNodes(final Network network, final BufferedWriter out) throws IOException {
		out.write("\t<nodes>\n");
	}

	@Override
	public void endNodes(final BufferedWriter out) throws IOException {
		out.write("\t</nodes>\n\n");
	}

	//////////////////////////////////////////////////////////////////////
	// <links ... > ... </links>
	//////////////////////////////////////////////////////////////////////

	@Override
	public void startLinks(final Network network, final BufferedWriter out) throws IOException {
		out.write("\t<links");
		if (network.getCapacityPeriod() != Integer.MIN_VALUE) {
			out.write(" capperiod=\"" + Time.writeTime(network.getCapacityPeriod()) + "\"");
		}

		if (network instanceof NetworkImpl) {
			out.write(" effectivecellsize=\"" + ((NetworkImpl) network).getEffectiveCellSize() + "\"");
			out.write(" effectivelanewidth=\"" + network.getEffectiveLaneWidth() + "\"");
		}

		out.write(">\n");
	}

	@Override
	public void endLinks(final BufferedWriter out) throws IOException {
		out.write("\t</links>\n\n");
	}

	//////////////////////////////////////////////////////////////////////
	// <node ... > ... </node>
	//////////////////////////////////////////////////////////////////////

	@Override
	public void startNode(final Node node, final BufferedWriter out) throws IOException {
		out.write("\t\t<node");
		out.write(" id=\"" + node.getId() + "\"");
		out.write(" x=\"" + node.getCoord().getX() + "\"");
		out.write(" y=\"" + node.getCoord().getY() + "\"");
		if (node instanceof NodeImpl) {
			NodeImpl node2 = (NodeImpl) node;
			if (node2.getType() != null) {
				out.write(" type=\"" + node2.getType() + "\"");
			}
			if (node2.getOrigId() != null) {
				out.write(" origid=\"" + node2.getOrigId() + "\"");
			}
		}
		out.write(" />\n");
	}

	@Override
	public void endNode(final BufferedWriter out) throws IOException {
	}

	//////////////////////////////////////////////////////////////////////
	// <link ... > ... </link>
	//////////////////////////////////////////////////////////////////////

	private Set<String> lastSet = null;
	private String lastModes = null;
	
	@Override
	public void startLink(final Link link, final BufferedWriter out) throws IOException {
		out.write("\t\t<link");
		out.write(" id=\"" + link.getId() + "\"");
		out.write(" from=\"" + link.getFromNode().getId() + "\"");
		out.write(" to=\"" + link.getToNode().getId() + "\"");
		out.write(" length=\"" + link.getLength() + "\"");
		out.write(" freespeed=\"" + link.getFreespeed() + "\"");
		out.write(" capacity=\"" + link.getCapacity() + "\"");
		out.write(" permlanes=\"" + link.getNumberOfLanes() + "\"");
		out.write(" oneway=\"1\"");

		Set<String> modes = link.getAllowedModes();
		if (modes != null) {
			if (modes != this.lastSet) { // default LinkImpl internally caches the modes-set, thus the != operator works indeed
				StringBuilder buffer = new StringBuilder();
				int counter = 0;
				for (String mode : modes) {
					if (counter > 0) {
						buffer.append(',');
					}
					buffer.append(mode);
					counter++;
				}
				this.lastModes = buffer.toString();
				this.lastSet = modes;
			}
			out.write(" modes=\"" + this.lastModes + "\"");
		}

		if (link instanceof LinkImpl) {
			LinkImpl li = (LinkImpl) link;
			if (li.getOrigId() != null) {
				out.write(" origid=\"" + li.getOrigId() + "\"");
			}
			if (li.getType() != null) {
				out.write(" type=\"" + li.getType() + "\"");
			}
		}
		out.write(" />\n");
	}

	@Override
	public void endLink(final BufferedWriter out) throws IOException {
	}

	//////////////////////////////////////////////////////////////////////
	// <!-- ============ ... ========== -->
	//////////////////////////////////////////////////////////////////////

	@Override
	public void writeSeparator(final BufferedWriter out) throws IOException {
		out.write("<!-- =================================================" +
							"===================== -->\n\n");
	}
}
