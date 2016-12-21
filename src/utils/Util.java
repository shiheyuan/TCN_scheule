package utils;

import java.util.List;

import requirement.Dataflow;

public class Util {
	/**
	 * 求宏周期
	 * 
	 * @param dataflows
	 * @return 宏周期数值
	 */
	public static int getHyper(List<Dataflow> dataflows) {
		int hyper = -1;
		boolean flag = false;
		for (Dataflow dataflow : dataflows) {
			if (!flag) {
				hyper = dataflow.period;
				flag = true;
			} else {
				hyper = Math.max(hyper, dataflow.period);
			}
		}
		return hyper;
	}

	/**
	 * 求调度单元数值
	 * 
	 * @param dataflows
	 * @return
	 */
	public static int getUnit(List<Dataflow> dataflows) {
		int unit = -1;
		boolean flag = false;
		for (Dataflow dataflow : dataflows) {
			if (!flag) {
				unit = dataflow.period;
				flag = true;
			} else {
				unit = Math.min(unit, dataflow.duration);
			}
		}
		return unit;
	}

	// great common division
	public static long gcd(long m, long n) {
		if (m < n) {// 保证m>n,若m<n,则进行数据交换
			long temp = m;
			m = n;
			n = temp;
		}
		if (m % n == 0) {// 若余数为0,返回最大公约数
			return n;
		} else { // 否则,进行递归,把n赋给m,把余数赋给n
			return gcd(n, m % n);
		}
	}

	// least common multiple
	public static long lcm(long m, long n) {
		return m * n / gcd(m, n);
	}
}
