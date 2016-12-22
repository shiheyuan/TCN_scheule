package requirement;

import java.util.List;

import topology.Main;
import utils.Util;

public class DataSet {
	@Override
	public String toString() {
		return "DataSet [hyper=" + hyper + ", unit=" + unit + ", dataflows=" + dataflows + "]";
	}

	public List<Dataflow> dataflows;
	public int hyper;
	public int unit;

	public DataSet(int flowNum, int maxPeriod, List<Integer> topoConfig) {
		this.dataflows = Main.initialDataflows(flowNum, maxPeriod, topoConfig);
		this.hyper = Util.getHyper(dataflows);
		this.unit = Util.getUnit(dataflows);
	}
}
