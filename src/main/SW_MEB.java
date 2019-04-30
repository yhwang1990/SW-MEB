package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.Point;
import model.Util;
import slidingwindow.SWMEB;
import slidingwindow.SWMEBPlus;

public class SW_MEB {
	public static void main(String[] args) {
		String algorithm = args[0];
		String data_file = args[1];
		
		int N = Integer.parseInt(args[2]);
		Util.W = Integer.parseInt(args[3]);
		Util.d = Integer.parseInt(args[4]);
		Util.CHUNK_SIZE = Util.W /10;
		
		double eps1 = Double.parseDouble(args[5]);
		Util.EPS_MIN = eps1 / 10.0;
		Util.LAMBDA = Double.parseDouble(args[6]);
		Util.MIN_INST = Integer.parseInt(args[6]);
		Util.EPS_MAX = Double.parseDouble(args[7]);

		switch (algorithm) {
		case "SWMEB":
			run_sw_meb(data_file, N, eps1);
			break;
		case "SWMEB+":
			run_sw_meb_plus(data_file, N, eps1);
			break;
		default:
			System.err.println("Invalid Algorithm");
			System.exit(0);
		}
	}

	private static void run_sw_meb(String data_file, int n, double eps1) {
		LinkedList<Point> buffer = new LinkedList<>();
		SWMEB inst = new SWMEB(eps1);
		try {
			BufferedReader br = new BufferedReader(new FileReader(data_file));
			String line;
			int offset = Util.W / 10;
			
			List<Point> inst_buffer = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				line = br.readLine();
				String[] tokens = line.split(" ");
				double[] data = new double[Util.d];
				for (int j = 0; j < Util.d; ++j) {
					data[j] = Double.parseDouble(tokens[j]);
				}
				Point new_point = new Point(i, data);
				
				buffer.addLast(new_point);
				while (buffer.size() > Util.W) {
					buffer.removeFirst();
				}
				
				inst_buffer.add(new_point);
				
				if (inst_buffer.size() >= Util.BATCH_SIZE) {
					inst.append(inst_buffer);
					inst_buffer.clear();
				}
				
				if ((i + 1) > Util.W && (i - Util.W + 1) % offset == 0) {
					System.out.println("SWMEB " + data_file + " " + Util.W + " " + Util.d + " " + eps1 + " " + Util.LAMBDA + " " + Util.EPS_MAX);
					System.out.println(i);
					System.out.print(inst.toString());
//					inst.validate(buffer);
					System.out.println();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void run_sw_meb_plus(String data_file, int n, double eps1) {
		LinkedList<Point> buffer = new LinkedList<>();
		SWMEBPlus inst = new SWMEBPlus(eps1);
		try {
			BufferedReader br = new BufferedReader(new FileReader(data_file));
			String line;
			int offset = Util.W / 10;
			
			List<Point> inst_buffer = new ArrayList<>();
			for (int i = 0; i < n; i++) {
				line = br.readLine();
				String[] tokens = line.split(" ");
				double[] data = new double[Util.d];
				for (int j = 0; j < Util.d; ++j) {
					data[j] = Double.parseDouble(tokens[j]);
				}
				Point new_point = new Point(i, data);
				
				buffer.addLast(new_point);
				while (buffer.size() > Util.W) {
					buffer.removeFirst();
				}
				
				inst_buffer.add(new_point);
				
				if (inst_buffer.size() >= Util.BATCH_SIZE) {
					inst.append(inst_buffer);
					inst_buffer.clear();
				}
				
				if ((i + 1) > Util.W && (i - Util.W + 1) % offset == 0) {
					System.out.println("SWMEB+ " + data_file + " " + Util.W + " " + Util.d + " " + eps1 + " " + Util.LAMBDA + " " + Util.EPS_MAX);
					System.out.println(i);
					System.out.print(inst.toString());
//					inst.validate(buffer);
					System.out.println();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
