package sw_meb;

import java.util.ArrayList;
import java.util.List;

import core_meb.KernelCoreMEB;
import model.Point;
import model.Util;

public class KernelAOMEB {

	public int idx;
	public ArrayList<Point> core_points;
	public ArrayList<Double> coefficients;
	
	public double radius2;
	public double cNorm;
	
	private ArrayList<ArrayList<Double>> kernel_cache;

	public double time_elapsed = 0.0;

	private double eps;

	public KernelAOMEB(List<Point> initPointSet, double eps, boolean append_mode) {
		this.idx = initPointSet.get(0).idx;
		this.eps = eps;
		
		this.core_points = new ArrayList<>();
		this.coefficients = new ArrayList<>();
		
		this.radius2 = 0.0;
		this.cNorm = 0.0;
		
		this.kernel_cache = new ArrayList<>();

		long t1 = System.nanoTime();
		coresetInitial(initPointSet);
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}

	public KernelAOMEB(int idx, KernelAOMEB inst) {
		this.idx = idx;
		this.eps = inst.eps;
		
		this.core_points = new ArrayList<>(inst.core_points);
		this.coefficients = new ArrayList<>(inst.coefficients);
		
		this.radius2 = inst.radius2;
		this.cNorm = inst.cNorm;
		
		this.kernel_cache = new ArrayList<>();
		for (int i = 0; i < inst.kernel_cache.size(); i++) {
			this.kernel_cache.add(i, new ArrayList<>(inst.kernel_cache.get(i)));
		}

		this.time_elapsed = 0;
	}

	public KernelAOMEB(List<Point> pointSet, double eps) {
		this.idx = pointSet.get(0).idx;
		this.eps = eps;
		
		this.core_points = new ArrayList<>();
		this.coefficients = new ArrayList<>();
		
		this.radius2 = 0.0;
		this.cNorm = 0.0;
		
		this.kernel_cache = new ArrayList<>();

		long t1 = System.nanoTime();
		coresetInitial(pointSet.subList(0, Util.BATCH_SIZE));
		
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

		KernelCoreMEB coreset = new KernelCoreMEB(this.core_points, 1e-6);
		this.radius2 = coreset.radius2;
		this.cNorm = coreset.cNorm;

		for (int i = 0; i < this.coefficients.size(); i++) {
			this.coefficients.set(i, 0.0);
		}
		for (int idx : coreset.core_indices) {
			this.coefficients.set(idx, coreset.coefficients[idx]);
		}
	}

	public void append(List<Point> points) {
		long t1 = System.nanoTime();
		ArrayList<Point> new_core_points = new ArrayList<>();
		for (Point p : points) {
			if (dist2wc(p) > (1.0 + eps) * (1.0 + eps) * radius2) {
				new_core_points.add(p);
			}
		}

		if (!new_core_points.isEmpty()) {
			solveApxBall(new_core_points);
		}
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}

	public void approxMEB() {
		KernelCoreMEB coreset = new KernelCoreMEB(core_points, 1e-6);
		radius2 = coreset.radius2;
		cNorm = coreset.cNorm;
		
		for (int i = 0; i < coefficients.size(); i++) {
			coefficients.set(i, 0.0);
		}
		
		for (int idx : coreset.core_indices) {
			coefficients.set(idx, coreset.coefficients[idx]);
		}
	}

	public void validate(List<Point> pointSet) {
		double max_sq_dist = 0.0;
		for (Point point : pointSet) {
			double sq_dist = dist2wc(point);
			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
			}
		}
		double exp_radius = Math.sqrt(max_sq_dist);
		System.out.println("meb_radius=" + exp_radius);
	}

	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(Math.sqrt(radius2)).append("\n");
		builder.append("sq_radius=").append(radius2).append("\n");
		System.out.print(builder.toString());
	}
	
	public int computeSupportSize() {
		int count = 0;
		for (int i = 0; i < coefficients.size(); i++) {
			if (coefficients.get(i) > 1e-12) {
				count++;
			}
		}
		return count;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(Math.sqrt(radius2)).append("\n");
		builder.append("cpu_time=").append(time_elapsed).append("s\n");
		builder.append("coreset_size=").append(core_points.size()).append("\n");
		return builder.toString();
	}

	private void coresetInitial(List<Point> points) {
		KernelCoreMEB initCoreset = new KernelCoreMEB(points, eps);
		
		radius2 = initCoreset.radius2;
		cNorm = initCoreset.cNorm;

		for (int idx : initCoreset.core_indices) {
			core_points.add(initCoreset.points.get(idx));
			coefficients.add(initCoreset.coefficients[idx]);
		}
		initKernelMatrix();
	}

	private void solveApxBall(List<Point> n_points) {
		for (Point p : n_points) {
			core_points.add(p);
			coefficients.add(0.0);
			addToKernelMatrix(p);
		}
		
		IdDist furthestPair = findFarthestPoint();
		IdDist nearestPair = findNearestPoint();
		
		double delta_plus = furthestPair.dist2 / radius2 - 1.0;
		double delta_minus = 1.0 - nearestPair.dist2 / radius2;
		double delta = Math.max(delta_plus, delta_minus);
		while (delta > (1.0 + eps) * (1.0 + eps) - 1.0) {
			if (delta > delta_minus) {
				double lambda = delta / (2.0 * (1.0 + delta));
				for (int i = 0; i < core_points.size(); i++) {
					if (coefficients.get(i) >= 1e-12) {
						coefficients.set(i, (1.0 - lambda) * coefficients.get(i));
					}
				}
				coefficients.set(furthestPair.idx, coefficients.get(furthestPair.idx) + lambda);
			} else {
				double lambda1 = delta_minus / (2.0 * (1.0 - delta_minus));
				double lambda2 = coefficients.get(nearestPair.idx) / (1.0 - coefficients.get(nearestPair.idx));
				double lambda = Math.min(lambda1, lambda2);

				for (int i = 0; i < core_points.size(); i++) {
					if (coefficients.get(i) >= 1e-12) {
						coefficients.set(i, (1.0 + lambda) * coefficients.get(i));
					}
				}
				coefficients.set(nearestPair.idx, coefficients.get(nearestPair.idx) - lambda);
			}
			updateCNorm();
			radius2 = 1.0 - cNorm;
			
			furthestPair = findFarthestPoint();
			nearestPair = findNearestPoint();
			
			delta_plus = furthestPair.dist2 / radius2 - 1.0;
			delta_minus = 1.0 - nearestPair.dist2 / radius2;
			delta = Math.max(delta_plus, delta_minus);
		}
	}
	
	private double dist2wc(int idx1) {
		double dist2 = 0.0;
		for (int idx2 = 0; idx2 < core_points.size(); idx2++) {
			if (coefficients.get(idx2) < 1e-12) {
				continue;
			}
			dist2 += (coefficients.get(idx2) * kernel_cache.get(idx1).get(idx2));
		}
		dist2  = 1.0 + cNorm - 2.0 * dist2;
		return dist2;
	}
	
	private double dist2wc(Point p) {
		double dist2 = 0.0;
		for (int i = 0; i < core_points.size(); i++) {
			if (coefficients.get(i) < 1e-12) {
				continue;
			}
			dist2 += (coefficients.get(i) * Util.rbf_eval(core_points.get(i), p));
		}
		dist2  = 1.0 + cNorm - 2.0 * dist2;
		return dist2;
	}

	private IdDist findFarthestPoint() {
		double max_sq_dist = 0.0;
		int farthestPoint = -1;
		for (int i = 0; i < core_points.size(); i++) {
			double sq_dist = dist2wc(i);

			if (sq_dist > max_sq_dist) {
				max_sq_dist = sq_dist;
				farthestPoint = i;
			}
		}

		return new IdDist(farthestPoint, max_sq_dist);
	}
	
	private IdDist findNearestPoint() {
		double min_sq_dist = Double.MAX_VALUE;
		int nearestPoint = -1;
		for (int i = 0; i < core_points.size(); i++) {
			if (coefficients.get(i) < 1e-12) {
				continue;
			}
			
			double sq_dist = dist2wc(i);

			if (sq_dist < min_sq_dist) {
				min_sq_dist = sq_dist;
				nearestPoint = i;
			}
		}

		return new IdDist(nearestPoint, min_sq_dist);
	}
	
	private void initKernelMatrix() {
		for (int i = 0; i < core_points.size(); i++) {
			ArrayList<Double> kernel_vector = new ArrayList<>();
			for (int j = 0; j < core_points.size(); j++) {
				kernel_vector.add(Util.rbf_eval(core_points.get(i), core_points.get(j)));
			}
			kernel_cache.add(i, kernel_vector);
		}
	}
	
	private void addToKernelMatrix(Point p) {
		ArrayList<Double> kernel_vector = new ArrayList<>();
		for (int i = 0; i < core_points.size() - 1; i++) {
			double value = Util.rbf_eval(p, core_points.get(i));
			kernel_vector.add(value);
			kernel_cache.get(i).add(value);
		}
		kernel_vector.add(Util.rbf_eval(p, p));
		kernel_cache.add(core_points.size() - 1, kernel_vector);
	}

	private void updateCNorm() {
		cNorm = 0.0;
		for (int i = 0; i < core_points.size(); i++) {
			if(coefficients.get(i) < 1e-12) {
				continue;
			}
			for (int j = 0; j < core_points.size(); j++) {
				if(coefficients.get(j) < 1e-12) {
					continue;
				}
				cNorm += (coefficients.get(i) * coefficients.get(j) * kernel_cache.get(i).get(j));
			}
		}
	}
	
	private class IdDist {
		int idx;
		double dist2;
		
		IdDist(int idx, double dist2) {
			this.idx = idx;
			this.dist2 = dist2;
		}
	}
}
