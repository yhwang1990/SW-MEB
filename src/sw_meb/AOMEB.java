package sw_meb;

import java.util.ArrayList;
import java.util.List;

import core_meb.CoreMEB;
import model.Point;
import model.Util;

public class AOMEB {

	public int idx;
	public ArrayList<Point> core_points;
	public double[] center;
	public double radius;

	public double time_elapsed = 0.0;

	private double eps;

	public AOMEB(List<Point> initPointSet, double eps, boolean append_mode) {
		this.idx = initPointSet.get(0).idx;
		this.eps = eps;
		this.core_points = new ArrayList<>();
		this.center = new double[Util.d];
		this.radius = 0.0;

		long t1 = System.nanoTime();
		coresetConstruct(initPointSet);
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}

	public AOMEB(int idx, AOMEB inst) {
		this.idx = idx;
		this.eps = inst.eps;
		this.core_points = new ArrayList<>(inst.core_points);
		this.center = new double[Util.d];
		System.arraycopy(inst.center, 0, this.center, 0, Util.d);
		this.radius = inst.radius;

		this.time_elapsed = 0;
	}

	public AOMEB(List<Point> pointSet, double eps) {
		this.idx = pointSet.get(0).idx;
		this.eps = eps;
		this.core_points = new ArrayList<>();
		this.center = new double[Util.d];
		this.radius = 0.0;

		long t1 = System.nanoTime();
		coresetConstruct(pointSet.subList(0, Util.BATCH_SIZE));
		int batch_id = 1;
		for (batch_id = 1; batch_id < pointSet.size() / Util.BATCH_SIZE; batch_id++) {
			List<Point> next_batch = pointSet.subList(batch_id * Util.BATCH_SIZE, (batch_id + 1) * Util.BATCH_SIZE);
			append(next_batch);
		}

		if (batch_id * Util.BATCH_SIZE < pointSet.size()) {
			List<Point> next_batch = pointSet.subList(batch_id * Util.BATCH_SIZE, pointSet.size());
			append(next_batch);
		}
		long t2 = System.nanoTime();
		this.time_elapsed = (t2 - t1) / 1e9;

		CoreMEB coreset = new CoreMEB(this.core_points, eps);
		this.center = coreset.center;
		this.radius = coreset.radius;
	}

	public void append(List<Point> points) {
		long t1 = System.nanoTime();
		ArrayList<Point> new_core_points = new ArrayList<>();
		for (Point p : points) {
			if (Math.sqrt(Util.dist2(p.data, this.center)) > (1.0 + this.eps) * this.radius) {
				new_core_points.add(p);
			}
		}

		if (!new_core_points.isEmpty()) {
			this.core_points.addAll(new_core_points);
			solveApxBall();
		}
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}

	public void approxMEB() {
		CoreMEB coreset = new CoreMEB(new ArrayList<>(core_points), eps);

		this.center = coreset.center;
		this.radius = coreset.radius;
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

	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(this.radius).append("\n");
		builder.append("sq_radius=").append(this.radius * this.radius).append("\n");
		System.out.print(builder.toString());
	}

	public int computeCoresetSize() {
		return core_points.size();
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
		builder.append("coreset_size=").append(computeCoresetSize()).append("\n");
		return builder.toString();
	}

	void coresetConstruct(List<Point> points) {
		Point firstPoint = points.get(0);
		Point p1 = findFarthestPoint(firstPoint, points);
		Point p2 = findFarthestPoint(p1, points);

		this.radius = Math.sqrt(Util.dist2(p1.data, p2.data)) / 2.0;
		for (int i = 0; i < Util.d; i++) {
			this.center[i] = (p1.data[i] + p2.data[i]) / 2.0;
		}
		this.core_points.add(p1);
		this.core_points.add(p2);

		while (true) {
			Point furthestPoint = findFarthestPoint(points);
			double max_dist = Math.sqrt(Util.dist2(this.center, furthestPoint.data));

			if (max_dist <= this.radius * (1.0 + this.eps)) {
				break;
			}

			this.radius = (this.radius * this.radius / max_dist + max_dist) / 2.0;
			for (int i = 0; i < Util.d; i++) {
				this.center[i] = furthestPoint.data[i] + (this.radius / max_dist) * (this.center[i] - furthestPoint.data[i]);
			}
			this.core_points.add(furthestPoint);

			solveApxBall();
		}
	}

	private void solveApxBall() {
		while (true) {
			Point furthestPoint = findFarthestPoint(this.core_points);
			double max_dist = Math.sqrt(Util.dist2(this.center, furthestPoint.data));
			if (max_dist <= this.radius * (1.0 + this.eps / 2.0)) {
				break;
			}
			this.radius = (this.radius * this.radius / max_dist + max_dist) / 2.0;
			for (int i = 0; i < Util.d; i++) {
				this.center[i] = furthestPoint.data[i] + (this.radius / max_dist) * (this.center[i] - furthestPoint.data[i]);
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
			double sq_dist = Util.dist2(this.center, point.data);
			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
				farthestPoint = point;
			}
		}
		return farthestPoint;
	}
}
