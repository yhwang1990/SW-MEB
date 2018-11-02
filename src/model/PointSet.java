package model;

import java.util.ArrayList;

public final class PointSet {
	public int dim, num;
	public ArrayList<Point> points;

	public PointSet(int d, ArrayList<Point> points) {
		this.dim = d;
		this.points = points;
		this.num = this.points.size();
	}
}
