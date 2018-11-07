package slidingwindow;

import java.util.ArrayList;
import java.util.List;

import coreset.Coreset;

import model.Point;
import model.PointSet;
import model.Util;

public class AppendOnlyMEB {
public static final int BATCH_SIZE = 100;
	
	public int dim;
	public ArrayList<Point> core_points;
	public double[] center;
	public double radius;

	public double time_elapsed = 0.0;

	private double eps;
	
	public AppendOnlyMEB(PointSet initPointSet, double eps, boolean append_mode) {
		this.dim = initPointSet.dim;
		this.eps = eps;
		this.core_points = new ArrayList<>();
		this.center = new double[this.dim];
		this.radius = 0.0;
		
		long t1 = System.nanoTime();
		coresetConstruct(initPointSet);	
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
		System.out.println(this.radius + "," + this.core_points.size());
	}
	
	public AppendOnlyMEB(PointSet pointSet, double eps) {
		this.dim = pointSet.dim;
		this.eps = eps;
		this.core_points = new ArrayList<>();
		this.center = new double[this.dim];
		this.radius = 0.0;
		
		long t1 = System.nanoTime();
		coresetConstruct(new PointSet(this.dim, pointSet.points.subList(0, BATCH_SIZE)));
		int batch_id = 1;
		for (batch_id = 1; batch_id < pointSet.points.size() / BATCH_SIZE; batch_id++) {
			PointSet next_batch = new PointSet(this.dim, pointSet.points.subList(batch_id * BATCH_SIZE, (batch_id + 1) * BATCH_SIZE));
			System.out.println(next_batch.points.get(0).idx);
			append(next_batch);
		}
		
		if (batch_id * BATCH_SIZE < pointSet.points.size()) {
			PointSet next_batch = new PointSet(this.dim, pointSet.points.subList(batch_id * BATCH_SIZE, pointSet.points.size()));
			System.out.println(next_batch.points.get(0).idx);
			append(next_batch);
		}
		long t2 = System.nanoTime();
		this.time_elapsed = (t2 - t1) / 1e9;
		
		Coreset coreset = new Coreset(new PointSet(this.dim, new ArrayList<>(this.core_points)), 1e-6);
		this.center = coreset.center;
		this.radius = coreset.radius;
	}
	
	public void append(PointSet pointSet) {
		long t1 = System.nanoTime();
		ArrayList<Point> new_core_points = new ArrayList<>();
		for (Point p : pointSet.points) {
			if (Math.sqrt(Util.sq_dist(p.data,this.center)) > (1.0 + this.eps) * this.radius) {
				new_core_points.add(p);
			}
		}
		
		if (! new_core_points.isEmpty()) {
			this.core_points.addAll(new_core_points);
			solveApxBall();
			
			System.out.println(this.radius + "," + this.core_points.size());
		}
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public void approxMEB() {
		Coreset coreset = new Coreset(new PointSet(this.dim, new ArrayList<>(this.core_points)), 1e-6);
		
		this.center = coreset.center;
		this.radius = coreset.radius;
	}
	
	public void validate(PointSet pointSet) {
		double max_sq_dist = 0.0;
		double sq_radius = this.radius * this.radius;
		int ext_count = 0;
		for (Point point : pointSet.points) {
			double sq_dist = Util.sq_dist(this.center, point.data);

			if (sq_dist > sq_radius) {
				ext_count += 1;
			}

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
			}
		}

		double exp_radius = Math.sqrt(max_sq_dist);
		double approx_ratio = exp_radius / this.radius;

		System.out.println("Actual Radius=" + exp_radius);
		System.out.println("Approx Ratio=" + approx_ratio);
		System.out.println("Exterior Count=" + ext_count);
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
	
	void coresetConstruct(PointSet pointSet) {
		Point firstPoint = pointSet.points.get(0);
		Point p1 = findFarthestPoint(firstPoint, pointSet.points);
		Point p2 = findFarthestPoint(p1, pointSet.points);

		this.radius = Math.sqrt(Util.sq_dist(p1.data, p2.data)) / 2.0;
		for (int i = 0; i < this.dim; i++) {
			this.center[i] = (p1.data[i] + p2.data[i]) / 2.0;
		}
		this.core_points.add(p1);
		this.core_points.add(p2);

		while (true) {
			Point furthestPoint = findFarthestPoint(pointSet.points);
			double max_dist = Math.sqrt(Util.sq_dist(center, furthestPoint.data));

			if (max_dist <= this.radius * (1.0 + this.eps)) {
				break;
			}

			this.radius = (this.radius * this.radius / max_dist + max_dist) / 2.0;
			for (int i = 0; i < this.dim; i++) {
				this.center[i] = furthestPoint.data[i] + (this.radius / max_dist) * (this.center[i] - furthestPoint.data[i]);
			}
			this.core_points.add(furthestPoint);

			solveApxBall();
		}
	}

	private void solveApxBall() {
		while (true) {
			Point furthestPoint = findFarthestPoint(this.core_points);
			double max_dist = Math.sqrt(Util.sq_dist(center, furthestPoint.data));

			if (max_dist <= this.radius * (1.0 + this.eps / 2.0)) {
				break;
			}

			this.radius = (this.radius * this.radius / max_dist + max_dist) / 2.0;
			for (int i = 0; i < this.dim; i++) {
				this.center[i] = furthestPoint.data[i] + (this.radius / max_dist) * (this.center[i] - furthestPoint.data[i]);
			}
		}
	}

	private Point findFarthestPoint(Point p, List<Point> points) {
		double max_sq_dist = 0.0;
		Point farthestPoint = null;
		for (Point point : points) {
			double sq_dist = Util.sq_dist(p.data, point.data);

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
				farthestPoint = point;
			}
		}

		return farthestPoint;
	}

	private Point findFarthestPoint(List<Point> points) {
		double max_sq_dist = 0.0;
		Point farthestPoint = null;
		for (Point point : points) {
			double sq_dist = Util.sq_dist(this.center, point.data);

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
				farthestPoint = point;
			}
		}

		return farthestPoint;
	}
}
