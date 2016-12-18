package requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import topology.Edge;

/**
 * 
 * @author heyuan
 */

public class Dataflow {
	@Override
	public String toString() {
		return "Dataflow [edges=" + edges + ", launch=" + launch + ", duration=" + duration + ", period=" + period
				+ "]\n";
	}

	public List<Integer> sender = new ArrayList<>();
	public List<Integer> receive = new ArrayList<>();
	public List<Edge> edges;

	public int launch;
	private int duration;
	private int period;
	public int maxLaunch;
	// public List<Boolean> communiInfo = new ArrayList<>();

	/**
	 * 
	 * @param maxNodePerLev
	 *            各个level的最大节点数
	 */
	public Dataflow(List<Integer> maxNodePerLev) {
		int maxLevel = maxNodePerLev.size();
		Random random = new Random();
		duration = 1;
		period = (int) Math.pow(2, 1 + random.nextInt(10));
		maxLaunch = period - duration;
		do {
			// randomly synthesize source and destination nodes
			int srcLev = 1 + random.nextInt(maxLevel);
			for (int i = 0; i < srcLev; i++) {
				sender.add(random.nextInt(maxNodePerLev.get(i)));
			}
			int dstLev = 1 + random.nextInt(maxLevel);
			for (int i = 0; i < dstLev; i++) {
				receive.add(random.nextInt(maxNodePerLev.get(i)));
			}
		} while (sender.equals(receive) == true);
	}

	public List<Boolean> forwardSlot(int start, int hyperPeriod) {
		List<Boolean> list = new ArrayList<>();
		int times = hyperPeriod / period;
		for (int i = 0; i < times; i++) {

		}
		return null;

	}

}
