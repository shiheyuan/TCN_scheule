package requirement;

import java.util.List;

import topology.FlowUtil;

public class DataSet {
	@Override
	public String toString() {
		return "DataSet [hyper=" + hyper + ", unit=" + unit + ", dataflows=" + dataflows + "]";
	}

	public List<Dataflow> dataflows;
	public int hyper;
	public int unit;

	public DataSet(int flowNum, int minPeriod, int maxPeriod, List<Integer> topoConfig) {
		this.dataflows = FlowUtil.initialDataflows(flowNum, minPeriod, maxPeriod, topoConfig);
		this.hyper = FlowUtil.getHyper(dataflows);
		this.unit = FlowUtil.getUnit(dataflows);
	}

	public int getFrain() {
		int frain = 0;
		for (Dataflow dataflow : dataflows) {
			frain += hyper / dataflow.period * dataflow.duration;
		}
		return frain;

	}

}
