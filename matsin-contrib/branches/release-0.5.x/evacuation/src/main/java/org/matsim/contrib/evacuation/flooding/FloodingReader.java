/* *********************************************************************** *
 * project: org.matsim.*
 * FloodingReader.java
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
package org.matsim.contrib.evacuation.flooding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.Index1D;
import ucar.ma2.Index2D;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.IOServiceProvider;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class FloodingReader {
	
	private static final Logger log = Logger.getLogger(FloodingReader.class);
	
	private static final double FLOODING_TRESHOLD = 0.05;

	
//	double offsetEast = 632968.461027224;
//	double offsetNorth = 9880201.726;
	
	double offsetEast = 0;
	double offsetNorth = 0;
	
	private NetcdfFile ncfile;

	private List<FloodingInfo> fis;

	private Envelope envelope;
	
	private boolean initialized = false;
	
	private boolean readTriangles = false;

	private List<int[]> triangles;
	private Map<Integer,Integer> idxMapping;

	private Geometry floodingArea = null;

	private GeometryFactory geofac;
	
	private int maxTimeStep = Integer.MAX_VALUE;

	private boolean readFloodingSeries = false;
	
	public FloodingReader(String netcdf) {
		try {
			this.ncfile = NetcdfFile.open(netcdf);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void readFile() throws IOException, InvalidRangeException {
		this.fis = new ArrayList<FloodingInfo>();
		this.envelope = new Envelope();
		
		
		log.info("initializing netcdf");
		

		
		IOServiceProvider ios =  this.ncfile.getIosp();
		Section sX = new Section();
		Variable varX = this.ncfile.findVariable("x");
		sX.appendRange(varX.getRanges().get(0));
		Array aX = ios.readData(varX, sX);
		
		Section sY = new Section();
		Variable varY = this.ncfile.findVariable("y");
		sY.appendRange(varY.getRanges().get(0));
		Array aY = ios.readData(varY, sY);

		Section sZ = new Section();
		Variable varZ = this.ncfile.findVariable("elevation");
		sZ.appendRange(varZ.getRanges().get(0));
		Array aZ = ios.readData(varZ, sY);
		
		Section sStage = new Section();
		Variable varStage = this.ncfile.findVariable("stage");
		sStage.appendRange(varStage.getRanges().get(0));
		sStage.appendRange(varStage.getRanges().get(1));
		Array aStage = ios.readData(varStage, sStage);
		
		if (this.readTriangles) {
			this.triangles = new ArrayList<int []>();
			this.idxMapping = new HashMap<Integer, Integer>();
			Section tri = new Section();
			Variable varTri = this.ncfile.findVariable("volumes");
			tri.appendRange(varTri.getRanges().get(0));
			tri.appendRange(varTri.getRanges().get(1));
			Array aTri = ios.readData(varTri, tri);	
			Index idxTri = new Index2D(tri.getShape());
			for (int i = 0; i < idxTri.getShape()[0]; i++) {
				int [] tripple = new int [3];
				idxTri.set(i, 0);
				for (int j = 0; j < idxTri.getShape()[1]; j++) {
					idxTri.set1(j);
					tripple[j] = aTri.getInt(idxTri);
				}
				this.triangles.add(tripple);
			}
		}
		
		Index idxStage = new Index2D(aStage.getShape());
		Index idx = new Index1D(aX.getShape());
		
		log.info("finished init.");
		
		log.info("found " + idx.getSize() + " coordinates");
		
		int next = 0;
		for (int i = 0; i < idx.getSize(); i++) {
			if (i  >= next){
				log.info(i + " coordinates processed.");
				next = i*2;
			}
			idx.set(i);
			idxStage.set(0,i);
			double x = aX.getDouble(idx) + this.offsetEast;
			double y = aY.getDouble(idx) + this.offsetNorth;
			double z = aZ.getDouble(idx);
			Coordinate c = new Coordinate(x,y,z);
			this.envelope.expandToInclude(c);
			
//			//offshore coord
//			if (z < 0  && (this.floodingArea == null || !this.floodingArea.contains(this.geofac.createPoint(new Coordinate(x, y))))) {
//				continue;
//			}
			
			if (this.floodingArea != null && !this.floodingArea.contains(this.geofac.createPoint(new Coordinate(x, y)))) {
				continue;
			}
			c.z = c.z < aStage.getFloat(idxStage) ? aStage.getFloat(idxStage)-0.6 : c.z;
			
			FloodingInfo flooding = processCoord(idxStage,aStage,c);
			if (flooding != null) {
				this.fis.add(flooding);
				if (this.readTriangles) {
					this.idxMapping.put(i, this.fis.size()-1);
				}
			}
		}
		this.initialized = true;
	}
	
	public List<FloodingInfo> getFloodingInfos() {
		if(!this.initialized) {
			try {
				readFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (InvalidRangeException e) {
				throw new RuntimeException(e);
			}
		}
		return this.fis;
	}
	
	public Envelope getEnvelope() {
		if(!this.initialized) {
			try {
				readFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (InvalidRangeException e) {
				throw new RuntimeException(e);
			}
		}
		return this.envelope;
	}
	
	public List<int[]> getTriangles() {
		if (!this.readTriangles && this.initialized) {
			throw new RuntimeException("Netcdf already initialized, could not read triangle. To initialize netcdf with triangles reading enabled, " +
					"please call method setReadTriangles(true) before initialization!");
		} else if (!this.initialized) {
			this.readTriangles = true;
			try {
				readFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (InvalidRangeException e) {
				throw new RuntimeException(e);
			}
		}
		return this.triangles;
	}
	
	
	public void setReadTriangles(boolean readTriangles) {
		if (this.initialized) {
			throw new RuntimeException("Netcdf already initialized, could not enable read triangle. To initialize netcdf with read triangles enabled, " +
			"please call this method before netcdf initialization!");
		}		
		this.readTriangles = readTriangles;
	}
	
	public void setMaxTimeStep(int maxTimeStep) {
		if (this.initialized) {
			throw new RuntimeException("Netcdf already initialized, could not set max time step. To initialize netcdf with max time step, " +
			"please call this method before netcdf initialization!");
		}		
		this.maxTimeStep = maxTimeStep;
	}
	
	
	public void setFloodingArea(Geometry geo) {
		if (this.initialized) {
			throw new RuntimeException("Netcdf already initialized, could not set flooding area. To initialize netcdf with flooding area geomtry, " +
			"please call this method before netcdf initialization!");
		}
		this.floodingArea = geo;
		this.geofac = new GeometryFactory();
	}

	
	public void setOffset(double offsetEast, double offsetNorth) {
		if (this.initialized) {
			throw new RuntimeException("Netcdf already initialized, could not set offset. To initialize netcdf with offset, " +
			"please call this method before netcdf initialization!");
		}
		this.offsetEast = offsetEast;
		this.offsetNorth = offsetNorth;
	}
	
	public void setReadFloodingSeries(boolean readFloodingSeries) {
		if (this.initialized) {
			throw new RuntimeException("Netcdf already initialized, could not enable read flooding series. To initialize netcdf with read flooding series enabled, " +
			"please call this method before netcdf initialization!");
		}
		this.readFloodingSeries  = readFloodingSeries;
	}

//	public void resetCache() {
//		this.triangles = null;
//		this.idxMapping = null;
//		this.fis = null;
//		this.initialized = false;
//		log.info("Cache has been reseted");
//	}
	
	/**
	 * Mapping from coordinate index from triangle list to flooding info index
	 * @return Map<Integer cordIdx, Integer fiIdx>
	 */
	public Map<Integer,Integer> getIdxMapping() {
		if (!this.readTriangles && this.initialized) {
			throw new RuntimeException("Netcdf already initialized, could not read triangle. To initialize netcdf with triangles reading enabled, " +
					"please use the corresponding contructor (FLoodingReader(file,true)!");
		} else if (!this.initialized) {
			this.readTriangles = true;
			try {
				readFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (InvalidRangeException e) {
				throw new RuntimeException(e);
			}
		} else if (!this.initialized){
			try {
				readFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (InvalidRangeException e) {
				throw new RuntimeException(e);
			}
		}
		return this.idxMapping;
	}
	
	private FloodingInfo processCoord(Index idxStage, Array stage, Coordinate c) {
		FloodingInfo flooding = null;
		List<Double> stages = new ArrayList<Double>();
		
		double time = -1;
		for (int i = 0 ; i < idxStage.getShape()[0]; i++) {
			if (i > this.maxTimeStep) {
				break;
			}
			idxStage.set0(i);
			double tmp = stage.getFloat(idxStage);
//			stages.add((tmp - c.z));
			if ((tmp - c.z) > FLOODING_TRESHOLD) {
				if (time == -1) {
					time = i;
				}
				stages.add(tmp - c.z);	
			} else {
				stages.add(0.);
			}
		}
		if (time != -1) {
			if (!this.readFloodingSeries){
				flooding = new FloodingInfo(c,null,time);	
			} else {
				flooding = new FloodingInfo(c,stages,time);
			}
			
		} 
		return flooding;
	}


	
	

}
