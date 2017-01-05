package requirement;

import java.util.ArrayList;
import java.util.Arrays;
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
		return "Dataflow [period=" + period + "]";
	}

	public List<Integer> sender = new ArrayList<>();
	public List<Integer> receive = new ArrayList<>();
	public List<Edge> edges;

	// public int launch;
	public int duration;
	public int period;
	// max slot
	public int maxLaunch;

	/**
	 * 
	 * @param maxNodePerLev
	 *            各个level的最大节点数
	 */
	public Dataflow(List<Integer> maxNodePerLev, int minPeriod, int maxPeriod) {
		// 拓扑层数
		Random random = new Random();
		// duration = 1;
		period = (int) Math.pow(2, minPeriod + random.nextInt(maxPeriod - minPeriod));
		// 确保通信时间小于通信周期
		int maxDur = 1;
		do {
			duration = 1 + random.nextInt(maxDur);
		} while (duration > period);
		maxLaunch = period - duration;
		// launch = random.nextInt(maxLaunch);
		do {
			sender = initialNode(maxNodePerLev);
			receive = initialNode(maxNodePerLev);
		} while (sender.equals(receive) == true);
	}

	public List<Boolean> getExist(int start, int hyper, int unit) {
		int slotNum = hyper / unit;
		// 参数标准化
		int periodU = period / unit;
		int durationU = duration / unit;
		// 宏周期内该数据流出现次数
		int times = hyper / period;

		// Random random = new Random();
		// int startU = random.nextInt(periodU - durationU);
		int startU = start / unit;

		Boolean[] slot = new Boolean[slotNum];
		for (int i = 0; i < times; i++) {
			for (int j = 0; j < durationU; j++) {
				slot[i * periodU + startU + j] = true;
			}
		}
		// for (int i = 0; i < times; i++) {
		// for (int j = 0; j < durationU; j++) {
		// // timeSlot.set(i * periodU + j + startU, true);
		// // timeline.add(i * periodU + j + startU);
		// if (temp == (i * periodU + (startU + j))) {
		// timeSlot.add(true);
		// } else {
		// timeSlot.add(false);
		// }
		// temp++;
		// }
		// }
		return Arrays.asList(slot);
	}

	/**
	 * 初始化节点
	 * 
	 * @param topo
	 *            拓扑结构
	 * @return 节点编号
	 */
	private static List<Integer> initialNode(List<Integer> topo) {
		List<Integer> list = new ArrayList<>();
		int maxLev = topo.size();
		Random random = new Random();
		int nodeLev = random.nextInt(maxLev);
		for (int i = 0; i <= nodeLev; i++) {
			list.add(random.nextInt(topo.get(i)));
		}
		return list;
	}

}
