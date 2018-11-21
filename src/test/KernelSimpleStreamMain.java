package test;

import java.io.IOException;
import java.util.List;

import model.Point;
import model.Util;
import simplestream.KernelSimpleStream;

public class KernelSimpleStreamMain {

	public static void main(String[] args) throws IOException {
//		String data_file = args[0];
//		int n = Integer.parseInt(args[1]);
//		int d = Integer.parseInt(args[2]);
//		streamFromFile(data_file, n, d, 0);
		
//		streamFromFile("../data/normal/normal-2000000-100.txt", 100000, 100, 0);
		
//		PointSet pts = PointSetUtils.pointsFromStream(data_file, n, d);
		
		List<Point> pts = Util.pointsFromStream("../data/normal/normal-2000000-100.txt", 100000, 100);
		KernelSimpleStream simpleStream = new KernelSimpleStream(pts);
		System.out.println("time elapsed: " + simpleStream.time_elapsed + "s");
		simpleStream.output();
		simpleStream.validate(pts);
	}
	
//	private static void streamFromFile(String filename, int n, int d, int start_id) throws IOException {
//		BufferedReader br = new BufferedReader(new FileReader(filename));
//		br.readLine();
//		String line = null;
//		
//		for (int i = 0; i < start_id; ++i) {
//			line = br.readLine();
//		}
//		System.out.println(start_id);
//		
//		line = br.readLine();
//		String[] tokens = line.split(" ");
//		double[] data0 = new double[d];
//		for (int j = 0; j < d; ++j) {
//			data0[j] = Double.parseDouble(tokens[j]);
//		}
//		KernelizedSimpleStream simpleStream = new KernelizedSimpleStream(new Point(0, data0));
//		
//		for (int i = 1; i < n; ++i) {
//			line = br.readLine();
//			tokens = line.split(" ");
//			double[] data = new double[d];
//			for (int j = 0; j < d; ++j) {
//				data[j] = Double.parseDouble(tokens[j]);
//			}
//			simpleStream.append(new Point(i, data));
//		}
//		br.close();
//		
//		System.out.println("time elapsed: " + simpleStream.time_elapsed + "s");
//		simpleStream.output();
//	}

}
