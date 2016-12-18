package topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import requirement.Dataflow;
import requirement.DataflowSchedule;

public class Main {

	public static void main(String[] args) {
		int flowNum = 1;
		int levNum = 3;
		Integer[] level = new Integer[levNum];
		level[0] = 4;
		level[1] = 2;
		level[2] = 2;
		List<Integer> topoConfig = Arrays.asList(level);

		List<Dataflow> dataflows = new ArrayList<>();

		for (int i = 0; i < flowNum; i++) {
			dataflows.add(new Dataflow(topoConfig));
		}
		for (Dataflow dataflow : dataflows) {
			dataflow.edges = route(dataflow.sender, dataflow.receive);
			System.out.println(dataflow);
		}
		Dataflow dataflow = dataflows.get(0);
		DataflowSchedule dataflowSchedule = new DataflowSchedule( 16, 1, dataflow);
		System.out.println(dataflowSchedule);
	}

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
	 * 
	 * @param dataflows
	 * @return overlap的Edge数
	 */
	public static int checkOverlap(List<Dataflow> dataflows) {
		Map<Edge, Integer> edgeCounter = new HashMap<>();
		for (Dataflow dataflow : dataflows) {
			for (Edge edge : dataflow.edges) {
				edgeCounter.put(edge, edgeCounter.get(edge));
			}
		}
		int overlapCount = 0;
		for (int usedCount : edgeCounter.values()) {
			if (usedCount > 1) {
				// 如果一条边被使用了n+1次，就存在n(n-1)对儿冲突
				// 這裡我需要再想想，也许直接统计重复dataflow数量比较好
				// 比如1->2，有三条dataflow同时占用，则此段edge有3-1=2次违反规则
				int n = usedCount - 1;
				overlapCount += n * n + 1;
			}
		}
		return overlapCount;
	}
}
