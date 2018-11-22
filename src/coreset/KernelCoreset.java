package coreset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import model.Point;
import model.Util;

public class KernelCoreset {
	public List<Point> points;
	public HashSet<Integer> core_indices;
	public double[] coefficients;
	public double radius2;
	
	public double cNorm;
	
	private HashMap<Integer, double[]> kernel_cache;
	public double time_elapsed = 0.0;
	private double eps;
	
	private class IdDistPair {
		int idx;
		double dist2;
		
		IdDistPair(int idx, double dist2) {
			this.idx = idx;
			this.dist2 = dist2;
		}
	}

	public KernelCoreset(List<Point> pointSet, double eps) {
		this.points = pointSet;
		this.core_indices = new HashSet<>();
		this.coefficients = new double[this.points.size()];
		this.radius2 = 0.0;
		
		this.cNorm = 0.0;
		this.kernel_cache = new HashMap<>();
		
		this.eps = eps;

		long t1 = System.nanoTime();
//		coresetConstruct();
		coresetConstructwithAwaySteps();
		long t2 = System.nanoTime();
		this.time_elapsed = (t2 - t1) / 1e9;
	}

//	private void coresetConstruct() {
//		Point firstPoint = points.get(0);
//		IdDistPair pair1 = findFarthestPoint(firstPoint);
//		IdDistPair pair2 = findFarthestPoint(points.get(pair1.idx));
//
//		core_indices.add(pair1.idx);
//		core_indices.add(pair2.idx);
//		coefficients[pair1.idx] = coefficients[pair2.idx] = 0.5;
//		
//		initKernelMatrix();
//		updateCNorm();
//		radius2 = 1.0 - cNorm;
//		System.out.println(core_indices.size() + "," + radius2);
//		
//		IdDistPair furthestPair = findFarthestPoint();
//		double delta = furthestPair.dist2 / radius2 - 1.0;
//		while (delta > (1.0 + eps) * (1.0 + eps) - 1.0) {
//			if (! core_indices.contains(furthestPair.idx)) {
//				core_indices.add(furthestPair.idx);
//				addToKernelMatrix(furthestPair.idx);
//			}
//			
//			double lambda = delta / (2.0 * (1.0 + delta));
//			
//			for (int idx : core_indices) {
//				coefficients[idx] = (1.0 - lambda) * coefficients[idx];
//			}
//			coefficients[furthestPair.idx] += lambda;
//			
//			updateCNorm();
//			radius2 = 1.0 - cNorm;
//			System.out.println(core_indices.size() + "," + radius2);
//			
//			furthestPair = findFarthestPoint();
//			delta = furthestPair.dist2 / radius2 - 1.0;
//			System.out.println(delta);
//		}
//	}
	
	private void coresetConstructwithAwaySteps() {
		Point firstPoint = points.get(0);
		IdDistPair pair1 = findFarthestPoint(firstPoint);
		IdDistPair pair2 = findFarthestPoint(points.get(pair1.idx));

		core_indices.add(pair1.idx);
		core_indices.add(pair2.idx);
		coefficients[pair1.idx] = coefficients[pair2.idx] = 0.5;
		
		initKernelMatrix();
		updateCNorm();
		radius2 = 1.0 - cNorm;
//		System.out.println(core_indices.size() + "," + radius2);
		
		IdDistPair furthestPair = findFarthestPoint();
		IdDistPair nearestPair = findNearestPoint();
		
		double delta_plus = furthestPair.dist2 / radius2 - 1.0;
		double delta_minus = 1.0 - nearestPair.dist2 / radius2;
		double delta = Math.max(delta_plus, delta_minus);
		while (delta > (1.0 + eps) * (1.0 + eps) - 1.0) {
			if (delta > delta_minus) {
				if (! core_indices.contains(furthestPair.idx)) {
					core_indices.add(furthestPair.idx);
					addToKernelMatrix(furthestPair.idx);
				}
				double lambda = delta / (2.0 * (1.0 + delta));
				for (int idx : core_indices) {
					coefficients[idx] = (1.0 - lambda) * coefficients[idx];
				}
				coefficients[furthestPair.idx] += lambda;
			} else {
				double lambda1 = delta_minus / (2.0 * (1.0 - delta_minus));
				double lambda2 = coefficients[nearestPair.idx] / (1.0 - coefficients[nearestPair.idx]);
				double lambda = Math.min(lambda1, lambda2);

				for (int idx : core_indices) {
					coefficients[idx] = (1.0 + lambda) * coefficients[idx];
				}
				coefficients[nearestPair.idx] -= lambda;
				
				if (coefficients[nearestPair.idx] <= 1e-12) {
					core_indices.remove(nearestPair.idx);
					deleteFromKernelMatrix(nearestPair.idx);
				}
			}
			updateCNorm();
			radius2 = 1.0 - cNorm;
//			System.out.println(core_indices.size() + "," + radius2);
			
			furthestPair = findFarthestPoint();
			nearestPair = findNearestPoint();
			
			delta_plus = furthestPair.dist2 / radius2 - 1.0;
			delta_minus = 1.0 - nearestPair.dist2 / radius2;
			delta = Math.max(delta_plus, delta_minus);
			
//			System.out.println(delta);
		}
	}
	
	private double dist2wc(int idx1) {
		double dist2 = 0.0;
		for (int idx2 : core_indices) {
			dist2 += (coefficients[idx2] * kernel_cache.get(idx2)[idx1]);
		}
		dist2  = 1.0 + cNorm - 2.0 * dist2;
		return dist2;
	}
	
	private double dist2wc(Point p) {
		double dist2 = 0.0;
		for (int idx : core_indices) {
			dist2 += (coefficients[idx] * Util.rbf_eval(points.get(idx), p));
		}
		dist2  = 1.0 + cNorm - 2.0 * dist2;
		return dist2;
	}

	private IdDistPair findFarthestPoint(Point p) {
		double max_sq_dist = 0.0;
		int farthestPoint = -1;
		for (int i = 0; i < points.size(); i++) {
			double sq_dist = Util.k_dist2(p, points.get(i));

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
				farthestPoint = i;
			}
		}

		return new IdDistPair(farthestPoint, max_sq_dist);
	}

	private IdDistPair findFarthestPoint() {
		double max_sq_dist = 0.0;
		int farthestPoint = -1;
		for (int i = 0; i < points.size(); i++) {
			double sq_dist = dist2wc(i);

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
				farthestPoint = i;
			}
		}

		return new IdDistPair(farthestPoint, max_sq_dist);
	}
	
	private IdDistPair findNearestPoint() {
		double min_sq_dist = Double.MAX_VALUE;
		int nearestPoint = -1;
		for (int idx : core_indices) {
			double sq_dist = dist2wc(idx);

			if (sq_dist < min_sq_dist) {
				min_sq_dist = sq_dist;
				nearestPoint = idx;
			}
		}

		return new IdDistPair(nearestPoint, min_sq_dist);
	}
	
	private void initKernelMatrix() {
		for (int idx : core_indices) {
			double[] kernel_vector = new double[points.size()];
			for (int i = 0; i < points.size(); i++) {
				kernel_vector[i] = Util.rbf_eval(points.get(idx), points.get(i));
			}
			kernel_cache.put(idx, kernel_vector);
		}
	}
	
	private void addToKernelMatrix(int idx) {
		double[] kernel_vector = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			kernel_vector[i] = Util.rbf_eval(points.get(idx), points.get(i));
		}
		kernel_cache.put(idx, kernel_vector);
	}
	
	private void deleteFromKernelMatrix(int idx) {
		kernel_cache.remove(idx);
	}

	private void updateCNorm() {
		cNorm = 0.0;
		for (int idx1 : core_indices) {
			for (int idx2 : core_indices) {
				cNorm += (coefficients[idx1] * coefficients[idx2] * kernel_cache.get(idx1)[idx2]);
			}
		}
	}

	public void validate(List<Point> pointSet) {
//		double sum = 0.0;
//		for (int idx : core_indices) {
//			System.out.print(idx + ":" + coefficients[idx] + " ");
//			sum += coefficients[idx];
//		}
//		System.out.println();
//		System.out.println(sum);
		
		double max_sq_dist = 0.0;
		for (Point point : pointSet) {
			double sq_dist = dist2wc(point);
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
		builder.append("support_size ").append(core_indices.size()).append("\n");
		return builder.toString();
	}

	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(Math.sqrt(radius2)).append("\n");
		builder.append("squared radius=").append(radius2).append("\n");
		System.out.print(builder.toString());
	}
}
