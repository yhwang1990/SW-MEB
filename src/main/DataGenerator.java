package main;

import generator.RandomGenerator;

public class DataGenerator {

	public static void main(String[] args) {
		int n = Integer.parseInt(args[1]);
		int d = Integer.parseInt(args[2]);

		switch (args[0]) {
		case "Uniform":
			RandomGenerator.genUniform(n, d);
			break;
		case "Normal":
			RandomGenerator.genNormal(n, d);
			break;
		default:
			System.err.println("Invalid distribution type.");
			System.exit(0);
		}
	}

}
