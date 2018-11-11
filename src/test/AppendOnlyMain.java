package test;

//import java.io.BufferedReader;
//import java.io.FileReader;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;

//import model.Point;
import model.PointSet;
import slidingwindow.AppendOnlyMEB;

public class AppendOnlyMain {

	public static void main(String[] args) throws IOException {
//		String data_file = args[0];
//		int n = Integer.parseInt(args[1]);
//		int d = Integer.parseInt(args[2]);
//		double eps = Double.parseDouble(args[3]);
//		streamFromFile(data_file, n, d, 0, eps);
		
//		PointSet pts = PointSetUtils.pointsFromStream(data_file, n, d);
		
		PointSet pts = PointSet.pointsFromStream("../data/normal-100000-100.txt", 100000, 100);
		double eps = 1e-3;
		System.out.println("dataset size: " + pts.num);

		AppendOnlyMEB coreset = new AppendOnlyMEB(pts, eps);

		System.out.println("time elapsed: " + coreset.time_elapsed + "s");
		coreset.output();
		coreset.validate(pts);
	}
	
//	private static void streamFromFile(String filename, int n, int d, int start_id, double eps) throws IOException {
//		BufferedReader br = new BufferedReader(new FileReader(filename));
//		br.readLine();
//		String line = null;
//		
//		for (int i = 0; i < start_id; ++i) {
//			line = br.readLine();
//		}
//		System.out.println(start_id);
//		
//		int idx = 0;
//		List<Point> init_points = new ArrayList<>();
//		for (int i = 0; i < AppendOnlyMEB.BATCH_SIZE; i++) {
//			line = br.readLine();
//			String[] tokens = line.split(" ");
//			double[] data = new double[d];
//			for (int j = 0; j < d; ++j) {
//				data[j] = Double.parseDouble(tokens[j]);
//			}
//			init_points.add(new Point(idx++, data));
//		}
//		AppendOnlyMEB ballCover = new AppendOnlyMEB(new PointSet(d, init_points), eps, true);
//		
//		for (int batch = 1; batch < n / AppendOnlyMEB.BATCH_SIZE; ++batch) {
//			List<Point> points = new ArrayList<>();
//			for (int i = 0; i < AppendOnlyMEB.BATCH_SIZE; i++) {
//				line = br.readLine();
//				String[] tokens = line.split(" ");
//				double[] data = new double[d];
//				for (int j = 0; j < d; ++j) {
//					data[j] = Double.parseDouble(tokens[j]);
//				}
//				points.add(new Point(idx++, data));
//			}
//			ballCover.append(new PointSet(d, points));
//		}
//		ballCover.approxMEB();
//		br.close();
//		
//		System.out.println("time elapsed: " + ballCover.time_elapsed + "s");
//		ballCover.output();
//	}

}
