package ktuan;

public class ParcelRequestDungPQ {

	int id;
	int time_call;
	int pickup_point;
	int delivery_point;
	int early_pickup_time;
	int late_pickup_time;
	int early_delivery_time;
	int late_delivery_time;

	public ParcelRequestDungPQ(int id, int time_call, int pickup_point,
			int delivery_point, int early_pickup_time, int late_pickup_time,
			int early_delivery_time, int late_delivery_time) {

		super();
		this.id = id;
		this.time_call = time_call;
		this.pickup_point = pickup_point;
		this.delivery_point = delivery_point;
		this.early_pickup_time = early_pickup_time;
		this.late_pickup_time = late_pickup_time;
		this.early_delivery_time = early_delivery_time;
		this.late_delivery_time = late_delivery_time;
	}

	@Override
	public String toString() {
		return "ParcelRequestDungPQ [id=" + id + ", time_call=" + time_call
				+ ", pickup_point=" + pickup_point + ", delivery_point="
				+ delivery_point + ", early_pickup_time=" + early_pickup_time
				+ ", late_pickup_time=" + late_pickup_time
				+ ", early_delivery_time=" + early_delivery_time
				+ ", late_delivery_time=" + late_delivery_time + "]";
	}

	public String toStringFormat() {
		return String.format("%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d",
				id,time_call,pickup_point,delivery_point,
				early_pickup_time,late_pickup_time,
				early_delivery_time,late_delivery_time);
	}

}
