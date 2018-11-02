package model;

public final class ArrayPointSet implements PointSet {
	private int d, n;
	private double[] c;

	public ArrayPointSet(int d, int n) {
		this.d = d;
		this.n = n;
		this.c = new double[n * d];
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public int dimension() {
		return d;
	}

	@Override
	public double get(int i, int j) {
		assert 0 <= i && i < n;
		assert 0 <= j && j < d;
		return c[i * d + j];
	}

	public void set(int i, int j, double v) {
		assert 0 <= i && i < n;
		assert 0 <= j && j < d;
		c[i * d + j] = v;
	}

	public String toString() {
		StringBuffer s = new StringBuffer("{");
		for (int i = 0; i < n; ++i) {
			s.append('[');
			for (int j = 0; j < d; ++j) {
				s.append(get(i, j));
				if (j < d - 1)
					s.append(",");
			}
			s.append(']');
			if (i < n - 1)
				s.append(", ");
		}
		s.append('}');
		return s.toString();
	}
}
