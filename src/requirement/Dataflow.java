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
	 *            ����level�����ڵ���
	 */
	public Dataflow(List<Integer> maxNodePerLev, int minPeriod, int maxPeriod) {
		// ���˲���
		Random random = new Random();
		// duration = 1;
		period = (int) Math.pow(2, minPeriod + random.nextInt(maxPeriod - minPeriod));
		// ȷ��ͨ��ʱ��С��ͨ������
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
		// ������׼��
		int periodU = period / unit;
		int durationU = duration / unit;
		// �������ڸ����������ִ���
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
	 * ��ʼ���ڵ�
	 * 
	 * @param topo
	 *            ���˽ṹ
	 * @return �ڵ���
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
