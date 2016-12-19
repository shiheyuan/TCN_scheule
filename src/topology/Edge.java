package topology;

import java.util.List;

public class Edge {
	// 组成一条边的两个点
	public List<Integer> v0;
	public List<Integer> v1;

	public Edge() {
	}

	@Override
	public String toString() {
		return v0 + "->" + v1;
	}

	public Edge(List<Integer> v0, List<Integer> v1) {
		super();
		this.v0 = v0;
		this.v1 = v1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		boolean flag = false;
		if (v0.equals(other.v0) && v1.equals(other.v1)) {
			flag = true;
		}
		return flag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((v0 == null) ? 0 : v0.hashCode());
		result = prime * result + v1.hashCode();
		return result;
	}
}
