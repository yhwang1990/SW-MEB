package main;

import generator.RandomGenerator;

public class Generator {

	public static void main(String[] args) {
		RandomGenerator.genUniform(200000, 100);
		RandomGenerator.genNormal(200000, 100);
	}

}
