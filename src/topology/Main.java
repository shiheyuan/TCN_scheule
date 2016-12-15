package topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import requirement.Dataflow;

public class Main {

	public static void main(String[] args) {
		int flowNum = 10;
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

	public static void checkOverlap(List<Dataflow> dataflows) {
		Map<Edge, Integer> edgeCounter = new HashMap<>();
		for (Dataflow dataflow : dataflows) {
			for (Edge edge : dataflow.edges) {
				edgeCounter.put(edge, edgeCounter.get(edge));
			}

		}
	}
}