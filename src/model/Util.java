package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Util {
	public static final int BATCH_SIZE = 100;
	
	public static final double BETA_MAX = 0.1;
	
	public static int W = 100000;
	public static int d = 10;
	public static int CHUNK_SIZE = W / 10;
	public static double GAMMA = 0.1;
	
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
	
	public static double rbf_eval(double[] d1, double[] d2) {
		return Math.exp(-GAMMA * dist2(d1, d2));
	}
	
	public static final List<Point> pointsFromStream(String data_file, int n, int d) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(data_file));
		String line = null;
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
}
