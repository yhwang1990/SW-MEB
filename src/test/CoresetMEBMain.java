package test;

import java.io.IOException;

import coreset.Coreset;

import model.PointSet;

public class CoresetMEBMain {

	public static void main(String[] args) throws IOException {
		String data_file = args[0];
		int n = Integer.parseInt(args[1]);
		int d = Integer.parseInt(args[2]);
		double eps = Double.parseDouble(args[3]);
		PointSet pts = PointSet.pointsFromStream(data_file, n, d);
		
//		PointSet pts = PointSetUtils.pointsFromStream("../data/normal-100000-100.txt", 100000, 100);
		System.out.println("dataset size: " + pts.num);

		Coreset coreset = new Coreset(pts, eps);

		System.out.println("time elapsed: " + coreset.time_elapsed + "s");
		coreset.output();
		coreset.validate(pts);
	}

}
