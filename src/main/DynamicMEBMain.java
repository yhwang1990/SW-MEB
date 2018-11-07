package main;

import java.io.IOException;

import dynamic.DynamicMEB;
import model.PointSet;
import model.PointSetUtils;

public class DynamicMEBMain {

	public static void main(String[] args) throws IOException {
//		String data_file = args[0];
//		int n = Integer.parseInt(args[1]);
//		int d = Integer.parseInt(args[2]);
//		double eps = Double.parseDouble(args[3]);
		
//		PointSet pts = PointSetUtils.pointsFromStream(data_file, n, d);
		
		PointSet pts = PointSetUtils.pointsFromStream("../data/normal-100000-100.txt", 100000, 100);
		double eps = 1e-3;
		System.out.println("dataset size: " + pts.num);

		DynamicMEB coreset = new DynamicMEB(new PointSet(100, pts.points.subList(0, 1000)), eps);
		
		for (int i = 1000; i < 100000; i++) {
			coreset.insert(pts.points.get(i));
			
			if (pts.points.get(i).idx % 1000 == 0) {
				System.out.println(pts.points.get(i).idx);
				System.out.println("time elapsed: " + coreset.time_elapsed + "s");
			}
		}
		
		coreset.approxMEB();

		System.out.println("time elapsed: " + coreset.time_elapsed + "s");
		coreset.output();
		coreset.validate(pts);
	}

}
