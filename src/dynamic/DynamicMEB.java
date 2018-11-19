package dynamic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import coreset.Coreset;

import model.Point;
import model.Util;

public class DynamicMEB {
	static final Random RAND = new Random(0);

	public double radius;
	public double[] center;
	public HashSet<Point> coreset;

	private double eps;
	private LevelSet root_level;

	public double time_elapsed = 0.0;

	public DynamicMEB(List<Point> pointSet, double eps) {
		this.center = new double[Util.d];
		this.radius = 0.0;

		this.eps = eps;

		long t1 = System.nanoTime();
		this.root_level = new LevelSet(0, new HashSet<>(pointSet));
		this.root_level.preprocess();
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
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
		Coreset core = new Coreset(new ArrayList<>(coreset), eps);
		radius = core.radius;
		center = core.center;
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
		System.out.println("Actual Radius " + exp_radius);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius ").append(radius).append("\n");
		builder.append("time ").append(time_elapsed).append("s\n");
		return builder.toString();
	}

	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(radius).append("\n");
		builder.append("squared radius=").append(radius * radius).append("\n");
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
			this.level_center = new double[Util.d];

			this.level_coreset = new HashSet<>();
			this.union_coreset = new HashSet<>();

			this.P = pointSet;
			this.Q = new HashSet<>();

			this.prev_level = null;
			this.next_level = null;
		}

		void preprocess() {
			counter = 0;
			level_radius = 0.0;
			level_coreset.clear();
			union_coreset.clear();

			Q.clear();
			next_level = null;

			if (P.size() <= Util.C) {
				level_coreset.addAll(P);
				if (prev_level != null) {
					union_coreset.addAll(prev_level.union_coreset);
				}
				union_coreset.addAll(level_coreset);
				coreset = union_coreset;
				return;
			}

			Q.addAll(P);
			Iterator<Point> iter = P.iterator();
			Point p0 = iter.next();
			level_coreset.add(p0);
			System.arraycopy(p0.data, 0, level_center, 0, Util.d);

			boolean has_ext_points = true;
			while (Q.size() > 0 && level_coreset.size() < 2.0 / eps && has_ext_points) {
				ArrayList<Point> candidate = farthestNeighbors();
				if (candidate.isEmpty()) {
					has_ext_points = false;
					break;
				} else {
					Q.removeAll(candidate);

					Point pi = candidate.get(RAND.nextInt(candidate.size()));
					level_coreset.add(pi);
					solveApxBall();
				}
			}

			if (prev_level != null) {
				union_coreset.addAll(prev_level.union_coreset);
			}
			union_coreset.addAll(level_coreset);

			HashSet<Point> R = new HashSet<>();
			R.addAll(P);
			R.removeAll(Q);
			R.addAll(union_coreset);
			if (P.size() > R.size()) {
				LevelSet next = new LevelSet(level_id + 1, R);
				next_level = next;
				next_level.prev_level = this;
				next_level.preprocess();
			} else {
				coreset = union_coreset;
			}

			counter = Math.max(1, (int) (Util.DELTA * P.size()));
		}

		void delete(Point p) {
			if (P.size() <= Util.C) {
				P.remove(p);
				level_coreset.remove(p);
				union_coreset.remove(p);

				coreset = union_coreset;
				return;
			}

			P.remove(p);
			counter -= 1;

			if (counter <= 0 || level_coreset.contains(p)) {
				preprocess();
			}

			if (next_level != null && next_level.P.contains(p)) {
				next_level.delete(p);
			}
		}

		void insert(Point p) {
			if (P.size() <= Util.C) {
				P.add(p);
				level_coreset.add(p);
				union_coreset.add(p);

				coreset = union_coreset;
				return;
			}

			P.add(p);
			counter -= 1;

			if (counter <= 0) {
				preprocess();
			}

			if (next_level != null) {
				next_level.insert(p);
			}
		}

		private ArrayList<Point> farthestNeighbors() {
			int result_size = Math.max(1, (int) (Util.ALPHA * P.size()));
			PriorityQueue<DistItem> queue = new PriorityQueue<>(result_size + 1);
			for (Point p : Q) {
				double dist = Math.sqrt(Util.dist2(level_center, p.data));
				if (dist > (1 + eps) * level_radius) {
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
				Point furthestPoint = findFarthestPoint(level_coreset);
				double max_dist = Math.sqrt(Util.dist2(level_center, furthestPoint.data));

				if (max_dist <= level_radius * (1.0 + eps / 2.0)) {
					break;
				}

				level_radius = (level_radius * level_radius / max_dist + max_dist) / 2.0;
				for (int i = 0; i < Util.d; i++) {
					level_center[i] = furthestPoint.data[i] + (level_radius / max_dist) * (level_center[i] - furthestPoint.data[i]);
				}
			}
		}

		private Point findFarthestPoint(HashSet<Point> points) {
			double max_sq_dist = 0.0;
			Point farthestPoint = null;
			for (Point point : points) {
				double sq_dist = Util.dist2(level_center, point.data);

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

			private DistItem(Point p, double dist) {
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
