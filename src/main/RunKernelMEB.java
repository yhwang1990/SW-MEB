package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import blurred_ball_cover.KernelBlurredBallCover;
import core_meb.KernelCoreMEB;
import model.Point;
import model.Util;
import ss_meb.KernelSSMEB;
import sw_meb.KernelAOMEB;

public class RunKernelMEB {
	
	public static final String GAMMA_FILE = "data/gamma.txt";
	
	public static void main(String[] args) {
		String algorithm = args[0];
		String data_file = args[1];
		int N = Integer.parseInt(args[2]);
		Util.W = Integer.parseInt(args[3]);
		Util.d = Integer.parseInt(args[4]);
		double eps;
		
		read_gamma_value(data_file, Util.d);

		switch (algorithm) {
		case "AOMEB":
			eps = Double.parseDouble(args[5]);
			run_append_only_meb(data_file, N, eps);
			break;
		case "BBC":
			eps = Double.parseDouble(args[5]);
			run_blurred_ball_cover(data_file, N, eps);
			break;
		case "CoreMEB":
			eps = Double.parseDouble(args[5]);
			run_coreset_meb(data_file, N, eps);
			break;
		case "SSMEB":
			run_simple_stream_meb(data_file, N);
			break;
		default:
			System.err.println("Invalid Algorithm Name");
			System.exit(0);
		}
	}

	private static void read_gamma_value(String data_file, int d) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(GAMMA_FILE));
			String line;
			while ((line = br.readLine()) != null) {
				if(line.startsWith(data_file + " " + d)) {
					String[] tokens = line.split(" ");
					Util.GAMMA = Double.parseDouble(tokens[2]);
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void run_append_only_meb(String data_file, int n, double eps) {
		LinkedList<Point> buffer = new LinkedList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(data_file));
			String line;
			int offset = Util.W / 10;
			for (int i = 0; i < n; i++) {
				line = br.readLine();
				String[] tokens = line.split(" ");
				double[] data = new double[Util.d];
				for (int j = 0; j < Util.d; ++j) {
					data[j] = Double.parseDouble(tokens[j]);
				}
				
				buffer.addLast(new Point(i, data));
				while (buffer.size() > Util.W) {
					buffer.removeFirst();
				}
				
				if ((i + 1) > Util.W && (i - Util.W + 1) % offset == 0) {
					KernelAOMEB inst = new KernelAOMEB(new ArrayList<>(buffer), eps);
					System.out.println("AOMEB");
					System.out.println(data_file + " " + Util.W + " " + Util.d + " " + eps);
					System.out.println(i);
					System.out.print(inst.toString());
					inst.validate(buffer);
					System.out.println();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void run_blurred_ball_cover(String data_file, int n, double eps) {
		LinkedList<Point> buffer = new LinkedList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(data_file));
			String line;
			int offset = Util.W / 10;
			for (int i = 0; i < n; i++) {
				line = br.readLine();
				String[] tokens = line.split(" ");
				double[] data = new double[Util.d];
				for (int j = 0; j < Util.d; ++j) {
					data[j] = Double.parseDouble(tokens[j]);
				}
				
				buffer.addLast(new Point(i, data));
				while (buffer.size() > Util.W) {
					buffer.removeFirst();
				}
				
				if ((i + 1) > Util.W && (i - Util.W + 1) % offset == 0) {
					KernelBlurredBallCover inst = new KernelBlurredBallCover(new ArrayList<>(buffer), eps);
					System.out.println("BBC");
					System.out.println(data_file + " " + Util.W + " " + Util.d + " " + eps);
					System.out.println(i);
					System.out.print(inst.toString());
					inst.validate(buffer);
					System.out.println();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void run_coreset_meb(String data_file, int n, double eps) {
		LinkedList<Point> buffer = new LinkedList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(data_file));
			String line;
			int offset = Util.W / 10;
			for (int i = 0; i < n; i++) {
				line = br.readLine();
				String[] tokens = line.split(" ");
				double[] data = new double[Util.d];
				for (int j = 0; j < Util.d; ++j) {
					data[j] = Double.parseDouble(tokens[j]);
				}
				
				buffer.addLast(new Point(i, data));
				while (buffer.size() > Util.W) {
					buffer.removeFirst();
				}
				
				if ((i + 1) > Util.W && (i - Util.W + 1) % offset == 0) {
					KernelCoreMEB inst = new KernelCoreMEB(new ArrayList<>(buffer), eps);
					System.out.println("CoreMEB");
					System.out.println(data_file + " " + Util.W + " " + Util.d + " " + eps);
					System.out.println(i);
					System.out.print(inst.toString());
					inst.validate(buffer);
					System.out.println();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void run_simple_stream_meb(String data_file, int n) {
		LinkedList<Point> buffer = new LinkedList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(data_file));
			String line;
			int offset = Util.W / 10;
			for (int i = 0; i < n; i++) {
				line = br.readLine();
				String[] tokens = line.split(" ");
				double[] data = new double[Util.d];
				for (int j = 0; j < Util.d; ++j) {
					data[j] = Double.parseDouble(tokens[j]);
				}
				
				buffer.addLast(new Point(i, data));
				while (buffer.size() > Util.W) {
					buffer.removeFirst();
				}
				
				if ((i + 1) > Util.W && (i - Util.W + 1) % offset == 0) {
					KernelSSMEB inst = new KernelSSMEB(new ArrayList<>(buffer));
					System.out.println("SSMEB");
					System.out.println(data_file + " " + Util.W + " " + Util.d);
					System.out.println(i);
					System.out.print(inst.toString());
					inst.validate(buffer);
					System.out.println();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
