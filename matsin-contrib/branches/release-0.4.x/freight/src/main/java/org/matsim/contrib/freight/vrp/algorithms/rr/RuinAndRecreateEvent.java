/*******************************************************************************
 * Copyright (C) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.matsim.contrib.freight.vrp.algorithms.rr;

import java.util.Collection;

import org.matsim.contrib.freight.vrp.basics.Tour;



/**
 * Collector of algo-information. Can be listened to.
 * 
 * @author stefan schroeder
 *
 */

public class RuinAndRecreateEvent {
	
	private int currentMutation;
	
	private double tentativeSolution;
	
	private double currentResult;
	
	private double threshold;
	
	private boolean solutionAccepted;
	
	private Collection<org.matsim.contrib.freight.vrp.basics.Tour> currentSolution;

	public RuinAndRecreateEvent(int currentMutation, double tentativeSolution,
			double currentResult, double currentThreshold, boolean solutionAccepted) {
		super();
		this.currentMutation = currentMutation;
		this.tentativeSolution = tentativeSolution;
		this.threshold = currentThreshold;
		this.solutionAccepted = solutionAccepted;
		this.currentResult = currentResult;
	}

	public Collection<org.matsim.contrib.freight.vrp.basics.Tour> getCurrentSolution() {
		return currentSolution;
	}

	public void setCurrentSolution(Collection<Tour> currentSolution) {
		this.currentSolution = currentSolution;
	}

	public double getCurrentResult() {
		return currentResult;
	}

	public int getCurrentMutation() {
		return currentMutation;
	}

	public double getTentativeSolution() {
		return tentativeSolution;
	}

	public double getThreshold() {
		return threshold;
	}

	public boolean isSolutionAccepted() {
		return solutionAccepted;
	}
	
	@Override
	public String toString() {
		return "[currentMutation=" + currentMutation + "][tentativeSolution=" + tentativeSolution + "][currentSolution=" + currentResult + "][currentThreshold=" + threshold + "][isAccepted=" + solutionAccepted + "]";
	}

}
