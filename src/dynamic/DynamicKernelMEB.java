package dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import coreset.KernelCoreset;

import model.Point;
import model.Util;

public class DynamicKernelMEB {
	static final Random RAND = new Random(0);

	public HashSet<Point> union_coreset;
	public ArrayList<Point> coreset;
	public ArrayList<Double> coefficients;
	public double cNorm;
	public double radius2;

	private double eps;
	private KernelLevelSet root_level;

	public double time_elapsed = 0.0;

	public DynamicKernelMEB(List<Point> pointSet, double eps) {
		this.union_coreset = new HashSet<>();
		this.coreset = new ArrayList<>();
		this.coefficients = new ArrayList<>();

		this.eps = eps;

		this.root_level = new KernelLevelSet(0, new HashSet<>(pointSet));
		this.root_level.preprocess();
	}

	public void delete(Point p) {
		long t1 = System.nanoTime();
		root_level.delete(p);
		long t2 = System.nanoTime();
		time_elapsed += (t2 - t1) / 1e9;
	}

	public void insert(Point p) {
		long t1 = System.nanoTime();
		root_level.insert(p);
		long t2 = System.nanoTime();
		time_elapsed += (t2 - t1) / 1e9;
	}

	public void approxMEB() {
		KernelCoreset k_coreset = new KernelCoreset(new ArrayList<>(union_coreset), 1e-6);
		radius2 = k_coreset.radius2;
		cNorm = k_coreset.cNorm;
		for (int idx : k_coreset.core_indices) {
			coreset.add(k_coreset.points.get(idx));
			coefficients.add(k_coreset.coefficients[idx]);
		}
	}

	public void validate(List<Point> pointSet) {
		double max_sq_dist = 0.0;
		for (Point point : pointSet) {
			double sq_dist = Util.dist2wc(coreset, coefficients, point, cNorm);
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
		builder.append("support_size ").append(coreset.size()).append("\n");
		return builder.toString();
	}
	
	public String statTime() {
		StringBuilder builder = new StringBuilder();
		builder.append("time ").append(time_elapsed).append("s\n");
		return builder.toString();
	}

	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(Math.sqrt(radius2)).append("\n");
		builder.append("squared radius=").append(radius2).append("\n");
		System.out.print(builder.toString());
	}

	class KernelLevelSet {
		int level_id;
		int counter;

		HashSet<Point> l_coreset;
		HashMap<Integer, Double> l_coefficients;
		HashSet<Point> u_coreset;
		
		double l_cNorm;
		double l_radius2;

		HashSet<Point> P;
		HashSet<Point> Q;

		KernelLevelSet prev_level;
		KernelLevelSet next_level;

		public KernelLevelSet(int level_id, HashSet<Point> pointSet) {
			this.level_id = level_id;
			this.counter = 0;

			this.l_coreset = new HashSet<>();
			this.l_coefficients = new HashMap<>();
			this.u_coreset = new HashSet<>();
			
			this.l_cNorm = 0.0;
			this.l_radius2 = 0.0;

			this.P = pointSet;
			this.Q = new HashSet<>();

			this.prev_level = null;
			this.next_level = null;
		}

		void preprocess() {
			counter = 0;
			l_radius2 = 0.0;
			l_coreset.clear();
			u_coreset.clear();

			Q.clear();
			next_level = null;

			if (P.size() <= Util.C || level_id >= 49) {
				l_coreset.addAll(P);
				if (prev_level != null) {
					u_coreset.addAll(prev_level.u_coreset);
				}
				u_coreset.addAll(l_coreset);
				union_coreset = u_coreset;
				return;
			}

			Q.addAll(P);
			Iterator<Point> iter = P.iterator();
			Point p0 = iter.next();
			l_coreset.add(p0);
			l_coefficients.put(p0.idx, 1.0);
			l_cNorm = 1.0;

			boolean has_ext_points = true;
			while (Q.size() > 0 && l_coreset.size() < 2.0 / eps && has_ext_points) {
				ArrayList<Point> candidate = farthestNeighbors();
				if (candidate.isEmpty()) {
					has_ext_points = false;
					break;
				} else {
					Q.removeAll(candidate);

					Point pi = candidate.get(RAND.nextInt(candidate.size()));
					l_coreset.add(pi);
					if (l_coreset.size() <= 2) {
						l_coefficients.put(p0.idx, 0.5);
						l_coefficients.put(pi.idx, 0.5);
						updateCNorm();
						l_radius2 = 1.0 - l_cNorm;
					} else {
//						double size = l_coefficients.size() + 1.0;
//						for (int idx : l_coefficients.keySet()) {
//							l_coefficients.put(idx, (1.0 - size) * l_coefficients.get(idx));
//						}
						l_coefficients.put(pi.idx, 0.0);
					}
					solveApxBall();
//					for (int idx : l_coefficients.keySet()) {
//						System.out.print(idx + ":" + l_coefficients.get(idx) + " ");
//					}
//					System.out.println();
//					System.out.println(level_id + ":" + l_radius2);
				}
			}

			if (prev_level != null) {
				u_coreset.addAll(prev_level.u_coreset);
			}
			u_coreset.addAll(l_coreset);

			HashSet<Point> R = new HashSet<>();
			R.addAll(P);
			R.removeAll(Q);
			R.addAll(u_coreset);
			if (Math.abs(P.size() - R.size()) > 1.5 && level_id < 50) {
				KernelLevelSet next = new KernelLevelSet(level_id + 1, R);
				next_level = next;
				next_level.prev_level = this;
				next_level.preprocess();
			} else {
				union_coreset = u_coreset;
			}

			counter = Math.max(1, (int) (Util.DELTA * P.size()));
			
			System.out.println(level_id + ":" + l_radius2 + "," + l_coreset.size() + "," + u_coreset.size());
		}

		void delete(Point p) {
			if (P.size() <= Util.C) {
				P.remove(p);
				l_coreset.remove(p);
				u_coreset.remove(p);

				union_coreset = u_coreset;
				return;
			}

			P.remove(p);
			counter -= 1;

			if (counter <= 0 || l_coreset.contains(p)) {
				preprocess();
			}

			if (next_level != null && next_level.P.contains(p)) {
				next_level.delete(p);
			}
		}

		void insert(Point p) {
			if (P.size() <= Util.C) {
				P.add(p);
				l_coreset.add(p);
				u_coreset.add(p);

				union_coreset = u_coreset;
				return;
			}

			P.add(p);
			counter -= 1;

			if (counter <= 0) {
				preprocess();
				return;
			}

			if (next_level != null) {
				next_level.insert(p);
			}
		}

		private ArrayList<Point> farthestNeighbors() {
			int result_size = Math.max(1, (int) (Util.ALPHA * P.size()));
			PriorityQueue<DistItem> queue = new PriorityQueue<>(result_size + 1);
			for (Point p : Q) {
				double dist2 = Util.dist2wc(l_coreset, l_coefficients, p, l_cNorm);
				if (dist2 > (1 + eps) * (1 + eps) * l_radius2) {
					if (queue.size() < result_size) {
						queue.offer(new DistItem(p, dist2));
					} else {
						if (dist2 > queue.peek().dist2) {
							queue.offer(new DistItem(p, dist2));
							queue.poll();
						}
					}
				}
			}
			ArrayList<Point> result = new ArrayList<>();
			while (!queue.isEmpty()) {
				result.add(queue.poll().p);
			}
			return result;
		}

		private void solveApxBall() {
			DistItem furthestItem = findFarthestPoint();
			DistItem nearestItem = findNearestPoint();
//			System.out.println(furthestItem.p.idx + "," + nearestItem.p.idx);
			
			double delta_plus = furthestItem.dist2 / l_radius2 - 1.0;
			double delta_minus = 1.0 - nearestItem.dist2 / l_radius2;
			double delta = Math.max(delta_plus, delta_minus);
			while (delta > (1.0 + eps) * (1.0 + eps) - 1.0) {
				if (delta > delta_minus) {
					double lambda = delta / (2.0 * (1.0 + delta));
					for (int idx : l_coefficients.keySet()) {
						l_coefficients.put(idx, (1.0 - lambda) * l_coefficients.get(idx));
					}
					l_coefficients.put(furthestItem.p.idx, l_coefficients.get(furthestItem.p.idx) + lambda);
				} else {
					double lambda1 = delta_minus / (2.0 * (1.0 - delta_minus));
					double lambda2 = l_coefficients.get(nearestItem.p.idx) / (1.0 - l_coefficients.get(nearestItem.p.idx));
					double lambda = Math.min(lambda1, lambda2);

					for (int idx : l_coefficients.keySet()) {
						l_coefficients.put(idx, (1.0 + lambda) * l_coefficients.get(idx));
					}
					l_coefficients.put(nearestItem.p.idx, l_coefficients.get(nearestItem.p.idx) - lambda);
				}
				updateCNorm();
				l_radius2 = 1.0 - l_cNorm;
//				System.out.println(l_coreset.size() + "," + radius2);
//				for (int idx : l_coefficients.keySet()) {
//					System.out.print(idx + ":" + l_coefficients.get(idx) + " ");
//				}
//				System.out.println();
				
				furthestItem = findFarthestPoint();
				nearestItem = findNearestPoint();
				
				delta_plus = furthestItem.dist2 / l_radius2 - 1.0;
				delta_minus = 1.0 - nearestItem.dist2 / l_radius2;
				delta = Math.max(delta_plus, delta_minus);
				
//				System.out.println(delta);
			}
		}
		
		private DistItem findFarthestPoint() {
			double max_sq_dist = 0.0;
			Point farthestPoint = null;
			for (Point p : l_coreset) {
				double sq_dist = Util.dist2wc(l_coreset, l_coefficients, p, l_cNorm);

				if (sq_dist > max_sq_dist) {
					max_sq_dist = sq_dist;
					farthestPoint = p;
				}
			}

			return new DistItem(farthestPoint, max_sq_dist);
		}
		
		private DistItem findNearestPoint() {
			double min_sq_dist = Double.MAX_VALUE;
			Point nearestPoint = null;
			for (Point p : l_coreset) {
				if (l_coefficients.get(p.idx) < 1e-12) {
					continue;
				}
				
				double sq_dist = Util.dist2wc(l_coreset, l_coefficients, p, l_cNorm);

				if (sq_dist < min_sq_dist) {
					min_sq_dist = sq_dist;
					nearestPoint = p;
				}
			}

			return new DistItem(nearestPoint, min_sq_dist);
		}
		
		private void updateCNorm() {
			l_cNorm = 0.0;
			for (Point p1 : l_coreset) {
				for (Point p2 : l_coreset) {
					l_cNorm += (l_coefficients.get(p1.idx) * l_coefficients.get(p2.idx) * Util.rbf_eval(p1, p2));
				}
			}
		}

		private class DistItem implements Comparable<DistItem> {
			Point p;
			double dist2;

			private DistItem(Point p, double dist2) {
				this.p = p;
				this.dist2 = dist2;
			}

			@Override
			public int compareTo(DistItem o) {
				if (this.dist2 - o.dist2 > 1e-9) {
					return 1;
				} else if (this.dist2 - o.dist2 < -1e-9) {
					return -1;
				} else {
					return (this.p.idx - o.p.idx);
				}
			}
		}
	}
}
