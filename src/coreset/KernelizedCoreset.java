package coreset;

import java.util.ArrayList;
import java.util.List;

import model.Point;
import model.Util;

public class KernelizedCoreset {
	public ArrayList<Point> core_points;
	public ArrayList<Double> coefficients;
	public double radius2;
	
	private double cNorm;
	private ArrayList<ArrayList<Double>> kernel_matrix;

	public double time_elapsed = 0.0;

	private double eps;

	public KernelizedCoreset(List<Point> pointSet, double eps) {
		this.core_points = new ArrayList<>();
		this.coefficients = new ArrayList<>();
		this.radius2 = 0.0;
		
		this.cNorm = 0.0;
		this.kernel_matrix = new ArrayList<>();
		
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

		core_points.add(p1);
		core_points.add(p2);
		coefficients.add(0.5);
		coefficients.add(0.5);
		
		initKernelMatrix();
		updateCNorm();
		radius2 = 1.0 - cNorm;

		while (true) {
			Point furthestPoint = findFarthestPoint(pointSet);
			double max_dist2 = Util.dist2wc(core_points, coefficients, furthestPoint, cNorm);

			if (max_dist2 <= radius2 * (1.0 + eps) * (1.0 + eps)) {
				break;
			}
			core_points.add(furthestPoint);
			updateKernelMatrix();
			
			reOptimize();
			updateCNorm();
			radius2 = 1.0 - cNorm;
		}
	}

	private void reOptimize() {
	}

	private Point findFarthestPoint(Point p, List<Point> points) {
		double max_sq_dist = 0.0;
		Point farthestPoint = null;
		for (Point point : points) {
			double sq_dist = Util.k_dist2(p, point);

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
			double sq_dist = Util.dist2wc(core_points, coefficients, point, cNorm);

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
				farthestPoint = point;
			}
		}

		return farthestPoint;
	}
	
	private void initKernelMatrix() {
		for (int i = 0; i < core_points.size(); i++) {
			ArrayList<Double> kernel_vector = new ArrayList<>();
			Point p = core_points.get(i);
			for (int j = 0; j < core_points.size(); j++) {
				double value = Util.rbf_eval(core_points.get(j), p);
				kernel_vector.add(value);
			}
			kernel_matrix.add(kernel_vector);
		}
	}
	
	private void updateKernelMatrix() {
		Point p = core_points.get(core_points.size() - 1);
		ArrayList<Double> kernel_vector = new ArrayList<>();
		for (int i = 0; i < core_points.size() - 1; i++) {
			double value = Util.rbf_eval(core_points.get(i), p);
			kernel_matrix.get(i).add(value);
			kernel_vector.add(value);
		}
		kernel_vector.add(Util.rbf_eval(p, p));
		kernel_matrix.add(kernel_vector);
	}

	private void updateCNorm() {
		cNorm = 0.0;
		for (int i = 0; i < core_points.size(); i++) {
			for (int j = 0; j < core_points.size(); j++) {
				cNorm += (coefficients.get(i) * coefficients.get(j) * kernel_matrix.get(i).get(j));
			}
		}
	}

	public void validate(List<Point> pointSet) {
		double max_sq_dist = 0.0;
		for (Point point : pointSet) {
			double sq_dist = Util.dist2wc(core_points, coefficients, point, cNorm);
			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
			}
		}
		double exp_radius = Math.sqrt(max_sq_dist);
		System.out.println("Actual Radius " + exp_radius);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius ").append(Math.sqrt(radius2)).append("\n");
		builder.append("time ").append(time_elapsed).append("s\n");
		return builder.toString();
	}

	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(Math.sqrt(radius2)).append("\n");
		builder.append("squared radius=").append(radius2).append("\n");
		System.out.print(builder.toString());
	}
}
