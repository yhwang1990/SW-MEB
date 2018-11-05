package model;

import java.util.List;

public final class PointSet {
	public int dim, num;
	public List<Point> points;

	public PointSet(int d, List<Point> points) {
		this.dim = d;
		this.points = points;
		this.num = this.points.size();
	}
}
