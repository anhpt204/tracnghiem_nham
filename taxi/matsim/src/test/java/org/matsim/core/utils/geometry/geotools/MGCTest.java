package org.matsim.core.utils.geometry.geotools;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.testcases.MatsimTestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author laemmel
 *
 */
public class MGCTest extends MatsimTestCase {

	public void testCoord2CoordinateAndViceVersa(){
		double x = 123.456789;
		double y = 987.654321;
		double delta = 0.0000001;
		Coord coord1 = new CoordImpl(x,y);
		Coordinate coord2 = MGC.coord2Coordinate(coord1);
		Coord coord3 = MGC.coordinate2Coord(coord2);
		double x1 = coord3.getX();
		double y1 = coord3.getY();
		junit.framework.Assert.assertEquals(x,x1,delta);
		junit.framework.Assert.assertEquals(y,y1,delta);
	}
	
	public void testCoord2PointAndViceVersa(){
		double x = 123.456789;
		double y = 987.654321;
		double delta = 0.0000001;
		Coord coord1 = new CoordImpl(x,y);
		Point p = MGC.coord2Point(coord1);
		Coord coord2 = MGC.point2Coord(p);
		double x1 = coord2.getX();
		double y1 = coord2.getY();
		junit.framework.Assert.assertEquals(x,x1,delta);
		junit.framework.Assert.assertEquals(y,y1,delta);

	}
	
	public void testGetCRS(){
		// CH1903_LV03 Id
		junit.framework.Assert.assertNotNull(MGC.getCRS("EPSG:21781"));

		try {
			MGC.getCRS("");
			junit.framework.Assert.fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) { }

		// unknown EPSG Id
		try {
			MGC.getCRS("EPSG:MATSim");
			junit.framework.Assert.fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) { }

	}
}
