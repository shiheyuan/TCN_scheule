package utils;

import java.util.List;

import requirement.Dataflow;

public class Util {
	/**
	 * �������
	 * 
	 * @param dataflows
	 * @return ��������ֵ
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
	 * ����ȵ�Ԫ��ֵ
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
		if (m < n) {// ��֤m>n,��m<n,��������ݽ���
			long temp = m;
			m = n;
			n = temp;
		}
		if (m % n == 0) {// ������Ϊ0,�������Լ��
			return n;
		} else { // ����,���еݹ�,��n����m,����������n
			return gcd(n, m % n);
		}
	}

	// least common multiple
	public static long lcm(long m, long n) {
		return m * n / gcd(m, n);
	}
}
