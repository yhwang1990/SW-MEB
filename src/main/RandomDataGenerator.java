package main;

import generator.RandomGenerator;

public class RandomDataGenerator {

	public static void main(String[] args) {
		int n = Integer.parseInt(args[0]);
		int d = Integer.parseInt(args[1]);
		
		RandomGenerator.genNormal(n, d);
	}

}
