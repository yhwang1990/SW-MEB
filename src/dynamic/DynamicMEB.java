package dynamic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

import coreset.Coreset;
import model.Point;
import model.PointSet;
import model.Util;

public class DynamicMEB {
	static int C = 115;
	static double ALPHA = 1e-3;
	static double DELTA = 1e-3;
	
	static final Random RAND = new Random(0);
	
	public int dim;
	public double radius;
	public double[] center;
	public HashSet<Point> coreset;
	
	private double eps;
	private LevelSet root_level;
	
	public double time_elapsed = 0.0;
	
	public DynamicMEB(PointSet pointSet, double eps) {
		this.dim = pointSet.dim;
		this.center = new double[this.dim];
		this.radius = 0.0;

		this.eps = eps;
		
		long t1 = System.nanoTime();
		this.root_level = new LevelSet(0, new HashSet<>(pointSet.points));
		this.root_level.preprocess();
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 -t1) / 1e9;
	}
	
	public void delete(Point p) {
		long t1 = System.nanoTime();
		this.root_level.delete(p);
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 -t1) / 1e9;
	} 
	
	public void insert(Point p) {
		long t1 = System.nanoTime();
		this.root_level.insert(p);
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 -t1) / 1e9;
	}
	
	public void approxMEB() {
		Coreset core = new Coreset(new PointSet(this.dim, new ArrayList<>(this.coreset)), this.eps);
		this.radius = core.radius;
		this.center = core.center;
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
	
	class LevelSet {
		int level_id;
		int counter;
		
		double level_radius;
		double[] level_center;
		
		HashSet<Point> level_coreset;
		HashSet<Point> union_coreset;
		
		HashSet<Point> P;
		HashSet<Point> Q;
		
		LevelSet prev_level;
		LevelSet next_level;
		
		public LevelSet(int level_id, HashSet<Point> pointSet) {
			this.level_id = level_id;
			this.counter = 0;
			
			this.level_radius = 0.0;
			this.level_center = new double[dim];
			
			this.level_coreset = new HashSet<>();
			this.union_coreset = new HashSet<>();
			
			this.P = pointSet;
			this.Q = new HashSet<>();
			
			this.prev_level = null;
			this.next_level = null;
		}
		
		void preprocess() {
			this.counter = 0;
			
			this.level_radius = 0.0;
			
			this.level_coreset.clear();
			this.union_coreset.clear();
			
			this.Q.clear();
			this.next_level = null;
			
			if (this.P.size() <= C) {
				this.level_coreset.addAll(this.P);
				
				if (this.prev_level != null) {
					this.union_coreset.addAll(this.prev_level.union_coreset);
				}
				this.union_coreset.addAll(this.level_coreset);
				
				coreset = this.union_coreset;
				
//				System.out.println(this.level_id + "," + this.P.size() + "," + this.counter + "," + this.level_radius + "," + this.level_coreset.size() + "," + this.union_coreset.size());
				
				return;
			}
			
			this.Q.addAll(this.P);
			Iterator<Point> iter = this.P.iterator();
			Point p0 = iter.next();
			this.level_coreset.add(p0);
			System.arraycopy(p0.data, 0, this.level_center, 0, dim);
			
			boolean has_ext_points = true;
			while (this.Q.size() > 0 && this.level_coreset.size() < 2.0 / eps && has_ext_points) {
				ArrayList<Point> candidate = farthestNeighbors();
				if (candidate.isEmpty()) {
					has_ext_points = false;
					break;
				} else {
					this.Q.removeAll(candidate);
					
					Point pi = candidate.get(RAND.nextInt(candidate.size()));
					this.level_coreset.add(pi);
					solveApxBall();
				}
			}
			
			if (this.prev_level != null) {
				this.union_coreset.addAll(this.prev_level.union_coreset);
			}
			this.union_coreset.addAll(this.level_coreset);
			
			HashSet<Point> R = new HashSet<>();
			R.addAll(this.P);
			R.removeAll(this.Q);
			R.addAll(this.union_coreset);
			if (this.P.size() > R.size()) {
				LevelSet next_level = new LevelSet(this.level_id + 1, R);
				this.next_level = next_level;
				next_level.prev_level = this;
				next_level.preprocess();
			} else {
				coreset = this.union_coreset;
			}
			
			this.counter = Math.max(1, (int) (DELTA * this.P.size()));
			
//			System.out.println(this.level_id + "," + this.P.size() + "," + this.counter + "," + this.level_radius + "," + this.level_coreset.size() + "," + this.union_coreset.size());
		}
		
		void delete(Point p) {
			if (this.P.size() <= C) {
				this.P.remove(p);
				this.level_coreset.remove(p);
				this.union_coreset.remove(p);
				
				coreset = this.union_coreset;
				return;
			}
			
			this.P.remove(p);
			this.counter -= 1;
			
			if (this.counter <= 0 || this.level_coreset.contains(p)) {
				this.preprocess();
			}
			
			if (this.next_level != null && this.next_level.P.contains(p)) {
				this.next_level.delete(p);
			}
		}
		
		void insert(Point p) {
			if (this.P.size() <= C) {
				this.P.add(p);
				this.level_coreset.add(p);
				this.union_coreset.add(p);
				
				coreset = this.union_coreset;
				return;
			}
			
			this.P.add(p);
			this.counter -= 1;
			
			if (this.counter <= 0) {
				this.preprocess();
			}
			
			if (this.next_level != null) {
				this.next_level.insert(p);
			}
		}
		
		private ArrayList<Point> farthestNeighbors() {
			int result_size = Math.max(1, (int) (ALPHA * this.P.size()));
			PriorityQueue<DistItem> queue = new PriorityQueue<>(result_size + 1);
			for (Point p : this.Q) {
				double dist = Math.sqrt(Util.sq_dist(this.level_center, p.data));
				if (dist > (1 + eps) * this.level_radius) {
					if (queue.size() < result_size) {
						queue.offer(new DistItem(p, dist));
					} else {
						if (dist > queue.peek().dist) {
							queue.offer(new DistItem(p, dist));
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
			while (true) {
				Point furthestPoint = findFarthestPoint(this.level_coreset);
				double max_dist = Math.sqrt(Util.sq_dist(this.level_center, furthestPoint.data));

				if (max_dist <= this.level_radius * (1.0 + eps / 2.0)) {
					break;
				}

				this.level_radius = (this.level_radius * this.level_radius / max_dist + max_dist) / 2.0;
				for (int i = 0; i < dim; i++) {
					this.level_center[i] = furthestPoint.data[i] + (this.level_radius / max_dist) * (this.level_center[i] - furthestPoint.data[i]);
				}
			}
		}
		
		private Point findFarthestPoint(HashSet<Point> points) {
			double max_sq_dist = 0.0;
			Point farthestPoint = null;
			for (Point point : points) {
				double sq_dist = Util.sq_dist(this.level_center, point.data);

				if (sq_dist > max_sq_dist) {
					max_sq_dist = sq_dist;
					farthestPoint = point;
				}
			}
			return farthestPoint;
		}
		
		private class DistItem implements Comparable<DistItem> {
			Point p;
			double dist;
			
			private DistItem (Point p, double dist) {
				this.p = p;
				this.dist = dist;
			}
			
			@Override
			public int compareTo(DistItem o) {
				if (this.dist - o.dist > 1e-9) {
					return 1;
				} else if (this.dist - o.dist < -1e-9) {
					return -1;
				} else {
					return (this.p.idx - o.p.idx);
				}
			}
		}
	}

}
