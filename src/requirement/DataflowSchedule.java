package requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataflowSchedule {
	public Dataflow dataflow;
	public int startU;

	public List<Boolean> timeSlot = new ArrayList<>();
	// public List<Integer> timeline = new ArrayList<>();

	/**
	 * 
	 * @param hyper
	 * @param unit
	 * @param dataflow
	 */
	public DataflowSchedule(int hyper, int unit, Dataflow dataflow) {
		this.dataflow = dataflow;
		int slotNum = hyper / unit;
		for (int i = 0; i < slotNum; i++) {
			timeSlot.add(false);
		}
		// 参数标准化
		int periodU = dataflow.period / unit;
		int durationU = dataflow.duration / unit;
		// 宏周期内该数据流出现次数
		int times = hyper / periodU;

		Random random = new Random();
		startU = random.nextInt(periodU - durationU);

		for (int i = 0; i < times; i++) {
			for (int j = 0; j < durationU; j++) {
				timeSlot.set(i * periodU + j + startU, true);
				// timeline.add(i * periodU + j + startU);
			}
		}
	}

	@Override
	public String toString() {
		return "DataflowSchedule [dataflow=" + dataflow + ", startU=" + startU + ", timeSlot=" + timeSlot + "]\n";
	}
}
