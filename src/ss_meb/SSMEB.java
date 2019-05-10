package ss_meb;

import java.util.ArrayList;
import java.util.List;

import model.Point;
import model.Util;

public class SSMEB {
	public ArrayList<Point> core_points;
	public double[] center;
	public double radius;
	
	public double time_elapsed = 0.0;
	
	public SSMEB(Point initPoint) {
		long t1 = System.nanoTime();
		this.core_points = new ArrayList<>();
		this.core_points.add(initPoint);
		this.center = new double[Util.d];
		for (int i = 0; i < Util.d; i++) {
			this.center[i] = initPoint.data[i];
		}
		this.radius = 0.0;
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public SSMEB(List<Point> pointSet) {
		long t1 = System.nanoTime();
		
		this.core_points = new ArrayList<>();
		this.core_points.add(pointSet.get(0));
		
		this.center = new double[Util.d];
		for (int i = 0; i < Util.d; i++) {
			this.center[i] = this.core_points.get(0).data[i];
		}
		
		this.radius = 0.0;
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
		
		for (int i = 1; i < pointSet.size(); i++) {
			append(pointSet.get(i));
		}
	}
	
	public void append(Point p) {
		long t1 = System.nanoTime();
		double dist = Math.sqrt(Util.dist2(this.center, p.data));
		if (dist > this.radius) {
			this.core_points.add(p);
			for (int i = 0; i < Util.d; i++) {
				this.center[i] = this.center[i] + 0.5 * (1.0 - this.radius / dist) * (p.data[i] - this.center[i]);
			}
			this.radius = this.radius + 0.5 * (dist - this.radius);
		}
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public void validate(List<Point> pointSet) {
		double max_sq_dist = 0.0;
		for (Point point : pointSet) {
			double sq_dist = Util.dist2(center, point.data);
			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
			}
		}
		double exp_radius = Math.sqrt(max_sq_dist);
		System.out.println("meb_radius=" + exp_radius);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
//		builder.append("center ");
//		for (int i = 0; i < Util.d - 1; i++) {
//			builder.append(center[i]).append(" ");
//		}
//		builder.append(center[Util.d - 1]).append("\n");
		builder.append("radius=").append(radius).append("\n");
		builder.append("cpu_time=").append(time_elapsed).append("s\n");
		return builder.toString();
	}
	
	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(this.radius).append("\n");
		builder.append("sq_radius=").append(this.radius * this.radius).append("\n");

		System.out.print(builder.toString());
	}
}
