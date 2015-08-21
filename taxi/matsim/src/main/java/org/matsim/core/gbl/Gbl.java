/* *********************************************************************** *
 * project: org.matsim.*
 * Gbl.java
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

package org.matsim.core.gbl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Some utility functions for dumping time and memory usage, and for logging.
 *
 */
public abstract class Gbl {
	
	private static final Logger log = Logger.getLogger(Gbl.class);
	
	public final static String ONLYONCE = " This message given only once.";
	
	public final static String FUTURE_SUPPRESSED = " Future occurences of this logging statement are suppressed." ;
	
	public final static String SEPARATOR = "****************************" ;

	public static final String CREATE_ROUTING_ALGORITHM_WARNING_MESSAGE = "This class wants to overwrite createRoutingAlgorithm(), which is no longer possible.  Making createRoutingAlgorithm() non-final would not help since, after recent code changes, it is only used during initialization but not in replanning.  kai, may'13.  Aborting ...";
	
	public static final void printMemoryUsage() {
		long totalMem = Runtime.getRuntime().totalMemory();
		long freeMem = Runtime.getRuntime().freeMemory();
		long usedMem = totalMem - freeMem;
		log.info("used RAM: " + usedMem + "B = " + (usedMem/1024) + "kB = " + (usedMem/1024/1024) + "MB" +
				"  free: " + freeMem + "B = " + (freeMem/1024/1024) + "MB  total: " + totalMem + "B = " + (totalMem/1024/1024) + "MB");
	}

	public static final void printSystemInfo() {
		log.info("JVM: " + System.getProperty("java.version") + "; "
				+ System.getProperty("java.vm.vendor") + "; "
				+ System.getProperty("java.vm.info") + "; "
				+ System.getProperty("sun.arch.data.model") + "-bit");
		log.info("OS: " + System.getProperty("os.name") + "; "
				+ System.getProperty("os.version") + "; "
				+ System.getProperty("os.arch"));
		log.info("CPU cores: " + Runtime.getRuntime().availableProcessors());
		log.info("max. Memory: " + Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 + "MB (" + Runtime.getRuntime().maxMemory() + "B)");
	}
	
	public static final String getBuildInfoString() {
		return getBuildInfoString("MATSim", "/revision.txt");
	}
	
	/** 
	 * Prints some information about the current build/revision of this code.
	 * Currently, this will only work with the Nightly-Build-Jars.
	 */
	public static final String getBuildInfoString(String component, String resourceFilename) {
		String revision = null;
		String date = null;
		URL url = Gbl.class.getResource(resourceFilename);
		if (url != null) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
				revision = reader.readLine();
				date = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (revision == null) {
				return component + "-Build: unknown";
			} else {
				return component + "-Build: " + revision + " (" + date + ")";
			}
		} else {
			return component + "-Build: unknown";
		}
	}
	
	public static final void printBuildInfo() {
		printBuildInfo("MATSim", "/revision.txt");
	}

	public static final void printBuildInfo(String component, String resourceFilename) {
		String infoString = getBuildInfoString(component, resourceFilename);
		log.info(infoString);
	}

	//////////////////////////////////////////////////////////////////////
	// time measurement
	//////////////////////////////////////////////////////////////////////

	private static long measurementStartTime = Long.MAX_VALUE;

	private static final String printTime() {
		if (Gbl.measurementStartTime == Long.MAX_VALUE) {
			log.error("Did not start measurements.");
			return "";
		}
		return printTimeDiff(System.currentTimeMillis(), Gbl.measurementStartTime);
	}

	private static final String printTimeDiff(final long later, final long earlier) {
		long elapsedTimeMillis = later - earlier;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		float elapsedTimeMin = elapsedTimeMillis/(60*1000F);
		float elapsedTimeHour = elapsedTimeMillis/(60*60*1000F);
		float elapsedTimeDay = elapsedTimeMillis/(24*60*60*1000F);

		return elapsedTimeMillis + " msecs; " +
			elapsedTimeSec    + " secs; " +
			elapsedTimeMin    + " mins; " +
			elapsedTimeHour   + " hours; " +
			elapsedTimeDay    + " days ###";
	}

	public static final void startMeasurement() {
		Gbl.measurementStartTime = System.currentTimeMillis();
	}

	public static final void printElapsedTime() {
		log.info("### elapsed time: " + Gbl.printTime());
	}

	public static final void printRoundTime() {
		log.info("### round time: " + Gbl.printTime());
		Gbl.startMeasurement();
	}

	//////////////////////////////////////////////////////////////////////
	// thread performance
	//////////////////////////////////////////////////////////////////////

	private final static ThreadMXBean tbe = ManagementFactory.getThreadMXBean();

	/**
	 * Tries to enable CPU time measurement for threads. Not all JVMs support this feature.
	 *
	 * @return <code>true</code> if the JVM supports time measurement for threads and the feature
	 * could be enabled, <code>false</code> otherwise.
	 */
	public static final boolean enableThreadCpuTimeMeasurement() {
		if (tbe.isThreadCpuTimeSupported()) {
			tbe.setThreadCpuTimeEnabled(true);
			return true;
		}
		return false;
	}

	/**
	 * @param thread
	 * @return cpu time for the given thread in seconds, <code>-1</code> if cpu time is not measured.
	 */
	public static final double getThreadCpuTime(final Thread thread) {
		if (tbe.isThreadCpuTimeEnabled()) {
			return tbe.getThreadCpuTime(thread.getId()) / 1.0e9;
		}
		return -1;
	}

	/**
	 * Prints the cpu time for the given thread, i.e. the time the thread was effectively active on the CPU.
	 *
	 * @param thread
	 */
	public static final void printThreadCpuTime(final Thread thread) {
		if (tbe.isThreadCpuTimeEnabled()) {
			log.info("Thread performance: Thread=" + thread.getName() + "  cpu-time=" + getThreadCpuTime(thread) + "sec");
		}
	}

	/**
	 * Prints the cpu time for the current thread, i.e. the time the current thread was effectively active on the CPU.
	 */
	public static final void printCurrentThreadCpuTime() {
		printThreadCpuTime(Thread.currentThread());
	}
	
}
