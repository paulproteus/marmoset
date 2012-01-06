package edu.umd.cs.marmoset.utilities;

import javax.annotation.Nonnull;

public class Check {

	public static void nonnull(@Nonnull Object o) {
		if (o == null)
			throw new NullPointerException();
	}

}
