package edu.umd.cs.marmoset.utilities;

public class Pair<T1, T2> {
	
	private final T1 v1;
	private final T2 v2;

	public T1 getV1() {
		return v1;
	}

	public T2 getV2() {
		return v2;
	}

	public static <T1, T2> Pair<T1,T2> build(T1 v1, T2 v2) {
		return new  Pair<T1, T2>(v1, v2);
	}
	private Pair(T1 v1, T2 v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	@Override
	public int hashCode() {
		return Objects.nullSafeHashCode(v1, v2);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair<?,?>) {
			Pair<?,?> that = (Pair<?,?>) obj;
			return Objects.nullSafeEquals(this.v1, that.v1)
					&& Objects.nullSafeEquals(this.v2, that.v2);
		}
		return false;
		
	}

}
