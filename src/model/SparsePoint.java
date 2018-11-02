package model;

public class SparsePoint {
	public int idx;
	public Feature[] data;

	public SparsePoint(int idx, Feature[] data) {
		this.idx = idx;
		this.data = data;
	}
}
