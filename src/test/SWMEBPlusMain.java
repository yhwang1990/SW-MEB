package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Point;
import model.PointSet;
import model.Util;
//import model.PointSet;
//import model.PointSetUtils;
import slidingwindow.SWMEBPlus;

public class SWMEBPlusMain {

	public static void main(String[] args) throws IOException {
//		String data_file = args[0];
//		int n = Integer.parseInt(args[1]);
//		int d = Integer.parseInt(args[2]);
		
		streamFromFile("../data/normal-100000-100.txt", 100000, 100);
		
//		PointSet pts = PointSetUtils.pointsFromStream(data_file, n, d);
		
//		PointSet pts = PointSetUtils.pointsFromStream("../data/normal-100000-100.txt", 100000, 100);
//		System.out.println("dataset size: " + pts.num);
//
//		SimpleStream simpleStream = new SimpleStream(pts);
//
//		System.out.println("time elapsed: " + simpleStream.time_elapsed + "s");
//		simpleStream.output();
//		simpleStream.validate(pts);
	}
	
	private static void streamFromFile(String filename, int n, int d) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		br.readLine();
		String line = null;
		
		SWMEBPlus swmeb = new SWMEBPlus(d, 1e-3, 1e-3);
		
		List<Point> buffer = new ArrayList<>();
		for (int i = 0; i < n; ++i) {
			line = br.readLine();
			String[] tokens = line.split(" ");
			double[] data = new double[d];
			for (int j = 0; j < d; ++j) {
				data[j] = Double.parseDouble(tokens[j]);
			}
			buffer.add(new Point(i, data));
			
			if (buffer.size() >= Util.BATCH_SIZE) {
				swmeb.append(new PointSet(d, buffer));
				buffer.clear();
			}
		}
		br.close();
		
		System.out.println("time elapsed: " + swmeb.time_elapsed + "s");
		swmeb.output();
	}

}
