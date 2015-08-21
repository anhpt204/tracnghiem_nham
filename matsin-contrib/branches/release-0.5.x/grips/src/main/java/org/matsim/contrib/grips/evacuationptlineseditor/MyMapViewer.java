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

package org.matsim.contrib.grips.evacuationptlineseditor;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.grips.config.ToolConfig;
import org.matsim.core.gbl.MatsimResource;
import org.matsim.core.network.LinkQuadTree;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

public class MyMapViewer extends JXMapViewer implements MouseListener, MouseWheelListener, KeyListener, MouseMotionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final boolean editMode = false;
	private boolean freezeMode = false;


	private final MouseListener m [];
	private final MouseMotionListener mm [];
	private final MouseWheelListener mw [];
	private final KeyListener k [];




	private LinkQuadTree links;

	private Point currentMousePosition = null;

	private final GeotoolsTransformation ct;
	private final GeotoolsTransformation ctInverse;

	private final ArrayList<Link> currentHoverLinks;


	private final EvacuationPTLinesEditor evacSel;

	private final ConcurrentHashMap<Id, Tuple<GeoPosition,GeoPosition>> busStops = new ConcurrentHashMap<Id, Tuple<GeoPosition,GeoPosition>>();

	private final BufferedImage image;

	private Polygon areaPolygon;

	public MyMapViewer(EvacuationPTLinesEditor evacuationPTLinesEditor) {
		super();
		this.m = super.getMouseListeners();
		for (MouseListener l : this.m) {
			super.removeMouseListener(l);
		}
		this.mm = super.getMouseMotionListeners();
		for (MouseMotionListener m : this.mm) {
			super.removeMouseMotionListener(m);
		}
		this.mw = super.getMouseWheelListeners();
		for (MouseWheelListener mw : this.mw) {
			super.removeMouseWheelListener(mw);
		}
		this.k = super.getKeyListeners();
		for (KeyListener k : this.k) {
			super.removeKeyListener(k);
		}

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addKeyListener(this);

		this.evacSel = evacuationPTLinesEditor;

		this.ct = new GeotoolsTransformation("EPSG:4326",this.evacSel.getScenario().getConfig().global().getCoordinateSystem());
		this.ctInverse = new GeotoolsTransformation(this.evacSel.getScenario().getConfig().global().getCoordinateSystem(),"EPSG:4326");
		createNetworkLinks();
		this.currentHoverLinks = new ArrayList<Link>();
		
		try {
			this.image =  ImageIO.read(MatsimResource.getAsInputStream("grips/bus_stop.png"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private void createNetworkLinks(){

		Envelope e = null;
		for (Node n : this.evacSel.getScenario().getNetwork().getNodes().values()) {
			Coord c = n.getCoord();
			if (e == null) {
				e = new Envelope();
			}
			e.expandToInclude(c.getX(), c.getY());
		}

		this.links = new LinkQuadTree(e.getMinX(),e.getMinY(),e.getMaxX(),e.getMaxY());

		NetworkImpl net = (NetworkImpl) this.evacSel.getScenario().getNetwork();

		for (Link link: net.getLinks().values()){
			if (link.getId().toString().contains("el")) {
				continue;
			}
			this.links.put(link);

		}

	}

	@Override
	public void mouseClicked(MouseEvent e)
	{

		//if left mouse button was clicked
		if (e.getButton() == MouseEvent.BUTTON1){
			//if edit mode is off
			if (!this.editMode){
				//if there was no prior selection
				if (!this.freezeMode){
					//activate edition mode (in gui)
					this.evacSel.setEditMode(true);

					if ((this.currentHoverLinks!=null) && (this.currentHoverLinks.size()>0))
					{
						//links are being selected. Freeze the selection
						this.freezeMode = true;

						//give gui the id of the first selected link
						this.evacSel.setLink1Id(this.currentHoverLinks.get(0).getId());

						//if there are more then just one link in hover
						if (this.currentHoverLinks.size()>1)
						{
							//give gui the second selection link
							this.evacSel.setLink2Id(this.currentHoverLinks.get(1).getId());
						}
						else
							//make sure the second link is null then
							this.evacSel.setLink2Id(null); 

					}
					else
					{
						//if nothing is selected, set them null
						this.evacSel.setLink1Id(null);
						this.evacSel.setLink2Id(null);						
					}
				} else {
					this.evacSel.setEditMode(false);
					this.freezeMode = false;
				}
			}
		}


	}


	@Override
	public void mouseEntered(MouseEvent e) {
		if (!this.editMode) {
			for (MouseListener m : this.m) {
				m.mouseEntered(e);
			}
		}

	}


	@Override
	public void mouseExited(MouseEvent e) {
		if (!this.editMode) {
			for (MouseListener m : this.m) {
				m.mouseExited(e);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {


		if (!this.editMode) {
			for (MouseListener m : this.m) {
				m.mousePressed(e);
			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (!this.editMode)
		{
			for (MouseListener m : this.m)
			{
				m.mouseReleased(e);
			}
		}


		if (e.getButton() == MouseEvent.BUTTON1)
			repaint();

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!this.editMode) {
			for (MouseWheelListener m : this.mw) {
				m.mouseWheelMoved(e);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!this.editMode) {
			for (KeyListener k : this.k) {
				k.keyPressed(e);
			}
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!this.editMode) {
			for (KeyListener k : this.k) {
				k.keyReleased(e);
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (!this.editMode) {
			for (KeyListener k : this.k) {
				k.keyTyped(e);
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0)
	{
		if (!this.editMode)
		{
			for (MouseMotionListener m : this.mm)
			{
				m.mouseDragged(arg0);
			}
		}
		else
			repaint();

	}

	@Override
	public void mouseMoved(MouseEvent arg0)
	{

		if (!this.editMode)
		{

			this.currentMousePosition = new Point(arg0.getX(), arg0.getY());
			repaint();

			for (MouseMotionListener m : this.mm)
			{
				m.mouseMoved(arg0);
			}
		}		
	}

	@Override
	public void paint(Graphics g){
		//paint map and links
		super.paint(g);
		{
			Graphics2D g2D = (Graphics2D) g;     
			g2D.setStroke(new BasicStroke(3F));
			
			//get viewport offset
			Rectangle b = this.getViewportBounds();

			//draw area polygon
			if (areaPolygon == null)
				areaPolygon = this.evacSel.getAreaPolygon();
			
			if (areaPolygon != null)
			{
				g.setColor(ToolConfig.COLOR_EVAC_AREA_BORDER);
				
				int [] x = new int[areaPolygon.getExteriorRing().getNumPoints()];
				int [] y = new int[areaPolygon.getExteriorRing().getNumPoints()];
				for (int i = 0; i < areaPolygon.getExteriorRing().getNumPoints(); i++) {
					Coordinate c = areaPolygon.getExteriorRing().getCoordinateN(i);
					Point2D wldPoint = this.getTileFactory().geoToPixel(new GeoPosition(c.y,c.x), this.getZoom());
					x[i] = (int) (wldPoint.getX()-b.x);
					y[i] = (int) (wldPoint.getY()-b.y);
					if (i > 0) {
						g.drawLine(x[i-1], y[i-1], x[i], y[i]);
					}
				}
				g.setColor(ToolConfig.COLOR_EVAC_AREA);
				g.fillPolygon(x, y, areaPolygon.getExteriorRing().getNumPoints());
			}
			
			g.setColor(Color.black);



			if ((!this.freezeMode)&&(this.currentHoverLinks.size()>0))
				this.currentHoverLinks.clear();

			GeoPosition wPoint = null;

			//get geo mouseposition
			if (this.currentMousePosition!=null){
				Point wldPoint = new Point(this.currentMousePosition.x+b.x,this.currentMousePosition.y+b.y);
				wPoint = this.getTileFactory().pixelToGeo(wldPoint, this.getZoom());


				Coord wCoord = new CoordImpl(wPoint.getLongitude(), wPoint.getLatitude());
				wCoord = this.ct.transform(wCoord);

				//			System.out.println(wCoord);
				//go through all links, draw them and check if the mouse cursor is nearby (for highlighting)
				//			Collection<Coord[]> collection = this.links.get(wCoord.getX(),wCoord.getY(), 1000);
				Link l = this.links.getNearest(wCoord.getX(),wCoord.getY());

				Coord from = this.ctInverse.transform(l.getFromNode().getCoord());
				Coord to = this.ctInverse.transform(l.getToNode().getCoord());

				Point2D from2D = this.getTileFactory().geoToPixel(new GeoPosition(from.getY(),from.getX()), this.getZoom());
				Point2D to2D = this.getTileFactory().geoToPixel(new GeoPosition(to.getY(),to.getX()), this.getZoom());

				int x1 = (int) (from2D.getX()-b.x);
				int y1 = (int) (from2D.getY()-b.y);
				int x2 = (int) (to2D.getX()-b.x);
				int y2 = (int) (to2D.getY()-b.y);

				g2D.setStroke(new BasicStroke(3F));

				g.setColor(ToolConfig.COLOR_HOVER);

				int x = (x2-x1);
				int y = (y2-y1);

				//check for nearby links (mouse cursor) 
				if (wPoint!=null){
					int mouseX = this.currentMousePosition.x;
					int mouseY = this.currentMousePosition.y;

					int maxX = Math.max(x2,x1);
					int maxY = Math.max(y2,y1);

					int minX = Math.min(x2,x1);
					int minY = Math.min(y2,y1);

					if ((mouseX <= maxX) && (mouseX >= minX) && (mouseY <= maxY) && (mouseY >= minY)){
						float r1 = ((float)mouseX - (float)x1) / x;
						float r2 = ((float)mouseY - (float)y1) / y;

						//if cursor is nearby, draw roads in another color
						if ((r1 - 3f < r2) && (r1 + 3f > r2))	{
							if ((!this.freezeMode) && (this.currentHoverLinks.size()<2)) {
								this.currentHoverLinks.add(l);
								for (Link revL : l.getToNode().getOutLinks().values()) {
									if (revL.getToNode() == l.getFromNode()) {
										this.currentHoverLinks.add(revL);
										break;
									}
								}

							}
							g2D.setStroke(new BasicStroke(8F));
							g.setColor(ToolConfig.COLOR_HOVER);
						}

					}

				}

				//draw link/road if its not a selected one 
				if ((!this.freezeMode)||(!this.currentHoverLinks.contains(l))) {
					g.drawLine(x1,y1,x2,y2);
				}
				g.setColor(Color.CYAN);
			}

			//display selected roads (with arrows)
			if ((this.freezeMode)&&(this.currentHoverLinks.size()>0))
			{
				g2D.setStroke(new BasicStroke(5F));

				//for each hover link
				for (int i = 0; i<this.currentHoverLinks.size();i++)				{
					//get the from & to nodes
					Link l = this.currentHoverLinks.get(i);

					Coord from = l.getFromNode().getCoord();
					Coord to = l.getToNode().getCoord();

					double length = Math.hypot(from.getX()-to.getX(), from.getY()-to.getY());
					double dx = to.getX() - from.getX();
					double dy = to.getY() - from.getY();
					double nX = dx/length;
					double nY = dy/length;

					//shift arrow 10% of the link length to the left
					double rightShiftX = nY * .1 * length;
					double rightShiftY = -nX * .1 * length;

					//from-to arrow
					double fXA = from.getX() + rightShiftX;
					double fYA = from.getY() + rightShiftY;
					double tXA = to.getX() + rightShiftX;
					double tYA = to.getY() + rightShiftY;
					//arrow peak is 10% of link length long;
					double leftPeakEndX = tXA-.1*length*nX + -nY * .1 * length;
					double leftPeakEndY = tYA-.1*length*nY + nX * .1 * length;
					double rightPeakEndX = tXA-.1*length*nX - -nY * .1 * length;
					double rightPeakEndY = tYA-.1*length*nY - nX * .1 * length;


					Coord tmp = this.ctInverse.transform(from);
					Point2D from2D = this.getTileFactory().geoToPixel(new GeoPosition(tmp.getY(),tmp.getX()), this.getZoom());

					tmp = this.ctInverse.transform(to);
					Point2D to2D = this.getTileFactory().geoToPixel(new GeoPosition(tmp.getY(),tmp.getX()), this.getZoom());
					int x1 = (int) (from2D.getX()-b.x);
					int y1 = (int) (from2D.getY()-b.y);
					int x2 = (int) (to2D.getX()-b.x);
					int y2 = (int) (to2D.getY()-b.y);


					tmp = this.ctInverse.transform(new CoordImpl(fXA,fYA));
					Point2D arrowFrom2D = this.getTileFactory().geoToPixel(new GeoPosition(tmp.getY(),tmp.getX()), this.getZoom());
					int ax1 = (int) (arrowFrom2D.getX()-b.x);
					int ay1 = (int) (arrowFrom2D.getY()-b.y);

					tmp = this.ctInverse.transform(new CoordImpl(tXA,tYA));
					Point2D arrowTo2D = this.getTileFactory().geoToPixel(new GeoPosition(tmp.getY(),tmp.getX()), this.getZoom());
					int ax2 = (int) (arrowTo2D.getX()-b.x);
					int ay2 = (int) (arrowTo2D.getY()-b.y);


					tmp = this.ctInverse.transform(new CoordImpl(leftPeakEndX,leftPeakEndY));
					Point2D arrowLeftPeakEnd = this.getTileFactory().geoToPixel(new GeoPosition(tmp.getY(),tmp.getX()), this.getZoom());
					int alx = (int) (arrowLeftPeakEnd.getX()-b.x);
					int aly = (int) (arrowLeftPeakEnd.getY()-b.y);

					tmp = this.ctInverse.transform(new CoordImpl(rightPeakEndX,rightPeakEndY));
					Point2D arrowRightPeakEnd = this.getTileFactory().geoToPixel(new GeoPosition(tmp.getY(),tmp.getX()), this.getZoom());
					int arx = (int) (arrowRightPeakEnd.getX()-b.x);
					int ary = (int) (arrowRightPeakEnd.getY()-b.y);


					g.setColor(ToolConfig.COLOR_ROAD_SELECTED);
					g.drawLine(x1,y1,x2,y2);

					//give each arrow a different color
					if (i ==0 ) {
						g.setColor(ToolConfig.COLOR_ROAD_1);
					} else {
						g.setColor(ToolConfig.COLOR_ROAD_2);
					}
					g.drawLine(ax1, ay1, ax2, ay2);
					g.drawLine(ax2,ay2,alx,aly);
					g.drawLine(ax2,ay2,arx,ary);


				}
			}


			for (Tuple<GeoPosition,GeoPosition> stop : this.busStops.values()) {
				g.setColor(new Color(0,255,0));
				Point2D topLeftPix = this.getTileFactory().geoToPixel(stop.getFirst(), this.getZoom());
				Point2D bottomRightPix = this.getTileFactory().geoToPixel(stop.getSecond(), this.getZoom());
				double dx = topLeftPix.getX() - bottomRightPix.getX();
				double dy = topLeftPix.getY() - bottomRightPix.getY();
				int width = (int)(0.5+Math.hypot(dx, dy));
				
				g.drawImage(this.image, (int)(topLeftPix.getX()-b.x), (int)(topLeftPix.getY()-b.y),width,width,null);
			}
		}
	}

	public void addBusStop(Id id) {
		Link link = this.evacSel.getScenario().getNetwork().getLinks().get(id);
		double dX = (link.getToNode().getCoord().getX()-link.getFromNode().getCoord().getX());
		double dY = (link.getToNode().getCoord().getY()-link.getFromNode().getCoord().getY());
		double dist = Math.hypot(dX, dY);
		double dirX = dX/dist;
		double dirY = dY/dist;

		double size = 50;
		
		double x = (link.getToNode().getCoord().getX()+link.getFromNode().getCoord().getX())/2+0.8*size*dirY;
		double y = (link.getToNode().getCoord().getY()+link.getFromNode().getCoord().getY())/2-0.8*size*dirX;
		Coord topLeft = new CoordImpl(x-size/2,y+size/2);
		Coord bottomRight = new CoordImpl(x+size/2,y-size/2);
		
		Coord transformed = this.ctInverse.transform(topLeft);
		GeoPosition posTopLeft = new GeoPosition(transformed.getY(),transformed.getX());
		
		transformed = this.ctInverse.transform(bottomRight);
		GeoPosition posBottomRight = new GeoPosition(transformed.getY(),transformed.getX());
		this.busStops.put(id, new Tuple<GeoPosition,GeoPosition>(posTopLeft,posBottomRight));
		repaint();

	}

}
