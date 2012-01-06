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

package edu.umd.cs.buildServer.inspection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;

import edu.umd.cs.buildServer.util.DevNullOutputStream;
import edu.umd.cs.findbugs.visitclass.DismantleBytecode;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;

public class CodeFeatures extends DismantleBytecode  {
	Set<String> classesReferenced = new TreeSet<String>();
	Set<String> methodsReferenced = new TreeSet<String>();
	BitSet opcodesUsed = new BitSet();
	List<String> opcodeList;
	MessageDigest digest1;
	MessageDigest digest2;
	DataOutputStream out1;
	DataOutputStream out2;
	String digestValue1;
	String digestValue2;
	ConstantPool cp;

	public String getDigest1() {
		compute();
		return digestValue1;
	}

	public String getDigest2() {
		compute();
		return digestValue2;
	}

	public Set<String> getClassesReferenced() {
		compute();
		return classesReferenced;
	}

	public Set<String> getMethodsReferenced() {
		compute();
		return methodsReferenced;
	}

	public List<String> getOpcodesUsed() {
		compute();
		return opcodeList;
	}

	public CodeFeatures() {
		try {
			digest1 = MessageDigest.getInstance("MD5");
			digest2 = MessageDigest.getInstance("MD5");
			out1 = new DataOutputStream(new DigestOutputStream(
					new DevNullOutputStream(), digest1));
			out2 = new DataOutputStream(new DigestOutputStream(
					new DevNullOutputStream(), digest2));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void visit(ConstantPool obj) {
		cp = obj;
	}

	@Override
	public void visit(ConstantClass obj) {
		String s = cp.constantToString(obj);
		if (s.startsWith("java"))
			classesReferenced.add(s);
	}

	@Override
	public void visit(ConstantMethodref obj) {
		String s = cp.constantToString(obj);
		if (s.startsWith("java"))
			methodsReferenced.add(s);
	}

	@Override
	public void visit(ConstantInterfaceMethodref obj) {
		String s = cp.constantToString(obj);
		if (s.startsWith("java"))
			methodsReferenced.add(s);
	}

	@Override
	public void sawOpcode(int seen) {
		opcodesUsed.set(seen);
		try {
			int v1 = seen;
			switch (seen) {
			case DCONST_0:
			case DCONST_1:
			case FCONST_0:
			case FCONST_1:
			case LCONST_0:
			case LCONST_1:
			case ICONST_0:
			case ICONST_1:
			case ICONST_2:
			case ICONST_3:
			case ICONST_4:
			case ICONST_M1:
			case BIPUSH:
			case SIPUSH:
			case LDC:
			case LDC_W:
			case LDC2_W:
				v1 = LDC;
				break;
			case IFEQ:
			case IFNE:
			case IFLT:
			case IFLE:
			case IFGT:
			case IFGE:
				v1 = IFEQ;
				break;
			case IF_ICMPEQ:
			case IF_ICMPNE:
			case IF_ICMPLT:
			case IF_ICMPLE:
			case IF_ICMPGT:
			case IF_ICMPGE:
				v1 = IF_ICMPEQ;
				break;
			case IFNULL:
			case IFNONNULL:
				v1 = IFNULL;
				break;
			case WIDE:
				return;
			}
			out1.write(v1);
			int v2 = v1;
			switch (v2) {
			case ALOAD:
			case ALOAD_0:
			case ALOAD_1:
			case ALOAD_2:
			case ALOAD_3:
				v2 = ALOAD;
				break;
			case ILOAD:
			case ILOAD_0:
			case ILOAD_1:
			case ILOAD_2:
			case ILOAD_3:
				v2 = ILOAD;
				break;
			case DLOAD:
			case DLOAD_0:
			case DLOAD_1:
			case DLOAD_2:
			case DLOAD_3:
				v2 = DLOAD;
				break;
			case FLOAD:
			case FLOAD_0:
			case FLOAD_1:
			case FLOAD_2:
			case FLOAD_3:
				v2 = FLOAD;
				break;
			case LLOAD:
			case LLOAD_0:
			case LLOAD_1:
			case LLOAD_2:
			case LLOAD_3:
				v2 = LLOAD;
				break;

			case ASTORE:
			case ASTORE_0:
			case ASTORE_1:
			case ASTORE_2:
			case ASTORE_3:
				v2 = ASTORE;
				break;
			case ISTORE:
			case ISTORE_0:
			case ISTORE_1:
			case ISTORE_2:
			case ISTORE_3:
				v2 = ISTORE;
				break;
			case DSTORE:
			case DSTORE_0:
			case DSTORE_1:
			case DSTORE_2:
			case DSTORE_3:
				v2 = DSTORE;
				break;
			case FSTORE:
			case FSTORE_0:
			case FSTORE_1:
			case FSTORE_2:
			case FSTORE_3:
				v2 = FSTORE;
				break;
			case LSTORE:
			case LSTORE_0:
			case LSTORE_1:
			case LSTORE_2:
			case LSTORE_3:
				v2 = LSTORE;
				break;
			}
			out2.write(v2);
		} catch (IOException e) {
		}
	}

	boolean computed = false;

	private void compute() {
		if (computed)
			return;
		digestValue1 = new BigInteger(digest1.digest()).abs().toString(16);
		digestValue2 = new BigInteger(digest2.digest()).abs().toString(16);
		opcodeList = new ArrayList<String>(opcodesUsed.cardinality());
		for (int i = opcodesUsed.nextSetBit(0); i >= 0; i = opcodesUsed
				.nextSetBit(i + 1)) {
			opcodeList.add(OPCODE_NAMES[i]);
		}
		computed = true;
	}

	@Override
	public void report(PrintStream out) {
		compute();
		for (Iterator<String> i = opcodeList.iterator(); i.hasNext();)
			out.println(TestOutcome.OPCODE_TEST + ": " + i.next());
		for (Iterator<String> i = classesReferenced.iterator(); i.hasNext();)
			out.println(TestOutcome.CLASS_TEST + ":" + i.next());
		for (Iterator<String> i = methodsReferenced.iterator(); i.hasNext();)
			out.println(TestOutcome.METHOD_TEST + ": " + i.next());

		out.println(TestOutcome.DIGEST1_TEST + ": " + digestValue1);
		out.println(TestOutcome.DIGEST2_TEST + ": " + digestValue2);
	}

	public void report(StringWriter writer) {
		compute();
		for (Iterator<String> i = opcodeList.iterator(); i.hasNext();)
			writer.write("opcode: " + i.next() + "\n");
		for (Iterator<String> i = classesReferenced.iterator(); i.hasNext();)
			writer.write("class: " + i.next() + "\n");
		for (Iterator<String> i = methodsReferenced.iterator(); i.hasNext();)
			writer.write("method: " + i.next() + "\n");

		writer.write("digest1: " + digestValue1 + "\n");
		writer.write("digest2: " + digestValue2 + "\n");
	}

	public static void main(String argv[]) throws Exception {
		String[] file_name = new String[argv.length];
		int files = 0;
		ClassParser parser = null;
		JavaClass java_class;
		boolean code = false, constants = false;
		String zip_file = null;
		String visitor = null;

		/*
		 * Parse command line arguments.
		 */
		for (int i = 0; i < argv.length; i++) {
			if (argv[i].endsWith(".zip") || argv[i].endsWith(".jar"))
				zip_file = argv[i];
			else { // add file name to list
				file_name[files++] = argv[i];
			}
		}

		if (files == 0 && zip_file == null) {
			System.err.println("list: No input files specified");
			return;
		}
		CodeFeatures v = new CodeFeatures();
		if (files == 0 && zip_file != null) {
			ZipFile z = new ZipFile(zip_file);
			TreeSet<ZipEntry> zipEntries = new TreeSet<ZipEntry>();
			for (Enumeration<? extends ZipEntry> e = z.entries(); e
					.hasMoreElements();)
				zipEntries.add(e.nextElement());

			for (Iterator<? extends ZipEntry> i = zipEntries.iterator(); i
					.hasNext();) {
				ZipEntry ze = i.next();
				String name = ze.getName();
				if (name.endsWith(".class")) {
					parser = new ClassParser(z.getInputStream(ze), name);
					java_class = parser.parse();
					java_class.accept(v);

				}
			}
		} else {
			for (int i = 0; i < files; i++) {
				if (zip_file == null)
					// Create parser object
					parser = new ClassParser(file_name[i]);
				else
					try {
						// Create parser object
						parser = new ClassParser(zip_file, file_name[i]);
					} finally {
					}

				// Initiate the parsing
				java_class = parser.parse();

				java_class.accept(v);
			}
		}
		v.report(System.out);
	}
}
