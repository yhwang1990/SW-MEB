package model;

public class Util {
	public static final int BATCH_SIZE = 100;
	
	public static final double BETA_MAX = 0.1;
	
	public static int W = 100000;
	public static int CHUNK_SIZE = W / 10;
	public static double GAMMA = 0.1;
	
	public static double sq_dist(double[] d1, double[] d2) {
		double sum_sq = 0.0;
		for (int i = 0; i < d1.length; i++) {
			sum_sq += ((d1[i] - d2[i]) * (d1[i] - d2[i]));
		}
		return sum_sq;
	}
	
	public static double rbf_eval(double[] d1, double[] d2) {
		return Math.exp(-GAMMA * sq_dist(d1, d2));
	}
	
	public static double sparse_sq_dist(Feature[] d1, Feature[] d2) {
		double sum_sq = 0;
		int len1 = d1.length;
		int len2 = d2.length;
		int i = 0;
		int j = 0;
		while (i < len1 && j < len2) {
			if (d1[i].dim == d2[j].dim) {
				double dist = d1[i++].value - d2[j++].value;
				sum_sq += (dist * dist);
			} else if (d1[i].dim > d2[j].dim) {
				sum_sq += (d2[j].value * d2[j].value);
				++j;
			} else {
				sum_sq += (d1[i].value * d1[i].value);
				++i;
			}
		}

		while (i < len1) {
			sum_sq += (d1[i].value * d1[i].value);
			++i;
		}

		while (j < len2) {
			sum_sq += (d2[j].value * d2[j].value);
			++j;
		}
		
		return sum_sq;
	}
	
	public static double sparse_rbf_eval(Feature[] d1, Feature[] d2) {
		return Math.exp(-GAMMA * sparse_sq_dist(d1, d2));
	}
}
