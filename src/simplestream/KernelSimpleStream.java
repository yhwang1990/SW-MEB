package simplestream;

import java.util.ArrayList;
import java.util.List;

import model.Point;
import model.Util;

public class KernelSimpleStream {
	public ArrayList<Point> core_points;
	public ArrayList<Double> coefficients;
	public double radius;
	
	private double cNorm;
	private ArrayList<ArrayList<Double>> kernel_matrix;
	
	public double time_elapsed = 0.0;
	
	public KernelSimpleStream(Point initPoint) {
		long t1 = System.nanoTime();
		
		this.core_points = new ArrayList<>();
		this.core_points.add(initPoint);
		
		this.coefficients = new ArrayList<>();
		this.coefficients.add(1.0);
		
		this.radius = 0.0;
		this.cNorm = 1.0;
		
		this.kernel_matrix = new ArrayList<>();
		ArrayList<Double> kernel_vector = new ArrayList<>();
		kernel_vector.add(Util.rbf_eval(initPoint, initPoint));
		this.kernel_matrix.add(kernel_vector);
		
		long t2 = System.nanoTime();
		
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	public KernelSimpleStream(List<Point> pointSet) {
		long t1 = System.nanoTime();
		
		this.core_points = new ArrayList<>();
		this.core_points.add(pointSet.get(0));
		
		this.coefficients = new ArrayList<>();
		this.coefficients.add(1.0);
		
		this.radius = 0.0;
		this.cNorm = 1.0;
		
		this.kernel_matrix = new ArrayList<>();
		ArrayList<Double> kernel_vector = new ArrayList<>();
		kernel_vector.add(Util.rbf_eval(pointSet.get(0), pointSet.get(0)));
		this.kernel_matrix.add(kernel_vector);
		
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
		
		for (int i = 1; i < pointSet.size(); i++) {
			append(pointSet.get(i));
		}
	}
	
	public void append(Point p) {
		long t1 = System.nanoTime();
		double dist = Math.sqrt(Util.dist2wc(core_points, coefficients, p, cNorm));
		if (dist > radius) {
			core_points.add(p);
			
			for (int i = 0; i < coefficients.size(); i++) {
				coefficients.set(i, coefficients.get(i) * (1.0 - 0.5 * (1.0 - radius / dist)));
			}
			coefficients.add(0.5 * (1.0 - radius / dist));
			radius = radius + 0.5 * (dist - radius);
			
			updateKernelMatrix();
			updateCNorm();
			
//			System.out.println(this.core_points.size() + "," + this.radius);
		}
		long t2 = System.nanoTime();
		this.time_elapsed += (t2 - t1) / 1e9;
	}
	
	private void updateKernelMatrix() {
		Point new_point = core_points.get(core_points.size() - 1);
		ArrayList<Double> kernel_vector = new ArrayList<>();
		for (int i = 0; i < core_points.size() - 1; i++) {
			double value = Util.rbf_eval(core_points.get(i), new_point);
			kernel_matrix.get(i).add(value);
			kernel_vector.add(value);
		}
		kernel_vector.add(Util.rbf_eval(new_point, new_point));
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

	public void validate(List<Point> pointSet){
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
		builder.append("radius ").append(radius).append("\n");
		builder.append("time ").append(time_elapsed).append("s\n");
		return builder.toString();
	}
	
	public void output() {
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(this.radius).append("\n");
		
		builder.append("core_indices=(");
		for (int i = 0; i < core_points.size(); i++) {
			builder.append(core_points.get(i).idx).append(" ");
		}
		builder.append(")\n");
		
		double sum = 0.0;
		builder.append("coefficients=(");
		for (int i = 0; i < coefficients.size(); i++) {
			builder.append(coefficients.get(i)).append(" ");
			sum += coefficients.get(i);
		}
		builder.append(")\n");
		builder.append(sum).append("\n");
		
		System.out.print(builder.toString());
	}
}
