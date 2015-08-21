package org.matsim.contrib.freight.carrier;

import org.matsim.api.core.v01.Id;

/**
 * 
 * 
 * @author sschroeder
 *
 */
public class CarrierVehicle {

	/**
	 * Returns a new instance of carrierVehicle.
	 * 
	 * The default values for other fields (being implicitly set) are [capacity=0][earliestStart=0.0][latestEnd=Integer.MaxValue()].
	 * 
	 * @param vehicleId
	 * @param locationId
	 * @return CarrierVehicle
	 * @see CarrierVehicle
	 */
	public static CarrierVehicle newInstance(Id vehicleId, Id locationId){
		return new CarrierVehicle(vehicleId,locationId);
	}

	/**
	 * Builder to build vehicles.
	 * 
	 * @author sschroeder
	 *
	 */
	public static class Builder {
		
		/**
		 * Returns a builder with vehicleId and locationId.
		 * 
		 * The default values for other fields (being implicitly set) are [capacity=0][earliestStart=0.0][latestEnd=Integer.MaxValue()].
		 * 
		 * @param vehicleId
		 * @param locationId
		 * @return a new vehicle builder
		 */
		public static Builder newInstance(Id vehicleId, Id locationId){
			return new Builder(vehicleId,locationId);
		}
		
		private Id locationId;
		private Id vehicleId;
		private CarrierVehicleType type;
		private Id typeId;
		private double earliestStart = 0.0;
		private double latestEnd = Integer.MAX_VALUE;
		
		
		public Builder(Id vehicleId, Id locationId){
			this.locationId = locationId;
			this.vehicleId = vehicleId;
		}
		
		public Builder setType(CarrierVehicleType type){
			this.type=type;
			return this;
		}
		
		
		public Builder setTypeId(Id typeId){
			this.typeId = typeId;
			return this;
		}
		
		
		public Builder setEarliestStart(double earliestStart){
			this.earliestStart=earliestStart;
			return this;
		}
		
		public Builder setLatestEnd(double latestEnd){
			this.latestEnd = latestEnd;
			return this;
		}
		
		public CarrierVehicle build(){
			return new CarrierVehicle(this);
		}
	}
	
	private final Id locationId;

	private final Id vehicleId;
	
	private Id typeId;

	private CarrierVehicleType vehicleType;

	private double earliestStartTime;

	private double latestEndTime;

	private CarrierVehicle(final Id vehicleId, final Id location) {
		this.vehicleId = vehicleId;
		this.locationId = location;
		earliestStartTime = 0.0;
		latestEndTime = Integer.MAX_VALUE;
	}
	
	private CarrierVehicle(Builder builder){
		vehicleId = builder.vehicleId;
		locationId = builder.locationId;
		vehicleType = builder.type;
		earliestStartTime = builder.earliestStart;
		latestEndTime = builder.latestEnd;
		typeId = builder.typeId;
	}

	public Id getLocation() {
		return locationId;
	}

	public Id getVehicleId() {
		return vehicleId;
	}
	
	@Override
	public String toString() {
		return vehicleId + " stationed at " + locationId;
	}

	public CarrierVehicleType getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(CarrierVehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}


	/**
	 * Returns the earliest time a vehicle can be deployed (and thus can departure from its origin).
	 * 
	 * The default value is 0.0;
	 * 
	 * @return the earliest start time
	 */
	public double getEarliestStartTime() {
		return earliestStartTime;
	}

	/**
	 * Returns the latest time a vehicle has to be back in the depot (and thus has to arrive at its final destination).
	 * 
	 * The default value is Integer.MaxValue().
	 * 
	 * @return latest arrival time
	 */
	public double getLatestEndTime() {
		return latestEndTime;
	}

	
	Id getVehicleTypeId() {
		return typeId;
	}

}