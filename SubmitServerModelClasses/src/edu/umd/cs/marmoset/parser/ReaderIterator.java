package edu.umd.cs.marmoset.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import edu.umd.cs.marmoset.utilities.Charsets;

public class ReaderIterator implements Iterator<String> {
	final BufferedReader reader;
	String nextLine;

	public ReaderIterator(BufferedReader reader) {
		this.reader = reader;
	}
	public ReaderIterator(Reader reader) {
		this(new BufferedReader(reader));
	}
	public ReaderIterator(InputStream in) {
		this(new InputStreamReader(in, Charsets.UTF8));
	}
	public ReaderIterator(File f) throws FileNotFoundException {
		this(new InputStreamReader(new FileInputStream(f), Charsets.UTF8));
	}

	@Override
	public boolean hasNext() {
		if (nextLine == null)
			try {
				nextLine = reader.readLine();
				if (nextLine == null)
					reader.close();
			} catch (IOException e) {
				return false;
			}
		return nextLine != null;
	}

	@Override
	public String next() {
		String result = nextLine;
		nextLine = null;
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}


}
