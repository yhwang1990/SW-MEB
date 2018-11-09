package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class SparsePointSet {
	public int num;
	public List<SparsePoint> points;

	public SparsePointSet(List<SparsePoint> points) {
		this.num = points.size();
		this.points = new ArrayList<>(this.num);
	}
	
	public static final SparsePointSet sparsePointsFromStream(String data_file, int n) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(data_file));
		br.readLine();
		String line = null;
		
		ArrayList<SparsePoint> points = new ArrayList<>(n);
		for (int i = 0; i < n; ++i) {
			line = br.readLine();
			SparsePoint sp = new SparsePoint(i, line);
			points.add(i, sp);
		}
		br.close();

		final SparsePointSet pts = new SparsePointSet(points);
		return pts;
	}
}
