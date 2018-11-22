package slidingwindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import model.Point;
import model.Util;

public class KernelSWMEB {
	int cur_id;
	double eps1;
	
	LinkedList<Integer> index;
	HashMap<Integer, AppendOnlyKernelMEB> instances;
	
	public double time_elapsed, deletion;

	public KernelSWMEB(double eps1) {
		this.cur_id = -1;
		this.eps1 = eps1;
		
		this.index = new LinkedList<>();
		this.instances = new HashMap<>();
	}

	public void append(List<Point> pointSet) {
		long t1 = System.nanoTime();
		cur_id = pointSet.get(Util.BATCH_SIZE - 1).idx;
		while (index.size() > 2 && index.get(1).intValue() <= cur_id - Util.W) {
			instances.remove(index.removeFirst());
		}

		if (instances.size() > 0) {
			for (AppendOnlyKernelMEB inst : instances.values()) {
				inst.append(pointSet);
			}
		}
		
		AppendOnlyKernelMEB new_inst = new AppendOnlyKernelMEB(pointSet, eps1, true);
			
		index.addLast(new_inst.idx);
		instances.put(new_inst.idx, new_inst);

		List<Integer> to_delete = new ArrayList<>();
		int cur = 0, pre;
		double beta = Util.EPS_MIN;
		while (cur < index.size() - 2) {
			pre = cur;
			cur = findNext(cur, beta);
			if (cur - pre > 1) {
				for (int i = pre + 1; i < cur; i++) {
					to_delete.add(index.get(i));
				}
			}
			beta *= Util.LAMBDA;
			beta = Math.min(beta, Util.EPS_MAX);
		}

		if (to_delete.size() > 0) {
			ListIterator<Integer> iter = to_delete.listIterator();
			while (iter.hasNext()) {
				Integer del_id = iter.next();
				index.remove(del_id);
				instances.remove(del_id);
			}
		}

		long t2 = System.nanoTime();
		time_elapsed += (t2 - t1) / 1e9;
	}

	int findNext(int cur, double beta) {
		int next;
		double cur_radius2 = instances.get(index.get(cur).intValue()).radius2;
		double nxt_radius2 = instances.get(index.get(cur + 1).intValue()).radius2;
		if (cur_radius2 / nxt_radius2 >= (1.0 + beta) * (1.0 + beta))
			next = cur + 1;
		else {
			int i = cur + 2;
			nxt_radius2 = instances.get(index.get(i)).radius2;
			while (i < index.size() - 1 && cur_radius2 / nxt_radius2 <= (1.0 + beta) * (1.0 + beta)) {
				i++;
				nxt_radius2 = instances.get(index.get(i)).radius2;
			}
			if (i == index.size() - 1 && cur_radius2 / nxt_radius2 <= (1.0 + beta) * (1.0 + beta))
				next = i;
			else
				next = i - 1;
		}
		return next;
	}
	
	public void approxMEB() {
		if (index.get(0) >= cur_id - Util.W + 1) {
			instances.get(index.get(1)).approxMEB();
		} else {
			instances.get(index.get(0)).approxMEB();
		}
	}
	
	public void validate(List<Point> pointSet) {
		if (index.get(0) >= cur_id - Util.W + 1) {
			instances.get(index.get(1)).validate(pointSet);
		} else {
			instances.get(index.get(0)).validate(pointSet);
		}
	}

	public void output() {
		if (index.get(0) >= cur_id - Util.W + 1) {
			instances.get(index.get(1)).output();
		} else {
			instances.get(index.get(0)).output();
		}
	}
	
	public int computeCoresetSize() {
		HashSet<Integer> core_idx = new HashSet<>();
		for (AppendOnlyKernelMEB inst : instances.values()) {
			for (Point p : inst.core_points) {
				core_idx.add(p.idx);
			}
		}
		return core_idx.size();
	}
	
	public String toString() {
		AppendOnlyKernelMEB inst = null;
		if (index.get(0) >= cur_id - Util.W + 1) {
			inst = instances.get(index.get(1));
		} else {
			inst = instances.get(index.get(0));
		}
		StringBuilder builder = new StringBuilder();
		builder.append("radius ").append(Math.sqrt(inst.radius2)).append("\n");
		builder.append("time ").append(time_elapsed).append("s\n");
		builder.append("coreset_size ").append(computeCoresetSize()).append("\n");
		return builder.toString();
	}
}
