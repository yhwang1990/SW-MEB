package coreset;

import java.util.ArrayList;
import java.util.List;

import model.Point;
import model.Util;

public class Coreset {
	public ArrayList<Point> core_points;
	public double[] center;
	public double radius;

	public double time_elapsed = 0.0;

	private double eps;

	public Coreset(List<Point> pointSet, double eps) {
		this.core_points = new ArrayList<>(Util.d);
		this.center = new double[Util.d];
		this.radius = 0.0;

		this.eps = eps;

		long t1 = System.nanoTime();
		coresetConstruct(pointSet);
		long t2 = System.nanoTime();
		this.time_elapsed = (t2 - t1) / 1e9;
	}

	void coresetConstruct(List<Point> pointSet) {
		Point firstPoint = pointSet.get(0);
		Point p1 = findFarthestPoint(firstPoint, pointSet);
		Point p2 = findFarthestPoint(p1, pointSet);

		radius = Math.sqrt(Util.dist2(p1.data, p2.data)) / 2.0;
		for (int i = 0; i < Util.d; i++) {
			center[i] = (p1.data[i] + p2.data[i]) / 2.0;
		}
		core_points.add(p1);
		core_points.add(p2);

		while (true) {
			Point furthestPoint = findFarthestPoint(pointSet);
			double max_dist = Math.sqrt(Util.dist2(center, furthestPoint.data));

			if (max_dist <= radius * (1.0 + eps)) {
				break;
			}
			radius = (radius * radius / max_dist + max_dist) / 2.0;
			for (int i = 0; i < Util.d; i++) {
				center[i] = furthestPoint.data[i] + (radius / max_dist) * (center[i] - furthestPoint.data[i]);
			}
			core_points.add(furthestPoint);
			solveApxBall();
		}
	}

	private void solveApxBall() {
		while (true) {
			Point furthestPoint = findFarthestPoint(core_points);
			double max_dist = Math.sqrt(Util.dist2(center, furthestPoint.data));

			if (max_dist <= radius * (1.0 + eps / 2.0)) {
				break;
			}

			radius = (radius * radius / max_dist + max_dist) / 2.0;
			for (int i = 0; i < Util.d; i++) {
				center[i] = furthestPoint.data[i] + (radius / max_dist) * (center[i] - furthestPoint.data[i]);
			}
		}
	}

	private Point findFarthestPoint(Point p, List<Point> points) {
		double max_sq_dist = 0.0;
		Point farthestPoint = null;
		for (Point point : points) {
			double sq_dist = Util.dist2(p.data, point.data);

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
			double sq_dist = Util.dist2(center, point.data);

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
				farthestPoint = point;
			}
		}

		return farthestPoint;
	}

	public void validate(List<Point> pointSet) {
		double max_sq_dist = 0.0;
		double sq_radius = radius * radius;
		int ext_count = 0;
		for (Point point : pointSet) {
			double sq_dist = Util.dist2(center, point.data);

			if (sq_dist > sq_radius) {
				ext_count += 1;
			}

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
			}
		}

		double exp_radius = Math.sqrt(max_sq_dist);
		double approx_ratio = exp_radius / radius;

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
		builder.append("radius=").append(radius).append("\n");
		builder.append("squared radius=").append(radius * radius).append("\n");

		System.out.print(builder.toString());
	}
}
