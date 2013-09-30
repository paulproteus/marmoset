package edu.umd.cs.marmoset.utilities;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class ImmutableList<T>  extends AbstractList<T> {

	private ImmutableList(T head, ImmutableList<T> tail) {
		super();
		this.head = head;
		this.tail = tail;
	}

	public static @Nonnull <T>  ImmutableList<T>  cons(T head, ImmutableList<T> tail) {
		return new ImmutableList<T>(head, tail);
	}

	static final ImmutableList<?> EMPTY = new ImmutableList<Object>(null, null);

	@SuppressWarnings("unchecked")
	static final <T> ImmutableList<T> empty() {
		return (ImmutableList<T>) EMPTY;
	}
	final T head;
	final @CheckForNull  ImmutableList<T> tail;


	@Override
	public int hashCode() {
	    if (this == EMPTY) return 0;
	    assert tail != null;
	    return head.hashCode() + 37 * tail.hashCode();
	}
	@Override
	public T get(int arg0) {
		if (arg0 == 0)
			return head;
		else if (tail == null)
			throw new IndexOutOfBoundsException();
		return tail.get(arg0-1);
	}

	@Override
	public int size() {
	    if (this == EMPTY)
	        return 0;
		if (tail == EMPTY)
			return 1;
		return 1 + tail.size();
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			ImmutableList<T> i = ImmutableList.this;

			@Override
			public boolean hasNext() {
				return i != EMPTY;
			}

			@Override
			public T next() {
			    if (!hasNext())
			        throw new NoSuchElementException();
				T result = i.head;
				i = i.tail;
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();

			}

		};
	}


}
