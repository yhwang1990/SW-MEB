package test;

import java.io.IOException;
import java.util.List;

import dynamic.DynamicMEB;
import model.Point;
import model.Util;

public class DynamicMEBMain {

	public static void main(String[] args) throws IOException {
//		String data_file = args[0];
//		int n = Integer.parseInt(args[1]);
//		int d = Integer.parseInt(args[2]);
//		double eps = Double.parseDouble(args[3]);
		
//		PointSet pts = PointSetUtils.pointsFromStream(data_file, n, d);
		
		List<Point> pts = Util.pointsFromStream("../data/normal-100000-100.txt", 100000, 100);
		double eps = 1e-3;
		System.out.println("dataset size: " + pts.size());

		DynamicMEB coreset = new DynamicMEB(pts.subList(0, 1000), eps);
		
		for (int i = 1000; i < 100000; i++) {
			coreset.insert(pts.get(i));
			
			if (pts.get(i).idx % 1000 == 0) {
				System.out.println(pts.get(i).idx);
				System.out.println("time elapsed: " + coreset.time_elapsed + "s");
			}
		}
		
		coreset.approxMEB();

		System.out.println("time elapsed: " + coreset.time_elapsed + "s");
		coreset.output();
		coreset.validate(pts);
	}

}
