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

/*
 * Created on Mar 9, 2005
 */
package edu.umd.cs.buildServer.tester;


import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.SimpleElementValue;

/**
 * Object identifying a single JUnit test in a class. Provides enough
 * information to execute the test using a TestRunner.s
 *
 * @author David Hovemeyer
 * @author William Pugh
 */
public class JUnitTestCase {
	private final String className;
	private final String methodName;
	private final String signature;
	private final long maxTimeInMilliseconds;

	/**
	 * Constructor.
	 *
	 * @param className
	 *            name of the class the test is in
	 * @param methodName
	 *            name of the test method
	 * @param signature
	 *            signature of the test method (should always be "()V")
	 * @deprecated Use {@link #JUnitTestCase(JavaClass,Method,AnnotationEntry)} instead
	 */
	public JUnitTestCase(JavaClass jClass, Method method) {
		this(jClass, method, null);
	}

	/**
	 * Constructor.
	 * @param annotation TODO
	 * @param className
	 *            name of the class the test is in
	 * @param methodName
	 *            name of the test method
	 * @param signature
	 *            signature of the test method (should always be "()V")
	 */
	public JUnitTestCase(JavaClass jClass, Method method, AnnotationEntry annotation) {

		this.className = jClass.getClassName();
		this.methodName = method.getName();
		this.signature = method.getSignature();

		long maxTime = 0;

		if (annotation != null && annotation.getAnnotationType().equals("Lorg/junit/Test;")) {
			for(ElementValuePair p : annotation.getElementValuePairs()) {
				if (!p.getNameString().equals("timeout"))
					continue;
				ElementValue value = p.getValue();
				if (value instanceof SimpleElementValue) {
					SimpleElementValue s = (SimpleElementValue) value;
					maxTime = s.getValueLong();
					break;
				}
			}
		}
		this.maxTimeInMilliseconds = maxTime;
	}

	public long getMaxTimeInMilliseconds() {
		return maxTimeInMilliseconds;
	}

	/**
	 * Get the class name.
	 *
	 * @return the class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Get the test method name.
	 *
	 * @return the test method name
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Get the test method signature.
	 *
	 * @return the test method signature
	 */
	public String getSignature() {
		return signature;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;

		JUnitTestCase other = (JUnitTestCase) obj;

		return className.equals(other.className)
				&& methodName.equals(other.methodName)
				&& signature.equals(other.signature);
	}

	@Override
	public int hashCode() {
		return className.hashCode() + methodName.hashCode()
				+ signature.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("Class ");
		buf.append(className);
		buf.append(", method ");
		buf.append(methodName);
		buf.append(", signature ");
		buf.append(signature);

		return buf.toString();
	}
}
