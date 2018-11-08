package model;

public class Util {
	public static final int BATCH_SIZE = 100;
	public static final double BETA_MAX = 0.1;
	
	public static int W = 100000;
	
	public static double sq_dist(double[] d1, double[] d2) {
		double sum_sq = 0.0;
		for (int i = 0; i < d1.length; i++) {
			sum_sq += ((d1[i] - d2[i]) * (d1[i] - d2[i]));
		}
		return sum_sq;
	}
}
