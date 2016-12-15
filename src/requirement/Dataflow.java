package requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import topology.Edge;

public class Dataflow {
	@Override
	public String toString() {
		return "Dataflow [edges=" + edges + ", duration=" + duration + ", period=" + period + "]";
	}

	public List<Integer> sender = new ArrayList<>();
	public List<Integer> receive = new ArrayList<>();
	public List<Edge> edges;
	public int duration;
	public int period;

	public Dataflow(List<Integer> maxNodeList) {
		int maxLevel = maxNodeList.size();
		Random random = new Random();
		duration = 1;
		period = (int) Math.pow(2, 1 + random.nextInt(10));
		do {
			// randomly synthesize source and destination nodes
			int srcLev = 1 + random.nextInt(maxLevel);
			for (int i = 0; i < srcLev; i++) {
				sender.add(random.nextInt(maxNodeList.get(i)));
			}
			int dstLev = 1 + random.nextInt(maxLevel);
			for (int i = 0; i < dstLev; i++) {
				receive.add(random.nextInt(maxNodeList.get(i)));
			}
		} while (sender.equals(receive) == true);
	}

}
