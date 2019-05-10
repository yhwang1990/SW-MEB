package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import model.Point;
import model.Util;

public class ThetaEstimator {

	public static final int SAMPLE_SIZE = 10000;
	public static final Random RAND = new Random(0);
	
	public static void main(String[] args) throws IOException {
		String data_file = args[0];
		int n = Integer.parseInt(args[1]);
		int d = Integer.parseInt(args[2]);
		
		List<Point> pts = Util.pointsFromStream(data_file, n, d);
		ArrayList<Point> samples = new ArrayList<Point>();
		HashSet<Integer> sample_indices = new HashSet<Integer>();
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			while (true) {
				int idx = RAND.nextInt(pts.size());
				if (! sample_indices.contains(idx)) {
					sample_indices.add(idx);
					samples.add(pts.get(idx));
					break;
				}
			}
		}
		
		double theta = Util.spread_estimator(samples);
		System.out.println(data_file + " " + d + " " + theta);
	}

}
