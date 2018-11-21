package coreset;

import java.util.ArrayList;
import java.util.List;

import model.Point;
import model.Util;

public class KernelizedCoreset {
	private static int K_SIZE = 1000;
	
	public ArrayList<Point> core_points;
	public double[] coefficients = new double[K_SIZE];
	public double radius2;
	
	private double cNorm;
	private double[][] kernel_matrix = new double[K_SIZE][K_SIZE];

	public double time_elapsed = 0.0;

	private double eps;

	public KernelizedCoreset(List<Point> pointSet, double eps) {
		this.core_points = new ArrayList<>();
		this.radius2 = 0.0;
		
		this.cNorm = 0.0;
		
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
		coefficients[0] = coefficients[1] = 0.5;
		
		initKernelMatrix();
		updateCNorm();
		radius2 = 1.0 - cNorm;
		System.out.println(core_points.size() + "," + radius2);
		
		Point furthestPoint = findFarthestPoint(pointSet);
		double max_dist2 = Util.dist2wc(core_points, coefficients, furthestPoint, cNorm);
		double delta = max_dist2 / radius2 - 1.0;
		while (delta > (1.0 + eps) * (1.0 + eps) - 1.0) {
			core_points.add(furthestPoint);
			updateKernelMatrix();
			
			double lambda = delta / (2.0 * (1.0 + delta));
			
			for (int i = 0; i < core_points.size() - 1; i++) {
				coefficients[i] = (1.0 - lambda) * coefficients[i];
			}
			coefficients[core_points.size() - 1] = lambda;
			updateCNorm();
			radius2 = 1.0 - cNorm;
			System.out.println(core_points.size() + "," + radius2);
			
			furthestPoint = findFarthestPoint(pointSet);
			max_dist2 = Util.dist2wc(core_points, coefficients, furthestPoint, cNorm);
			delta = max_dist2 / radius2 - 1.0;
		}
	}

//	private int findFarthestIndex() {
//		double max_sq_dist = 0.0;
//		int idx = -1;
//		for (int i = 0; i < core_points.size(); i++) {
//			double sq_dist = k_dist2(i);
//
//			if (sq_dist > max_sq_dist) {
//				max_sq_dist = sq_dist;
//				idx = i;
//			}
//		}
//		return idx;
//	}
	
//	private double k_dist2(int idx) {
//		double dist2 = 0.0;
//		for (int i = 0; i < core_points.size(); i++) {
//			dist2 += (coefficients[i] * kernel_matrix[idx][i]);
//		}
//		dist2  = 1.0 + cNorm - 2.0 * dist2;
//		return dist2;
//	}

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
			Point p = core_points.get(i);
			for (int j = i; j < core_points.size(); j++) {
				double value = Util.rbf_eval(p, core_points.get(j));
				kernel_matrix[i][j] = kernel_matrix[j][i] = value;
			}
		}
	}
	
	private void updateKernelMatrix() {
		if (core_points.size() > K_SIZE) {
			K_SIZE *= 2;
			double[][] expanded_kernel_matrix = new double[K_SIZE][K_SIZE];
			double[] expanded_coefficients = new double[K_SIZE];
			
			System.arraycopy(coefficients, 0, expanded_coefficients, 0, core_points.size() - 1);
			for (int i = 0; i < core_points.size() - 1; i++) {
				System.arraycopy(kernel_matrix[i], 0, expanded_kernel_matrix[i], 0, core_points.size() - 1);
			}
			
			coefficients = expanded_coefficients;
			kernel_matrix = expanded_kernel_matrix;
		}

		Point p = core_points.get(core_points.size() - 1);
//		System.out.println(p.idx + ":" + p.data[0] + "," + p.data[1]);
		for (int i = 0; i < core_points.size() - 1; i++) {
			double value = Util.rbf_eval(p, core_points.get(i));
			kernel_matrix[i][core_points.size() - 1] = value;
			kernel_matrix[core_points.size() - 1][i] = value;
		}
	}

	private void updateCNorm() {
		cNorm = 0.0;
		for (int i = 0; i < core_points.size(); i++) {
			for (int j = 0; j < core_points.size(); j++) {
				cNorm += (coefficients[i] * coefficients[j] * kernel_matrix[i][j]);
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
