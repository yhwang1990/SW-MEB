package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import model.Point;
import model.PointSet;
import model.PointSetUtils;
import simplestream.SimpleStream;

public class SimpleStreamMain {

	public static void main(String[] args) throws IOException {
//		String data_file = args[0];
//		int n = Integer.parseInt(args[1]);
//		int d = Integer.parseInt(args[2]);
		
//		streamFromFile(data_file, n, d, 0);
		
//		PointSet pts = PointSetUtils.pointsFromStream(data_file, n, d);
		
		PointSet pts = PointSetUtils.pointsFromStream("../data/normal-100000-100.txt", 100000, 100);
		System.out.println("dataset size: " + pts.num);

		SimpleStream simpleStream = new SimpleStream(pts);

		System.out.println("time elapsed: " + simpleStream.time_elapsed + "s");
		simpleStream.output();
		simpleStream.validate(pts);
	}
	
	private static void streamFromFile(String filename, int n, int d, int start_id) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		br.readLine();
		String line = null;
		
		for (int i = 0; i < start_id; ++i) {
			line = br.readLine();
		}
		System.out.println(start_id);
		
		line = br.readLine();
		String[] tokens = line.split(" ");
		double[] data0 = new double[d];
		for (int j = 0; j < d; ++j) {
			data0[j] = Double.parseDouble(tokens[j]);
		}
		SimpleStream simpleStream = new SimpleStream(d, new Point(0, data0));
		
		for (int i = 1; i < n; ++i) {
			line = br.readLine();
			tokens = line.split(" ");
			double[] data = new double[d];
			for (int j = 0; j < d; ++j) {
				data[j] = Double.parseDouble(tokens[j]);
			}
			simpleStream.append(new Point(i, data));
		}
		br.close();
		
		System.out.println("time elapsed: " + simpleStream.time_elapsed + "s");
		simpleStream.output();
	}

}
