package slidingwindow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import model.Point;
import model.Util;

public class KernelSWMEBPlus {
	int cur_id;
	private double eps1;
	
	LinkedList<AppendOnlyKernelMEB> instances;
	List<Point> buffer;
	
	public double time_elapsed;

	public KernelSWMEBPlus(double eps1) {
		this.cur_id = -1;
		this.eps1 = eps1;
		
		this.buffer = new ArrayList<>();
		this.instances = new LinkedList<>();
	}

	public void append(List<Point> pointSet) {
		long t1 = System.nanoTime();
		cur_id = pointSet.get(Util.BATCH_SIZE - 1).idx;
		while (! instances.isEmpty() && instances.getFirst().idx <= cur_id - Util.W) {
			instances.removeFirst();
		}

		if (instances.size() > 0) {
			for (AppendOnlyKernelMEB inst : instances) {
				inst.append(pointSet);
			}
		}
		
		buffer.addAll(pointSet);
		
		if (buffer.size() >= Util.CHUNK_SIZE) {
			addInstances(buffer);
			buffer.clear();
		}
		
		if (cur_id % 10000 == 9999) {
			System.out.println(cur_id);
			for (AppendOnlyKernelMEB inst : instances) {
				System.out.print(inst.idx + ":" + inst.radius2 + " ");
			}
			System.out.println();
		}
		long t2 = System.nanoTime();
		time_elapsed += (t2 - t1) / 1e9;
	}
	
	private void addInstances(List<Point> buffer) {
		LinkedList<AppendOnlyKernelMEB> new_instances = new LinkedList<>();
		int init_batch_id = buffer.size() - Util.BATCH_SIZE;
		AppendOnlyKernelMEB baseInstance = new AppendOnlyKernelMEB(buffer.subList(init_batch_id, init_batch_id + Util.BATCH_SIZE), eps1, true);
		
		double beta = Util.EPS_MAX;
		double cur_radius2 = baseInstance.radius2;
		new_instances.addFirst(new AppendOnlyKernelMEB(baseInstance.idx, baseInstance));
		for (int batch_id = Util.CHUNK_SIZE / Util.BATCH_SIZE - 2; batch_id >= 0; batch_id--) {
			int cur_batch_id = batch_id * Util.BATCH_SIZE;
			baseInstance.append(buffer.subList(cur_batch_id, cur_batch_id + Util.BATCH_SIZE));
			
			if(baseInstance.radius2 / cur_radius2 >= (1.0 + beta) * (1.0 + beta)) {
				new_instances.addFirst(new AppendOnlyKernelMEB(buffer.get(cur_batch_id).idx, baseInstance));
				cur_radius2 = baseInstance.radius2;
				beta /= Util.LAMBDA;
				beta = Math.max(Util.EPS_MIN, beta);
			}
		}
		
		for (AppendOnlyKernelMEB inst : new_instances) {
			instances.addLast(inst);
		}
	}

	public void approxMEB() {
		instances.getFirst().approxMEB();
	}
	
	public void validate(List<Point> pointSet) {
		instances.getFirst().validate(pointSet);
	}

	public void output() {
		instances.getFirst().output();
	}
	
	public int computeCoresetSize() {
		HashSet<Integer> core_idx = new HashSet<>();
		for (AppendOnlyKernelMEB inst : instances) {
			for (Point p : inst.core_points) {
				core_idx.add(p.idx);
			}
		}
		return core_idx.size();
	}
	
	public String toString() {
		AppendOnlyKernelMEB inst = instances.getFirst();
		
		StringBuilder builder = new StringBuilder();
		builder.append("radius ").append(Math.sqrt(inst.radius2)).append("\n");
		builder.append("time ").append(time_elapsed).append("s\n");
		builder.append("coreset_size ").append(computeCoresetSize()).append("\n");
		builder.append("support_size ").append(inst.computeSupportSize()).append("\n");
		return builder.toString();
	}
}
