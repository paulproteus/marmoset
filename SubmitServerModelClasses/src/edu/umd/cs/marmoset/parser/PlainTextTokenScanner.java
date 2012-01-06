/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package edu.umd.cs.marmoset.parser;

import java.util.Iterator;

/**
 * TokenScanner which just returns lines of text, classified as
 * TokenType.UNKNOWN. This is useful for just displaying plain text files.
 *
 * @author David Hovemeyer
 */
public class PlainTextTokenScanner implements TokenScanner {

	private final Iterator<String> base;

	public PlainTextTokenScanner(Iterator<String> base) {
		this.base = base;
	}

	private boolean needNewline = false;

	@Override
	public boolean hasNext() {
		return needNewline || base.hasNext();
	}

	@Override
	public Token next() {
		if (needNewline) {
			needNewline = false;
			return new Token(TokenType.NEWLINE, "\n");
		}
		needNewline = true;
		return new Token(TokenType.UNKNOWN, base.next());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}

}
