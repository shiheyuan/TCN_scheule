package deprecate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
	public int id;
	public List<Integer> location = new ArrayList<>();
	public Map<Integer, Constant.nodeType> neigh = new HashMap<>();

	/**
	 * construct node in terms of configurations
	 */

	public Node(int id) {
		super();
		this.id = id;
	}

	@Override
	public String toString() {
		return "Node [id=" + id + ", location=" + location + ", neigh=" + neigh + "]" + "\n";
	}

}
