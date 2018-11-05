package model;

public class Point {

	public int idx;
	public double[] data;

	public Point(int idx, double[] data) {
		this.idx = idx;
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idx;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (idx != other.idx)
			return false;
		return true;
	}
}
