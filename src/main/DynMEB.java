package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import dynamic.DynamicMEB;
import model.Point;
import model.Util;

public class DynMEB {
	public static void main(String[] args) {
		String algorithm = args[0];
		String data_file = args[1];
		int N = Integer.parseInt(args[2]);
		Util.W = Integer.parseInt(args[3]);
		Util.d = Integer.parseInt(args[4]);
		double eps = Double.parseDouble(args[5]);

		switch (algorithm) {
		case "Dynamic":
			run_dynamic_meb(data_file, N, eps);
			break;
		default:
			System.err.println("Invalid Algorithm");
			System.exit(0);
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
			DynamicMEB inst = null;
			for (int i = 0; i < n; i++) {
				line = br.readLine();
				String[] tokens = line.split(" ");
				double[] data = new double[Util.d];
				for (int j = 0; j < Util.d; ++j) {
					data[j] = Double.parseDouble(tokens[j]);
				}
				
				Point new_point = new Point(i, data);
//				Point  expired_point = null;
				buffer.addLast(new_point);
				if (buffer.size() > Util.W) {
//					expired_point = buffer.removeFirst();
					buffer.removeFirst();
				}
				
				if (i + 1 > Util.W && (i - Util.W + 1) % offset == 0) {
					inst = new DynamicMEB(new ArrayList<>(buffer), eps);
//					inst.approxMEB();
					System.out.println("Dynamic " + data_file + " " + Util.W + " " + Util.d + " " + eps);
					System.out.println(i);
					System.out.print(inst.toString());
//					inst.validate(buffer);
					System.out.println();
				}
//				} else if (inst != null && i + 1 > Util.W && (i - Util.W + 1) % offset > 0 && (i - Util.W + 1) % offset <= 10) {
//					inst.insert(new_point);
//					inst.delete(expired_point);
//					
//					if ((i - Util.W + 1) % offset == 10) {
//						System.out.println(inst.statTime());
//					}
//				}
			}
			System.out.println();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
