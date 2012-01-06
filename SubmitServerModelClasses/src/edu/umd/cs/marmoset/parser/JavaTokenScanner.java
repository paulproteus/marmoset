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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Token scanner for Java code highlighting.
 *
 * @author David Hovemeyer
 */
public class JavaTokenScanner implements TokenScanner {
	public JavaTokenScanner(Iterator<String> iterator) {
		this.iterator = iterator;
		if (this.iterator.hasNext())
			this.reader = new PushbackReader(new StringReader(this.iterator.next()));
	}

	private static final Map<Character, TokenType> singleCharacterTokenMap = new HashMap<Character, TokenType>();
	static {
		singleCharacterTokenMap.put(Character.valueOf(','), TokenType.PUNCTUATION);
		singleCharacterTokenMap.put(Character.valueOf(':'), TokenType.PUNCTUATION);
		singleCharacterTokenMap.put(Character.valueOf('?'), TokenType.PUNCTUATION);
		singleCharacterTokenMap.put(Character.valueOf(';'), TokenType.PUNCTUATION);
		singleCharacterTokenMap.put(Character.valueOf('~'), TokenType.OPERATOR);
		singleCharacterTokenMap.put(Character.valueOf('('), TokenType.PAREN);
		singleCharacterTokenMap.put(Character.valueOf(')'), TokenType.PAREN);
		singleCharacterTokenMap.put(Character.valueOf('['), TokenType.PAREN);
		singleCharacterTokenMap.put(Character.valueOf(']'), TokenType.PAREN);
		singleCharacterTokenMap.put(Character.valueOf('{'), TokenType.PAREN);
		singleCharacterTokenMap.put(Character.valueOf('}'), TokenType.PAREN);
		singleCharacterTokenMap.put(Character.valueOf('\n'), TokenType.NEWLINE);
	}

	private static final Set<String> keywordSet = new HashSet<String>();
	static {
		keywordSet.add("abstract");
		keywordSet.add("boolean");
		keywordSet.add("break");
		keywordSet.add("byte");
		keywordSet.add("case");
		keywordSet.add("catch");
		keywordSet.add("char");
		keywordSet.add("class");
		keywordSet.add("const");
		keywordSet.add("continue");
		keywordSet.add("default");
		keywordSet.add("do");
		keywordSet.add("double");
		keywordSet.add("else");
		keywordSet.add("extends");
		keywordSet.add("final");
		keywordSet.add("finally");
		keywordSet.add("float");
		keywordSet.add("for");
		keywordSet.add("goto");
		keywordSet.add("if");
		keywordSet.add("implements");
		keywordSet.add("import");
		keywordSet.add("instanceof");
		keywordSet.add("int");
		keywordSet.add("interface");
		keywordSet.add("long");
		keywordSet.add("native");
		keywordSet.add("new");
		keywordSet.add("package");
		keywordSet.add("private");
		keywordSet.add("protected");
		keywordSet.add("public");
		keywordSet.add("return");
		keywordSet.add("short");
		keywordSet.add("static");
		keywordSet.add("strictfp");
		keywordSet.add("super");
		keywordSet.add("switch");
		keywordSet.add("synchronized");
		keywordSet.add("this");
		keywordSet.add("throw");
		keywordSet.add("throws");
		keywordSet.add("transient");
		keywordSet.add("try");
		keywordSet.add("void");
		keywordSet.add("volatile");
		keywordSet.add("while");

		keywordSet.add("define");
		keywordSet.add("ifndef");
		keywordSet.add("typedef");
		keywordSet.add("include");
		keywordSet.add("endif");
		keywordSet.add("undef");
		keywordSet.add("struct");
		keywordSet.add("unsigned");
		keywordSet.add("else");
		keywordSet.add("ifdef");
		keywordSet.add("let");
		keywordSet.add("constraint");
		keywordSet.add("functor");
		keywordSet.add("match");
		keywordSet.add("rec");
		keywordSet.add("then");
		keywordSet.add("with");
		keywordSet.add("fun");
		keywordSet.add("function");
	}

	private static final Set<String> specialLiteralSet = new HashSet<String>();
	static {
		specialLiteralSet.add("true");
		specialLiteralSet.add("false");
		specialLiteralSet.add("null");
	}

	private static final Map<Character, Integer> timesPermittedMap = new HashMap<Character, Integer>();
	static {
		timesPermittedMap.put('+', 2);
		timesPermittedMap.put('-', 2);
		timesPermittedMap.put('=', 2);
		timesPermittedMap.put('<', 3);
		timesPermittedMap.put('>', 3);
	}

	private StringBuilder lexeme;
	private PushbackReader reader;
    private final Iterator<String> iterator;


	/* (non-Javadoc)
	 * @see edu.umd.cs.submitServer.TokenScanner#getLexeme()
	 */
	public String getLexeme() {
		return lexeme.toString();
	}



	private @Nonnull TokenType readToken() throws IOException {
		lexeme = new StringBuilder();
		if (inMultilineComment)
			return scanMultiLineComment();
		int first = read();
		if (first < 0) {
			return TokenType.NEWLINE;
		}
		consume(first);

		TokenType currentTokenType = singleCharacterTokenMap.get((char) first);
		if (currentTokenType != null) {
			return currentTokenType;
		}

		if (Character.isJavaIdentifierStart((char) first)) {
			return scanIdentifierOrKeyword((char) first, reader);
		} else if (first == '.') {
			// Requires some special handling because it may be the
			// beginning of a floating point literal
			int next = read();
			if (next >= 0 && isJavaDigit((char) next)) {
				consume(next);
				return scanNumericLiteral(reader, (char) first, N_POINT);
			} else {
				putback(next);
				return TokenType.PUNCTUATION;
			}
		} else if (isJavaSpace(first)) {
			int next;
			while ((next = read()) >= 0) {
				if (isJavaSpace(next)) {
					consume(next);
				} else {
					putback(next);
					break;
				}
			}
			return TokenType.HORIZONTAL_WHITESPACE;
		} else if (first == '/') {
			int next = read();
			if (next == '/') {
				consume(next);
				return scanSingleLineComment();
			} else if (next == '*') {
				consume(next);
				return scanMultiLineComment();
			} else {
				putback(next);
				return scanOperator(first);
			}
		} else {
			switch (first) {
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				return scanNumericLiteral(reader, (char) first, N_START);
			case '!': case '~':
			case '+': case '-':
			case '*': case '%':
			case '|': case '&': case '^':
			case '>': case '<': case '=':
				return scanOperator(first);
			case '\'':
				return scanStringLiteral('\'');
			case '"':
				return scanStringLiteral('"');
			default:
				return TokenType.UNKNOWN;
			}
		}
	}



	private int read() throws IOException {
		return reader.read();
	}

	private void putback(int next) throws IOException {
		reader.unread(next);
	}


	private TokenType scanSingleLineComment() throws IOException {
		int c;
		while ((c = reader.read()) >= 0)
			consume(c);
		return TokenType.SINGLE_LINE_COMMENT;
	}

	enum MultiLineScanState { START, AFTER_STAR };

	private boolean inMultilineComment;
	private TokenType scanMultiLineComment() throws IOException {
		int c ;
		inMultilineComment = true;
		MultiLineScanState state = MultiLineScanState.START;
		while ((c = reader.read()) >= 0) {
			switch (state) {
			case START:
				if (c == '*') {
					consume(c);
					state = MultiLineScanState.AFTER_STAR;
				} else {
					consume(c);
				}
				break;

			case AFTER_STAR:
				if (c == '/') {
					consume(c);
					inMultilineComment = false;
					return TokenType.MULTI_LINE_COMMENT;
				} else {
					consume(c);
					if (c != '*')
						state = MultiLineScanState.START;
				}
				break;

			default:
				throw new IllegalStateException();
			}
		}
		if (lexeme.length() == 0)
			return TokenType.NEWLINE;

		return TokenType.MULTI_LINE_COMMENT;
	}

	private boolean isJavaSpace(int c) {
		return c == ' ' || c == '\t' || c == '\f';
	}

	private TokenType scanIdentifierOrKeyword(char first, Reader reader) throws IOException {
		// FIXME: handle unicode escapes somehow

		int c;
		while ((c = read()) >= 0) {
			if (Character.isJavaIdentifierPart((char) c)) {
				consume(c);
			} else {
				putback(c);
				break;
			}
		}

		String value = lexeme.toString();
		if (keywordSet.contains(value))
			return TokenType.KEYWORD;
		else if (specialLiteralSet.contains(value))
			return TokenType.LITERAL;
		else
			return TokenType.IDENTIFIER;
	}



	private void consume(int c) {
		assert c > 0 && c < 0x10000;
		lexeme.append((char) c);
	}

	private static final int N_START = 0;
	private static final int N_POINT = 1;
	private static final int N_HEX = 2;
	private static final int N_EXPONENT = 3;
	private static final int N_EXPONENT_SIGN = 4;

	private TokenType scanNumericLiteral(Reader reader, char first, int startState) throws IOException {
		int state = startState;
		boolean done = false;

		int c;
		while (!done && (c = read()) >= 0) {
			switch (state) {
			case N_START:
				if (c == '.') {
					consume((char)c);
					state = N_POINT;
				} else if (first == '0' && lexeme.length() == 1 && isHexSignifier(c)) {
					consume((char)c);
					state = N_HEX;
				} else if (isJavaDigit(c)) {
					consume((char)c);
				} else if (isExponentSignifier(c)) {
					consume((char)c);
					state = N_EXPONENT;
				} else if (isIntTypeSuffix(c) || isFloatTypeSuffix(c)) {
					consume((char)c);
					done = true;
				} else {
					putback(c);
					done = true;
				}
				break;

			case N_POINT:
				if (isExponentSignifier(c)) {
					consume((char)c);
					state = N_EXPONENT;
				} else if (isFloatTypeSuffix(c)) {
					// FIXME: should exclude things like ".D"
					consume((char)c);
					done = true;
				} else {
					putback(c);
					done = true;
				}
				break;

			case N_HEX:
				if (isHexDigit(c)) {
					consume((char)c);
				} else if (isIntTypeSuffix(c)) {
					// FIXME: should exclude things like "0xL"
					consume((char)c);
					done = true;
				} else {
					putback(c);
					done = true;
				}
				break;

			case N_EXPONENT:
				if (c == '+' || c == '-' || isJavaDigit(c)) {
					consume((char)c);
					state = N_EXPONENT_SIGN;
				} else {
					// Invalid token?
					putback(c);
					done = true;
				}
				break;

			case N_EXPONENT_SIGN:
				if (isJavaDigit(c)) {
					consume((char)c);
				} else if (isFloatTypeSuffix(c)) {
					consume((char)c);
					done = true;
				} else {
					putback(c);
					done = true;
				}
				break;

			default:
				throw new IllegalStateException();
			}
		}

		return TokenType.LITERAL;
	}

	private boolean isFloatTypeSuffix(int c) {
		return c == 'f' || c == 'F' || c == 'd' || c == 'D';
	}

	private boolean isHexDigit(int c) {
		return isJavaDigit(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
	}

	private boolean isExponentSignifier(int c) {
		return c == 'e' || c == 'E';
	}

	private boolean isIntTypeSuffix(int c) {
		return c == 'L' || c == 'l';
	}

	private boolean isJavaDigit(int c) {
		return c >= '0' && c <= '9';
	}

	private boolean isHexSignifier(int c) {
		return c == 'x' || c == 'X';
	}

		/*
	 * This is perhaps overly clever.
	 * But, it should recognize exactly the set of Java operators.
	 * (Except '?' and ':', which we treat as punctuation.)
	 */
	private TokenType scanOperator(int first) throws IOException {
        if (first <= 0) throw new IllegalArgumentException("first is " + first);
		Integer timesPermitted = timesPermittedMap.get((char)first);
		if (timesPermitted == null) {
			timesPermitted = 1;
		}

		int c;
		while ((c = read()) >= 0) {
			if (c == first) {
				if (lexeme.length() < timesPermitted.intValue()) {
					consume((char)c);
				} else {
					putback(c);
					break;
				}
			} else {
				if (mayEndWithEq(first) && c == '=') {
					consume((char)c);
				} else {
					putback(c);
				}
				break;
			}
		}

		return TokenType.OPERATOR;
	}

	private boolean mayEndWithEq(int first) {
		if (first == '~')
			return false;
		if ((first == '+' || first == '-') && lexeme.length() > 1)
			// Don't allow things like "++="
			return false;
		return true;
	}

	enum StringLiteralState { NORMAL, BACKSLASH, UNICODE_START, UNICODE };



	private TokenType scanStringLiteral(char terminator) throws IOException {
		StringLiteralState state = StringLiteralState.NORMAL;
		boolean done = false;
		int c;
		while (!done && (c = read()) >= 0) {
			switch (state) {
			case NORMAL:
				if (c == terminator) {
					consume((char)c);
					done = true;
				} else if (c == '\\') {
					consume((char)c);
					state = StringLiteralState.BACKSLASH;
				} else {
					consume((char)c);
				}
				break;

			case BACKSLASH:
				if (c == 'u') {
					consume((char)c);
					state = StringLiteralState.UNICODE_START;
				} else {
					consume((char)c);
					state = StringLiteralState.NORMAL;
				}
				break;

			case UNICODE_START:
				if (c == 'u') {
					consume((char)c);
				} else if (isHexDigit(c)) {
					consume((char)c);
					state = StringLiteralState.UNICODE;
				} else {
					consume((char)c);
					state = StringLiteralState.NORMAL;
				}
				break;

			case UNICODE:
				if (isHexDigit(c)) {
					consume((char)c);
				} else {
					putback(c);
					state = StringLiteralState.NORMAL;
				}
				break;

			default:
				throw new IllegalStateException();
			}
		}

		return TokenType.STRING_LITERAL;
	}

	public static void main(String[] args) throws Exception {
			BufferedReader reader = new BufferedReader(new FileReader(args[0]));
			TokenScanner scanner = new JavaTokenScanner(new ReaderIterator(reader));
			for(Iterator<Token> i = scanner; i.hasNext(); )
				System.out.println(i.next());
	}

	@Override
	public boolean hasNext() {
		return reader != null;
	}

	@Override
	public Token next() {
		TokenType tokenType;
		try {
			tokenType = readToken();

		if (tokenType == TokenType.NEWLINE) {
			if (iterator.hasNext())
				reader = new PushbackReader(new StringReader(iterator.next()));
			else
				reader = null;
			return new Token(TokenType.NEWLINE, "\n");
		}
		return new Token(tokenType, lexeme.toString());
		} catch (IOException e) {
			reader = null;
			return new Token(TokenType.UNKNOWN, e.getMessage());
		}

	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();

	}
}
