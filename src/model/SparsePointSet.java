package model;

import java.util.ArrayList;

public class SparsePointSet {
		public int dim, num;
		public ArrayList<SparsePoint> points;

		public SparsePointSet(int d, int n) {
			this.dim = d;
			this.num = n;
			this.points = new ArrayList<>(this.num);
		}
}
