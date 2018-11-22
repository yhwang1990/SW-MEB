package ballcover;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import coreset.KernelCoreset;
import model.Point;
import model.Util;

public class KernelBlurredBallCover {
	public HashSet<Point> union_coreset;
	public ArrayList<Point> result_coreset;
	public ArrayList<Double> result_coefficients;
	public double cNorm;
	public double radius2;

	public double time_elapsed = 0.0;

	private double eps;
	private LinkedList<KernelBlurredBall> blurred_cover;
	
	public KernelBlurredBallCover(List<Point> initPointSet, double eps, boolean append_mode) {
		this.eps = eps;
		this.union_coreset = new HashSet<>();
		this.result_coreset = new ArrayList<>();
		this.result_coefficients = new ArrayList<>();
		
		this.blurred_cover = new LinkedList<>();
		
		long t1 = System.nanoTime();
		KernelBlurredBall initBall = new KernelBlurredBall(0, initPointSet);
		this.union_coreset.addAll(initBall.ball_coreset);
		this.blurred_cover.addFirst(initBall);
				
//		System.out.println(this.blurred_cover.getFirst().ball_radius + "," + this.union_coreset.size());
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public KernelBlurredBallCover(List<Point> pointSet, double eps) {
		this.eps = eps;
		this.union_coreset = new HashSet<>();
		this.result_coreset = new ArrayList<>();
		this.result_coefficients = new ArrayList<>();
		
		this.blurred_cover = new LinkedList<>();
		
		long t1 = System.nanoTime();
		int batch_id = 0;
		for (batch_id = 0; batch_id < pointSet.size() / Util.BATCH_SIZE; batch_id++) {
			List<Point> next_batch = pointSet.subList(batch_id * Util.BATCH_SIZE, (batch_id + 1) * Util.BATCH_SIZE);
			System.out.println(next_batch.get(0).idx);
			
			if (this.blurred_cover.isEmpty()) {
				KernelBlurredBall initBall = new KernelBlurredBall(0, next_batch);
				this.union_coreset.addAll(initBall.ball_coreset);
				this.blurred_cover.addFirst(initBall);
				
				System.out.println(this.blurred_cover.getFirst().ball_radius2 + "," + this.union_coreset.size());
			} else {
				append(next_batch);
			}
		}
		
		if (batch_id * Util.BATCH_SIZE < pointSet.size()) {
			List<Point> next_batch = pointSet.subList(batch_id * Util.BATCH_SIZE, pointSet.size());
			System.out.println(next_batch.get(0).idx);
			append(next_batch);
		}
		long t2 = System.nanoTime();
		this.time_elapsed = (t2 - t1) / 1e9;
		
		KernelCoreset coreset = new KernelCoreset(new ArrayList<>(this.union_coreset), 1e-6);
		this.radius2 = coreset.radius2;
		this.cNorm = coreset.cNorm;
		for (int idx : coreset.core_indices) {
			this.result_coreset.add(coreset.points.get(idx));
			this.result_coefficients.add(coreset.coefficients[idx]);
		}
	}
	
	public void append(List<Point> pointSet) {
//		System.out.println(pointSet.get(0).idx);
		long t1 = System.nanoTime();
		boolean need_update = false;
		for (Point p : pointSet) {
			boolean point_in_ballcover = false;
			Iterator<KernelBlurredBall> iter = blurred_cover.iterator();
			while (iter.hasNext()) {
				KernelBlurredBall cur_ball = iter.next();
				if (Util.dist2wc(cur_ball.ball_coreset, cur_ball.ball_coefficients, p, cur_ball.ball_cNorm) <= (1.0 + eps) * (1.0 + eps) * cur_ball.ball_radius2) {
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
			candidate.addAll(union_coreset);
			candidate.addAll(pointSet);
			int next_ball_id = blurred_cover.getFirst().ball_id + 1;
			KernelBlurredBall nextBall = new KernelBlurredBall(next_ball_id, new ArrayList<>(candidate));
			
			union_coreset.addAll(nextBall.ball_coreset);
			blurred_cover.addFirst(nextBall);
			
			System.out.println(this.blurred_cover.getFirst().ball_radius2 + "," + this.union_coreset.size());
		}
		long t2 = System.nanoTime();
		time_elapsed += (t2 - t1) / 1e9;
	}
	
	public void approxMEB() {
		KernelCoreset coreset = new KernelCoreset(new ArrayList<>(union_coreset), 1e-6);
		radius2 = coreset.radius2;
		cNorm = coreset.cNorm;
		for (int idx : coreset.core_indices) {
			result_coreset.add(coreset.points.get(idx));
			result_coefficients.add(coreset.coefficients[idx]);
		}
	}
	
	public void validate(List<Point> pointSet) {
		double max_sq_dist = 0.0;
		for (Point point : pointSet) {
			double sq_dist = Util.dist2wc(result_coreset, result_coefficients, point, cNorm);
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
		builder.append("support_size ").append(result_coreset.size()).append("\n");
		return builder.toString();
	}

	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(Math.sqrt(radius2)).append("\n");
		builder.append("squared radius=").append(radius2).append("\n");
		System.out.print(builder.toString());
	}
	
	class KernelBlurredBall {
		int ball_id;
		ArrayList<Double> ball_coefficients;
		double ball_radius2;
		ArrayList<Point> ball_coreset;
		double ball_cNorm;
		
		KernelBlurredBall(int id, List<Point> pointSet) {
			this.ball_id = id;
			this.ball_coreset = new  ArrayList<>();
			this.ball_coefficients = new ArrayList<>();
			
			KernelCoreset coreset = new KernelCoreset(pointSet, eps / 3.0);
			this.ball_radius2 = coreset.radius2;
			this.ball_cNorm = coreset.cNorm;
			for (int idx : coreset.core_indices) {
				this.ball_coreset.add(coreset.points.get(idx));
				this.ball_coefficients.add(coreset.coefficients[idx]);
			}
		}
	}
}
