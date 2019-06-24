package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Util {
	public static final int BATCH_SIZE = 100;
	
	public static double EPS_MAX = 0.1;
	public static double EPS_MIN = 1e-6;
	public static double LAMBDA = 4.0;
	public static int MIN_INST = 10;
	
	public static int W = 100000;
	public static int d = 10;
	
	public static int CHUNK_SIZE = W / 10;
	
	public static double GAMMA = 0;
	
	//Parameters for dynamic MEB
	public static int C = (int) (10 * Math.log(W));
	public static double ALPHA = 1e-3;
	public static double DELTA = 1e-3;
	
	public static double dist2(double[] d1, double[] d2) {
		double sum_sq = 0.0;
		for (int i = 0; i < d1.length; i++) {
			sum_sq += ((d1[i] - d2[i]) * (d1[i] - d2[i]));
		}
		return sum_sq;
	}
	
	public static double rbf_eval(Point p1, Point p2) {
		if (p1.idx == p2.idx)
			return 1.0;
		return Math.exp(- dist2(p1.data, p2.data) / GAMMA);
	}
	
	public static double k_dist2(Point p1, Point p2) {
		return (2.0 - 2.0 * rbf_eval(p1, p2));
	}
	
	public static double dist2wc(List<Point> pts, List<Double> coeff, Point p, double cNorm) {
		double dist2 = 0.0;
		for (int i = 0; i < pts.size(); i++) {
			dist2 += (coeff.get(i) * rbf_eval(p, pts.get(i)));
		}
		dist2  = 1.0 + cNorm - 2.0 * dist2;
		return dist2;
	}
	
	public static double dist2wc(HashSet<Point> pts, HashMap<Integer, Double> coeff, Point p1, double cNorm) {
		double dist2 = 0.0;
		for (Point p2 : pts) {
			dist2 += (coeff.get(p2.idx) * rbf_eval(p1, p2));
		}
		dist2  = 1.0 + cNorm - 2.0 * dist2;
		return dist2;
	}
	
//	public static double dist2wc(List<Point> pts, double[] coeff, Point p, double cNorm) {
//		double dist2 = 0.0;
//		for (int i = 0; i < pts.size(); i++) {
//			dist2 += (coeff[i] * rbf_eval(p, pts.get(i)));
//		}
//		dist2  = 1.0 + cNorm - 2.0 * dist2;
//		return dist2;
//	}
	
	public static List<Point> pointsFromStream(String data_file, int n, int d) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(data_file));
		String line;
		ArrayList<Point> points = new ArrayList<>(n);
		for (int i = 0; i < n; ++i) {
			line = br.readLine();
			String[] tokens = line.split(" ");
			double[] data = new double[d];
			for (int j = 0; j < d; ++j) {
				data[j] = Double.parseDouble(tokens[j]);
			}
			
			points.add(i, new Point(i, data));
		}
		br.close();
		return points;
	}
	
	public static double kernel_width_estimator(List<Point> points) {
		double size = points.size() * points.size();
		double sum_dist2  = 0.0;
		for (Point p1 : points) {
			for (Point p2 : points) {
				sum_dist2 += dist2(p1.data, p2.data);
			}
		}
		return sum_dist2 / size;
	}
	
	public static double spread_estimator(List<Point> points) {
		double max_dist2 = 0.0, min_dist2 = Double.MAX_VALUE;
		for (int i = 0; i < points.size(); i++) {
			for (int j = 0; j < points.size(); j++) {
				double dist2 = dist2(points.get(i).data, points.get(j).data);
				if (dist2 > max_dist2) {
					max_dist2 = dist2;
				}
				if (dist2 < min_dist2 && i != j && dist2 > 0) {
					min_dist2 = dist2;
				}
			}
		}
		return Math.sqrt(max_dist2 / min_dist2);
	}
}
