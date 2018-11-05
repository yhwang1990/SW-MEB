package simplestream;

import java.util.ArrayList;

import coreset.Coreset;
import model.Point;
import model.PointSet;
import model.Util;

public class SimpleStream {
	
	public int dim;
	public ArrayList<Point> core_points;
	public double[] center;
	public double radius;
	
	public double time_elapsed = 0.0;
	
	public SimpleStream(int dim, Point initPoint) {
		long t1 = System.nanoTime();
		this.dim = dim;
		
		this.core_points = new ArrayList<>();
		this.core_points.add(initPoint);
		
		this.center = new double[this.dim];
		for (int i = 0; i < this.dim; i++) {
			this.center[i] = initPoint.data[i];
		}
		
		this.radius = 0.0;
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public SimpleStream(PointSet pointSet) {
		long t1 = System.nanoTime();
		this.dim = pointSet.dim;
		
		this.core_points = new ArrayList<>();
		this.core_points.add(pointSet.points.get(0));
		
		this.center = new double[this.dim];
		for (int i = 0; i < this.dim; i++) {
			this.center[i] = this.core_points.get(0).data[i];
		}
		
		this.radius = 0.0;
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
		
		for (int i = 1; i < pointSet.points.size(); i++) {
			append(pointSet.points.get(i));
		}
	}
	
	public void append(Point p) {
		long t1 = System.nanoTime();
		double dist = Math.sqrt(Util.sq_dist(this.center, p.data));
		if (dist > this.radius) {
			this.core_points.add(p);
			
			for (int i = 0; i < this.dim; i++) {
				this.center[i] = this.center[i] + 0.5 * (1.0 - this.radius / dist) * (p.data[i] - this.center[i]);
			}
			
			this.radius = this.radius + 0.5 * (dist - this.radius);
			
			System.out.println(this.core_points.size() + "," + this.radius);
		}
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public void validate(PointSet pointSet){
		Coreset coreset = new Coreset(new PointSet(this.dim, this.core_points), 1e-9);
		System.out.println("Core Radius=" + coreset.radius);
		
		double max_sq_dist = 0.0;
		for (Point point : pointSet.points) {
			double sq_dist = Util.sq_dist(this.center, point.data);

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
			}
		}

		double exp_radius = Math.sqrt(max_sq_dist);
		double approx_ratio = exp_radius / coreset.radius;

		System.out.println("Actual Radius=" + exp_radius);
		System.out.println("Approx Ratio=" + approx_ratio);
	}
	
	public void output() {
		StringBuilder builder = new StringBuilder();
//		builder.append("center=(");
//		for (int i = 0; i < this.dim - 1; i++) {
//			builder.append(this.center[i]).append(", ");
//		}
//		builder.append(this.center[this.dim - 1]).append(")\n");
		builder.append("radius=").append(this.radius).append("\n");
		builder.append("squared radius=").append(this.radius * this.radius).append("\n");

		System.out.print(builder.toString());
	}
}
