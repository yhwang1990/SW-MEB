package coreset;

import java.util.ArrayList;

import model.Point;
import model.PointSet;
import model.Util;

public class Coreset {

	public int dim;
	public ArrayList<Point> core_points;
	public final double[] center;
	public double radius;

	public double time_elapsed = 0.0;

	private double eps;

	public Coreset(PointSet pointSet, double eps) {
		this.dim = pointSet.dim;
		this.core_points = new ArrayList<>(this.dim);
		this.center = new double[this.dim];
		this.radius = 0.0;

		this.eps = eps;

		long t1 = System.nanoTime();
		coresetConstruct(pointSet);
		long t2 = System.nanoTime();
		this.time_elapsed = (t2 - t1) / 1e9;
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

//		System.out.println(this.core_points.size() + "," + this.radius);

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

//			System.out.println(this.core_points.size() + "," + this.radius);
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

	private Point findFarthestPoint(Point p, ArrayList<Point> points) {
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

	private Point findFarthestPoint(ArrayList<Point> points) {
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
}
