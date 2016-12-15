package utils;

public class Util {
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
