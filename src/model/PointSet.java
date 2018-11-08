package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class PointSet {
	public int dim, num;
	public List<Point> points;

	public PointSet(int d, List<Point> points) {
		this.dim = d;
		this.points = points;
		this.num = this.points.size();
	}
	
	public static final PointSet pointsFromStream(String data_file, int n, int d) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(data_file));
		br.readLine();
		String line = null;
		
		ArrayList<Point> points = new ArrayList<>(n);
		for (int i = 0; i < n; ++i) {
			line = br.readLine();
			String[] tokens = line.split(" ");
			double[] data = new double[d];
			for (int j = 0; j < d; ++j) {
				data[j] = Double.parseDouble(tokens[j]);
			}
			
			points.add(i, new Point(i, data));
		}
		br.close();

		final PointSet pts = new PointSet(d, points);
		return pts;
	}
}
