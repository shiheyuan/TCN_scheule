package requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DataflowSchedule {
	Dataflow dataflow;
	public int startU;
	
	public List<Boolean> timeline= new ArrayList<>();
	
	
	/**
	 * 
	 * @param starU
	 * @param hyper
	 * @param unit
	 * @param dataflow
	 */
	public DataflowSchedule(int hyper,int unit, Dataflow dataflow) {
		List<Integer> list = new ArrayList<>();
		
		this.dataflow = dataflow;
		int slotNum = hyper / unit;
		for (int i = 0; i < slotNum; i++) {
			timeline.add(false);
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
				timeline.set(i*periodU+j+startU, true);
			}
		}
	}
	@Override
	public String toString() {
		return "DataflowSchedule [dataflow=" + dataflow + ", startU=" + startU + ", timeline=" + timeline + "]";
	}
}
