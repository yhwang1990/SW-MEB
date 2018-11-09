package model;

public class SparsePoint {
	public int idx;
	public Feature[] data;

	public SparsePoint(int idx, String line) {
		this.idx = idx;
		String[] tokens = line.split(" ");

		this.data = new Feature[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			String[] token_f = tokens[i].split(":");
			Feature feature = new Feature(Integer.parseInt(token_f[0]), Double.parseDouble(token_f[1]));
			this.data[i - 1] = feature;
		}
	}
}
