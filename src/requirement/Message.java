package requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import topology.Edge;

public class Message {

	// public static void main(String[] args) {
	// List<Integer> topo = new ArrayList<>();
	// topo.add(8);
	// topo.add(8);
	// List<Message> messages = new ArrayList<>();
	// for (int i = 0; i < 10; i++) {
	// Message message = new Message(topo);
	// messages.add(message);
	// }
	// double period_max = 0;
	// double duration_min = 1024;
	// for (Message message : messages) {
	// period_max = Math.max(period_max, message.period_ms);
	// duration_min = Math.min(duration_min, message.duration_ms);
	// }
	//
	// // 归一化，最大周期时隙
	// int hyper = (int) (Math.pow(2, (int) (Math.log(period_max / duration_min)
	// / Math.log(2))));
	// System.out.println(period_max);
	// System.out.println(duration_min);
	// System.out.println(hyper);
	// System.out.println("----------------");
	// for (Message message : messages) {
	// System.out.println(message);
	// System.out.println((int) (message.period_ms * hyper / period_max));
	// System.out.println((int) (message.duration_ms / duration_min + 1));
	// System.out.println();
	// }
	// }

	@Override
	public String toString() {
		return "Message [len_bytes=" + len_bytes + ", src=" + src + ", dst=" + dst + ", route=" + route + ", period_ms="
				+ period_ms + ", duration_ms=" + duration_ms + "]";
	}

	public int len_bytes;
	public List<Integer> src = new ArrayList<>();
	public List<Integer> dst = new ArrayList<>();
	public List<Edge> route;
	public int period_ms;
	public double duration_ms;

	public Message(List<Integer> topo) {
		Random random = new Random();
		// 消息比特与毫秒信息（2ms - 32 ms）
		period_ms = (int) Math.pow(2, 1 + (int) (Math.random() * 5));
		// len_bytes = 64 + random.nextInt(1518 - 64);
		// 默认最小帧长64byte
		len_bytes = 64;
		// 路径规划
		do {
			src = initialNode(topo);
			dst = initialNode(topo);
		} while (src.equals(dst));
		route = getRoute(src, dst);
		duration_ms = getDuration(len_bytes, route);
	}

	/**
	 * 计算通信延时，以ms为单位
	 * 
	 * @param len_bytes
	 * @param route
	 * @return
	 */
	private static double getDuration(int len_bytes, List<Edge> route) {
		double SW_CONSTANT = 0.2;
		double BYTE_DELAY = 0.08;
		double duration = (SW_CONSTANT + len_bytes * BYTE_DELAY) * route.size();
		return duration / 1000;
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
	public static List<Edge> getRoute(List<Integer> src, List<Integer> dst) {

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
