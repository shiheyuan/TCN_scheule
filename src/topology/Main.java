package topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import requirement.Dataflow;
import requirement.DataflowSchedule;
import utils.Util;

public class Main {

	public static void main(String[] args) {
		// ����������
		int flowNum = 5;
		// �������������
		int maxPeriod = 1;
		// ���˽ṹ
		List<Integer> topoConfig = new ArrayList<>();
		topoConfig.add(0, 1);
		topoConfig.add(1, 1);
		// ��ʼ��������
		List<Dataflow> dataflows = initialDataflows(flowNum, maxPeriod, topoConfig);
		// ��ʼ������������
		int hyper = Util.getHyper(dataflows);
		int unit = Util.getUnit(dataflows);
		// ���������Ȳ����������ڣ����ȵ�Ԫ
		System.out.println(hyper + "\t" + unit);
		List<DataflowSchedule> dataflowSchedules = iniDataSche(dataflows, hyper, unit);
		System.out.println(dataflowSchedules);
		// ��Ӧ��
		int feasibility = violate(dataflowSchedules);
		System.out.println(feasibility);
	}

	public static int violate(List<DataflowSchedule> dataflowSchedules) {
		int slotNum = dataflowSchedules.get(0).timeSlot.size();
		int fit = 0;
		for (int i = 0; i < slotNum; i++) {
			List<Dataflow> coDataflows = coexist(dataflowSchedules, i);
			// ��ʱ�������ڹ����2������������
			if (coDataflows.size() > 1) {
				fit += checkOverlap(coDataflows);
			}
		}
		return fit;
	}

	/**
	 * ���������������ĳһʱ�̹����������
	 * 
	 * @param dataflows
	 * @param slot
	 *            ĳһʱ��
	 * @return ����������
	 */
	public static List<Dataflow> coexist(List<DataflowSchedule> dataflowSchedules, int slot) {
		List<Dataflow> coexistDataflows = new ArrayList<>();
		for (DataflowSchedule dataflowSchedule : dataflowSchedules) {
			if (dataflowSchedule.timeSlot.get(slot)) {
				coexistDataflows.add(dataflowSchedule.dataflow);
			}
		}
		return coexistDataflows;
	}

	/**
	 * ��ʼ������������
	 * 
	 * @param dataflows
	 *            ������
	 * @return ����������
	 */
	public static List<DataflowSchedule> iniDataSche(List<Dataflow> dataflows, int hyper, int unit) {
		List<DataflowSchedule> dataflowSchedules = new ArrayList<>();
		for (Dataflow dataflow : dataflows) {
			dataflowSchedules.add(new DataflowSchedule(hyper, unit, dataflow));
		}
		return dataflowSchedules;
	}

	/**
	 * ��ʼ��������
	 * 
	 * @param flowNum
	 *            ����������
	 * @param maxPeriod
	 *            �������������
	 * @param topoConfig
	 *            ���˽ṹ
	 * @return һ��������
	 */
	public static List<Dataflow> initialDataflows(int flowNum, int maxPeriod, List<Integer> topoConfig) {

		List<Dataflow> dataflows = new ArrayList<>();

		for (int i = 0; i < flowNum; i++) {
			dataflows.add(new Dataflow(topoConfig, maxPeriod));
		}
		for (Dataflow dataflow : dataflows) {
			dataflow.edges = route(dataflow.sender, dataflow.receive);
		}
		return dataflows;
	}

	/**
	 * �����ڵ�֮���·��·��
	 * 
	 * @param src
	 *            Դ�ڵ�
	 * @param dst
	 *            Ŀ�Ľڵ�
	 * @return ·��
	 */
	public static List<Edge> route(List<Integer> src, List<Integer> dst) {

		List<List<Integer>> route = new ArrayList<>();

		int srcLev = src.size();
		int dstLev = dst.size();

		int boneDiff = dst.get(0) - src.get(0);

		// add route start node
		route.add(new ArrayList<>(src));
		// route to bridge
		List<Integer> upNode = new ArrayList<>(src);
		for (int i = 0; i < srcLev - 1; i++) {
			upNode.remove(upNode.size() - 1);
			route.add(new ArrayList<>(upNode));
		}
		// route by bone bridge
		int routeBone = src.get(0);
		for (int i = 0; i < Math.abs(boneDiff); i++) {
			List<Integer> boneRoute = new ArrayList<>();
			if (boneDiff < 0) {
				routeBone--;
			} else {
				routeBone++;
			}
			boneRoute.add(routeBone);
			route.add(new ArrayList<>(boneRoute));
		}
		// route from bridge to destination
		List<Integer> downNode = new ArrayList<>();
		downNode.add(dst.get(0));
		for (int i = 1; i < dstLev; i++) {
			downNode.add(dst.get(i));
			route.add(new ArrayList<>(downNode));
		}

		List<Edge> edges = new ArrayList<>();
		for (int i = 0; i < route.size() - 1; i++) {
			List<Integer> v0 = route.get(i);
			List<Integer> v1 = route.get(i + 1);
			Edge edge = new Edge(v0, v1);
			edges.add(edge);

		}

		return edges;

	}

	/**
	 * ����������������ظ�����
	 * 
	 * @param dataflows
	 * @return �����ظ�����
	 */
	public static int checkOverlap(List<Dataflow> dataflows) {
		Map<Edge, Integer> edgeCounter = new HashMap<>();
		// ��ʼ��edgeCounter
		for (Dataflow dataflow : dataflows) {
			for (Edge edge : dataflow.edges) {
				edgeCounter.put(edge, 0);
			}
		}
		// ����������
		for (Dataflow dataflow : dataflows) {
			for (Edge edge : dataflow.edges) {
				if (edgeCounter.containsKey(edge)) {
					edgeCounter.put(edge, edgeCounter.get(edge) + 1);
				}
			}
		}
		int overlapCount = 0;
		for (Integer val : edgeCounter.values()) {
			if (val > 1) {
				overlapCount = overlapCount + val - 1;
			}
		}
		// for (int usedCount : edgeCounter.values()) {
		// if (usedCount > 1) {
		// // ���һ���߱�ʹ����n+1�Σ��ʹ���n(n-1)�Զ���ͻ
		// // �@�e����Ҫ�����룬Ҳ��ֱ��ͳ���ظ�dataflow�����ȽϺ�
		// // ����1->2��������dataflowͬʱռ�ã���˶�edge��3-1=2��Υ������
		// int n = usedCount - 1;
		// overlapCount += n * n + 1;
		// }
		// }
		System.out.println(edgeCounter);
		return overlapCount;
	}
}
