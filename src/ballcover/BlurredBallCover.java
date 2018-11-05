package ballcover;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import coreset.Coreset;

import model.Point;
import model.PointSet;
import model.Util;

public class BlurredBallCover {
	
	public static final int BATCH_SIZE = 100;
	
	public int dim;
	public HashSet<Point> union_coreset;
	public double[] center;
	public double radius;

	public double time_elapsed = 0.0;

	private double eps;
	private LinkedList<BlurredBall> blurred_cover;
	
	public BlurredBallCover(PointSet initPointSet, double eps, boolean append_mode) {
		this.dim = initPointSet.dim;
		this.eps = eps;
		this.union_coreset = new HashSet<>();
		this.blurred_cover = new LinkedList<>();
		this.center = new double[this.dim];
		this.radius = 0.0;
		
		long t1 = System.nanoTime();
		BlurredBall initBall = new BlurredBall(0, initPointSet);
		this.union_coreset.addAll(initBall.ball_coreset);
		this.blurred_cover.addFirst(initBall);
				
		System.out.println(this.blurred_cover.getFirst().ball_radius + "," + this.union_coreset.size());
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public BlurredBallCover(PointSet pointSet, double eps) {
		this.dim = pointSet.dim;
		this.eps = eps;
		this.union_coreset = new HashSet<>();
		this.blurred_cover = new LinkedList<>();
		this.center = new double[this.dim];
		this.radius = 0.0;
		
		long t1 = System.nanoTime();
		int batch_id = 0;
		for (batch_id = 0; batch_id < pointSet.points.size() / BATCH_SIZE; batch_id++) {
			PointSet next_batch = new PointSet(this.dim, pointSet.points.subList(batch_id * BATCH_SIZE, (batch_id + 1) * BATCH_SIZE));
			System.out.println(next_batch.points.get(0).idx);
			
			if (this.blurred_cover.isEmpty()) {
				BlurredBall initBall = new BlurredBall(0, next_batch);
				this.union_coreset.addAll(initBall.ball_coreset);
				this.blurred_cover.addFirst(initBall);
				
				System.out.println(this.blurred_cover.getFirst().ball_radius + "," + this.union_coreset.size());
			} else {
				append(next_batch);
			}
		}
		
		if (batch_id * BATCH_SIZE < pointSet.points.size()) {
			PointSet next_batch = new PointSet(this.dim, pointSet.points.subList(batch_id * BATCH_SIZE, pointSet.points.size()));
			System.out.println(next_batch.points.get(0).idx);
			append(next_batch);
		}
		long t2 = System.nanoTime();
		this.time_elapsed = (t2 - t1) / 1e9;
		
		Coreset coreset = new Coreset(new PointSet(this.dim, new ArrayList<>(this.union_coreset)), 1e-6);
		this.center = coreset.center;
		this.radius = coreset.radius;
	}
	
	public void append(PointSet pointSet) {
		System.out.println(pointSet.points.get(0).idx);
		long t1 = System.nanoTime();
		boolean need_update = false;
		for (Point p : pointSet.points) {
			boolean point_in_ballcover = false;
			Iterator<BlurredBall> iter = this.blurred_cover.iterator();
			while (iter.hasNext()) {
				BlurredBall cur_ball = iter.next();
				if (Math.sqrt(Util.sq_dist(p.data,cur_ball.ball_center)) <= (1.0 + this.eps) * cur_ball.ball_radius) {
					point_in_ballcover = true;
					break;
				}
			}
			if (! point_in_ballcover) {
				need_update = true;
				break;
			}
		}
		
		if (need_update) {
			HashSet<Point> candidate = new HashSet<>();
			candidate.addAll(this.union_coreset);
			candidate.addAll(pointSet.points);
			int next_ball_id = this.blurred_cover.getFirst().ball_id + 1;
			BlurredBall nextBall = new BlurredBall(next_ball_id, new PointSet(this.dim, new ArrayList<>(candidate)));
			
			this.union_coreset.addAll(nextBall.ball_coreset);
			this.blurred_cover.addFirst(nextBall);
			
			System.out.println(this.blurred_cover.getFirst().ball_radius + "," + this.union_coreset.size());
		}
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public void approxMEB() {
		Coreset coreset = new Coreset(new PointSet(this.dim, new ArrayList<>(this.union_coreset)), 1e-6);
		
		this.center = coreset.center;
		this.radius = coreset.radius;
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
	
	class BlurredBall {
		int ball_id;
		double[] ball_center;
		double ball_radius;
		ArrayList<Point> ball_coreset;
		
		BlurredBall(int id, PointSet pointSet) {
			this.ball_id = id;
			
			Coreset coreset = new Coreset(pointSet, eps / 3.0);
			this.ball_center = coreset.center;
			this.ball_radius = coreset.radius;
			this.ball_coreset = coreset.core_points;
		}
	}
}
