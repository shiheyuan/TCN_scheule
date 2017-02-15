package topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import requirement.Dataflow;
import requirement.DataflowSchedule;
import requirement.Message;

public class FlowUtil {

	// public static void main(String[] args) {
	// // ����������
	// int flowNum = 5;
	// // �������������
	// int minPeriod = 3;
	//
	// int maxPeriod = 3;
	// // ���˽ṹ
	// List<Integer> topoConfig = new ArrayList<>();
	// topoConfig.add(0, 8);
	// topoConfig.add(1, 4);
	//
	// DataSet dataSet = new DataSet(flowNum, maxPeriod, topoConfig);
	// System.out.println(dataSet);
	//
	// // ��ʼ��������
	// List<Dataflow> dataflows = initialDataflows(flowNum, maxPeriod,
	// topoConfig);
	// // ��ʼ������������
	// int hyper = Util.getHyper(dataflows);
	// int unit = Util.getUnit(dataflows);
	// // ���������Ȳ����������ڣ����ȵ�Ԫ
	// System.out.println(hyper + "\t" + unit);
	//
	// for (Dataflow dataflow : dataflows) {
	// List<Boolean> list = dataflow.getExist(1, hyper, unit);
	// // System.out.println(dataflow);
	// // System.out.println(list);
	// }
	//
	// // List<DataflowSchedule> dataflowSchedules = iniDataSche(dataflows,
	// // hyper, unit);
	// // System.out.println(dataflowSchedules);
	// // // ��Ӧ��
	// // int feasibility = violate(dataflowSchedules);
	// // System.out.println(feasibility);
	// }

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
	public static List<Dataflow> initialDataflows(int flowNum, int minPeriod, int maxPeriod, List<Integer> topoConfig) {

		List<Dataflow> dataflows = new ArrayList<>();

		for (int i = 0; i < flowNum; i++) {
			dataflows.add(new Dataflow(topoConfig, minPeriod, maxPeriod));
		}
		for (Dataflow dataflow : dataflows) {
			dataflow.edges = route(dataflow.sender, dataflow.receive);
		}
		return dataflows;
	}

	/**
	 * ����ԭʼ����Ȼ���ݴλ��������ӳ٣���������������
	 * 
	 * @param flowNum
	 * @param topo
	 * @return
	 */
	public static List<Dataflow> initialDataflows(int flowNum, List<Integer> topo) {
		List<Message> messages = new ArrayList<>();
		// ����ԭʼ������Ϣ����Ϊbyte���ӳ�Ϊms
		for (int i = 0; i < flowNum; i++) {
			Message message = new Message(topo);
			messages.add(message);
		}
		// �������ӳٵ�2���ݴλ�
		// ͳ��������ڡ���С�ӳ�
		double period_max = 0;
		double duration_min = 1024;
		for (Message message : messages) {
			period_max = Math.max(period_max, message.period_ms);
			duration_min = Math.min(duration_min, message.duration_ms);
		}
		// ��һ�����������ʱ϶
		int hyper_unity = (int) (Math.pow(2, (int) (Math.log(period_max / duration_min) / Math.log(2))));
		// System.out.println(period_max);
		// System.out.println(duration_min);
		// System.out.println(hyper_unity);
		// System.out.println("----------------");
		// ���ɹ�һ��������
		List<Dataflow> dataflows = new ArrayList<>();
		for (Message message : messages) {
			// System.out.println(message);
			int period = (int) (message.period_ms * hyper_unity / period_max);
			int delay = (int) Math.ceil((message.duration_ms / duration_min));
			Dataflow dataflow = new Dataflow(message.src, message.dst, message.route, delay, period);
			dataflows.add(dataflow);
			// System.out.println(message);
			// System.out.println(dataflow);
			// System.out.println();
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
		// System.out.println(edgeCounter);
		return overlapCount;
	}

	/**
	 * �������
	 * 
	 * @param dataflows
	 * @return ��������ֵ
	 */
	public static int getHyper(List<Dataflow> dataflows) {
		int hyper = -1;
		boolean flag = false;
		for (Dataflow dataflow : dataflows) {
			if (!flag) {
				hyper = dataflow.period;
				flag = true;
			} else {
				hyper = Math.max(hyper, dataflow.period);
			}
		}
		return hyper;
	}

	/**
	 * ����ȵ�Ԫ��ֵ
	 * 
	 * @param dataflows
	 * @return
	 */
	public static int getUnit(List<Dataflow> dataflows) {
		int unit = -1;
		boolean flag = false;
		for (Dataflow dataflow : dataflows) {
			if (!flag) {
				unit = dataflow.duration;
				flag = true;
			} else {
				unit = Math.min(unit, dataflow.duration);
			}
		}
		return unit;
	}
}
