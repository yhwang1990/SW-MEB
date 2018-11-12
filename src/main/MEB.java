package main;

public class MEB {

	public static void main(String[] args) {
		String algorithm = args[0];
		String data_file = args[1];
		int N = Integer.parseInt(args[2]);
		int W = Integer.parseInt(args[3]);
		int d = Integer.parseInt(args[4]);
		double eps;

		switch (algorithm) {
		case "AppendOnly":
			eps = Double.parseDouble(args[5]);
			run_append_only_meb(data_file, N, W, d, eps);
			break;
		case "BlurredBallCover":
			eps = Double.parseDouble(args[5]);
			run_blurred_ball_cover(data_file, N, W, d, eps);
			break;
		case "Coreset":
			eps = Double.parseDouble(args[5]);
			run_coreset_meb(data_file, N, W, d, eps);
			break;
		case "Dynamic":
			eps = Double.parseDouble(args[5]);
			run_dynamic_meb(data_file, N, W, d, eps);
			break;
		case "SimpleStream":
			run_simple_stream_meb(data_file, N, W, d);
			break;
		case "SWMEB":
			eps = Double.parseDouble(args[5]);
			run_sw_meb(data_file, N, W, d, eps);
			break;
		case "SWMEB+":
			eps = Double.parseDouble(args[5]);
			run_sw_meb_plus(data_file, N, W, d, eps);
			break;
		default:
			System.err.println("Invalid Algorithm");
			System.exit(0);
		}
	}

	private static void run_append_only_meb(String data_file, int n, int w, int d, double eps) {
	}

	private static void run_blurred_ball_cover(String data_file, int n, int w, int d, double eps) {
	}

	private static void run_coreset_meb(String data_file, int n, int w, int d, double eps) {
	}

	private static void run_dynamic_meb(String data_file, int n, int w, int d, double eps) {
	}

	private static void run_simple_stream_meb(String data_file, int n, int w, int d) {
	}

	private static void run_sw_meb(String data_file, int n, int w, int d, double eps1) {
	}

	private static void run_sw_meb_plus(String data_file, int n, int w, int d, double eps1) {
	}
}
