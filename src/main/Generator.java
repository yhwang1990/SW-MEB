package main;

import generator.RandomGenerator;

public class Generator {

	public static void main(String[] args) {
		RandomGenerator.genUniform(100000, 10000);
		RandomGenerator.genNormal(100000, 10000);
	}

}
