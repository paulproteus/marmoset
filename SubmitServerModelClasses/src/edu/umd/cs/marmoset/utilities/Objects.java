package edu.umd.cs.marmoset.utilities;

import javax.annotation.CheckForNull;


public class Objects {


	public static int nullSafeHashCode(@CheckForNull Object x) {
		if (x == null)
			return 42;
		return x.hashCode();
	}
	public static int nullSafeHashCode(@CheckForNull Object... values) {
		int result = 0;
		for(Object x : values)
			result = result * 37 + nullSafeHashCode(x);
		return result;
	}
	public static boolean nullSafeEquals(@CheckForNull Object x, @CheckForNull Object y) {
		if (x == y) return true;
		if (x == null || y == null) return false;
		return x.equals(y);
	}
	
	public static int compareTo(long x, long y) {
		if (x < y)
			return -1;
		if (x > y)
			return 1;
		return 0;
	}
	public static int compareTo(int x, int y) {
		if (x < y)
			return -1;
		if (x > y)
			return 1;
		return 0;
	}
	public static <T> int identityCompareTo(T x, T y) {
		return compareTo(System.identityHashCode(x), System.identityHashCode(y));
	}
	


}
