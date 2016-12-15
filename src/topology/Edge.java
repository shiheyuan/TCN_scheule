package topology;

import java.util.List;

public class Edge {
	public List<Integer> v0;
	public List<Integer> v1;

	public Edge() {
	}

	@Override
	public String toString() {
		return "Edge [v0=" + v0 + ", v1=" + v1 + "]";
	}

	public Edge(List<Integer> v0, List<Integer> v1) {
		super();
		this.v0 = v0;
		this.v1 = v1;
	}

}
