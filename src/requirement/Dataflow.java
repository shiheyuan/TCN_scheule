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
		return "Dataflow [sender=" + sender + ", receive=" + receive + ", launch=" + launch + ", duration=" + duration
				+ ", period=" + period + ", maxLaunch=" + maxLaunch + "]";
	}

	public List<Integer> sender = new ArrayList<>();
	public List<Integer> receive = new ArrayList<>();
	public List<Edge> edges;

	public int launch;
	public int duration;
	public int period;
	public int maxLaunch;

	/**
	 * 
	 * @param maxNodePerLev
	 *            ����level�����ڵ���
	 */
	public Dataflow(List<Integer> maxNodePerLev, int maxPeriod) {
		// ���˲���
		Random random = new Random();
		duration = 1;
		period = (int) Math.pow(2, 1 + random.nextInt(maxPeriod));
		maxLaunch = period - duration + 1;
		launch = random.nextInt(maxLaunch);
		do {
			sender = initialNode(maxNodePerLev);
			receive = initialNode(maxNodePerLev);
		} while (sender.equals(receive) == true);
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
