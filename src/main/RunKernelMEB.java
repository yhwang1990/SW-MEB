package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import blurred_ball_cover.KernelBlurredBallCover;
import core_meb.KernelCoreMEB;
import dyn_meb.KernelDynMEB;
import model.Point;
import model.Util;
import ss_meb.KernelSSMEB;
import sw_meb.KernelAOMEB;
import sw_meb.KernelSWMEB;
import sw_meb.KernelSWMEB_Plus;

public class RunKernelMEB {
	
	public static final String GAMMA_FILE = "data/gamma.txt";
	
	public static void main(String[] args) {
		String algorithm = args[0];
		String data_file = args[1];
		int N = Integer.parseInt(args[2]);
		Util.W = Integer.parseInt(args[3]);
		Util.d = Integer.parseInt(args[4]);
		
		read_gamma_value(data_file, Util.d);
		
		double eps = 1e-4;
		if (! algorithm.equals("SSMEB")) {
			eps = Double.parseDouble(args[5]);
		}
		Util.EPS_MIN = eps / 10.0;

		switch (algorithm) {
		case "AOMEB":
			run_append_only_meb(data_file, N, eps);
			break;
		case "BBC":
			run_blurred_ball_cover(data_file, N, eps);
			break;
		case "CoreMEB":
			run_coreset_meb(data_file, N, eps);
			break;
		case "DynMEB":
			run_dynamic_meb(data_file, N, eps);
			break;
		case "SSMEB":
			run_simple_stream_meb(data_file, N);
			break;
		case "SWMEB":
			run_sw_meb(data_file, N, eps);
			break;
		case "SWMEB+":
			run_sw_meb_plus(data_file, N, eps);
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
	
	private static void run_dynamic_meb(String data_file, int n, double eps) {
		Util.C = (int) (10 * Math.log(Util.W));
		Util.ALPHA = eps;
		Util.DELTA = eps;
		LinkedList<Point> buffer = new LinkedList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(data_file));
			String line;
			int offset = Util.W / 10;
			KernelDynMEB inst = null;
			for (int i = 0; i < n; i++) {
				line = br.readLine();
				String[] tokens = line.split(" ");
				double[] data = new double[Util.d];
				for (int j = 0; j < Util.d; ++j) {
					data[j] = Double.parseDouble(tokens[j]);
				}
				
				Point new_point = new Point(i, data);
				Point expired_point = null;
				buffer.addLast(new_point);
				if (buffer.size() > Util.W) {
					expired_point = buffer.removeFirst();
				}
				if (i + 1 > Util.W && (i - Util.W + 1) % offset == 0) {
					inst = new KernelDynMEB(new ArrayList<>(buffer), eps);
					inst.approxMEB();
					System.out.println("DynMEB");
					System.out.println(data_file + " " + Util.W + " " + Util.d + " " + eps);
					System.out.println(i);
					System.out.print(inst.toString());
					inst.validate(buffer);
				} else if (inst != null && i + 1 > Util.W && (i - Util.W + 1) % offset > 0 && (i - Util.W + 1) % offset <= 10) {
					inst.insert(new_point);
					inst.delete(expired_point);
					
					if ((i - Util.W + 1) % offset == 10) {
						System.out.println(inst.statTime());
					}
				}
			}
			System.out.println();
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
	
	private static void run_sw_meb(String data_file, int n, double eps1) {
		LinkedList<Point> buffer = new LinkedList<>();
		KernelSWMEB inst = new KernelSWMEB(eps1);
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
					System.out.println("SWMEB");
					System.out.println(data_file + " " + Util.W + " " + Util.d + " " + eps1);
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

	private static void run_sw_meb_plus(String data_file, int n, double eps1) {
		LinkedList<Point> buffer = new LinkedList<>();
		KernelSWMEB_Plus inst = new KernelSWMEB_Plus(eps1);
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
					System.out.println("SWMEB+");
					System.out.println(data_file + " " + Util.W + " " + Util.d + " " + eps1);
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
