package main;

import generator.RandomGenerator;

public class Generator {

	public static void main(String[] args) {
		RandomGenerator.genUniform(2000000, 100);
		RandomGenerator.genNormal(2000000, 100);
		
		RandomGenerator.genUniform(2000000, 1000);
		RandomGenerator.genNormal(2000000, 1000);
		
		RandomGenerator.genUniform(200000, 10000);
		RandomGenerator.genNormal(200000, 10000);
	}

}
