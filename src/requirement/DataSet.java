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
		// this.dataflows = FlowUtil.initialDataflows(flowNum, minPeriod,
		// maxPeriod, topoConfig);

		this.dataflows = FlowUtil.initialDataflows(flowNum, topoConfig);
		this.hyper = FlowUtil.getHyper(dataflows);
		this.unit = FlowUtil.getUnit(dataflows);
	}

	/**
	 * 数据流序列的时间空间粒度总数
	 * 
	 * @return
	 */
	public int getGrain() {
		int grain = 0;
		for (Dataflow dataflow : dataflows) {
			grain += (hyper / dataflow.period) * dataflow.duration / unit * dataflow.edges.size();
		}
		return grain;
	}

	/**
	 * 数据流序列的时间粒度总数
	 * 
	 * @return
	 */
	public int getTemporalGrain() {
		int grain = 0;
		for (Dataflow dataflow : dataflows) {
			grain += (hyper / dataflow.period) * dataflow.duration / unit;
		}
		return grain;
	}

	public int getRawFrain() {
		int frain = 0;
		for (Dataflow dataflow : dataflows) {
			frain += hyper / dataflow.period * dataflow.duration;
		}
		return frain;
	}
}
