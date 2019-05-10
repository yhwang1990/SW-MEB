package sw_meb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import model.Point;
import model.Util;

public class KernelSWMEB {
	int cur_id;
	private double eps1;
	
	LinkedList<KernelAOMEB> instances;
	List<Point> buffer;
	
	public double time_elapsed;

	public KernelSWMEB(double eps1) {
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
			for (KernelAOMEB inst : instances) {
				inst.append(pointSet);
			}
		}
		
		buffer.addAll(pointSet);
		
		if (buffer.size() >= Util.CHUNK_SIZE) {
			addInstances(buffer);
			buffer.clear();
		}
		long t2 = System.nanoTime();
		time_elapsed += (t2 - t1) / 1e9;
	}
	
	private void addInstances(List<Point> buffer) {
		LinkedList<KernelAOMEB> new_instances = new LinkedList<>();
		int init_batch_id = buffer.size() - Util.BATCH_SIZE;
		int last_inst_idx = init_batch_id;
		KernelAOMEB baseInstance = new KernelAOMEB(buffer.subList(init_batch_id, init_batch_id + Util.BATCH_SIZE), eps1, true);
		
		double beta = Util.EPS_MAX;
		double cur_radius2 = baseInstance.radius2;
		new_instances.addFirst(new KernelAOMEB(baseInstance.idx, baseInstance));
		for (int batch_id = Util.CHUNK_SIZE / Util.BATCH_SIZE - 2; batch_id >= 0; batch_id--) {
			int cur_batch_id = batch_id * Util.BATCH_SIZE;
			baseInstance.append(buffer.subList(cur_batch_id, cur_batch_id + Util.BATCH_SIZE));
			
			if(baseInstance.radius2 / cur_radius2 >= (1.0 + beta) * (1.0 + beta)) {
				new_instances.addFirst(new KernelAOMEB(buffer.get(cur_batch_id).idx, baseInstance));
				cur_radius2 = baseInstance.radius2;
				last_inst_idx = cur_batch_id;
			} else if ((last_inst_idx - cur_batch_id) >= (buffer.size() / Util.MIN_INST)) {
				new_instances.addFirst(new KernelAOMEB(buffer.get(cur_batch_id).idx, baseInstance));
				last_inst_idx = cur_batch_id;
			}
		}
		
		for (KernelAOMEB inst : new_instances) {
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
	
	public int computeNumberPoints() {
		HashSet<Integer> core_idx = new HashSet<>();
		for (KernelAOMEB inst : instances) {
			for (Point p : inst.core_points) {
				core_idx.add(p.idx);
			}
		}
		return core_idx.size();
	}
	
	public String toString() {
		KernelAOMEB inst = instances.getFirst();
		
		StringBuilder builder = new StringBuilder();
		builder.append("radius=").append(Math.sqrt(inst.radius2)).append("\n");
		builder.append("cpu_time=").append(time_elapsed).append("s\n");
		builder.append("coreset_size=").append(inst.core_points.size()).append("\n");
		builder.append("num_points=").append(computeNumberPoints()).append("\n");
		return builder.toString();
	}
}
