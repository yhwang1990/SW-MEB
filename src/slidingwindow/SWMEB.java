package slidingwindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import model.Point;
import model.Util;

public class SWMEB {
	
	int cur_id;
	double eps1, eps2;
	
	LinkedList<Integer> index;
	HashMap<Integer, AppendOnlyMEB> instances;
	
	public double time_elapsed, deletion;

	public SWMEB(double eps1, double eps2) {
		this.cur_id = -1;
		this.eps1 = eps1;
		this.eps2 = eps2;
		
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
			for (AppendOnlyMEB inst : instances.values()) {
				inst.append(pointSet);
			}
		}
		
		AppendOnlyMEB new_inst = new AppendOnlyMEB(pointSet, eps1, true);
			
		index.addLast(new_inst.idx);
		instances.put(new_inst.idx, new_inst);

		List<Integer> to_delete = new ArrayList<>();
		int cur = 0, pre;
		double beta = eps2;
		while (cur < index.size() - 2) {
			pre = cur;
			cur = findNext(cur, beta);
			if (cur - pre > 1) {
				for (int i = pre + 1; i < cur; i++) {
					to_delete.add(index.get(i));
				}
			}
			beta *= 3;
			beta = Math.min(beta, Util.BETA_MAX);
		}

		if (to_delete.size() > 0) {
			ListIterator<Integer> iter = to_delete.listIterator();
			while (iter.hasNext()) {
				Integer del_id = iter.next();
				index.remove(del_id);
				instances.remove(del_id);
			}
		}
		
//		if (cur_id % 1000 == 999) {
//			System.out.println(cur_id);
//			for (int id : index) {
//				System.out.print(instances.get(id).idx + ":" + instances.get(id).radius + " ");
//			}
//			System.out.println();
//		}
		long t2 = System.nanoTime();
		time_elapsed += (t2 - t1) / 1e9;
	}

	int findNext(int cur, double beta) {
		int next;
		double cur_radius = instances.get(index.get(cur).intValue()).radius;
		double nxt_radius = instances.get(index.get(cur + 1).intValue()).radius;
		if (cur_radius / nxt_radius >= 1.0 + beta)
			next = cur + 1;
		else {
			int i = cur + 2;
			nxt_radius = instances.get(index.get(i)).radius;
			while (i < index.size() - 1 && cur_radius / nxt_radius <= 1.0 + beta) {
				i++;
				nxt_radius = instances.get(index.get(i)).radius;
			}
			if (i == index.size() - 1 && cur_radius / nxt_radius <= 1.0d + beta)
				next = i;
			else
				next = i - 1;
		}
//		System.out.println(cur + "," + next + "," + beta);
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
	
	public String toString() {
		AppendOnlyMEB inst = null;
		if (index.get(0) >= cur_id - Util.W + 1) {
			inst = instances.get(index.get(1));
		} else {
			inst = instances.get(index.get(0));
		}
		StringBuilder builder = new StringBuilder();
		builder.append("center ");
		for (int i = 0; i < Util.d - 1; i++) {
			builder.append(inst.center[i]).append(" ");
		}
		builder.append(inst.center[Util.d - 1]).append("\n");
		builder.append("radius ").append(inst.radius).append("\n");
		builder.append("time ").append(time_elapsed).append("s\n");
		return builder.toString();
	}
}
