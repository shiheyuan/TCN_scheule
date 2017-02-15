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
	// // 数据流数量
	// int flowNum = 5;
	// // 最大数据流周期
	// int minPeriod = 3;
	//
	// int maxPeriod = 3;
	// // 拓扑结构
	// List<Integer> topoConfig = new ArrayList<>();
	// topoConfig.add(0, 8);
	// topoConfig.add(1, 4);
	//
	// DataSet dataSet = new DataSet(flowNum, maxPeriod, topoConfig);
	// System.out.println(dataSet);
	//
	// // 初始化数据流
	// List<Dataflow> dataflows = initialDataflows(flowNum, maxPeriod,
	// topoConfig);
	// // 初始化数据流调度
	// int hyper = Util.getHyper(dataflows);
	// int unit = Util.getUnit(dataflows);
	// // 数据流调度参数：宏周期，调度单元
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
	// // // 适应度
	// // int feasibility = violate(dataflowSchedules);
	// // System.out.println(feasibility);
	// }

	public static int violate(List<DataflowSchedule> dataflowSchedules) {
		int slotNum = dataflowSchedules.get(0).timeSlot.size();
		int fit = 0;
		for (int i = 0; i < slotNum; i++) {
			List<Dataflow> coDataflows = coexist(dataflowSchedules, i);
			// 该时刻若存在共存的2条以上数据流
			if (coDataflows.size() > 1) {
				fit += checkOverlap(coDataflows);
			}
		}
		return fit;
	}

	/**
	 * 检查数据流集合中某一时刻共存的数据流
	 * 
	 * @param dataflows
	 * @param slot
	 *            某一时刻
	 * @return 共存数据流
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
	 * 初始化数据流调度
	 * 
	 * @param dataflows
	 *            数据流
	 * @return 数据流调度
	 */
	public static List<DataflowSchedule> iniDataSche(List<Dataflow> dataflows, int hyper, int unit) {
		List<DataflowSchedule> dataflowSchedules = new ArrayList<>();
		for (Dataflow dataflow : dataflows) {
			dataflowSchedules.add(new DataflowSchedule(hyper, unit, dataflow));
		}
		return dataflowSchedules;
	}

	/**
	 * 初始化数据流
	 * 
	 * @param flowNum
	 *            数据流数量
	 * @param maxPeriod
	 *            数据流最大周期
	 * @param topoConfig
	 *            拓扑结构
	 * @return 一组数据流
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
	 * 生成原始需求，然后幂次化周期与延迟，生成数据流序列
	 * 
	 * @param flowNum
	 * @param topo
	 * @return
	 */
	public static List<Dataflow> initialDataflows(int flowNum, List<Integer> topo) {
		List<Message> messages = new ArrayList<>();
		// 生成原始需求，消息长度为byte，延迟为ms
		for (int i = 0; i < flowNum; i++) {
			Message message = new Message(topo);
			messages.add(message);
		}
		// 周期与延迟的2的幂次化
		// 统计最大周期、最小延迟
		double period_max = 0;
		double duration_min = 1024;
		for (Message message : messages) {
			period_max = Math.max(period_max, message.period_ms);
			duration_min = Math.min(duration_min, message.duration_ms);
		}
		// 归一化，最大周期时隙
		int hyper_unity = (int) (Math.pow(2, (int) (Math.log(period_max / duration_min) / Math.log(2))));
		// System.out.println(period_max);
		// System.out.println(duration_min);
		// System.out.println(hyper_unity);
		// System.out.println("----------------");
		// 生成归一化数据流
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
	 * 两个节点之间的路由路径
	 * 
	 * @param src
	 *            源节点
	 * @param dst
	 *            目的节点
	 * @return 路由
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
	 * 检查给定数据流间的重复次数
	 * 
	 * @param dataflows
	 * @return 超量重复次数
	 */
	public static int checkOverlap(List<Dataflow> dataflows) {
		Map<Edge, Integer> edgeCounter = new HashMap<>();
		// 初始化edgeCounter
		for (Dataflow dataflow : dataflows) {
			for (Edge edge : dataflow.edges) {
				edgeCounter.put(edge, 0);
			}
		}
		// 数据流计数
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
		// // 如果一条边被使用了n+1次，就存在n(n-1)对儿冲突
		// // 這裡我需要再想想，也许直接统计重复dataflow数量比较好
		// // 比如1->2，有三条dataflow同时占用，则此段edge有3-1=2次违反规则
		// int n = usedCount - 1;
		// overlapCount += n * n + 1;
		// }
		// }
		// System.out.println(edgeCounter);
		return overlapCount;
	}

	/**
	 * 求宏周期
	 * 
	 * @param dataflows
	 * @return 宏周期数值
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
	 * 求调度单元数值
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
